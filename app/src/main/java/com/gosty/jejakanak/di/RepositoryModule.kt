package com.gosty.jejakanak.di

import com.gosty.jejakanak.data.repositories.GeofenceRepository
import com.gosty.jejakanak.data.repositories.GeofenceRepositoryImpl
import com.gosty.jejakanak.data.repositories.UserRepository
import com.gosty.jejakanak.data.repositories.UserRepositoryImpl
import com.gosty.jejakanak.data.source.FirebaseDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseDataSource: FirebaseDataSource
    ): UserRepository = UserRepositoryImpl(firebaseDataSource)

    @Provides
    @Singleton
    fun provideGeofenceRepository(
        firebaseDataSource: FirebaseDataSource
    ): GeofenceRepository = GeofenceRepositoryImpl(firebaseDataSource)
}