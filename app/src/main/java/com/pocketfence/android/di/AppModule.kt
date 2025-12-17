package com.pocketfence.android.di

import android.content.Context
import com.pocketfence.android.monetization.AdManager
import com.pocketfence.android.monetization.BillingManager
import com.pocketfence.android.monetization.MonetizationRepository
import com.pocketfence.android.repository.PocketFenceRepository
import com.pocketfence.android.util.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing app-wide dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun providePocketFenceRepository(
        preferencesManager: PreferencesManager
    ): PocketFenceRepository {
        return PocketFenceRepository(preferencesManager)
    }
    
    @Provides
    @Singleton
    fun provideMonetizationRepository(
        @ApplicationContext context: Context
    ): MonetizationRepository {
        return MonetizationRepository(context)
    }
    
    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        monetizationRepository: MonetizationRepository
    ): BillingManager {
        return BillingManager(context, monetizationRepository)
    }
    
    @Provides
    @Singleton
    fun provideAdManager(
        @ApplicationContext context: Context,
        monetizationRepository: MonetizationRepository
    ): AdManager {
        return AdManager(context, monetizationRepository)
    }
}
