package com.example.data.model

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val phone: String = "",
    val activeMessId: String = "",
    val themeMode: String = "system", // "system", "light", "dark"
    val language: String = "en",      // "en", "bn"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Mess(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val inviteCode: String = "",
    val currentPeriodId: String = "",
    val creatorId: String = "",
    val managerIds: List<String> = emptyList(),
    val subManagerIds: List<String> = emptyList(),
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MemberRole {
    MANAGER,
    SUB_MANAGER,
    MEMBER;

    val displayName: String
        get() = when (this) {
            MANAGER -> "Manager"
            SUB_MANAGER -> "Sub-Manager"
            MEMBER -> "Member"
        }

    companion object {
        fun fromString(role: String?): MemberRole {
            return when (role?.lowercase()?.trim()) {
                "manager" -> MANAGER
                "sub-manager", "submanager" -> SUB_MANAGER
                else -> MEMBER
            }
        }
    }
}

data class Member(
    val id: String = "",
    val messId: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val isActive: Boolean = true,
    val openingBalancePaisa: Long = 0L, // 100 paisa = 1 Taka
    val joinedAt: Long = System.currentTimeMillis()
)

data class Period(
    val id: String = "", // e.g. "2026-11"
    val messId: String = "",
    val year: Int = 2026,
    val month: Int = 11,
    val name: String = "", // "November 2026"
    val isClosed: Boolean = false,
    val closedAt: Long? = null,
    val closedBy: String? = null,
    val snapshotTotalMeals: Double = 0.0,
    val snapshotMealCostPaisa: Long = 0L,
    val snapshotMealRatePaisa: Long = 0L,
    val snapshotTotalDepositsPaisa: Long = 0L,
    val snapshotTotalBillsPaisa: Long = 0L,
    val snapshotMessBalancePaisa: Long = 0L
)

data class MealRecord(
    val id: String = "",
    val messId: String = "",
    val periodId: String = "",
    val date: String = "", // "yyyy-MM-dd"
    val memberId: String = "",
    val memberName: String = "",
    val breakfast: Double = 0.0,
    val lunch: Double = 0.0,
    val dinner: Double = 0.0,
    val totalMeals: Double = 0.0,
    val updatedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class BazarItem(
    val name: String = "",
    val quantity: String = "",
    val costPaisa: Long = 0L
)

data class BazarEntry(
    val id: String = "",
    val messId: String = "",
    val periodId: String = "",
    val date: String = "", // "yyyy-MM-dd"
    val shopperMemberId: String = "",
    val shopperName: String = "",
    val items: List<BazarItem> = emptyList(),
    val totalCostPaisa: Long = 0L,
    val isAddedToDeposit: Boolean = false,
    val depositId: String? = null,
    val note: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Deposit(
    val id: String = "",
    val messId: String = "",
    val periodId: String = "",
    val date: String = "", // "yyyy-MM-dd"
    val memberId: String = "",
    val memberName: String = "",
    val amountPaisa: Long = 0L, // negative allows corrections
    val paymentMethod: String = "Cash", // Cash, bKash, Nagad, Bank
    val note: String = "",
    val isFromBazar: Boolean = false,
    val bazarId: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class BillSplitMode {
    EQUAL,
    PER_PERSON,
    SELECTED;

    companion object {
        fun fromString(mode: String?): BillSplitMode {
            return when (mode?.lowercase()) {
                "per_person" -> PER_PERSON
                "selected" -> SELECTED
                else -> EQUAL
            }
        }
    }
}

data class MonthlyBill(
    val id: String = "",
    val messId: String = "",
    val periodId: String = "",
    val name: String = "",
    val category: String = "Other", // Gas, Electricity, Maid, Internet, Other
    val amountPaisa: Long = 0L,
    val splitMode: BillSplitMode = BillSplitMode.EQUAL,
    val includedMemberIds: List<String> = emptyList(),
    val note: String = "",
    val updatedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExtraCost(
    val id: String = "",
    val messId: String = "",
    val periodId: String = "",
    val title: String = "",
    val amountPaisa: Long = 0L,
    val participantMemberIds: List<String> = emptyList(),
    val date: String = "",
    val note: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Notice(
    val id: String = "",
    val messId: String = "",
    val title: String = "",
    val content: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MenuItem(
    val id: String = "",
    val messId: String = "",
    val dayOfWeek: String = "", // "Saturday", "Sunday", etc.
    val breakfast: String = "",
    val lunch: String = "",
    val dinner: String = "",
    val updatedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class RoutineDay(
    val id: String = "",
    val messId: String = "",
    val dayOfWeek: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val assignedBy: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class AuditLog(
    val id: String = "",
    val messId: String = "",
    val action: String = "", // "CREATE", "UPDATE", "DELETE", "CLOSE_MONTH"
    val entityType: String = "", // "MEAL", "BAZAR", "DEPOSIT", "BILL", "MEMBER"
    val entityId: String = "",
    val details: String = "",
    val performedBy: String = "",
    val performedByName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
