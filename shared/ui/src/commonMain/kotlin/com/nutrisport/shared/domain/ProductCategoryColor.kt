package com.nutrisport.shared.domain

import androidx.compose.ui.graphics.Color
import com.nutrisport.shared.CategoryBlue
import com.nutrisport.shared.CategoryGreen
import com.nutrisport.shared.CategoryPurple
import com.nutrisport.shared.CategoryRed
import com.nutrisport.shared.CategoryYellow

val ProductCategory.color: Color
    get() = when (this) {
        ProductCategory.Protein -> CategoryYellow
        ProductCategory.Creatine -> CategoryBlue
        ProductCategory.PreWorkout -> CategoryGreen
        ProductCategory.Gainers -> CategoryPurple
        ProductCategory.Accessories -> CategoryRed
    }
