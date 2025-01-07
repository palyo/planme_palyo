package aurora.reminder.todolist.calendar.module

import com.google.gson.annotations.*

data class ConfigJson(
    @field:SerializedName("app_name")
    val appName: String? = null,
    @field:SerializedName("app_id")
    val appId: String? = null,
    @field:SerializedName("native")
    val nativeID: String? = null,
    @field:SerializedName("package_name")
    val packageName: String? = null,
    @field:SerializedName("inter")
    val interID: String? = null,
    @field:SerializedName("open")
    val openID: String? = null,
    @field:SerializedName("policy_url")
    val policyUrl: String? = null,
)
