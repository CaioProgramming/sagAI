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

    val MIGRATION_5_6 =
        object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schemas v5 and v6 share the same identity hash; no DDL changes required.
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

    val MIGRATION_9_10 =
        object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `usedTools` TEXT")
            }
        }

    val MIGRATION_10_11 =
        object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_currentLocation` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_charactersPresent` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_immediateObjective` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_currentConflict` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_mood` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_currentTimeOfDay` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_tensionLevel` INTEGER")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_spatialContext` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_narrativePacing` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_worldStateChanges` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_relevantPastContext` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_establishedFacts` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_possibleOutcomes` TEXT")
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_quote` TEXT")
            }
        }

    val MIGRATION_11_12 =
        object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timelines ADD COLUMN `emotionalTone` TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN `emotional_personaTitle` TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN `emotional_actionText` TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN `emotional_emotionalContent` TEXT")
                db.execSQL("ALTER TABLE sagas ADD COLUMN `emotional_dominantTone` TEXT")
            }
        }

    val MIGRATION_12_13 =
        object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE acts ADD COLUMN `book_actTitle` TEXT")
                db.execSQL("ALTER TABLE acts ADD COLUMN `book_sagaTitle` TEXT")
                db.execSQL("ALTER TABLE acts ADD COLUMN `book_coverQuote` TEXT")
                db.execSQL("ALTER TABLE acts ADD COLUMN `book_pages` TEXT")
            }
        }

    val MIGRATION_13_14 =
        object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE acts ADD COLUMN `book_authorNote` TEXT")
            }
        }

    val MIGRATION_14_15 =
        object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `safetyStatus` TEXT")
            }
        }

    val MIGRATION_15_16 =
        object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the new books table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `books` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `actId` INTEGER NOT NULL, `actTitle` TEXT NOT NULL, `sagaTitle` TEXT NOT NULL, `coverQuote` TEXT NOT NULL, `chapters` TEXT NOT NULL, `authorNote` TEXT, FOREIGN KEY(`actId`) REFERENCES `acts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_books_actId` ON `books` (`actId`)")

                // 2. Migrate embedded book data from acts into books (legacy columns from v12–15)
                db.execSQL(
                    """
                    INSERT INTO `books` (`actId`, `actTitle`, `sagaTitle`, `coverQuote`, `chapters`, `authorNote`)
                    SELECT
                        `id`,
                        COALESCE(`book_actTitle`, ''),
                        COALESCE(`book_sagaTitle`, ''),
                        COALESCE(`book_coverQuote`, ''),
                        COALESCE(`book_pages`, ''),
                        `book_authorNote`
                    FROM `acts`
                    WHERE `book_pages` IS NOT NULL AND TRIM(`book_pages`) != ''
                    """.trimIndent(),
                )

                // 3. Strip legacy book columns from acts table
                db.execSQL(
                    "CREATE TABLE `acts_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `introduction` TEXT NOT NULL DEFAULT '', `emotionalReview` TEXT DEFAULT '', `sagaId` INTEGER, `currentChapterId` INTEGER, FOREIGN KEY(`currentChapterId`) REFERENCES `Chapter`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )",
                )
                db.execSQL(
                    "INSERT INTO `acts_new` (`id`, `title`, `content`, `introduction`, `emotionalReview`, `sagaId`, `currentChapterId`) SELECT `id`, `title`, `content`, `introduction`, `emotionalReview`, `sagaId`, `currentChapterId` FROM `acts`",
                )
                db.execSQL("DROP TABLE `acts`")
                db.execSQL("ALTER TABLE `acts_new` RENAME TO `acts`")

                // 4. Re-create indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_acts_sagaId` ON `acts` (`sagaId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_acts_currentChapterId` ON `acts` (`currentChapterId`)")
            }
        }

    val MIGRATION_16_17 =
        object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Update sagas table
                db.execSQL("ALTER TABLE sagas ADD COLUMN `worldState` TEXT DEFAULT ''")

                // 2. Update timelines table
                db.execSQL("ALTER TABLE timelines ADD COLUMN `narrativeGuide` TEXT DEFAULT ''")

                // 3. Update Chapter table
                db.execSQL("ALTER TABLE Chapter ADD COLUMN `narrativeGuide` TEXT DEFAULT ''")

                // 4. Update acts table
                db.execSQL("ALTER TABLE acts ADD COLUMN `narrativeGuide` TEXT DEFAULT ''")

                // 5. Update wikis table
                db.execSQL("ALTER TABLE wikis ADD COLUMN `isFeatured` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE wikis ADD COLUMN `chapterId` INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_wikis_chapterId` ON `wikis` (`chapterId`)")

                // 6. Create character_arcs table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `character_arcs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `characterId` INTEGER NOT NULL, 
                        `sourceId` INTEGER NOT NULL, 
                        `sourceType` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `content` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        FOREIGN KEY(`characterId`) REFERENCES `Characters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_character_arcs_characterId` ON `character_arcs` (`characterId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_character_arcs_sourceId` ON `character_arcs` (`sourceId`)")
            }
        }

    fun getAllMigrations(): Array<Migration> =
        arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
        )
}
