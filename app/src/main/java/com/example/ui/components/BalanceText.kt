package com.example.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.data.calc.CalculationService
import com.example.ui.theme.AppColors

@Composable
fun BalanceText(
    amountPaisa: Long,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    showPrefix: Boolean = false,
    testTag: String = "balance_text"
) {
    val isPositive = amountPaisa >= 0
    val color = if (isPositive) AppColors.Positive else AppColors.Negative

    val absPaisa = kotlin.math.abs(amountPaisa)
    val formatted = CalculationService.formatTaka(absPaisa)
    val text = when {
        showPrefix && isPositive -> "+ $formatted"
        showPrefix && !isPositive -> "- $formatted"
        !isPositive -> "- $formatted"
        else -> formatted
    }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier.testTag(testTag)
    )
}
