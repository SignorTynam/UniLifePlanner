package com.example.unilifeplanner.university.publicimport.unibo

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object UniboPublicOkHttpDebugLogging {
    fun applyTo(builder: OkHttpClient.Builder) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        builder.addInterceptor(loggingInterceptor)
    }
}
