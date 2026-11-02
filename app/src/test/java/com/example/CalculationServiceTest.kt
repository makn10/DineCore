package com.example

import com.example.data.calc.CalculationService
import com.example.data.model.BazarEntry
import com.example.data.model.BazarItem
import com.example.data.model.BillSplitMode
import com.example.data.model.Deposit
import com.example.data.model.ExtraCost
import com.example.data.model.MealRecord
import com.example.data.model.Member
import com.example.data.model.MemberRole
import com.example.data.model.MonthlyBill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculationServiceTest {

    @Test
    fun testPaisaFormatting() {
        val formatted = CalculationService.formatTaka(125050L) // 1250.50 Taka
        assertEquals("৳ 1,250.50", formatted)

        val formattedZero = CalculationService.formatTaka(0L)
        assertEquals("৳ 0.00", formattedZero)
    }

    @Test
    fun testExactMealRateAndMemberCostCalculation() {
        val member1 = Member(id = "m1", name = "Kuddus", role = MemberRole.MANAGER, openingBalancePaisa = 10000L) // 100 ৳ opening
        val member2 = Member(id = "m2", name = "Rahim", role = MemberRole.MEMBER, openingBalancePaisa = 0L)

        val meals = listOf(
            MealRecord(id = "meal1", memberId = "m1", breakfast = 1.0, lunch = 1.0, dinner = 1.0, totalMeals = 3.0),
            MealRecord(id = "meal2", memberId = "m2", breakfast = 0.5, lunch = 1.0, dinner = 1.0, totalMeals = 2.5)
        ) // total meals = 5.5

        val bazar = listOf(
            BazarEntry(
                id = "b1",
                totalCostPaisa = 27500L, // 275.00 ৳ -> Rate = 27500 / 5.5 = 5000 paisa (50.00 ৳ / meal)
                items = listOf(BazarItem(name = "Fish & Veg", quantity = "1kg", costPaisa = 27500L))
            )
        )

        val deposits = listOf(
            Deposit(id = "d1", memberId = "m1", amountPaisa = 20000L), // 200 ৳ deposit
            Deposit(id = "d2", memberId = "m2", amountPaisa = 15000L)  // 150 ৳ deposit
        )

        val bills = listOf(
            MonthlyBill(id = "bill1", name = "Gas", amountPaisa = 1001L, splitMode = BillSplitMode.EQUAL) // 1001 paisa between 2 members: 501 and 500
        )

        val extraCosts = listOf(
            ExtraCost(id = "e1", title = "Chicken Feast", amountPaisa = 6000L, participantMemberIds = listOf("m1", "m2")) // 3000 each
        )

        val summary = CalculationService.calculate(
            members = listOf(member1, member2),
            meals = meals,
            bazarEntries = bazar,
            deposits = deposits,
            bills = bills,
            extraCosts = extraCosts
        )

        assertEquals(5.5, summary.totalMeals, 0.001)
        assertEquals(27500L, summary.totalBazarCostPaisa)
        assertEquals(5000.0, summary.mealRatePaisa, 0.001) // 50.00 Taka per meal
        assertEquals(35000L, summary.totalDepositsPaisa)
        assertEquals(1001L, summary.totalBillsPaisa)
        assertEquals(6000L, summary.totalExtraCostsPaisa)

        // Mess balance = deposits (35000) - bazar (27500) - bills (1001) = 6499 paisa
        assertEquals(6499L, summary.messBalancePaisa)

        val m1Calc = summary.memberCalculations.first { it.member.id == "m1" }
        // m1 meal cost: 3.0 meals * 5000 = 15000 paisa
        assertEquals(15000L, m1Calc.mealCostPaisa)
        assertEquals(3000L, m1Calc.extraCostsSharePaisa)
        // total cost = 15000 + 501 (or 500) + 3000 = 18501 paisa
        // net balance = 10000 (opening) + 20000 (deposit) - 18501 = 11499 paisa
        assertTrue(m1Calc.currentBalancePaisa > 0)
    }
}
