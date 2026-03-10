package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SignOutUseCaseTest {

    private val fakeRepo = FakeCustomerRepository()
    private val useCase = SignOutUseCase(fakeRepo)

    @Test
    fun `should return success when sign out succeeds`() = runTest {
        fakeRepo.signOutResult = Either.Right(Unit)

        val result = useCase()

        assertThat(result.isRight).isTrue()
    }

    @Test
    fun `should return error when sign out fails`() = runTest {
        fakeRepo.signOutResult = Either.Left(AppError.Network("Sign out failed"))

        val result = useCase()

        assertThat(result.isLeft).isTrue()
        assertThat(result.leftOrNull()?.message).isEqualTo("Sign out failed")
    }
}
