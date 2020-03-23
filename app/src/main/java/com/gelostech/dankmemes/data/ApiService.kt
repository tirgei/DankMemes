package com.gelostech.dankmemes.data

import com.gelostech.dankmemes.data.responses.RssResponse
import com.gelostech.dankmemes.utils.Constants
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface to declare REST Api endpoints
 */
interface ApiService {

    /**
     * Fetch memes from 9gag RSS
     * @param source - Source of memes
     */
    @GET(Constants.MEMES_SOURCE)
    suspend fun fetchMemes(): Call<RssResponse>

}