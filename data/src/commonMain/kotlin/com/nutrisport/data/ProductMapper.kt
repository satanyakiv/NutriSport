package com.nutrisport.data

import com.nutrisport.shared.domain.Product
import dev.gitlive.firebase.firestore.DocumentSnapshot

class ProductMapper {
  fun map(document: DocumentSnapshot): Product = Product(
    id = document.id,
    createdAt = document.get("createdAt"),
    title = (document.get("title") as String).uppercase(),
    description = document.get("description"),
    thumbnail = document.get("thumbnail"),
    category = document.get("category"),
    flavors = document.get("flavors"),
    weight = document.get("weight"),
    price = document.get("price"),
    isPopular = document.get("isPopular"),
    isDiscounted = document.get("isDiscounted"),
    isNew = document.get("isNew"),
  )
}
