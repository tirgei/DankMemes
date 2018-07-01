package com.gelostech.dankmemes.utils;

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException

class CacheManager(private val context: Context) {

    companion object {
        private const val TAG: String = "CacheManager"
        const val DIR: String = "/PocketBartender/"
    }

    fun createJsonFile(fileName: String, jsonResponse: String) {
        deleteFile()

        try {
            val checkFile = File(context.applicationInfo.dataDir + DIR)

            if(!checkFile.exists()) checkFile.mkdir()

            val file = FileWriter(checkFile.absolutePath + "/$fileName")
            file.write(jsonResponse)
            file.flush()
            file.close()
        } catch (e: IOException) {
            Log.d(TAG, e.localizedMessage)
        }
    }

    fun readJsonFile(fileName: String): String? {
        try {
            val checkFile = File(context.applicationInfo.dataDir + "${DIR}$fileName")

            if (!checkFile.exists()) return null

            val file = FileInputStream(checkFile)
            val size = file.available()
            val buffer = ByteArray(size)
            file.read(buffer)
            file.close()
            return String(buffer)
        } catch (e: IOException) {
            Log.d(TAG, e.localizedMessage)
        }

        return null
    }

   private fun deleteFile() {
       val file = File(context.applicationInfo.dataDir + DIR)
       if(file.exists()) {
           val files = file.listFiles()

           for (f: File in files) f.delete()
       }

   }

    fun deleteFile(fileName: String) {
        val file = File(context.applicationInfo.dataDir + "${DIR}$fileName")

        if(file.exists()) file.delete()
    }

}