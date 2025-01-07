package aurora.reminder.todolist.calendar.database

import androidx.room.migration.*
import androidx.sqlite.db.*

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE task_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                start_date INTEGER NOT NULL, -- Corresponds to Long in Kotlin
                end_date INTEGER NOT NULL,   -- Corresponds to Long in Kotlin
                daily_start_time INTEGER NOT NULL, -- Corresponds to Long in Kotlin
                daily_end_time INTEGER NOT NULL,   -- Corresponds to Long in Kotlin
                workspace_id INTEGER NOT NULL,
                is_completed INTEGER NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT INTO task_new (id, title, body, start_date, end_date, daily_start_time, daily_end_time, workspace_id, is_completed)
            SELECT id, title, body, start_date, end_date, daily_start_time, daily_end_time, workspace_id, is_completed
            FROM task
            """.trimIndent()
        )

        database.execSQL("DROP TABLE task")
        database.execSQL("ALTER TABLE task_new RENAME TO task")
    }
}
