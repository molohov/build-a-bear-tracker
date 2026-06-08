package com.buildabear.tracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MediaWikiApi {
    @GET("api.php")
    suspend fun queryAllPages(
        @Query("action") action: String = "query",
        @Query("list") list: String = "allpages",
        @Query("apnamespace") namespace: Int = 0,
        @Query("aplimit") limit: Int = 50,
        @Query("apcontinue") continueToken: String? = null,
        @Query("format") format: String = "json",
    ): WikiQueryResponse

    @GET("api.php")
    suspend fun queryPages(
        @Query("action") action: String = "query",
        @Query("prop") prop: String,
        @Query("titles") titles: String,
        @Query("rvprop") rvProp: String? = null,
        @Query("rvslots") rvSlots: String? = null,
        @Query("cllimit") clLimit: Int? = null,
        @Query("piprop") piProp: String? = null,
        @Query("pithumbsize") piThumbSize: Int? = null,
        @Query("iiprop") iiProp: String? = null,
        @Query("iiurlwidth") iiUrlWidth: Int? = null,
        @Query("redirects") redirects: Int? = null,
        @Query("format") format: String = "json",
    ): WikiQueryResponse
}
