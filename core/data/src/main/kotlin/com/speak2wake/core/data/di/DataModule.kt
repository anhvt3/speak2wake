package com.speak2wake.core.data.di

import com.speak2wake.core.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
        @Binds
        @Singleton
        internal abstract fun bindAlarmRepository(
                impl: OfflineFirstAlarmRepository
        ): AlarmRepository

        @Binds
        @Singleton
        internal abstract fun bindVocabularyRepository(
                impl: OfflineFirstVocabularyRepository
        ): VocabularyRepository

        @Binds
        @Singleton
        internal abstract fun bindChallengeHistoryRepository(
                impl: OfflineFirstChallengeHistoryRepository
        ): ChallengeHistoryRepository
}
