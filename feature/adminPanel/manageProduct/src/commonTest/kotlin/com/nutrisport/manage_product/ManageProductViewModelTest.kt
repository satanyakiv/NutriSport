package com.nutrisport.manage_product

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import androidx.lifecycle.SavedStateHandle
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManageProductViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val fakeAdminRepo = FakeAdminRepository(
        products = listOf(
            fakeProduct(
                id = "prod-1",
                title = "T",
                description = "D",
                thumbnail = "U",
                category = "Protein",
                price = 10.0,
                flavors = null,
                weight = null,
            ),
        ),
    )

    private fun createViewModel(productId: String = ""): ManageProductViewModel {
        val savedState = SavedStateHandle(mapOf("id" to productId))
        return ManageProductViewModel(fakeAdminRepo, savedState)
    }

    @Test
    fun `should start with empty form state`() {
        val viewModel = createViewModel()

        assertThat(viewModel.screenState.title).isEqualTo("")
        assertThat(viewModel.screenState.price).isEqualTo(0.0)
        assertThat(viewModel.isFormValid).isEqualTo(false)
    }

    @Test
    fun `should update title`() {
        val viewModel = createViewModel()

        viewModel.updateTitle("New Protein")

        assertThat(viewModel.screenState.title).isEqualTo("New Protein")
    }

    @Test
    fun `should update description`() {
        val viewModel = createViewModel()

        viewModel.updateDescription("Best protein ever")

        assertThat(viewModel.screenState.description).isEqualTo("Best protein ever")
    }

    @Test
    fun `should update price`() {
        val viewModel = createViewModel()

        viewModel.updatePrice(29.99)

        assertThat(viewModel.screenState.price).isEqualTo(29.99)
    }

    @Test
    fun `should update category`() {
        val viewModel = createViewModel()

        viewModel.updateCategory(ProductCategory.Creatine)

        assertThat(viewModel.screenState.category).isEqualTo(ProductCategory.Creatine)
    }

    @Test
    fun `should validate form when all required fields are filled`() {
        val viewModel = createViewModel()

        viewModel.updateTitle("Protein")
        viewModel.updateDescription("Description")
        viewModel.updateThumbnail("https://img.com/p.jpg")
        viewModel.updatePrice(10.0)

        assertThat(viewModel.isFormValid).isEqualTo(true)
    }

    @Test
    fun `should invalidate form when price is zero`() {
        val viewModel = createViewModel()

        viewModel.updateTitle("Protein")
        viewModel.updateDescription("Description")
        viewModel.updateThumbnail("https://img.com/p.jpg")

        assertThat(viewModel.isFormValid).isEqualTo(false)
    }

    @Test
    fun `should call onSuccess when creating product`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.createNewProduct(onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when creating product fails`() = runTest(testDispatcher) {
        fakeAdminRepo.createProductError = "Create failed"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.createNewProduct(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Create failed")
    }

    @Test
    fun `should load existing product when id is provided`() = runTest(testDispatcher) {
        val viewModel = createViewModel("prod-1")
        advanceUntilIdle()

        assertThat(viewModel.screenState.title).isEqualTo("T")
    }

    @Test
    fun `should set thumbnail uploader error when file is null`() {
        val viewModel = createViewModel()

        viewModel.uploadThumbnailToStorage(file = null, onSuccess = {})

        assertThat(viewModel.thumbnailUploaderState).isInstanceOf<UiState.Content<Unit>>()
        val content = viewModel.thumbnailUploaderState as UiState.Content<Unit>
        assertThat(content.result).isInstanceOf<Either.Left<AppError>>()
    }

    @Test
    fun `should update boolean flags`() {
        val viewModel = createViewModel()

        viewModel.updateIsNew(true)
        viewModel.updateIsPopular(true)
        viewModel.updateIsDiscounted(true)

        assertThat(viewModel.screenState.isNew).isEqualTo(true)
        assertThat(viewModel.screenState.isPopular).isEqualTo(true)
        assertThat(viewModel.screenState.isDiscounted).isEqualTo(true)
    }

    @Test
    fun `should call onError when form is invalid on update`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.updateProduct(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Please fill in the form correctly")
    }
}
