package com.gelostech.dankmemes.data

sealed class Result<out T : Any>{
    data class Success<out T : Any>(val data: T) : Result<T>()
    data class Error(val error: String) : Result<Nothing>()
}