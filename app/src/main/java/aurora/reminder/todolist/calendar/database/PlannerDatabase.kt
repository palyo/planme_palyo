package aurora.reminder.todolist.calendar.database

import android.content.*
import androidx.room.*
import aurora.reminder.todolist.calendar.database.converter.*
import aurora.reminder.todolist.calendar.database.dao.*
import aurora.reminder.todolist.calendar.database.table.*

@Database(entities = [Workspace::class, Task::class, TaskActivity::class], version = 1)
@TypeConverters(Converters::class)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun taskDao(): TaskDao
    abstract fun workspaceTaskDao(): WorkspaceTaskDao
    abstract fun activityDao(): ActivityDao
    abstract fun taskActivityDao(): TaskActivityDao

    companion object {
        @Volatile
        private var INSTANCE: PlannerDatabase? = null
        fun getDatabase(context: Context): PlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlannerDatabase::class.java,
                    "planner_database.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}