package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.FakeCustomerRepository
import com.nutrisport.shared.domain.fakeCustomer
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class UpdateCustomerUseCaseTest {

    private val fakeRepo = FakeCustomerRepository()
    private val useCase = UpdateCustomerUseCase(fakeRepo)

    @Test
    fun `should return Right when customer is updated`() = runTest {
        val result = useCase(customer = fakeCustomer())

        assertThat(result).isEqualTo(Either.Right(Unit))
    }

    @Test
    fun `should return Left when update fails`() = runTest {
        fakeRepo.updateCustomerError = "Update failed"

        val result = useCase(customer = fakeCustomer())

        assertThat(result.isLeft).isTrue()
        assertThat(result.leftOrNull()?.message).isEqualTo("Update failed")
    }
}
