package com.buildabear.tracker.util

object InfoboxParser {
    private val infoboxRegex = Regex(
        pattern = """\{\{Build-A-Bear\s*\n?(.*?)\n?\}\}""",
        options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
    )

    fun parse(wikitext: String): Map<String, String> {
        val match = infoboxRegex.find(wikitext) ?: return emptyMap()
        val body = match.groupValues[1]
        val fields = mutableMapOf<String, String>()
        val lines = body.split("\n")
        for (line in lines) {
            val trimmed = line.trim().removePrefix("|")
            val eqIndex = trimmed.indexOf('=')
            if (eqIndex <= 0) continue
            val key = trimmed.substring(0, eqIndex).trim().lowercase()
            val value = trimmed.substring(eqIndex + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                fields[key] = value
            }
        }
        return fields
    }

    fun extractImageUrls(fields: Map<String, String>, pageTitle: String): List<String> {
        val imageField = fields["image1"] ?: fields["image"] ?: return emptyList()
        val fileNames = imageField.split("\n")
            .map { it.substringBefore("|").trim() }
            .filter { it.isNotEmpty() && !it.startsWith("http") }
        return fileNames.map { fileName ->
            val normalized = fileName.replace(' ', '_')
            "https://static.wikia.nocookie.net/buildabear/images/${normalized.substring(0, 1).lowercase()}/${normalized.substring(0, 2).lowercase()}/$normalized/revision/latest/scale-to-width-down/400"
        }
    }

    fun mapToBearFields(
        fields: Map<String, String>,
        pageTitle: String,
        pageId: Int,
        categories: List<String>,
    ): ParsedBear {
        val imageUrls = extractImageUrls(fields, pageTitle)
        val availableRaw = fields["available"]?.lowercase()
        return ParsedBear(
            externalId = pageId.toString(),
            name = pageTitle,
            description = null,
            yearReleased = fields["year_released"],
            furColor = fields["fur_color"],
            eyeColor = fields["eye_color"],
            height = fields["height"],
            weight = fields["weight"],
            sku = fields["sku"],
            price = fields["price"],
            available = when (availableRaw) {
                "yes", "true" -> true
                "no", "false" -> false
                else -> null
            },
            imageUrls = imageUrls,
            sourceUrl = "https://buildabear.fandom.com/wiki/${pageTitle.replace(' ', '_')}",
            categories = categories,
            extraMetadataJson = null,
        )
    }
}

data class ParsedBear(
    val externalId: String,
    val name: String,
    val description: String?,
    val yearReleased: String?,
    val furColor: String?,
    val eyeColor: String?,
    val height: String?,
    val weight: String?,
    val sku: String?,
    val price: String?,
    val available: Boolean?,
    val imageUrls: List<String>,
    val sourceUrl: String,
    val categories: List<String>,
    val extraMetadataJson: String?,
)
