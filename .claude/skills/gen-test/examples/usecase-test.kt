// Example: Pure use case test (no Android deps, fake repository)
package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.FakeCustomerRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SignOutUseCaseTest {

    private val fakeRepo = FakeCustomerRepository()
    private val useCase = SignOutUseCase(fakeRepo)

    @Test
    fun `should return success when sign out succeeds`() = runTest {
        // Arrange
        fakeRepo.signOutResult = Either.Right(Unit)

        // Act
        val result = useCase()

        // Assert
        assertThat(result.isRight).isTrue()
    }

    @Test
    fun `should return error when sign out fails`() = runTest {
        // Arrange
        fakeRepo.signOutResult = Either.Left(AppError.Network("Sign out failed"))

        // Act
        val result = useCase()

        // Assert
        assertThat(result.isLeft).isTrue()
        assertThat(result.leftOrNull()?.message).isEqualTo("Sign out failed")
    }
}
