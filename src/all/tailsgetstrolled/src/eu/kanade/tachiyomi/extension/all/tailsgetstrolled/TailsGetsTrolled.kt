package eu.kanade.tachiyomi.extension.all.tailsgetstrolled

import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import rx.Observable

open class TailsGetsTrolled(
    final override val baseUrl: String,
    final override val lang: String,
    dateFormat: String = "MMMM dd, yyyy"
) : HttpSource() {
    final override val name = "Tails Gets Trolled"

    final override val supportsLatest = false

    protected open val creator = "Lazerbot"

    protected open val synopsis =
        "A story about internet trolls, revenge, multiple opposing factions, betrayal, " +
            "necromancy, the clap, weed, romance, and more."

    protected open val chapterListSelector = ".desktoptable a"

    protected open val pagesSelector = "body > p:last-child > a"

    protected open val allSelector = "body > center > img"

    protected open val imageSelector = "body > p > img"

    private val chapterRegex = Regex("ch([0-9]+)/$")

    // private val dateFormat = SimpleDateFormat(dateFormat, Locale.ROOT)

    // protected fun String.timestamp() = dateFormat.parse(this)?.time ?: 0L

    final override fun fetchPopularManga(page: Int) =
        SManga.create().apply {
            title = name
            artist = creator
            author = creator
            description = synopsis
            status = SManga.ONGOING
            thumbnail_url = THUMBNAIL_URL
            setUrlWithoutDomain("MainSeries.html")
        }.let { Observable.just(MangasPage(listOf(it), false))!! }

    final override fun fetchSearchManga(page: Int, query: String, filters: FilterList) =
        Observable.just(MangasPage(emptyList(), false))!!

    final override fun fetchMangaDetails(manga: SManga) =
        Observable.just(manga.apply { initialized = true })!!

    override fun chapterListParse(response: Response) =
        response.asJsoup().select(chapterListSelector).map {
            // TODO: Date and chapter names are on a separate page. See if website admin is willing to cooperate?
            SChapter.create().apply {
                val regMatch = chapterRegex.find(it.attr("abs:href"))?.groups
                url = regMatch?.get(0)?.value ?: "ch1/"
                chapter_number = regMatch?.get(1)?.value?.toFloat() ?: 0F
                name = "Chapter ${chapter_number.toInt()}"
                date_upload = System.currentTimeMillis()
            }
        }.asReversed()

    override fun pageListParse(response: Response): List<Page> {

        val jsoup = response.asJsoup()

        val cover = jsoup.selectFirst(imageSelector).attr("abs:src")

        val pages = mutableListOf<Page>()

        if (jsoup.selectFirst(pagesSelector).attr("href").equals("all.html")) {
            val request = Request.Builder()
                .url(jsoup.selectFirst(pagesSelector).attr("abs:href"))
                .build()

            client.newCall(request).execute().use {
                val images = it.asJsoup().select(allSelector).map { x -> x.attr("abs:src") }
                for (image in images) {
                    pages.add(Page(pages.size, "", image))
                }
            }
        } else {
            pages.add(Page(0, "", cover))
            for (pageLink in jsoup.select(pagesSelector)) {
                val request = Request.Builder()
                    .url(pageLink.attr("abs:href"))
                    .build()

                client.newCall(request).execute().use {
                    val image = it.asJsoup().selectFirst(imageSelector).attr("abs:src")
                    pages.add(Page(pages.size, "", image))
                }
            }
        }

        return pages.toList()
    }

    final override fun imageUrlParse(response: Response) =
        throw UnsupportedOperationException("Not used")

    final override fun latestUpdatesParse(response: Response) =
        throw UnsupportedOperationException("Not used")

    final override fun latestUpdatesRequest(page: Int) =
        throw UnsupportedOperationException("Not used")

    final override fun mangaDetailsParse(response: Response) =
        throw UnsupportedOperationException("Not used")

    final override fun popularMangaParse(response: Response) =
        throw UnsupportedOperationException("Not used")

    final override fun popularMangaRequest(page: Int) =
        throw UnsupportedOperationException("Not used")

    final override fun searchMangaParse(response: Response) =
        throw UnsupportedOperationException("Not used")

    final override fun searchMangaRequest(page: Int, query: String, filters: FilterList) =
        throw UnsupportedOperationException("Not used")

    companion object {
        private const val THUMBNAIL_URL =
            "https://cdn.discordapp.com/attachments/910635666650591292/910649846678036520/tachiyomi_cover.png"
    }
}
