package com.speak2wake.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    // Migration from v2 to v3: added alwaysPronounce column
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN alwaysPronounce INTEGER NOT NULL DEFAULT 0")
        }
    }

    // Migration from v3 to v4: added challengeWordCount column
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeWordCount INTEGER NOT NULL DEFAULT 1")
        }
    }

    // Migration from v2 directly to v4 (adds both columns)
    private val MIGRATION_2_4 = object : Migration(2, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN alwaysPronounce INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeWordCount INTEGER NOT NULL DEFAULT 1")
        }
    }

    // Migration from v4 to v5: added challengeLanguage to alarms + language to vocabulary
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeLanguage TEXT NOT NULL DEFAULT 'DE'")
            db.execSQL("ALTER TABLE vocabulary ADD COLUMN language TEXT NOT NULL DEFAULT 'de'")
        }
    }

    // Skip migrations for older versions directly to v5
    private val MIGRATION_3_5 = object : Migration(3, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeWordCount INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeLanguage TEXT NOT NULL DEFAULT 'DE'")
            db.execSQL("ALTER TABLE vocabulary ADD COLUMN language TEXT NOT NULL DEFAULT 'de'")
        }
    }

    private val MIGRATION_2_5 = object : Migration(2, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE alarms ADD COLUMN alwaysPronounce INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeWordCount INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE alarms ADD COLUMN challengeLanguage TEXT NOT NULL DEFAULT 'DE'")
            db.execSQL("ALTER TABLE vocabulary ADD COLUMN language TEXT NOT NULL DEFAULT 'de'")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Speak2WakeDatabase =
            Room.databaseBuilder(context, Speak2WakeDatabase::class.java, "speak2wake.db")
                    .addMigrations(
                        MIGRATION_2_3, MIGRATION_3_4, MIGRATION_2_4,
                        MIGRATION_4_5, MIGRATION_3_5, MIGRATION_2_5,
                    )
                    .fallbackToDestructiveMigration() // last resort for v1 users
                    .build()

    @Provides fun provideAlarmDao(db: Speak2WakeDatabase) = db.alarmDao()
    @Provides fun provideVocabularyDao(db: Speak2WakeDatabase) = db.vocabularyDao()
    @Provides fun provideChallengeDao(db: Speak2WakeDatabase) = db.challengeDao()
}
