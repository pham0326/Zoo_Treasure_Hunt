package com.pham0326.flinders.zootreasurehunt.di

import com.pham0326.flinders.zootreasurehunt.data.FileSightingRepository
import com.pham0326.flinders.zootreasurehunt.data.SightingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSightingRepository(
        fileSightingRepository: FileSightingRepository
    ): SightingRepository
}