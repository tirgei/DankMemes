package com.gelostech.dankmemes.data

import com.gelostech.dankmemes.utils.Constants
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

/**
 * Class to declare Retrofit instances
 */
class ApiClient {

    /**
     * Function to declare default 9gag client
     */
    fun getApiClient(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.NINE_GAG_BASE_URL)
                .addConverterFactory( SimpleXmlConverterFactory.createNonStrict(Persister(AnnotationStrategy())))
                .build()
    }

}