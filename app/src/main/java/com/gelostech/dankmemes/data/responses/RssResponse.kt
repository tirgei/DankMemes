package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.models.RssChannel
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class RssResponse (
        @Element
        val channel: RssChannel
)