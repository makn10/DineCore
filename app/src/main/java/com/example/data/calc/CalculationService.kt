package com.example.data.calc

import com.example.data.model.BazarEntry
import com.example.data.model.BillSplitMode
import com.example.data.model.Deposit
import com.example.data.model.ExtraCost
import com.example.data.model.MealRecord
import com.example.data.model.Member
import com.example.data.model.MonthlyBill
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.round

data class MemberCalculation(
    val member: Member,
    val openingBalancePaisa: Long,
    val totalMeals: Double,
    val mealCostPaisa: Long,
    val totalDepositsPaisa: Long,
    val billsSharePaisa: Long,
    val extraCostsSharePaisa: Long,
    val totalCostPaisa: Long,
    val currentBalancePaisa: Long,
    val isDue: Boolean
)

data class PeriodCalculationSummary(
    val totalMeals: Double,
    val totalBazarCostPaisa: Long,
    val mealRatePaisa: Double, // in paisa per meal, e.g. 4550.25 paisa = 45.50 Taka
    val totalDepositsPaisa: Long,
    val totalBillsPaisa: Long,
    val totalExtraCostsPaisa: Long,
    val messBalancePaisa: Long,
    val memberCalculations: List<MemberCalculation>
)

object CalculationService {
    private val currencyFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    private val mealCountFormat = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

    fun formatTaka(paisa: Long, withSymbol: Boolean = true): String {
        val amount = paisa / 100.0
        val formatted = currencyFormat.format(amount)
        return if (withSymbol) "৳ $formatted" else formatted
    }

    fun formatMealRate(ratePaisa: Double): String {
        val amount = ratePaisa / 100.0
        return "৳ ${currencyFormat.format(amount)}"
    }

    fun formatMeals(meals: Double): String {
        return mealCountFormat.format(meals)
    }

    fun calculate(
        members: List<Member>,
        meals: List<MealRecord>,
        bazarEntries: List<BazarEntry>,
        deposits: List<Deposit>,
        bills: List<MonthlyBill>,
        extraCosts: List<ExtraCost>
    ): PeriodCalculationSummary {
        val activeMembers = members.filter { it.isActive }
        val memberMap = members.associateBy { it.id }

        // 1. Total meals across period
        val totalMeals = meals.sumOf { it.totalMeals }

        // 2. Total bazar cost
        val totalBazarCostPaisa = bazarEntries.sumOf { it.totalCostPaisa }

        // 3. Meal rate (paisa per meal)
        val mealRatePaisa = if (totalMeals > 0) {
            totalBazarCostPaisa.toDouble() / totalMeals
        } else {
            0.0
        }

        // 4. Total deposits
        val totalDepositsPaisa = deposits.sumOf { it.amountPaisa }

        // 5. Total bills
        val totalBillsPaisa = bills.sumOf { it.amountPaisa }

        // 6. Total extra costs
        val totalExtraCostsPaisa = extraCosts.sumOf { it.amountPaisa }

        // 7. Mess balance = Total deposits - Total bazar cost - Total bills
        val messBalancePaisa = totalDepositsPaisa - totalBazarCostPaisa - totalBillsPaisa

        // Map member meals
        val mealsByMember = meals.groupBy { it.memberId }
        val depositsByMember = deposits.groupBy { it.memberId }

        // Calculate bills share per member
        val billsShareMap = mutableMapOf<String, Long>()
        for (bill in bills) {
            val includedMembers = when (bill.splitMode) {
                BillSplitMode.SELECTED -> bill.includedMemberIds.ifEmpty { activeMembers.map { it.id } }
                BillSplitMode.EQUAL, BillSplitMode.PER_PERSON -> activeMembers.map { it.id }
            }
            if (includedMembers.isNotEmpty()) {
                val perPersonPaisa = bill.amountPaisa / includedMembers.size
                val remainder = bill.amountPaisa % includedMembers.size
                includedMembers.forEachIndexed { index, mId ->
                    val extra = if (index < remainder) 1L else 0L
                    billsShareMap[mId] = (billsShareMap[mId] ?: 0L) + perPersonPaisa + extra
                }
            }
        }

        // Calculate extra costs per member
        val extraCostShareMap = mutableMapOf<String, Long>()
        for (cost in extraCosts) {
            val participants = cost.participantMemberIds.ifEmpty { activeMembers.map { it.id } }
            if (participants.isNotEmpty()) {
                val perPerson = cost.amountPaisa / participants.size
                val remainder = cost.amountPaisa % participants.size
                participants.forEachIndexed { index, mId ->
                    val extra = if (index < remainder) 1L else 0L
                    extraCostShareMap[mId] = (extraCostShareMap[mId] ?: 0L) + perPerson + extra
                }
            }
        }

        // Member calculations
        val memberCalculations = members.map { member ->
            val mMeals = mealsByMember[member.id]?.sumOf { it.totalMeals } ?: 0.0
            val mMealCostPaisa = round(mMeals * mealRatePaisa).toLong()
            val mDepositsPaisa = depositsByMember[member.id]?.sumOf { it.amountPaisa } ?: 0L
            val mBillSharePaisa = billsShareMap[member.id] ?: 0L
            val mExtraCostPaisa = extraCostShareMap[member.id] ?: 0L

            val mTotalCostPaisa = mMealCostPaisa + mBillSharePaisa + mExtraCostPaisa
            val mCurrentBalancePaisa = member.openingBalancePaisa + mDepositsPaisa - mTotalCostPaisa

            MemberCalculation(
                member = member,
                openingBalancePaisa = member.openingBalancePaisa,
                totalMeals = mMeals,
                mealCostPaisa = mMealCostPaisa,
                totalDepositsPaisa = mDepositsPaisa,
                billsSharePaisa = mBillSharePaisa,
                extraCostsSharePaisa = mExtraCostPaisa,
                totalCostPaisa = mTotalCostPaisa,
                currentBalancePaisa = mCurrentBalancePaisa,
                isDue = mCurrentBalancePaisa < 0
            )
        }

        return PeriodCalculationSummary(
            totalMeals = totalMeals,
            totalBazarCostPaisa = totalBazarCostPaisa,
            mealRatePaisa = mealRatePaisa,
            totalDepositsPaisa = totalDepositsPaisa,
            totalBillsPaisa = totalBillsPaisa,
            totalExtraCostsPaisa = totalExtraCostsPaisa,
            messBalancePaisa = messBalancePaisa,
            memberCalculations = memberCalculations
        )
    }
}
