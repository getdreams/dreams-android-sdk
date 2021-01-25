/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.getdreams

/**
 * A simple result class that represent if something was successful or failed.
 *
 * @param V The type for the [Success] value.
 * @param E The type for the [Failure] value.
 */
sealed class Result<out V : Any, out E : Any> {
    companion object {
        fun <E : Any> failure(error: E) = Failure(error)
        fun <V : Any> success(value: V) = Success(value)
    }

    val isFailure: Boolean get() = this is Failure<E>
    val isSuccess: Boolean get() = this is Success<V>

    open operator fun component1(): V? = null
    open operator fun component2(): E? = null

    fun getOrNull(): V? = component1()
    fun exceptionOrNull(): E? = component2()

    class Success<out V : Any>(val value: V) : Result<V, Nothing>() {
        override fun component1(): V = value

        override fun toString(): String = "[Success $value]"

        override fun hashCode(): Int = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Success<*> && value == other.value
        }
    }

    class Failure<out E : Any>(val error: E) : Result<Nothing, E>() {
        override fun component2(): E = error

        override fun toString(): String = "[Failure $error]"

        override fun hashCode(): Int = error.hashCode()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Failure<*> && error == other.error
        }
    }
}
