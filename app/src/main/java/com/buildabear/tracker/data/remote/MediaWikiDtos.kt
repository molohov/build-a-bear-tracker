package com.buildabear.tracker.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WikiQueryResponse(
    @Json(name = "batchcomplete") val batchComplete: String? = null,
    val query: WikiQuery? = null,
    @Json(name = "continue") val continueToken: WikiContinue? = null,
)

@JsonClass(generateAdapter = true)
data class WikiQuery(
    val allpages: List<WikiPageRef>? = null,
    val pages: Map<String, WikiPageDetail>? = null,
    val categorymembers: List<WikiPageRef>? = null,
)

@JsonClass(generateAdapter = true)
data class WikiContinue(
    val apcontinue: String? = null,
    val cmcontinue: String? = null,
    val continueToken: String? = null,
)

@JsonClass(generateAdapter = true)
data class WikiPageRef(
    val pageid: Int,
    val title: String,
)

@JsonClass(generateAdapter = true)
data class WikiPageDetail(
    val pageid: Int,
    val title: String,
    val revisions: List<WikiRevision>? = null,
    val categories: List<WikiCategory>? = null,
)

@JsonClass(generateAdapter = true)
data class WikiRevision(
    val slots: WikiSlots? = null,
)

@JsonClass(generateAdapter = true)
data class WikiSlots(
    val main: WikiSlotContent? = null,
)

@JsonClass(generateAdapter = true)
data class WikiSlotContent(
    @Json(name = "*") val content: String? = null,
)

@JsonClass(generateAdapter = true)
data class WikiCategory(
    val title: String,
)
