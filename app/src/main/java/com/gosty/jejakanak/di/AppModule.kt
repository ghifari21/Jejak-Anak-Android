package com.gosty.jejakanak.di

import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCase
import com.gosty.jejakanak.core.domain.usecases.GeofenceUseCaseImpl
import com.gosty.jejakanak.core.domain.usecases.UserUseCase
import com.gosty.jejakanak.core.domain.usecases.UserUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun provideUserUseCase(userUseCaseImpl: UserUseCaseImpl): UserUseCase

    @Binds
    abstract fun provideGeofenceUseCase(geofenceUseCaseImpl: GeofenceUseCaseImpl): GeofenceUseCase
}