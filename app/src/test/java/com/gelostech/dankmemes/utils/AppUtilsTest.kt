package com.gelostech.dankmemes.utils

import com.gelostech.dankmemes.utils.AppUtils.isValidUsername
import org.junit.Test

import org.junit.Assert.*

class AppUtilsTest {

    @Test
    fun isValidUsername_containsDank_returnsFalse() {
        val result = isValidUsername("Dank Username")
        assertFalse(result)
    }

    @Test
    fun isValidUsername_appName_returnsFalse() {
        val result = isValidUsername("Dank Memes")
        assertFalse(result)
    }

    @Test
    fun isValidUsername_appNameLowercase_returnsFalse() {
        val result = isValidUsername("dank memes")
        assertFalse(result)
    }

    @Test
    fun isValidUsername_containsMemes_returnsTrue() {
        val result = isValidUsername("Memes Username")
        assertTrue(result)
    }

    @Test
    fun isValidUsername_validName_returnsTrue() {
        val result = isValidUsername("Username")
        assertTrue(result)
    }

    @Test
    fun isValidUsername_emptyName_returnsFalse() {
        val result = isValidUsername("")
        assertFalse(result)
    }

    @Test
    fun isValidUsername_containsNumbers_returnsTrue() {
        val result = isValidUsername("007")
        assertTrue(result)
    }

    @Test
    fun isValidUsername_nullInput_returnsFalse() {
        val result = isValidUsername(null)
        assertFalse(result)
    }
}