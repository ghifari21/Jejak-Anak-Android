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
import com.gosty.jejakanak.core.data.models.ChildEntity
import com.gosty.jejakanak.core.data.models.CoordinateEntity
import com.gosty.jejakanak.core.data.models.GeofenceEntity
import com.gosty.jejakanak.core.data.models.ParentEntity
import com.gosty.jejakanak.utils.Result
import com.gosty.jejakanak.utils.getRandomString
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
                    // Check if user already exists in database
                    val uid = auth.currentUser?.uid
                    val ref = if (isParent) {
                        database.reference.child(BuildConfig.PARENT_REF)
                    } else {
                        database.reference.child(BuildConfig.CHILD_REF)
                    }

                    ref.child(uid!!).get()
                        .addOnSuccessListener { data ->
                            if (!data.exists()) {
                                val (firstName, lastName) = auth.currentUser?.displayName!!.splitName()
                                val email = auth.currentUser?.email
                                val photo = auth.currentUser?.photoUrl.toString()
                                if (isParent) {
                                    val user = ParentEntity(
                                        id = uid,
                                        photo = photo,
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
                                    val user = ChildEntity(
                                        id = uid,
                                        photo = photo,
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email,
                                        uniqueCode = getRandomString()
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
                            } else {
                                val photo = auth.currentUser?.photoUrl.toString()
                                if (isParent) {
                                    ref.child(uid).child("photo").setValue(photo)
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                FirebaseDataSourceImpl::class.java.simpleName,
                                                "error updating data",
                                                e
                                            )
                                            crashlytics.log(e.message.toString())
                                        }
                                } else {
                                    ref.child(uid).child("photo").setValue(photo)
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                FirebaseDataSourceImpl::class.java.simpleName,
                                                "error updating data",
                                                e
                                            )
                                            crashlytics.log(e.message.toString())
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                FirebaseDataSourceImpl::class.java.simpleName,
                                "error getting data",
                                e
                            )
                            crashlytics.log(e.message.toString())
                        }
                } else {
                    crashlytics.log(it.exception.toString())
                    result.value = Result.Error(it.exception.toString())
                }
            }
            .addOnFailureListener {
                crashlytics.log(it.message.toString())
                result.value = Result.Error(it.message.toString())
            }

        return result
    }

    override fun isUserPhoneNumberExist(isParent: Boolean): LiveData<Result<Boolean>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<Boolean>>()
        result.value = Result.Loading

        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).child("phone").get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.getValue(String::class.java)
                result.value = Result.Success(data != null)
            }
            .addOnFailureListener { error ->
                result.value = Result.Error(error.message.toString())
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun inputUserPhoneNumber(
        phoneNumber: String,
        isParent: Boolean
    ): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).child("phone").setValue(phoneNumber)
            .addOnSuccessListener {
                result.value = Result.Success("Berhasil memasukkan nomor telepon")
            }
            .addOnFailureListener { error ->
                result.value = Result.Error(error.message.toString())
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun updateChildCoordinate(
        coordinate: CoordinateEntity
    ): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child(uid!!).child("coordinate").get()
            .addOnSuccessListener {
                val data = it.getValue(CoordinateEntity::class.java)
                if (data?.latitude != coordinate.latitude && data?.longitude != coordinate.longitude) {
                    ref.child(uid).child("coordinate").setValue(coordinate)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil update koordinat")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                }
            }

        return result
    }

    override fun addChild(uniqueCode: String): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        // checking if child is exist
        val parentRef = database.reference.child(BuildConfig.PARENT_REF)
        val childRef = database.reference.child(BuildConfig.CHILD_REF)
        childRef.get()
            .addOnSuccessListener { snapshot ->
                val children = snapshot.children.map { dataSnapshot ->
                    val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                    if (child.uniqueCode == uniqueCode) {
                        child
                    } else {
                        null
                    }
                }

                val childrenList = children.filterNotNull()

                if (childrenList.isEmpty()) {
                    result.value = Result.Error("Kode unik tidak ditemukan")
                } else {
                    val child = childrenList.first()
                    val parentIds = mutableListOf<String>()
                    parentIds.add(uid!!)

                    if (child.parentId.isNullOrEmpty()) {
                        parentIds.addAll(child.parentId!!)
                    }

                    parentRef.child(uid).child("childrenId").get()
                        .addOnSuccessListener { idSnapshot ->
                            val childrenIds = idSnapshot.children.map { it.value.toString() }
                            val newChildrenIds = mutableListOf<String>()
                            newChildrenIds.add(child.id!!)

                            if (childrenIds.isNotEmpty()) {
                                newChildrenIds.addAll(childrenIds)
                            }

                            parentRef.child(uid).child("childrenId").setValue(newChildrenIds)
                                .addOnSuccessListener {
                                    childRef.child(child.id).child("parentId").get()
                                        .addOnSuccessListener { parentIdSnapshot ->
                                            val parentsId = parentIdSnapshot.children.map {
                                                it.value.toString()
                                            }
                                            val newParentsId = mutableListOf<String>()
                                            newParentsId.add(uid)

                                            if (parentsId.isNotEmpty()) {
                                                newParentsId.addAll(parentsId)
                                            }

                                            childRef.child(child.id).child("parentId")
                                                .setValue(newParentsId)
                                                .addOnSuccessListener {
                                                    parentRef.child(uid).child("geofences").get()
                                                        .addOnSuccessListener { geofenceSnapshot ->
                                                            val geofences =
                                                                geofenceSnapshot.children.map {
                                                                    it.getValue(GeofenceEntity::class.java)!!
                                                                }

                                                            childRef.child(child.id)
                                                                .child("geofences")
                                                                .get()
                                                                .addOnSuccessListener { childGeofenceSnapshot ->
                                                                    val childGeofences =
                                                                        childGeofenceSnapshot.children.map {
                                                                            it.getValue(
                                                                                GeofenceEntity::class.java
                                                                            )!!
                                                                        }
                                                                    val geofencesList =
                                                                        mutableListOf<GeofenceEntity>()
                                                                    geofencesList.addAll(geofences)
                                                                    if (childGeofences.isNotEmpty()) {
                                                                        geofencesList.addAll(
                                                                            childGeofences
                                                                        )
                                                                    }

                                                                    childRef.child(child.id)
                                                                        .child("geofences")
                                                                        .setValue(geofences)
                                                                        .addOnSuccessListener {
                                                                            result.value =
                                                                                Result.Success("Berhasil menambahkan anak")
                                                                        }
                                                                        .addOnFailureListener { error ->
                                                                            result.value =
                                                                                Result.Error(error.message.toString())
                                                                            crashlytics.log(error.message.toString())
                                                                        }
                                                                }
                                                                .addOnFailureListener { error ->
                                                                    result.value =
                                                                        Result.Error(error.message.toString())
                                                                    crashlytics.log(error.message.toString())
                                                                }
                                                        }
                                                        .addOnFailureListener { error ->
                                                            result.value =
                                                                Result.Error(error.message.toString())
                                                            crashlytics.log(error.message.toString())
                                                        }
                                                }
                                                .addOnFailureListener { error ->
                                                    result.value =
                                                        Result.Error(error.message.toString())
                                                    crashlytics.log(error.message.toString())
                                                }
                                        }
                                        .addOnFailureListener { error ->
                                            result.value = Result.Error(error.message.toString())
                                            crashlytics.log(error.message.toString())
                                        }
                                }
                                .addOnFailureListener { error ->
                                    result.value = Result.Error(error.message.toString())
                                    crashlytics.log(error.message.toString())
                                }
                        }
                        .addOnFailureListener { error ->
                            result.value = Result.Error(error.message.toString())
                            crashlytics.log(error.message.toString())
                        }
                }
            }
            .addOnFailureListener { error ->
                result.value = Result.Error(error.message.toString())
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun getAllChildren(): LiveData<Result<List<ChildEntity>>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<List<ChildEntity>>>()

        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map { dataSnapshot ->
                        val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                        if (child.parentId?.contains(uid) == true) {
                            child
                        } else {
                            null
                        }
                    }
                    result.value = Result.Success(data.filterNotNull())
                }

                override fun onCancelled(error: DatabaseError) {
                    result.value = Result.Error(error.message)
                    crashlytics.log(error.message)
                }
            }
        )

        return result
    }

    override fun getAllChildrenService(callback: (List<ChildEntity>) -> Unit) {
        val uid = auth.currentUser?.uid

        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map { dataSnapshot ->
                        val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                        if (child.parentId?.contains(uid) == true) {
                            child
                        } else {
                            null
                        }
                    }
                    callback(data.filterNotNull())
                }

                override fun onCancelled(error: DatabaseError) {
                    crashlytics.log(error.message)
                }
            }
        )
    }

    override fun getAllChildrenOnceService(callback: (List<ChildEntity>) -> Unit) {
        val uid = auth.currentUser?.uid

        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.children.map { dataSnapshot ->
                    val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                    if (child.parentId?.contains(uid) == true) {
                        child
                    } else {
                        null
                    }
                }
                callback(data.filterNotNull())
            }
            .addOnFailureListener { error ->
                crashlytics.log(error.message.toString())
            }
    }

    override fun removeChild(childId: String): LiveData<Result<String>> {
        val uid = auth.currentUser?.uid
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val parentRef = database.reference.child(BuildConfig.PARENT_REF)
        val childRef = database.reference.child(BuildConfig.CHILD_REF)

        parentRef.child(uid!!).child("childrenId").get()
            .addOnSuccessListener { snapshot ->
                val childrenIds = snapshot.children.map { it.value.toString() }
                val newChildrenIds = childrenIds.filter { it != childId }
                parentRef.child(uid).child("childrenId").setValue(newChildrenIds)
                    .addOnSuccessListener {
                        childRef.child(childId).child("parentId").get()
                            .addOnSuccessListener { dataSnapshot ->
                                val parentIds = dataSnapshot.children.map { it.value.toString() }
                                val newParentIds = parentIds.filter { it != uid }
                                childRef.child(childId).child("parentId").setValue(newParentIds)
                                    .addOnSuccessListener {
                                        childRef.child(childId).child("geofences").get()
                                            .addOnSuccessListener { geofenceSnapshot ->
                                                val geofences = geofenceSnapshot.children.map {
                                                    it.getValue(GeofenceEntity::class.java)!!
                                                }
                                                val newGeofences = geofences.filter {
                                                    it.parentId != uid.toString()
                                                }
                                                Log.d("geofencesdelete", newGeofences.toString())
                                                childRef.child(childId).child("geofences")
                                                    .setValue(newGeofences)
                                                    .addOnSuccessListener {
                                                        result.value =
                                                            Result.Success("Berhasil menghapus anak")
                                                    }
                                                    .addOnFailureListener { error ->
                                                        result.value =
                                                            Result.Error(error.message.toString())
                                                        crashlytics.log(error.message.toString())
                                                    }
                                            }
                                            .addOnFailureListener { error ->
                                                result.value =
                                                    Result.Error(error.message.toString())
                                                crashlytics.log(error.message.toString())
                                            }
                                    }
                                    .addOnFailureListener { error ->
                                        result.value = Result.Error(error.message.toString())
                                        crashlytics.log(error.message.toString())
                                    }
                            }
                            .addOnFailureListener { error ->
                                result.value = Result.Error(error.message.toString())
                                crashlytics.log(error.message.toString())
                            }
                    }
                    .addOnFailureListener { error ->
                        result.value = Result.Error(error.message.toString())
                        crashlytics.log(error.message.toString())
                    }
            }
            .addOnFailureListener { error ->
                result.value = Result.Error(error.message.toString())
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun addGeofence(geofence: GeofenceEntity): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val parentRef = database.reference.child(BuildConfig.PARENT_REF)
        geofence.parentId = uid
        geofence.id = getRandomString()
        parentRef.child(uid!!).child("geofences").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val geofences = mutableListOf<GeofenceEntity>()
                    snapshot.children.forEach { dataSnapshot ->
                        val data = dataSnapshot.getValue(GeofenceEntity::class.java)!!
                        geofences.add(data)
                    }
                    geofences.add(geofence)
                    parentRef.child(uid).child("geofences").setValue(geofences)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil menambahkan geofence")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                } else {
                    val geofences = listOf(geofence)
                    parentRef.child(uid).child("geofences").setValue(geofences)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil menambahkan geofence")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                }
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        val childRef = database.reference.child(BuildConfig.CHILD_REF)
        childRef.get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { dataSnapshot ->
                    val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                    if (child.parentId?.contains(uid) == true) {
                        childRef.child(child.id!!).child("geofences").get()
                            .addOnSuccessListener { childSnapshot ->
                                if (childSnapshot.exists()) {
                                    val geofences = mutableListOf<GeofenceEntity>()
                                    childSnapshot.children.forEach { data ->
                                        val geofenceData =
                                            data.getValue(GeofenceEntity::class.java)!!
                                        geofences.add(geofenceData)
                                    }
                                    geofences.add(geofence)
                                    childRef.child(child.id).child("geofences").setValue(geofences)
                                        .addOnSuccessListener {
                                            result.value =
                                                Result.Success("Berhasil menambahkan geofence")
                                        }
                                        .addOnFailureListener { e ->
                                            result.value = Result.Error(e.message.toString())
                                            crashlytics.log(e.message.toString())
                                        }
                                } else {
                                    val geofences = listOf(geofence)
                                    childRef.child(child.id).child("geofences").setValue(geofences)
                                        .addOnSuccessListener {
                                            result.value =
                                                Result.Success("Berhasil menambahkan geofence")
                                        }
                                        .addOnFailureListener { e ->
                                            result.value = Result.Error(e.message.toString())
                                            crashlytics.log(e.message.toString())
                                        }
                                }
                            }
                            .addOnFailureListener {
                                result.value = Result.Error(it.message.toString())
                                crashlytics.log(it.message.toString())
                            }
                    }
                }
            }
            .addOnFailureListener { error ->
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun removeGeofence(id: String): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val parentRef = database.reference.child(BuildConfig.PARENT_REF)
        parentRef.child(uid!!).child("geofences").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val geofences = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(GeofenceEntity::class.java)!!
                    }
                    val newGeofences = geofences.filter { it.id != id }
                    parentRef.child(uid).child("geofences").setValue(newGeofences)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil menghapus geofence")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                }
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        val childRef = database.reference.child(BuildConfig.CHILD_REF)
        childRef.get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { dataSnapshot ->
                    val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                    if (child.parentId?.contains(uid) == true) {
                        childRef.child(child.id!!).child("geofences").get()
                            .addOnSuccessListener { childSnapshot ->
                                if (childSnapshot.exists()) {
                                    val geofences = childSnapshot.children.map { data ->
                                        data.getValue(GeofenceEntity::class.java)!!
                                    }
                                    val newGeofences = geofences.filter { it.id != id }
                                    childRef.child(child.id).child("geofences")
                                        .setValue(newGeofences)
                                        .addOnSuccessListener {
                                            result.value =
                                                Result.Success("Berhasil menghapus geofence")
                                        }
                                        .addOnFailureListener { e ->
                                            result.value = Result.Error(e.message.toString())
                                            crashlytics.log(e.message.toString())
                                        }
                                }
                            }
                            .addOnFailureListener { error ->
                                result.value = Result.Error(error.message.toString())
                                crashlytics.log(error.message.toString())
                            }
                    }
                }
            }
            .addOnFailureListener { error ->
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun updateGeofence(geofence: GeofenceEntity): LiveData<Result<String>> {
        val result = MediatorLiveData<Result<String>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        geofence.parentId = uid
        val parentRef = database.reference.child(BuildConfig.PARENT_REF)
        parentRef.child(uid!!).child("geofences").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val geofences = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(GeofenceEntity::class.java)!!
                    }
                    val newGeofences = geofences.map {
                        if (it.id == geofence.id) {
                            geofence
                        } else {
                            it
                        }
                    }
                    parentRef.child(uid).child("geofences").setValue(newGeofences)
                        .addOnSuccessListener {
                            result.value = Result.Success("Berhasil mengupdate geofence")
                        }
                        .addOnFailureListener { e ->
                            result.value = Result.Error(e.message.toString())
                            crashlytics.log(e.message.toString())
                        }
                }
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        val childRef = database.reference.child(BuildConfig.CHILD_REF)
        childRef.get()
            .addOnSuccessListener { snapshot ->
                snapshot.children.forEach { dataSnapshot ->
                    val child = dataSnapshot.getValue(ChildEntity::class.java)!!
                    if (child.parentId?.contains(uid) == true) {
                        childRef.child(child.id!!).child("geofences").get()
                            .addOnSuccessListener { childSnapshot ->
                                if (childSnapshot.exists()) {
                                    val geofences = childSnapshot.children.map { data ->
                                        data.getValue(GeofenceEntity::class.java)!!
                                    }
                                    val newGeofences = geofences.map {
                                        if (it.id == geofence.id) {
                                            geofence
                                        } else {
                                            it
                                        }
                                    }
                                    childRef.child(child.id).child("geofences")
                                        .setValue(newGeofences)
                                        .addOnSuccessListener {
                                            result.value =
                                                Result.Success("Berhasil mengupdate geofence")
                                        }
                                        .addOnFailureListener { e ->
                                            result.value = Result.Error(e.message.toString())
                                            crashlytics.log(e.message.toString())
                                        }
                                }
                            }
                            .addOnFailureListener { error ->
                                result.value = Result.Error(error.message.toString())
                                crashlytics.log(error.message.toString())
                            }
                    }
                }
            }
            .addOnFailureListener { error ->
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun getAllGeofences(isParent: Boolean): LiveData<Result<List<GeofenceEntity>>> {
        val result = MediatorLiveData<Result<List<GeofenceEntity>>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).child("geofences")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map {
                        it.getValue(GeofenceEntity::class.java)!!
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

    override fun getAllGeofencesService(
        isParent: Boolean,
        callback: (List<GeofenceEntity>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).child("geofences")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.children.map {
                        it.getValue(GeofenceEntity::class.java)!!
                    }
                    callback(data)
                }

                override fun onCancelled(error: DatabaseError) {
                    crashlytics.log(error.message)
                }
            })
    }

    override fun getAllGeofencesOnceService(
        isParent: Boolean,
        callback: (List<GeofenceEntity>) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        val ref = if (isParent) {
            database.reference.child(BuildConfig.PARENT_REF)
        } else {
            database.reference.child(BuildConfig.CHILD_REF)
        }

        ref.child(uid!!).child("geofences")
            .get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.children.map {
                    it.getValue(GeofenceEntity::class.java)!!
                }
                callback(data)
            }
            .addOnFailureListener { error ->
                crashlytics.log(error.message.toString())
            }
    }

    override fun updateParentProfile(user: ParentEntity): LiveData<Result<String>> {
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

    override fun getParentProfile(): LiveData<Result<ParentEntity>> {
        val result = MediatorLiveData<Result<ParentEntity>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)
        ref.child(uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(ParentEntity::class.java)!!
                result.value = Result.Success(data)
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = Result.Error(error.message)
                crashlytics.log(error.message)
            }
        })

        return result
    }

    override fun updateChildProfile(user: ChildEntity): LiveData<Result<String>> {
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

    override fun getChildProfile(): LiveData<Result<ChildEntity>> {
        val result = MediatorLiveData<Result<ChildEntity>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.CHILD_REF)
        ref.child(uid!!).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.getValue(ChildEntity::class.java)!!
                result.value = Result.Success(data)
            }
            .addOnFailureListener { error ->
                result.value = Result.Error(error.message)
                crashlytics.log(error.message.toString())
            }

        return result
    }

    override fun getParentsProfile(): LiveData<Result<List<ParentEntity>>> {
        val result = MediatorLiveData<Result<List<ParentEntity>>>()
        result.value = Result.Loading

        val uid = auth.currentUser?.uid
        val ref = database.reference.child(BuildConfig.PARENT_REF)

        ref.get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.children.map { dataSnapshot ->
                    val parent = dataSnapshot.getValue(ParentEntity::class.java)!!
                    if (parent.childrenId?.contains(uid) == true) {
                        parent
                    } else {
                        null
                    }
                }
                result.value = Result.Success(data.filterNotNull())
            }
            .addOnFailureListener {
                result.value = Result.Error(it.message.toString())
                crashlytics.log(it.message.toString())
            }

        return result
    }
}