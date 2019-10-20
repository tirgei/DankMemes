package com.gelostech.dankmemes.utils;

import android.text.format.DateUtils
import android.util.Log
import com.kizitonwose.time.hours
import com.kizitonwose.time.seconds
import java.text.SimpleDateFormat
import java.util.*

class TimeFormatter {

    private var timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    private var weekFormat = SimpleDateFormat("EEE, h:mm a", Locale.US)
    private var fullFormat = SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.US)
    private var detailFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.US)
    private var normalYearFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private var fullYearFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)

    fun getTimeStamp(time: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = getTimeDifference(currentTime, time)

        return when {
            timeDifference <= 10.seconds.inMilliseconds.longValue -> "Just now"
            timeDifference <= 24.hours.inMilliseconds.longValue -> DateUtils.getRelativeTimeSpanString(time, currentTime, 0).toString()
            timeDifference <= 168.hours.inMilliseconds.longValue -> getTimeWeek(time)
            else -> if (isThisYear(time)) getDetailDate(time) else getFullFormat(time)
        }
    }

    private fun getTimeDifference(currentTime: Long, postTime: Long) = currentTime - postTime

    fun getTime(millis: Long): String {
        return timeFormat.format(millis)
    }

     fun getTimeWeek(millis: Long): String {
        return weekFormat.format(millis)
    }

    fun getFullFormat(millis: Long): String {
        return fullFormat.format(millis)
    }

    fun getDetailDate(millis: Long): String {
        return detailFormat.format(millis)
    }

    fun getNormalYear(millis: Long): String {
        return normalYearFormat.format(millis)
    }

    fun getFullYear(millis: Long): String {
        return fullYearFormat.format(millis)
    }

    private fun isThisYear(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.time = Date(millis)

        return cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
    }


}