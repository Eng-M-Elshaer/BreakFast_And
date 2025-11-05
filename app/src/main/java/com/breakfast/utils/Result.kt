package com.breakfast.utils

/**
 * Represents the state of a resource that is being loaded asynchronously.
 */
sealed class Result<out T> {
    /**
     * Indicates that the data is currently being loaded.
     */
    object Loading : Result<Nothing>()

    /**
     * Indicates that the data has been successfully loaded.
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Indicates that an error has occurred while loading the data.
     */
    data class Error(val message: String?) : Result<Nothing>()
}