package com.reelreminder.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Reel::class, Category::class, WatchLog::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reelDao(): ReelDao
    abstract fun categoryDao(): CategoryDao
    abstract fun watchLogDao(): WatchLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `gradientStart` TEXT NOT NULL,
                        `gradientEnd` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("INSERT OR IGNORE INTO `categories` (`id`, `name`, `gradientStart`, `gradientEnd`, `isDefault`, `createdAt`) VALUES (1, 'Morning', '#0891b2', '#155e75', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `categories` (`id`, `name`, `gradientStart`, `gradientEnd`, `isDefault`, `createdAt`) VALUES (2, 'Motivation', '#0d9488', '#134e4a', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `categories` (`id`, `name`, `gradientStart`, `gradientEnd`, `isDefault`, `createdAt`) VALUES (3, 'Evening', '#2563eb', '#1e3a8a', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `categories` (`id`, `name`, `gradientStart`, `gradientEnd`, `isDefault`, `createdAt`) VALUES (4, 'Study', '#059669', '#064e3b', 1, 0)")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `categoryId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reels_categoryId` ON `reels` (`categoryId`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `repeatInterval` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `repeatUnit` TEXT NOT NULL DEFAULT 'DAYS'")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `endType` TEXT NOT NULL DEFAULT 'NEVER'")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `endDateIso` TEXT")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `endAfterCount` INTEGER")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `firedCount` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `reels` ADD COLUMN `coverUri` TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `watch_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `reelId` INTEGER NOT NULL,
                        `watchedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_watch_logs_reelId` ON `watch_logs` (`reelId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_watch_logs_watchedAt` ON `watch_logs` (`watchedAt`)")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reelreminder.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).categoryDao().insertAll(Category.DEFAULT_CATEGORIES)
                            }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).categoryDao().insertAll(Category.DEFAULT_CATEGORIES)
                            }
                        }
                    })
                    .build()
                    .also { instance = it }
            }
    }
}
