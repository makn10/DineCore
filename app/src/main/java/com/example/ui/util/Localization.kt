package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return if (code.startsWith("bn", ignoreCase = true)) BANGLA else ENGLISH
        }
    }
}

class AppStrings(val language: AppLanguage) {
    val isBangla = language == AppLanguage.BANGLA

    val appName get() = if (isBangla) "ডাইন কোর" else "Dine Core"
    val appSubtitle get() = if (isBangla) "মেস ডাইনিং ও মিল হিসাব ব্যবস্থাপনা" else "Hostel & Dining Meal Manager"

    // Nav
    val navCharts get() = if (isBangla) "চার্ট" else "Charts"
    val navHistory get() = if (isBangla) "ইতিহাস" else "History"
    val navHome get() = if (isBangla) "হোম" else "Home"
    val navSummary get() = if (isBangla) "সামারি" else "Summary"
    val navProfile get() = if (isBangla) "প্রোফাইল" else "Profile"

    // Home
    val messBalance get() = if (isBangla) "মেস ব্যালেন্স" else "Mess Balance"
    val mealRate get() = if (isBangla) "মিল রেট" else "Meal Rate"
    val todayMeals get() = if (isBangla) "আজকের মিল" else "Today's Meals"
    val breakfast get() = if (isBangla) "সকাল" else "Breakfast"
    val lunch get() = if (isBangla) "দুপুর" else "Lunch"
    val dinner get() = if (isBangla) "রাত" else "Dinner"
    val mySummary get() = if (isBangla) "আমার হিসাব" else "My Summary"
    val totalMeals get() = if (isBangla) "মোট মিল" else "Total Meals"
    val mealCost get() = if (isBangla) "মিল খরচ" else "Meal Cost"
    val myDeposits get() = if (isBangla) "জমা" else "Deposits"
    val currentBalance get() = if (isBangla) "বর্তমান ব্যালেন্স" else "Current Balance"
    val due get() = if (isBangla) "বকেয়া" else "Due"
    val advance get() = if (isBangla) "উদ্বৃত্ত / অগ্রিম" else "Remaining"
    val myPersonalExpenses get() = if (isBangla) "ব্যক্তিগত অন্যান্য খরচ" else "My Personal Expenses"
    val noPersonalExpenses get() = if (isBangla) "কোন ব্যক্তিগত খরচ নেই" else "No personal expenses this month"

    // Quick Actions
    val quickActions get() = if (isBangla) "কুইক অ্যাকশন" else "Quick Actions"
    val addMeal get() = if (isBangla) "মিল এন্ট্রি" else "Add Meal"
    val addBazar get() = if (isBangla) "বাজার যোগ করুন" else "Add Bazar"
    val addDeposit get() = if (isBangla) "টাকা জমা" else "Add Deposit"
    val allMembers get() = if (isBangla) "সব সদস্য" else "All Members"
    val mealSheet get() = if (isBangla) "মিল শিট" else "Meal Sheet"
    val monthlyBills get() = if (isBangla) "মাসিক বিল ও খরচ" else "Monthly Bills"
    val noticeBoard get() = if (isBangla) "নোটিশ বোর্ড" else "Notice Board"
    val weeklyMenu get() = if (isBangla) "খাবারের মেনু" else "Weekly Menu"
    val bazarRoutine get() = if (isBangla) "বাজারের রুটিন" else "Bazar Routine"

    // Bazar
    val bazarList get() = if (isBangla) "বাজারের তালিকা" else "Bazar List"
    val totalBazar get() = if (isBangla) "মোট বাজার খরচ" else "Total Bazar Cost"
    val shopper get() = if (isBangla) "বাজারকারী" else "Shopper"
    val selectShopper get() = if (isBangla) "বাজারকারী নির্বাচন করুন" else "Select Shopper"
    val itemName get() = if (isBangla) "আইটেমের নাম" else "Item Name"
    val quantity get() = if (isBangla) "পরিমাণ" else "Quantity"
    val price get() = if (isBangla) "দাম (৳)" else "Price (৳)"
    val addItem get() = if (isBangla) "+ আইটেম যোগ করুন" else "+ Add Item"
    val addToDeposit get() = if (isBangla) "জমার সাথে যোগ করুন" else "Add to Member's Deposit"
    val addToDepositHint get() = if (isBangla) "বাজারকারীর জমা হিসেবে স্বয়ংক্রিয়ভাবে রেকর্ড হবে" else "Automatically recorded as shopper's deposit"

    // Deposit
    val depositList get() = if (isBangla) "জমার তালিকা" else "Deposit List"
    val amount get() = if (isBangla) "পরিমাণ (৳)" else "Amount (৳)"
    val paymentMethod get() = if (isBangla) "পেমেন্ট মাধ্যম" else "Payment Method"
    val noteOptional get() = if (isBangla) "নোট (ঐচ্ছিক / সংশোধনের কারণ)" else "Note (Optional / Reason)"

    // Month close
    val startNewMonth get() = if (isBangla) "নতুন মাস শুরু করুন" else "Start New Month"
    val carryOverBalances get() = if (isBangla) "বর্তমান ব্যালেন্স পরবর্তী মাসে স্থানান্তর করুন" else "Carry Over Balances to Next Month"
    val closeMonthConfirm get() = if (isBangla) "চলতি মাসের হিসাব ক্লোজ করে নতুন মাস খুলতে চান?" else "Freeze current month calculations and open next period?"

    // Common
    val save get() = if (isBangla) "সংরক্ষণ করুন" else "Save"
    val cancel get() = if (isBangla) "বাতিল" else "Cancel"
    val delete get() = if (isBangla) "মুছে ফেলুন" else "Delete"
    val confirm get() = if (isBangla) "নিশ্চিত করুন" else "Confirm"
    val search get() = if (isBangla) "খুঁজুন..." else "Search..."
    val edit get() = if (isBangla) "সম্পাদনা" else "Edit"
    val share get() = if (isBangla) "শেয়ার করুন" else "Share"
    val download get() = if (isBangla) "ডাউনলোড" else "Download"
    val noData get() = if (isBangla) "কোন তথ্য পাওয়া যায়নি" else "No records found"
}

val LocalAppStrings = compositionLocalOf { AppStrings(AppLanguage.ENGLISH) }

@Composable
fun currentStrings(): AppStrings = LocalAppStrings.current
