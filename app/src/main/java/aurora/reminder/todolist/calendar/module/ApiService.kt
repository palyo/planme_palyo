package aurora.reminder.todolist.calendar.module

import retrofit2.*
import retrofit2.http.*

interface ApiService {

    @GET("/{configFile}")
    fun getConfig(@Path("configFile") fileName: String): Call<ConfigJson>
}