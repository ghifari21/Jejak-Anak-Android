package com.gosty.jejakanak.core.data.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gosty.jejakanak.BuildConfig
import com.gosty.jejakanak.core.data.models.ChildModel
import com.gosty.jejakanak.core.data.models.CoordinateModel
import com.gosty.jejakanak.core.data.models.GeofenceModel
import com.gosty.jejakanak.core.data.models.ParentModel
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.splitName
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSourceImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : FirebaseDataSource {
    override fun signIn(credential: AuthCredential, isParent: Boolean): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.value = Result.Success("Berhasil: ${auth.currentUser?.displayName}")
                } else {
                    crashlytics.log(it.exception.toString())
                    result.value = Result.Error(it.exception.toString())
                }
            }
            .addOnFailureListener {
                crashlytics.log(it.message.toString())
                result.value = Result.Error(it.message.toString())
            }

        // Check if user already exists in database
        val uid = auth.currentUser?.uid
        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).get()
            .addOnSuccessListener {
                if (!it.exists()) {
                    val (firstName, lastName) = auth.currentUser?.displayName!!.splitName()
                    val email = auth.currentUser?.email
                    if (isParent) {
                        val user = ParentModel(
                            id = uid,
                            firstName = firstName,
                            lastName = lastName,
                            email = email
                        )
                        ref.child(uid).setValue(user)
                            .addOnFailureListener { e ->
                                Log.e(
                                    FirebaseDataSourceImpl::class.java.simpleName,
                                    "error posting data",
                                    e
                                )
                                crashlytics.log(e.message.toString())
                            }
                    } else {
                        val user = ChildModel(
                            id = uid,
                            firstName = firstName,
                            lastName = lastName,
                            email = email
                        )
                        ref.child(uid).setValue(user)
                            .addOnFailureListener { e ->
                                Log.e(
                                    FirebaseDataSourceImpl::class.java.simpleName,
                                    "error posting data",
                                    e
                                )
                                crashlytics.log(e.message.toString())
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseDataSourceImpl::class.java.simpleName, "error getting data", it)
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun updateChildCoordinate(
        coordinate: CoordinateModel
    ): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        // TODO buat update coordinate kalau coordinate sudah berubah
        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child(uid!!).child("coordinates").child(coordinate.id!!).get()
            .addOnSuccessListener {
                val data = it.getValue(CoordinateModel::class.java)
                if (data?.latitude != coordinate.latitude && data?.longitude != coordinate.longitude) {
                    ref.child(uid).child("coordinates").child(coordinate.id!!).setValue(coordinate)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil update koordinat")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                } else {
                    result.value = Result.Success("Koordinat tidak berubah")
                }
            }

        // TODO remove?
        ref.child(uid!!).child(coordinate.id!!).setValue(coordinate)
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil update koordinat")
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun addChild(email: String): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val name = auth.currentUser?.displayName
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val childRef = database.reference.child(BuildConfig.CHILD_REF)
        childRef.equalTo(email).get()
            .addOnSuccessListener {
                val child = it.children.first().getValue(ChildModel::class.java)
                val parentRef = database.reference.child(BuildConfig.PARENT_REF)
                parentRef.child(uid!!).child("children").child(child?.id.toString()).setValue(child)
                    .addOnSuccessListener {
                        result.value = Result.Success("Berhasil menambahkan anak")
                    }
                    .addOnFailureListener { e ->
                        result.value = Result.Error(e.message.toString())
                        crashlytics.log(e.message.toString())
                    }
                childRef.child(child?.id!!).child("parentId").child(name!!).setValue(uid)
                    .addOnFailureListener { e ->
                        result.value = Result.Error(e.message.toString())
                        crashlytics.log(e.message.toString())
                    }
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun getAllChildren(): LiveData<Result<List<ChildModel>>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<List<ChildModel>>>()

        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child("parentId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map {
                        it.getValue(ChildModel::class.java)!!
                    }
                    result.value = Result.Success(data)
                }

                override fun onCancelled(error: DatabaseError) {
                    result.value = Result.Error(error.message)
                    crashlytics.log(error.message)
                }
            })

        return result
    }

    override fun addGeofence(geofence: GeofenceModel): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        val key = ref.child(uid!!).child("geofences").push().key
        geofence.parentId = uid
        geofence.id = key
        ref.child(uid).child("geofences").child(key!!).setValue(geofence)
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil menambahkan geofence")
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun removeGeofence(id: String): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        ref.child(uid!!).child("geofences").child(id).removeValue()
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil menghapus geofence")
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun getAllGeofences(): LiveData<Result<List<GeofenceModel>>> {
        val result = MediatorLiveData<Result<List<GeofenceModel>>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        ref.child(uid!!).child("geofences")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map {
                        it.getValue(GeofenceModel::class.java)!!
                    }
                    result.value = Result.Success(data)
                }

                override fun onCancelled(error: DatabaseError) {
                    result.value = Result.Error(error.message)
                    crashlytics.log(error.message)
                }
            })

        return result
    }

    override fun updateParentProfile(user: ParentModel): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        ref.child(uid!!).setValue(user)
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil update profil")
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun getParentProfile(): LiveData<Result<ParentModel>> {
        val result = MediatorLiveData<Result<ParentModel>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        ref.child(uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(ParentModel::class.java)!!
                result.value = Result.Success(data)
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = Result.Error(error.message)
                crashlytics.log(error.message)
            }
        })

        return result
    }

    override fun updateChildProfile(user: ChildModel): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child(uid!!).setValue(user)
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil update profil")
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }

    override fun getChildProfile(): LiveData<Result<ChildModel>> {
        val result = MediatorLiveData<Result<ChildModel>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child(uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(ChildModel::class.java)!!
                result.value = Result.Success(data)
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = Result.Error(error.message)
                crashlytics.log(error.message)
            }
        })

        return result
    }
}