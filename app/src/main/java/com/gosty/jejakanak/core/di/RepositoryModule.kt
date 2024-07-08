package com.gosty.jejakanak.core.di

import com.gosty.jejakanak.core.data.repositories.GeofenceRepositoryImpl
import com.gosty.jejakanak.core.data.repositories.UserRepositoryImpl
import com.gosty.jejakanak.core.domain.repositories.GeofenceRepository
import com.gosty.jejakanak.core.domain.repositories.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [DataSourceModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideUserRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun provideGeofenceRepository(repository: GeofenceRepositoryImpl): GeofenceRepository
}