package com.gelostech.dankmemes.data.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "item", strict = false)
data class RssMeme (
        @Element
        val author: String,

        @Element(required = false)
        val title: String,

        @Element
        val description: String
) {
    val link: String
        get() = description
}