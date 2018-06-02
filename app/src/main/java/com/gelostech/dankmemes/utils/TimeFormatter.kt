package com.gelostech.dankmemes.utils;

import android.text.format.DateUtils
import android.util.Log
import com.kizitonwose.time.hours
import com.kizitonwose.time.seconds
import java.text.SimpleDateFormat
import java.util.*

class TimeFormatter {

    private var timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private var weekFormat = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
    private var fullFormat = SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault())
    private var detailFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
    private var simpleYearFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    private var normalYearFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var saveFormat = SimpleDateFormat("yyyyMMdd-mmss", Locale.getDefault())

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

    fun getChatTimeStamp(time: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = getTimeDifference(currentTime, time)
        val formattedTime: String

        formattedTime = when {
            timeDifference <= 12.hours.inMilliseconds.longValue -> getTime(time)
            isYesterday(time) -> "Yesterday"
            else -> getNormalYear(time)
        }

        Log.d(javaClass.simpleName, "${isYesterday(time)}")

        return formattedTime
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

    fun getSimpleYear(millis: Long): String {
        return simpleYearFormat.format(millis)
    }

    fun getNormalYear(millis: Long): String {
        return normalYearFormat.format(millis)
    }

    fun getSaveFormat(millis: Long): String {
        return saveFormat.format(millis)
    }

    private fun isThisYear(millis: Long): Boolean {
        val cal = Calendar.getInstance()
        cal.time = Date(millis)

        return cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)
    }

    private fun isToday(millis: Long): Boolean {
        return DateUtils.isToday(millis)
    }

    private fun isYesterday(millis: Long): Boolean {
        return DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS)
    }

}