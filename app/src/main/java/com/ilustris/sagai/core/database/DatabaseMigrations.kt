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

    val MIGRATION_17_18 =
        object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `systemInstruction` TEXT")
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `sentVariables` TEXT")
            }
        }

    val MIGRATION_18_19 =
        object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Chapter RENAME COLUMN `overview` TO `content`")
            }
        }

    val MIGRATION_19_20 =
        object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `promptTokens` INTEGER")
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `candidatesTokens` INTEGER")
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `totalTokens` INTEGER")
            }
        }

    val MIGRATION_20_21 =
        object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_audit_logs ADD COLUMN `queueWaitMs` INTEGER NOT NULL DEFAULT 0")
            }
        }

    val MIGRATION_21_22 =
        object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val continuityColumns =
                    listOf(
                        "establishedFacts",
                        "openThreads",
                        "consequences",
                        "characterStates",
                        "persistentSetups",
                    )
                continuityColumns.forEach { column ->
                    db.execSQL("ALTER TABLE Chapter ADD COLUMN `continuity_$column` TEXT")
                    db.execSQL("ALTER TABLE acts ADD COLUMN `continuity_$column` TEXT")
                }
            }
        }

    val MIGRATION_22_23 =
        object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Chapter ADD COLUMN `artwork` TEXT DEFAULT ''")
            }
        }

    val MIGRATION_23_24 =
        object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_notificationHook` TEXT")
            }
        }

    val MIGRATION_24_25 =
        object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE timelines ADD COLUMN `scene_notificationCharacterName` TEXT")
            }
        }

    val MIGRATION_25_26 =
        object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Characters ADD COLUMN `age` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE sagas ADD COLUMN `artwork` TEXT DEFAULT ''")
            }
        }

    val MIGRATION_26_27 =
        object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Characters ADD COLUMN `artwork` TEXT DEFAULT ''")
            }
        }

    val MIGRATION_27_28 =
        object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sagas ADD COLUMN `farewells` TEXT")
            }
        }

    val MIGRATION_28_29 =
        object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // The column default has to be 0 to match Message.viewed's @ColumnInfo(defaultValue
                // = "0") — Room validates the migrated schema against the entity's expected
                // TableInfo, and a DEFAULT 1 here fails that check with an IllegalStateException at
                // startup ("Migration didn't properly handle: messages(...)").
                db.execSQL("ALTER TABLE messages ADD COLUMN `viewed` INTEGER NOT NULL DEFAULT 0")
                // Backfill separately: every message that already exists has, by definition,
                // already been read — otherwise the whole history would re-run the typewriter
                // animation on the next launch.
                db.execSQL("UPDATE messages SET viewed = 1")
            }
        }

    val MIGRATION_30_31 =
        object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Local accounting of what this app spends of the user's API key. Keyed by the
                // Pacific date because that is when Google's daily counters reset.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `api_usage_days` (
                        `day` TEXT NOT NULL,
                        `model` TEXT NOT NULL,
                        `requests` INTEGER NOT NULL DEFAULT 0,
                        `promptTokens` INTEGER NOT NULL DEFAULT 0,
                        `candidatesTokens` INTEGER NOT NULL DEFAULT 0,
                        `thoughtsTokens` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`day`, `model`)
                    )
                    """.trimIndent(),
                )
            }
        }

    val MIGRATION_29_30 =
        object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Smart zoom was dropped, so the four embedded zoom_* columns have to go. SQLite
                // only learned ALTER TABLE DROP COLUMN in 3.35 (API 34) and minSdk here is 27, so
                // the table has to be rebuilt instead.
                //
                // Six tables reference Characters with ON DELETE CASCADE (messages among them), and
                // DROP TABLE fires those cascades whenever foreign keys are enforced. That is safe
                // here only because Room enables enforcement in onOpen, which runs after migrations
                // — the same reason Room's own auto-migrations recreate tables this way.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `Characters_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `lastName` TEXT,
                        `nicknames` TEXT,
                        `knowledge` TEXT DEFAULT null,
                        `backstory` TEXT NOT NULL,
                        `image` TEXT NOT NULL,
                        `hexColor` TEXT NOT NULL,
                        `sagaId` INTEGER NOT NULL,
                        `joinedAt` INTEGER NOT NULL,
                        `firstSceneId` INTEGER,
                        `emojified` INTEGER NOT NULL,
                        `voice` TEXT DEFAULT '',
                        `artwork` TEXT DEFAULT '',
                        `race` TEXT NOT NULL,
                        `gender` TEXT NOT NULL,
                        `ethnicity` TEXT NOT NULL,
                        `age` INTEGER NOT NULL,
                        `height` REAL NOT NULL,
                        `weight` REAL NOT NULL,
                        `hair` TEXT NOT NULL,
                        `eyes` TEXT NOT NULL,
                        `mouth` TEXT NOT NULL,
                        `distinctiveMarks` TEXT NOT NULL,
                        `jawline` TEXT NOT NULL,
                        `buildAndPosture` TEXT NOT NULL,
                        `skinAppearance` TEXT NOT NULL,
                        `distinguishFeatures` TEXT NOT NULL,
                        `outfitDescription` TEXT NOT NULL,
                        `accessories` TEXT NOT NULL,
                        `carriedItems` TEXT NOT NULL,
                        `skillsAndProficiencies` TEXT NOT NULL,
                        `uniqueOrSignatureTalents` TEXT NOT NULL,
                        `occupation` TEXT NOT NULL,
                        `personality` TEXT NOT NULL,
                        FOREIGN KEY(`sagaId`) REFERENCES `sagas`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `Characters_new` (
                        `id`, `name`, `lastName`, `nicknames`, `knowledge`, `backstory`, `image`,
                        `hexColor`, `sagaId`, `joinedAt`, `firstSceneId`, `emojified`, `voice`,
                        `artwork`, `race`, `gender`, `ethnicity`, `age`, `height`, `weight`, `hair`,
                        `eyes`, `mouth`, `distinctiveMarks`, `jawline`, `buildAndPosture`,
                        `skinAppearance`, `distinguishFeatures`, `outfitDescription`, `accessories`,
                        `carriedItems`, `skillsAndProficiencies`, `uniqueOrSignatureTalents`,
                        `occupation`, `personality`
                    )
                    SELECT
                        `id`, `name`, `lastName`, `nicknames`, `knowledge`, `backstory`, `image`,
                        `hexColor`, `sagaId`, `joinedAt`, `firstSceneId`, `emojified`, `voice`,
                        `artwork`, `race`, `gender`, `ethnicity`, `age`, `height`, `weight`, `hair`,
                        `eyes`, `mouth`, `distinctiveMarks`, `jawline`, `buildAndPosture`,
                        `skinAppearance`, `distinguishFeatures`, `outfitDescription`, `accessories`,
                        `carriedItems`, `skillsAndProficiencies`, `uniqueOrSignatureTalents`,
                        `occupation`, `personality`
                    FROM `Characters`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `Characters`")
                db.execSQL("ALTER TABLE `Characters_new` RENAME TO `Characters`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_Characters_sagaId` ON `Characters` (`sagaId`)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_Characters_firstSceneId` ON `Characters` (`firstSceneId`)",
                )
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
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
        )
}
