package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.models.RssMeme

data class RssMemesResponse(
        val status: Status,
        val data: List<RssMeme>?,
        val error: String?
) {
    companion object {
        fun loading(): RssMemesResponse = RssMemesResponse(Status.LOADING, null, null)

        fun success(data: List<RssMeme>): RssMemesResponse
                = RssMemesResponse(Status.SUCCESS, data, null)

        fun error(error: String): RssMemesResponse = RssMemesResponse(Status.ERROR, null, error)
    }
}