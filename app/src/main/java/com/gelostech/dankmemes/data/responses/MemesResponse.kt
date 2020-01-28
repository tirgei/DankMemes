package com.gelostech.dankmemes.data.responses

import com.gelostech.dankmemes.data.Status
import com.gelostech.dankmemes.data.wrappers.ObservableMeme

data class MemesResponse(
        val status: Status,
        val data: ObservableMeme?,
        val error: String?
) {
    companion object {
        fun loading(): MemesResponse = MemesResponse(Status.LOADING, null, null)

        fun success(meme: ObservableMeme): MemesResponse
                = MemesResponse(Status.SUCCESS, meme, null)

        fun error(error: String): MemesResponse = MemesResponse(Status.ERROR, null, error)
    }
}