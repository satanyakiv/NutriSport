package com.nutrisport.shared.domain

fun fakeProduct(
    id: String = "prod-1",
    title: String = "WHEY PROTEIN",
    description: String = "High quality protein",
    thumbnail: String = "https://example.com/img.jpg",
    category: String = "Protein",
    price: Double = 29.99,
    flavors: List<String>? = listOf("Chocolate", "Vanilla"),
    weight: Int? = 1000,
    isPopular: Boolean = false,
    isDiscounted: Boolean = false,
    isNew: Boolean = false,
) = Product(
    id = id,
    title = title,
    description = description,
    thumbnail = thumbnail,
    category = category,
    price = price,
    flavors = flavors,
    weight = weight,
    isPopular = isPopular,
    isDiscounted = isDiscounted,
    isNew = isNew,
)

fun fakeCustomer(
    id: String = "user-1",
    firstName: String = "John",
    lastName: String = "Doe",
    email: String = "john@example.com",
    cart: List<CartItem> = emptyList(),
    isAdmin: Boolean = false,
) = Customer(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    cart = cart,
    isAdmin = isAdmin,
)

fun fakeCartItem(
    id: String = "cart-1",
    productId: String = "prod-1",
    flavor: String? = "Chocolate",
    quantity: Int = 1,
) = CartItem(
    id = id,
    productId = productId,
    flavor = flavor,
    quantity = quantity,
)

fun fakeOrder(
    customerId: String = "user-1",
    items: List<CartItem> = listOf(fakeCartItem()),
    totalAmount: Double = 29.99,
    token: String? = null,
) = Order(
    customerId = customerId,
    items = items,
    totalAmount = totalAmount,
    token = token,
)
