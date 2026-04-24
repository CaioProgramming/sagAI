package com.ilustris.sagai.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Drop old columns
                db.execSQL("ALTER TABLE sagas DROP COLUMN introduction")
                db.execSQL("ALTER TABLE sagas DROP COLUMN playstyle")
                db.execSQL("ALTER TABLE sagas DROP COLUMN topCharacters")
                db.execSQL("ALTER TABLE sagas DROP COLUMN actsInsight")
                db.execSQL("ALTER TABLE sagas DROP COLUMN conclusion")

                // Add new columns for ReviewStage (introduction)
                db.execSQL("ALTER TABLE sagas ADD COLUMN intro_hook TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN intro_content TEXT")

                // Add new columns for ReviewStage (playstyle)
                db.execSQL("ALTER TABLE sagas ADD COLUMN playstyle_hook TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN playstyle_content TEXT")

                // Add new columns for ReviewStage (topCharacters)
                db.execSQL("ALTER TABLE sagas ADD COLUMN character_hook TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN character_content TEXT")

                // Add new columns for ReviewStage (actsInsight)
                db.execSQL("ALTER TABLE sagas ADD COLUMN journey_hook TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN journey_content TEXT")

                // Add new columns for ReviewStage (conclusion)
                db.execSQL("ALTER TABLE sagas ADD COLUMN conclusion_hook TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN conclusion_content TEXT")
            }
        }

    val MIGRATION_2_3 =
        object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val stages = listOf("intro", "playstyle", "character", "journey", "conclusion")
                stages.forEach { stage ->
                    // Drop version 2 columns
                    db.execSQL("ALTER TABLE sagas DROP COLUMN ${stage}_hook")
                    db.execSQL("ALTER TABLE sagas DROP COLUMN ${stage}_content")

                    // Add version 3 columns
                    db.execSQL("ALTER TABLE sagas ADD COLUMN ${stage}_hook_title TEXT")
                    db.execSQL("ALTER TABLE sagas ADD COLUMN ${stage}_hook_subtitle TEXT")
                    db.execSQL("ALTER TABLE sagas ADD COLUMN ${stage}_content_title TEXT")
                    db.execSQL("ALTER TABLE sagas ADD COLUMN ${stage}_content_subtitle TEXT")
                }
            }
        }

    val MIGRATION_3_4 =
        object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sagas ADD COLUMN activity_hook_title TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN activity_hook_subtitle TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN activity_content_title TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN activity_content_subtitle TEXT")
            }
        }

    val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sagas ADD COLUMN variationId TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_sagas_variationId` ON `sagas` (`variationId`)")
            }
        }

    val MIGRATION_6_7 =
        object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ai_audit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `model` TEXT NOT NULL, `blueprintKey` TEXT, `dataType` TEXT NOT NULL, `status` TEXT NOT NULL, `reasoning` TEXT, `rawResponse` TEXT, `errorMessage` TEXT)",
                )
            }
        }

    val MIGRATION_7_8 =
        object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `suggestion` TEXT")
            }
        }

    val MIGRATION_8_9 =
        object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `responseTime` INTEGER NOT NULL DEFAULT 0")
            }
        }

    fun getAllMigrations(): Array<Migration> =
        arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
        )
}
