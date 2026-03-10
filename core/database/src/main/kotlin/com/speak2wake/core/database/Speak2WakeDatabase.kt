package com.speak2wake.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        entities =
                [
                        AlarmEntity::class,
                        VocabularyEntity::class,
                        ChallengeSessionEntity::class,
                        ChallengeAttemptEntity::class,
                ],
        version = 5,
        exportSchema = true,
)
abstract class Speak2WakeDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun challengeDao(): ChallengeDao
}
