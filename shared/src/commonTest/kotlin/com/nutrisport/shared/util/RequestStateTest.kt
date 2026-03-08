package com.nutrisport.shared.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.test.assertFailsWith

class RequestStateTest {

    @Test
    fun `Idle state should report isIdle true`() {
        val state: RequestState<String> = RequestState.Idle

        assertThat(state.isIdle()).isTrue()
        assertThat(state.isLoading()).isFalse()
        assertThat(state.isSuccess()).isFalse()
        assertThat(state.isError()).isFalse()
    }

    @Test
    fun `Loading state should report isLoading true`() {
        val state: RequestState<String> = RequestState.Loading

        assertThat(state.isLoading()).isTrue()
        assertThat(state.isIdle()).isFalse()
    }

    @Test
    fun `Success state should hold data`() {
        val state = RequestState.Success("hello")

        assertThat(state.isSuccess()).isTrue()
        assertThat(state.getSuccessData()).isEqualTo("hello")
        assertThat(state.getSuccessDataOrNull()).isEqualTo("hello")
    }

    @Test
    fun `Error state should hold message`() {
        val state = RequestState.Error("something broke")

        assertThat(state.isError()).isTrue()
        assertThat(state.getErrorMessage()).isEqualTo("something broke")
    }

    @Test
    fun `getSuccessDataOrNull should return null for non-success`() {
        val state: RequestState<String> = RequestState.Loading

        assertThat(state.getSuccessDataOrNull()).isNull()
    }

    @Test
    fun `getSuccessData on Error should throw`() {
        val state: RequestState<String> = RequestState.Error("fail")

        assertFailsWith<ClassCastException> {
            state.getSuccessData()
        }
    }
}
