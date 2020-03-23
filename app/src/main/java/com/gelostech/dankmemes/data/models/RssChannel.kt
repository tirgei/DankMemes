package com.gelostech.dankmemes.data.models

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "channel", strict = false)
data class RssChannel (
        @ElementList(inline = true)
        val memes: List<RssMeme> = mutableListOf()
)