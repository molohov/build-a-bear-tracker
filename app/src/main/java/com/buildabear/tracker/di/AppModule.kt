package com.buildabear.tracker.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.buildabear.tracker.data.local.AppDatabase
import com.buildabear.tracker.data.remote.MediaWikiApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bear_tracker.db").build()

    @Provides
    fun provideBearDao(db: AppDatabase) = db.bearDao()

    @Provides
    fun provideCollectionStatusDao(db: AppDatabase) = db.collectionStatusDao()

    @Provides
    fun provideSavedFilterDao(db: AppDatabase) = db.savedFilterDao()

    @Provides
    fun provideImportRunDao(db: AppDatabase) = db.importRunDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
        )
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "BuildABearTracker/1.0 (personal collector app)")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    fun provideMediaWikiApi(client: OkHttpClient, moshi: Moshi): MediaWikiApi =
        Retrofit.Builder()
            .baseUrl("https://buildabear.fandom.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MediaWikiApi::class.java)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
