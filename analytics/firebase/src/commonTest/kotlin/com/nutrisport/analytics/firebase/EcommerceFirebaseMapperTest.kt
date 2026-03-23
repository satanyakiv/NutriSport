package com.nutrisport.analytics.firebase

import com.nutrisport.analytics.core.AuthEvent
import com.nutrisport.analytics.core.EcommerceEvent
import com.nutrisport.analytics.firebase.mapper.EcommerceFirebaseMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EcommerceFirebaseMapperTest {

    private val mapper = EcommerceFirebaseMapper()

    @Test
    fun `should map AddToCart event`() {
        val event = EcommerceEvent.AddToCart(
            productId = "p1",
            productName = "Whey",
            price = 29.99,
            quantity = 2,
            category = "protein",
        )
        val result = mapper.map(event)

        assertNotNull(result)
        assertEquals("add_to_cart", result.eventName)
        assertEquals("p1", result.params["product_id"])
        assertEquals("Whey", result.params["product_name"])
        assertEquals(29.99, result.params["price"])
        assertEquals(2, result.params["quantity"])
        assertEquals("protein", result.params["category"])
    }

    @Test
    fun `should map Purchase event`() {
        val event = EcommerceEvent.Purchase(
            orderId = "ord-1",
            totalAmount = 59.98,
            itemCount = 3,
        )
        val result = mapper.map(event)

        assertNotNull(result)
        assertEquals("purchase", result.eventName)
        assertEquals("ord-1", result.params["order_id"])
        assertEquals(59.98, result.params["total_amount"])
        assertEquals(3, result.params["item_count"])
    }

    @Test
    fun `should map Search event`() {
        val event = EcommerceEvent.Search(query = "creatine")
        val result = mapper.map(event)

        assertNotNull(result)
        assertEquals("search", result.eventName)
        assertEquals("creatine", result.params["query"])
    }

    @Test
    fun `should return null for non-ecommerce event`() {
        val event = AuthEvent.SignIn()
        assertNull(mapper.map(event))
    }
}
