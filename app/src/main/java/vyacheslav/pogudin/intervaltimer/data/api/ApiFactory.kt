package vyacheslav.pogudin.intervaltimer.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiFactory {
    fun create(): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("App-Token", "test-app-token")
                    .addHeader("Authorization", "Bearer test-token")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(5, TimeUnit.SECONDS)      // таймаут подключения
            .readTimeout(5, TimeUnit.SECONDS)         // таймаут чтения ответа
            .writeTimeout(5, TimeUnit.SECONDS)        // таймаут записи запроса
            .build()

        return Retrofit.Builder()
            .baseUrl("https://71-cl5.tz.testing.place/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}