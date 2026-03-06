package com.speak2wake.core.database.di

import android.content.Context
import androidx.room.Room
import com.speak2wake.core.database.Speak2WakeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Speak2WakeDatabase =
            Room.databaseBuilder(context, Speak2WakeDatabase::class.java, "speak2wake.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides fun provideAlarmDao(db: Speak2WakeDatabase) = db.alarmDao()
    @Provides fun provideVocabularyDao(db: Speak2WakeDatabase) = db.vocabularyDao()
    @Provides fun provideChallengeDao(db: Speak2WakeDatabase) = db.challengeDao()
}
