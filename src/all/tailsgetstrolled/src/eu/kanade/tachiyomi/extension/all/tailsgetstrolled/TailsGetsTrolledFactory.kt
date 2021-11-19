package eu.kanade.tachiyomi.extension.all.tailsgetstrolled

import eu.kanade.tachiyomi.source.SourceFactory

class TailsGetsTrolledFactory : SourceFactory {
    override fun createSources() = listOf(
        TailsGetsTrolled("http://www.tailsgetstrolled.org/", "en")
    )
}
