package com.buildabear.tracker.util

object InfoboxParser {
    private val infoboxRegex = Regex(
        pattern = """\{\{Build-A-Bear\s*\n?(.*?)\n?\}\}""",
        options = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
    )

    fun parse(wikitext: String): Map<String, String> {
        val match = infoboxRegex.find(wikitext) ?: return emptyMap()
        val body = match.groupValues[1].trim()
        val fields = mutableMapOf<String, String>()

        if (!body.contains('\n')) {
            body.removePrefix("|").split("|").forEach { segment ->
                putFieldSegment(segment.trim(), fields)
            }
            return fields
        }

        var currentKey: String? = null
        for (line in body.split("\n")) {
            val trimmed = line.trim().removePrefix("|").trim()
            if (trimmed.isEmpty()) continue
            val eqIndex = trimmed.indexOf('=')
            val keyCandidate = if (eqIndex > 0) trimmed.substring(0, eqIndex).trim() else ""
            val looksLikeNewField = eqIndex > 0 && keyCandidate.all { it.isLetterOrDigit() || it == '_' }

            if (looksLikeNewField) {
                currentKey = keyCandidate.lowercase()
                fields[currentKey] = trimmed.substring(eqIndex + 1).trim()
            } else if (currentKey != null) {
                fields[currentKey] = fields.getValue(currentKey) + "\n" + trimmed
            }
        }
        return fields
    }

    private fun putFieldSegment(segment: String, fields: MutableMap<String, String>) {
        val eqIndex = segment.indexOf('=')
        if (eqIndex <= 0) return
        val key = segment.substring(0, eqIndex).trim().lowercase()
        val value = segment.substring(eqIndex + 1).trim()
        if (key.isNotEmpty() && value.isNotEmpty()) {
            fields[key] = value
        }
    }

    fun extractImageFileNames(fields: Map<String, String>): List<String> {
        val imageField = fields["image1"] ?: fields["image"] ?: return emptyList()
        return parseImageFieldContent(imageField)
    }

    fun extractImageFileNamesFromWikitext(wikitext: String): List<String> {
        val fromInfobox = extractImageFileNames(parse(wikitext))
        if (fromInfobox.isNotEmpty()) return fromInfobox

        val galleryMatch = Regex("""<gallery>(.*?)</gallery>""", RegexOption.DOT_MATCHES_ALL)
            .find(wikitext)
        if (galleryMatch != null) {
            val fromGallery = parseImageFieldContent(galleryMatch.groupValues[1])
            if (fromGallery.isNotEmpty()) return fromGallery
        }

        return Regex("""\[\[File:([^\]|]+)""", RegexOption.IGNORE_CASE)
            .findAll(wikitext)
            .map { it.groupValues[1].trim() }
            .filter { it.contains('.') }
            .distinct()
            .toList()
    }

    private fun parseImageFieldContent(content: String): List<String> {
        val galleryMatch = Regex("""<gallery>(.*?)</gallery>""", RegexOption.DOT_MATCHES_ALL)
            .find(content)
        val body = galleryMatch?.groupValues?.get(1) ?: content

        return body.split("\n")
            .map { line ->
                line.trim()
                    .substringBefore("|")
                    .removePrefix("File:")
                    .removePrefix("Image:")
                    .trim()
            }
            .filter { name ->
                name.isNotEmpty() &&
                    !name.startsWith("http") &&
                    !name.startsWith("<") &&
                    name.contains('.')
            }
    }

    fun mapToBearFields(
        fields: Map<String, String>,
        pageTitle: String,
        pageId: Int,
        categories: List<String>,
    ): ParsedBear {
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
            imageUrls = emptyList(),
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
