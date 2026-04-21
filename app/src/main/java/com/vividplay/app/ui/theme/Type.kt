package com.vividplay.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val brand = FontFamily.SansSerif

val VividTypography = Typography(
    displayLarge  = TextStyle(brand, FontWeight.SemiBold, 48.sp, lineHeight = 56.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(brand, FontWeight.SemiBold, 36.sp, lineHeight = 44.sp, letterSpacing = (-0.3).sp),
    headlineLarge = TextStyle(brand, FontWeight.SemiBold, 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(brand, FontWeight.Medium,   22.sp, lineHeight = 28.sp),
    titleLarge    = TextStyle(brand, FontWeight.Medium,   20.sp, lineHeight = 26.sp),
    titleMedium   = TextStyle(brand, FontWeight.Medium,   16.sp, lineHeight = 22.sp),
    bodyLarge     = TextStyle(brand, FontWeight.Normal,   16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(brand, FontWeight.Normal,   14.sp, lineHeight = 20.sp),
    labelLarge    = TextStyle(brand, FontWeight.Medium,   14.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    labelMedium   = TextStyle(brand, FontWeight.Medium,   12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
)
