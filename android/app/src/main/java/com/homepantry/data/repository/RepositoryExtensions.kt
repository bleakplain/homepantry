package com.homepantry.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Extension functions for Flow to handle errors consistently across repositories.
 */
inline fun <T> Flow<T>.catchAndLog(
    crossinline onError: (Throwable) -> Unit = {}
): Flow<T> = catch { e ->
    onError(e)
    // Could add logging here
}

/**
 * Result wrapper for repository operations that can fail.
 */
sealed class RepositoryResult<out T> {
    data class Success<T>(val data: T) : RepositoryResult<T>()
    data class Error(val exception: Throwable) : RepositoryResult<Nothing>()
}

/**
 * Execute a suspending function and wrap the result in RepositoryResult.
 */
suspend fun <T> repositoryCall(block: suspend () -> T): RepositoryResult<T> {
    return try {
        RepositoryResult.Success(block())
    } catch (e: Exception) {
        RepositoryResult.Error(e)
    }
}

/**
 * Extension to get data or null from RepositoryResult.
 */
fun <T> RepositoryResult<T>.getOrNull(): T? = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> null
}

/**
 * Extension to get data or default value from RepositoryResult.
 */
fun <T> RepositoryResult<T>.getOrElse(defaultValue: T): T = when (this) {
    is RepositoryResult.Success -> data
    is RepositoryResult.Error -> defaultValue
}

/**
 * Extension to map success data to another type.
 */
inline fun <T, R> RepositoryResult<T>.map(transform: (T) -> R): RepositoryResult<R> = when (this) {
    is RepositoryResult.Success -> RepositoryResult.Success(transform(data))
    is RepositoryResult.Error -> RepositoryResult.Error(exception)
}
