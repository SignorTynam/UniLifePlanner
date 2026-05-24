package com.example.unilifeplanner.university.publicimport.unibo

import com.example.unilifeplanner.university.publicimport.academicYearStart
import com.example.unilifeplanner.university.publicimport.normalizeText
import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

class UniboPublicClient(
    client: OkHttpClient? = null
) {
    private val httpClient = client ?: defaultClient()

    suspend fun searchDegreeProgramsPages(
        query: String,
        campus: String?,
        degreeType: String?
    ): List<String> {
        return degreeSearchUrls(
            query = query,
            campus = campus,
            degreeType = degreeType
        ).map { url -> fetch(url) }
    }

    suspend fun searchDegreeProgramsPage(
        query: String,
        campus: String?,
        degreeType: String?
    ): String {
        return searchDegreeProgramsPages(
            query = query,
            campus = campus,
            degreeType = degreeType
        ).joinToString(separator = "\n")
    }

    suspend fun getDegreeProgramPage(url: String): String = fetch(url.toHttpUrl())

    suspend fun getTeachingPlanIndexPage(
        degreeProgramSiteUrl: String,
        academicYear: String,
        degreeProgramCode: String
    ): String {
        val url = degreeProgramSiteUrl.toHttpUrl()
            .newBuilder()
            .addPathSegment("insegnamenti")
            .addQueryParameter("year", academicYearStart(academicYear))
            .addQueryParameter("code", degreeProgramCode)
            .build()
        return fetch(url)
    }

    suspend fun getTeachingPage(url: String): String = fetch(url.toHttpUrl())

    suspend fun searchTeachingByCodePage(
        code: String,
        academicYear: String
    ): String {
        val url = UniboPublicConfig.BASE_URL.toHttpUrl()
            .newBuilder()
            .encodedPath(UniboPublicConfig.TEACHINGS_SEARCH_PATH)
            .addQueryParameter("search", "True")
            .addQueryParameter("codiceMateria", code)
            .addQueryParameter("annoAccademico", academicYearStart(academicYear))
            .addQueryParameter("CodeInsegnamentoButton", "cerca")
            .build()
        return fetch(url)
    }

    private suspend fun fetch(url: HttpUrl): String = withContext(Dispatchers.IO) {
        ensurePublicUrl(url)
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UniboPublicConfig.USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                when (response.code) {
                    404 -> throw UniboPublicImportException("Pagina UniBo non trovata")
                    in 500..599 -> throw UniboPublicImportException(
                        "Servizio UniBo temporaneamente non disponibile"
                    )
                }
                if (!response.isSuccessful) {
                    throw UniboPublicImportException("Errore UniBo HTTP ${response.code}")
                }
                response.body.string()
            }
        } catch (exception: SocketTimeoutException) {
            throw UniboPublicImportException("Connessione lenta o non disponibile", exception)
        } catch (exception: SSLException) {
            throw UniboPublicImportException("Connessione sicura non riuscita", exception)
        } catch (exception: UnknownHostException) {
            throw UniboPublicImportException("Il sito UniBo non e raggiungibile", exception)
        } catch (exception: IOException) {
            throw UniboPublicImportException("Il sito UniBo non e raggiungibile", exception)
        }
    }

    private fun degreeSearchUrls(
        query: String,
        campus: String?,
        degreeType: String?
    ): List<HttpUrl> {
        val normalizedType = normalizeText(degreeType.orEmpty())
        val requests = when (normalizedType) {
            "laurea" -> listOf(
                DegreeSearchRequest(
                    path = UniboPublicConfig.DEGREE_SINGLE_CYCLE_SEARCH_PATH,
                    duration = "3"
                )
            )
            "laurea magistrale" -> listOf(
                DegreeSearchRequest(
                    path = UniboPublicConfig.DEGREE_MASTER_SEARCH_PATH,
                    duration = "2"
                )
            )
            "laurea magistrale a ciclo unico" -> listOf(
                DegreeSearchRequest(
                    path = UniboPublicConfig.DEGREE_SINGLE_CYCLE_SEARCH_PATH,
                    duration = "5"
                )
            )
            else -> listOf(
                DegreeSearchRequest(path = UniboPublicConfig.DEGREE_SINGLE_CYCLE_SEARCH_PATH),
                DegreeSearchRequest(path = UniboPublicConfig.DEGREE_MASTER_SEARCH_PATH)
            )
        }

        return requests.map { request ->
            UniboPublicConfig.BASE_URL.toHttpUrl()
                .newBuilder()
                .encodedPath(request.path)
                .addQueryParameter("orderby", "alphabetic")
                .addQueryParameter("fulltext", query)
                .apply {
                    campus?.takeIf { it.isNotBlank() }?.let { addQueryParameter("sede", it) }
                    request.duration?.let { addQueryParameter("durata", it) }
                }
                .build()
        }
    }

    private fun ensurePublicUrl(url: HttpUrl) {
        val host = url.host.lowercase()
        val allowed = host == "www.unibo.it" || host == "corsi.unibo.it"
        if (!allowed) {
            throw UniboPublicImportException(UniboPublicConfig.PRIVATE_INFORMATION_MESSAGE)
        }
    }

    private data class DegreeSearchRequest(
        val path: String,
        val duration: String? = null
    )

    companion object {
        private fun defaultClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
            UniboPublicOkHttpDebugLogging.applyTo(builder)
            return builder.build()
        }
    }
}
