package com.nutrisport.shared.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProductCategoryTest {

    @Test
    fun `valueOfProductCategory should parse protein`() {
        assertThat("protein".valueOfProductCategory()).isEqualTo(ProductCategory.Protein)
    }

    @Test
    fun `valueOfProductCategory should parse creatine case-insensitive`() {
        assertThat("CREATINE".valueOfProductCategory()).isEqualTo(ProductCategory.Creatine)
    }

    @Test
    fun `valueOfProductCategory should parse preworkout`() {
        assertThat("Pre-Workout".valueOfProductCategory()).isEqualTo(ProductCategory.PreWorkout)
    }

    @Test
    fun `valueOfProductCategory should parse gainers`() {
        assertThat("Gainers".valueOfProductCategory()).isEqualTo(ProductCategory.Gainers)
    }

    @Test
    fun `valueOfProductCategory should parse accessories`() {
        assertThat("accessories".valueOfProductCategory()).isEqualTo(ProductCategory.Accessories)
    }

    @Test
    fun `valueOfProductCategory should throw for invalid category`() {
        assertFailsWith<IllegalArgumentException> {
            "unknown".valueOfProductCategory()
        }
    }

    @Test
    fun `valueOfProductCategory should filter non-letter chars`() {
        assertThat("Pre-Workout!!!".valueOfProductCategory()).isEqualTo(ProductCategory.PreWorkout)
    }
}
