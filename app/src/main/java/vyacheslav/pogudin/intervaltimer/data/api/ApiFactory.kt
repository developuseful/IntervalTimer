package vyacheslav.pogudin.intervaltimer.data.api

object ApiFactory {
    fun create(): ApiService {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request().newBuilder()
                    .addHeader("App-Token", "test-app-token")
                    .addHeader("Authorization", "Bearer test-token")
                    .build()
                it.proceed(request)
            }
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl("https://71-cl5.tz.testing.place/")
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}