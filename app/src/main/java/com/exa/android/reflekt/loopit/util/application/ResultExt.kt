package com.exa.android.reflekt.loopit.util.application

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> Result<R>.onSuccess(action: (value: R) -> Unit): Result<R> {
    contract {
        callsInPlace(action, kotlin.contracts.InvocationKind.AT_MOST_ONCE)
    }
    if (isSuccess) action(getOrThrow())
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <R> Result<R>.onFailure(action: (exception: Throwable) -> Unit): Result<R> {
    contract {
        callsInPlace(action, kotlin.contracts.InvocationKind.AT_MOST_ONCE)
    }
    exceptionOrNull()?.let { action(it) }
    return this
}

fun <T> Result<T>.getOrThrow(): T {
    return getOrElse { throw it }
}

fun <T> Result<T>.exceptionOrNull(): Throwable? {
    return when {
        isFailure -> exceptionOrNull()
        else -> null
    }
}