package com.nutrisport.di

import com.nutrisport.shared.domain.AdminRepository
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.test.FakeAdminRepository
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.test.FakeOrderRepository
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.fakeCartItem
import com.nutrisport.shared.test.fakeCustomer
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.Either
import org.koin.dsl.module

val fakeNetworkModule = module {
  single<ProductRepository> {
    FakeProductRepository().apply {
      val products = listOf(
        fakeProduct(
          id = "prod-1",
          title = "WHEY PROTEIN",
          category = "Protein",
          price = 29.99,
          flavors = listOf("Chocolate", "Vanilla", "Strawberry"),
          weight = 1000,
          isPopular = true,
        ),
        fakeProduct(
          id = "prod-2",
          title = "CREATINE MONOHYDRATE",
          category = "Creatine",
          price = 19.99,
          flavors = listOf("Unflavored"),
          weight = 500,
          isNew = true,
        ),
        fakeProduct(
          id = "prod-3",
          title = "PRE-WORKOUT ENERGY",
          category = "Pre-Workout",
          price = 34.99,
          flavors = listOf("Blue Raspberry", "Fruit Punch"),
          weight = 300,
          isDiscounted = true,
        ),
        fakeProduct(
          id = "prod-4",
          title = "MASS GAINER",
          category = "Gainers",
          price = 49.99,
          flavors = listOf("Chocolate", "Vanilla"),
          weight = 2500,
        ),
        fakeProduct(
          id = "prod-5",
          title = "SHAKER BOTTLE",
          category = "Accessories",
          price = 9.99,
          flavors = null,
          weight = null,
          isPopular = true,
        ),
        fakeProduct(
          id = "prod-6",
          title = "CASEIN PROTEIN",
          category = "Protein",
          price = 39.99,
          flavors = listOf("Chocolate", "Vanilla"),
          weight = 900,
          isNew = true,
        ),
      )
      discountedProducts = kotlinx.coroutines.flow.flowOf(
        Either.Right(products.filter { it.isDiscounted }),
      )
      newProducts = kotlinx.coroutines.flow.flowOf(
        Either.Right(products.filter { it.isNew }),
      )
      productsByIdsFlow = kotlinx.coroutines.flow.flowOf(Either.Right(products))
      products.forEach { product ->
        productByIdFlows[product.id] = kotlinx.coroutines.flow.flowOf(Either.Right(product))
      }
    }
  }

  single<CustomerRepository> {
    FakeCustomerRepository().apply {
      customerFlow.value = Either.Right(
        fakeCustomer(
          id = "user-1",
          firstName = "John",
          lastName = "Doe",
          email = "john@example.com",
          cart = listOf(
            fakeCartItem(id = "cart-1", productId = "prod-1", flavor = "Chocolate", quantity = 2),
            fakeCartItem(id = "cart-2", productId = "prod-3", flavor = "Blue Raspberry", quantity = 1),
          ),
        ),
      )
    }
  }

  single<OrderRepository> { FakeOrderRepository() }
  single<AdminRepository> { FakeAdminRepository() }
}
