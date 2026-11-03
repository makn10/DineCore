package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.calc.PeriodCalculationSummary
import com.example.data.model.AuditLog
import com.example.data.model.BazarEntry
import com.example.data.model.BazarItem
import com.example.data.model.BillSplitMode
import com.example.data.model.Deposit
import com.example.data.model.ExtraCost
import com.example.data.model.MealRecord
import com.example.data.model.Member
import com.example.data.model.MemberRole
import com.example.data.model.MenuItem
import com.example.data.model.Mess
import com.example.data.model.MonthlyBill
import com.example.data.model.Notice
import com.example.data.model.Period
import com.example.data.model.RoutineDay
import com.example.data.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class FirestoreRepository(private val context: Context) {

    val auth: FirebaseAuth by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            android.util.Log.e("FirestoreRepository", "FirebaseAuth initialization error: ${e.message}", e)
            FirebaseAuth.getInstance()
        }
    }

    private val firestore: FirebaseFirestore by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
            val dbId = if (resId != 0) context.getString(resId) else ""
            if (dbId.isNotEmpty() && dbId != "default") {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Throwable) {
            android.util.Log.e("FirestoreRepository", "FirebaseFirestore initialization error: ${e.message}", e)
            FirebaseFirestore.getInstance()
        }
    }

    val currentUser: FirebaseUser?
        get() = try { auth.currentUser } catch (e: Throwable) { null }

    // ---------------- AUTH & USER ----------------

    suspend fun signInWithEmail(email: String, pass: String): FirebaseUser {
        val res = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
        return res.user ?: throw IllegalStateException("User not found after sign in")
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String, phone: String): FirebaseUser {
        val res = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
        val user = res.user ?: throw IllegalStateException("User creation failed")
        val profile = UserProfile(
            id = user.uid,
            email = email.trim(),
            displayName = name.trim().ifEmpty { email.substringBefore("@") },
            phone = phone.trim(),
            activeMessId = "",
            themeMode = "system",
            language = "en"
        )
        try {
            saveUserProfile(profile)
        } catch (e: Exception) {
            // Local fallback if initial network write is deferred
        }
        return user
    }

    fun userProfileFlow(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = UserProfile(
                        id = snapshot.getString("id") ?: userId,
                        email = snapshot.getString("email") ?: "",
                        displayName = snapshot.getString("displayName") ?: "",
                        phone = snapshot.getString("phone") ?: "",
                        activeMessId = snapshot.getString("activeMessId") ?: "",
                        themeMode = snapshot.getString("themeMode") ?: "system",
                        language = snapshot.getString("language") ?: "en"
                    )
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val data = hashMapOf(
            "id" to profile.id,
            "email" to profile.email,
            "displayName" to profile.displayName,
            "phone" to profile.phone,
            "activeMessId" to profile.activeMessId,
            "themeMode" to profile.themeMode,
            "language" to profile.language,
            "updatedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(profile.id).set(data).await()
    }

    suspend fun ensureUserProfile(userId: String, email: String, displayName: String, phone: String): UserProfile {
        val docRef = firestore.collection("users").document(userId)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return UserProfile(
                id = snapshot.getString("id") ?: userId,
                email = snapshot.getString("email") ?: email,
                displayName = snapshot.getString("displayName") ?: displayName,
                phone = snapshot.getString("phone") ?: phone,
                activeMessId = snapshot.getString("activeMessId") ?: "",
                themeMode = snapshot.getString("themeMode") ?: "system",
                language = snapshot.getString("language") ?: "en"
            )
        } else {
            val fallbackName = if (displayName.isNotBlank()) displayName else (if (email.contains("@")) email.substringBefore("@") else "Mess Member")
            val newProfile = UserProfile(
                id = userId,
                email = email,
                displayName = fallbackName,
                phone = phone,
                activeMessId = "",
                themeMode = "system",
                language = "en"
            )
            saveUserProfile(newProfile)
            return newProfile
        }
    }

    suspend fun updateThemeAndLanguage(userId: String, themeMode: String, language: String) {
        firestore.collection("users").document(userId).update(
            mapOf("themeMode" to themeMode, "language" to language)
        ).await()
    }

    // ---------------- MESS ----------------

    fun messFlow(messId: String): Flow<Mess?> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                @Suppress("UNCHECKED_CAST")
                val mess = Mess(
                    id = snapshot.getString("id") ?: messId,
                    name = snapshot.getString("name") ?: "",
                    address = snapshot.getString("address") ?: "",
                    inviteCode = snapshot.getString("inviteCode") ?: "",
                    currentPeriodId = snapshot.getString("currentPeriodId") ?: "",
                    creatorId = snapshot.getString("creatorId") ?: "",
                    managerIds = (snapshot.get("managerIds") as? List<String>) ?: emptyList(),
                    subManagerIds = (snapshot.get("subManagerIds") as? List<String>) ?: emptyList(),
                    memberIds = (snapshot.get("memberIds") as? List<String>) ?: emptyList()
                )
                trySend(mess)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createMess(name: String, address: String, creatorName: String, phone: String): Mess {
        val uid = auth.currentUser?.uid ?: "user_${System.currentTimeMillis()}"
        val messId = UUID.randomUUID().toString()
        val inviteCode = "MESS" + (1000..9999).random()

        val cal = Calendar.getInstance()
        val periodFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val periodNameFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val periodId = periodFormat.format(cal.time)
        val periodName = periodNameFormat.format(cal.time)

        val mess = Mess(
            id = messId,
            name = name,
            address = address,
            inviteCode = inviteCode,
            currentPeriodId = periodId,
            creatorId = uid,
            managerIds = listOf(uid),
            subManagerIds = emptyList(),
            memberIds = listOf(uid)
        )

        try {
            val batch = firestore.batch()

            // 1. Mess document
            val messRef = firestore.collection("messes").document(messId)
            batch.set(messRef, mapOf(
                "id" to mess.id,
                "name" to mess.name,
                "address" to mess.address,
                "inviteCode" to mess.inviteCode,
                "currentPeriodId" to mess.currentPeriodId,
                "creatorId" to mess.creatorId,
                "managerIds" to mess.managerIds,
                "subManagerIds" to mess.subManagerIds,
                "memberIds" to mess.memberIds,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ))

            // 2. Creator Member record (Manager)
            val memberRef = messRef.collection("members").document(uid)
            batch.set(memberRef, mapOf(
                "id" to uid,
                "messId" to messId,
                "userId" to uid,
                "name" to creatorName,
                "phone" to phone,
                "role" to MemberRole.MANAGER.name,
                "isActive" to true,
                "openingBalancePaisa" to 0L,
                "joinedAt" to System.currentTimeMillis()
            ))

            // 3. Initial Period document
            val periodRef = messRef.collection("periods").document(periodId)
            batch.set(periodRef, mapOf(
                "id" to periodId,
                "messId" to messId,
                "year" to cal.get(Calendar.YEAR),
                "month" to cal.get(Calendar.MONTH) + 1,
                "name" to periodName,
                "isClosed" to false,
                "snapshotTotalMeals" to 0.0,
                "snapshotMealCostPaisa" to 0L,
                "snapshotMealRatePaisa" to 0L,
                "snapshotTotalDepositsPaisa" to 0L,
                "snapshotTotalBillsPaisa" to 0L,
                "snapshotMessBalancePaisa" to 0L
            ))

            // 4. Initial Audit Log
            val auditRef = messRef.collection("audit").document(UUID.randomUUID().toString())
            batch.set(auditRef, mapOf(
                "id" to auditRef.id,
                "messId" to messId,
                "action" to "CREATE_MESS",
                "performerName" to creatorName,
                "details" to "Mess '$name' created by manager $creatorName",
                "timestamp" to System.currentTimeMillis()
            ))

            // 5. Pre-seed 7-Day Routine structure
            val days = listOf("Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
            days.forEach { dayName ->
                val routineDoc = messRef.collection("routine").document(dayName)
                batch.set(routineDoc, mapOf(
                    "id" to dayName,
                    "messId" to messId,
                    "dayOfWeek" to dayName,
                    "memberId" to uid,
                    "memberName" to creatorName,
                    "assignedBy" to creatorName,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }

            // 6. Pre-seed 7-Day Weekly Menu structure
            days.forEach { dayName ->
                val menuDoc = messRef.collection("menu").document(dayName)
                batch.set(menuDoc, mapOf(
                    "id" to dayName,
                    "messId" to messId,
                    "dayOfWeek" to dayName,
                    "breakfast" to "Roti / Paratha & Egg",
                    "lunch" to "Rice, Dal, Fish/Chicken",
                    "dinner" to "Rice, Dal & Vegetables",
                    "updatedBy" to creatorName,
                    "updatedAt" to System.currentTimeMillis()
                ))
            }

            // 7. Update user active mess and profile
            val userRef = firestore.collection("users").document(uid)
            batch.set(userRef, mapOf(
                "id" to uid,
                "displayName" to creatorName,
                "phone" to phone,
                "activeMessId" to messId,
                "updatedAt" to System.currentTimeMillis()
            ), SetOptions.merge())

            kotlinx.coroutines.withTimeoutOrNull(4000L) {
                batch.commit().await()
            }
        } catch (e: Exception) {
            android.util.Log.w("FirestoreRepository", "createMess network notice: ${e.message}", e)
        }
        return mess
    }

    suspend fun joinMessByInviteCode(inviteCode: String, memberName: String, phone: String): Mess {
        val uid = auth.currentUser?.uid ?: "user_${System.currentTimeMillis()}"
        val snapshot = firestore.collection("messes")
            .whereEqualTo("inviteCode", inviteCode.trim().uppercase())
            .get().await()

        if (snapshot.isEmpty) {
            throw IllegalArgumentException("Invalid invite code. Please check and try again.")
        }

        val doc = snapshot.documents.first()
        val messId = doc.id
        @Suppress("UNCHECKED_CAST")
        val currentMemberIds = (doc.get("memberIds") as? List<String>)?.toMutableList() ?: mutableListOf()
        if (!currentMemberIds.contains(uid)) {
            currentMemberIds.add(uid)
        }

        val batch = firestore.batch()
        batch.update(doc.reference, mapOf("memberIds" to currentMemberIds))

        val memberRef = doc.reference.collection("members").document(uid)
        batch.set(memberRef, mapOf(
            "id" to uid,
            "messId" to messId,
            "userId" to uid,
            "name" to memberName,
            "phone" to phone,
            "role" to MemberRole.MEMBER.name,
            "isActive" to true,
            "openingBalancePaisa" to 0L,
            "joinedAt" to System.currentTimeMillis()
        ))

        // Audit log
        val auditRef = doc.reference.collection("audit").document(UUID.randomUUID().toString())
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "MEMBER_JOIN",
            "performerName" to memberName,
            "details" to "$memberName joined the mess via invite code",
            "timestamp" to System.currentTimeMillis()
        ))

        batch.set(firestore.collection("users").document(uid), mapOf(
            "id" to uid,
            "displayName" to memberName,
            "phone" to phone,
            "activeMessId" to messId,
            "updatedAt" to System.currentTimeMillis()
        ), SetOptions.merge())

        batch.commit().await()

        return Mess(
            id = messId,
            name = doc.getString("name") ?: "",
            inviteCode = inviteCode,
            currentPeriodId = doc.getString("currentPeriodId") ?: ""
        )
    }

    // ---------------- SUBCOLLECTIONS ----------------

    fun membersFlow(messId: String): Flow<List<Member>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { d ->
                    Member(
                        id = d.id,
                        messId = messId,
                        userId = d.getString("userId") ?: d.id,
                        name = d.getString("name") ?: "Member",
                        phone = d.getString("phone") ?: "",
                        role = MemberRole.fromString(d.getString("role")),
                        isActive = d.getBoolean("isActive") ?: true,
                        openingBalancePaisa = d.getLong("openingBalancePaisa") ?: 0L,
                        joinedAt = d.getLong("joinedAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun periodsFlow(messId: String): Flow<List<Period>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("periods")
            .orderBy("id", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    Period(
                        id = d.id,
                        messId = messId,
                        year = (d.getLong("year") ?: 2026).toInt(),
                        month = (d.getLong("month") ?: 1).toInt(),
                        name = d.getString("name") ?: d.id,
                        isClosed = d.getBoolean("isClosed") ?: false,
                        closedAt = d.getLong("closedAt"),
                        closedBy = d.getString("closedBy"),
                        snapshotTotalMeals = d.getDouble("snapshotTotalMeals") ?: 0.0,
                        snapshotMealCostPaisa = d.getLong("snapshotMealCostPaisa") ?: 0L,
                        snapshotMealRatePaisa = d.getLong("snapshotMealRatePaisa") ?: 0L,
                        snapshotTotalDepositsPaisa = d.getLong("snapshotTotalDepositsPaisa") ?: 0L,
                        snapshotTotalBillsPaisa = d.getLong("snapshotTotalBillsPaisa") ?: 0L,
                        snapshotMessBalancePaisa = d.getLong("snapshotMessBalancePaisa") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun mealsFlow(messId: String, periodId: String): Flow<List<MealRecord>> = callbackFlow {
        if (messId.isEmpty() || periodId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("meals")
            .whereEqualTo("periodId", periodId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    MealRecord(
                        id = d.id,
                        messId = messId,
                        periodId = periodId,
                        date = d.getString("date") ?: "",
                        memberId = d.getString("memberId") ?: "",
                        memberName = d.getString("memberName") ?: "",
                        breakfast = d.getDouble("breakfast") ?: 0.0,
                        lunch = d.getDouble("lunch") ?: 0.0,
                        dinner = d.getDouble("dinner") ?: 0.0,
                        totalMeals = d.getDouble("totalMeals") ?: 0.0,
                        updatedBy = d.getString("updatedBy") ?: "",
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun bazarFlow(messId: String, periodId: String): Flow<List<BazarEntry>> = callbackFlow {
        if (messId.isEmpty() || periodId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("bazar")
            .whereEqualTo("periodId", periodId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    @Suppress("UNCHECKED_CAST")
                    val rawItems = (d.get("items") as? List<Map<String, Any>>) ?: emptyList()
                    val items = rawItems.map {
                        BazarItem(
                            name = it["name"]?.toString() ?: "",
                            quantity = it["quantity"]?.toString() ?: "",
                            costPaisa = (it["costPaisa"] as? Number)?.toLong() ?: 0L
                        )
                    }
                    BazarEntry(
                        id = d.id,
                        messId = messId,
                        periodId = periodId,
                        date = d.getString("date") ?: "",
                        shopperMemberId = d.getString("shopperMemberId") ?: "",
                        shopperName = d.getString("shopperName") ?: "",
                        items = items,
                        totalCostPaisa = d.getLong("totalCostPaisa") ?: 0L,
                        isAddedToDeposit = d.getBoolean("isAddedToDeposit") ?: false,
                        depositId = d.getString("depositId"),
                        note = d.getString("note") ?: "",
                        createdBy = d.getString("createdBy") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }.sortedByDescending { it.date }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun depositsFlow(messId: String, periodId: String): Flow<List<Deposit>> = callbackFlow {
        if (messId.isEmpty() || periodId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("deposits")
            .whereEqualTo("periodId", periodId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    Deposit(
                        id = d.id,
                        messId = messId,
                        periodId = periodId,
                        date = d.getString("date") ?: "",
                        memberId = d.getString("memberId") ?: "",
                        memberName = d.getString("memberName") ?: "",
                        amountPaisa = d.getLong("amountPaisa") ?: 0L,
                        paymentMethod = d.getString("paymentMethod") ?: "Cash",
                        note = d.getString("note") ?: "",
                        isFromBazar = d.getBoolean("isFromBazar") ?: false,
                        bazarId = d.getString("bazarId"),
                        createdBy = d.getString("createdBy") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }.sortedByDescending { it.date }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun billsFlow(messId: String, periodId: String): Flow<List<MonthlyBill>> = callbackFlow {
        if (messId.isEmpty() || periodId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("bills")
            .whereEqualTo("periodId", periodId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    @Suppress("UNCHECKED_CAST")
                    MonthlyBill(
                        id = d.id,
                        messId = messId,
                        periodId = periodId,
                        name = d.getString("name") ?: "",
                        category = d.getString("category") ?: "Other",
                        amountPaisa = d.getLong("amountPaisa") ?: 0L,
                        splitMode = BillSplitMode.fromString(d.getString("splitMode")),
                        includedMemberIds = (d.get("includedMemberIds") as? List<String>) ?: emptyList(),
                        note = d.getString("note") ?: "",
                        updatedBy = d.getString("updatedBy") ?: "",
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun extraCostsFlow(messId: String, periodId: String): Flow<List<ExtraCost>> = callbackFlow {
        if (messId.isEmpty() || periodId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("extraCosts")
            .whereEqualTo("periodId", periodId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    @Suppress("UNCHECKED_CAST")
                    ExtraCost(
                        id = d.id,
                        messId = messId,
                        periodId = periodId,
                        title = d.getString("title") ?: "",
                        amountPaisa = d.getLong("amountPaisa") ?: 0L,
                        participantMemberIds = (d.get("participantMemberIds") as? List<String>) ?: emptyList(),
                        date = d.getString("date") ?: "",
                        note = d.getString("note") ?: "",
                        createdBy = d.getString("createdBy") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun noticesFlow(messId: String): Flow<List<Notice>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("notices")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    Notice(
                        id = d.id,
                        messId = messId,
                        title = d.getString("title") ?: "",
                        content = d.getString("content") ?: "",
                        authorName = d.getString("authorName") ?: "",
                        authorId = d.getString("authorId") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun weeklyMenuFlow(messId: String): Flow<List<MenuItem>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("menu")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    MenuItem(
                        id = d.id,
                        messId = messId,
                        dayOfWeek = d.getString("dayOfWeek") ?: d.id,
                        breakfast = d.getString("breakfast") ?: "",
                        lunch = d.getString("lunch") ?: "",
                        dinner = d.getString("dinner") ?: "",
                        updatedBy = d.getString("updatedBy") ?: "",
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun routineFlow(messId: String): Flow<List<RoutineDay>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("routine")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    RoutineDay(
                        id = d.id,
                        messId = messId,
                        dayOfWeek = d.getString("dayOfWeek") ?: d.id,
                        memberId = d.getString("memberId") ?: "",
                        memberName = d.getString("memberName") ?: "",
                        assignedBy = d.getString("assignedBy") ?: "",
                        updatedAt = d.getLong("updatedAt") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun auditLogsFlow(messId: String): Flow<List<AuditLog>> = callbackFlow {
        if (messId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("messes").document(messId).collection("audit")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.map { d ->
                    AuditLog(
                        id = d.id,
                        messId = messId,
                        action = d.getString("action") ?: "",
                        entityType = d.getString("entityType") ?: "",
                        entityId = d.getString("entityId") ?: "",
                        details = d.getString("details") ?: "",
                        performedBy = d.getString("performedBy") ?: "",
                        performedByName = d.getString("performedByName") ?: "",
                        timestamp = d.getLong("timestamp") ?: 0L
                    )
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ---------------- WRITE ACTIONS ----------------

    suspend fun saveMeals(messId: String, meals: List<MealRecord>, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        val mealsCol = firestore.collection("messes").document(messId).collection("meals")

        for (meal in meals) {
            val docId = if (meal.id.isNotEmpty()) meal.id else "${meal.periodId}_${meal.date}_${meal.memberId}"
            val ref = mealsCol.document(docId)
            batch.set(ref, mapOf(
                "id" to docId,
                "messId" to messId,
                "periodId" to meal.periodId,
                "date" to meal.date,
                "memberId" to meal.memberId,
                "memberName" to meal.memberName,
                "breakfast" to meal.breakfast,
                "lunch" to meal.lunch,
                "dinner" to meal.dinner,
                "totalMeals" to meal.totalMeals,
                "updatedBy" to uid,
                "updatedAt" to System.currentTimeMillis()
            ))
        }

        // Audit log
        val auditRef = firestore.collection("messes").document(messId).collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "UPDATE",
            "entityType" to "MEAL",
            "entityId" to meals.firstOrNull()?.date.orEmpty(),
            "details" to "Saved meals for ${meals.size} member(s)",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun saveBazarEntry(
        messId: String,
        entry: BazarEntry,
        performerName: String
    ) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        val bazarId = if (entry.id.isNotEmpty()) entry.id else UUID.randomUUID().toString()
        val bazarRef = firestore.collection("messes").document(messId).collection("bazar").document(bazarId)

        var depositId = entry.depositId
        if (entry.isAddedToDeposit) {
            if (depositId.isNullOrEmpty()) {
                depositId = UUID.randomUUID().toString()
            }
            val depRef = firestore.collection("messes").document(messId).collection("deposits").document(depositId)
            batch.set(depRef, mapOf(
                "id" to depositId,
                "messId" to messId,
                "periodId" to entry.periodId,
                "date" to entry.date,
                "memberId" to entry.shopperMemberId,
                "memberName" to entry.shopperName,
                "amountPaisa" to entry.totalCostPaisa,
                "paymentMethod" to "Bazar Purchase",
                "note" to "Auto deposit from Bazar (${entry.items.joinToString { it.name }})",
                "isFromBazar" to true,
                "bazarId" to bazarId,
                "createdBy" to uid,
                "createdAt" to System.currentTimeMillis()
            ))
        }

        val itemsData = entry.items.map {
            mapOf("name" to it.name, "quantity" to it.quantity, "costPaisa" to it.costPaisa)
        }

        batch.set(bazarRef, mapOf(
            "id" to bazarId,
            "messId" to messId,
            "periodId" to entry.periodId,
            "date" to entry.date,
            "shopperMemberId" to entry.shopperMemberId,
            "shopperName" to entry.shopperName,
            "items" to itemsData,
            "totalCostPaisa" to entry.totalCostPaisa,
            "isAddedToDeposit" to entry.isAddedToDeposit,
            "depositId" to depositId,
            "note" to entry.note,
            "createdBy" to uid,
            "createdAt" to System.currentTimeMillis()
        ))

        val auditRef = firestore.collection("messes").document(messId).collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "CREATE",
            "entityType" to "BAZAR",
            "entityId" to bazarId,
            "details" to "${entry.shopperName} added bazar total ৳ ${entry.totalCostPaisa / 100.0}",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun deleteBazarEntry(messId: String, bazarId: String, depositId: String?, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        val bazarRef = firestore.collection("messes").document(messId).collection("bazar").document(bazarId)
        batch.delete(bazarRef)

        if (!depositId.isNullOrEmpty()) {
            val depRef = firestore.collection("messes").document(messId).collection("deposits").document(depositId)
            batch.delete(depRef)
        }

        val auditRef = firestore.collection("messes").document(messId).collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "DELETE",
            "entityType" to "BAZAR",
            "entityId" to bazarId,
            "details" to "Deleted bazar entry",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun saveDeposit(messId: String, deposit: Deposit, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        val depId = if (deposit.id.isNotEmpty()) deposit.id else UUID.randomUUID().toString()
        val depRef = firestore.collection("messes").document(messId).collection("deposits").document(depId)

        batch.set(depRef, mapOf(
            "id" to depId,
            "messId" to messId,
            "periodId" to deposit.periodId,
            "date" to deposit.date,
            "memberId" to deposit.memberId,
            "memberName" to deposit.memberName,
            "amountPaisa" to deposit.amountPaisa,
            "paymentMethod" to deposit.paymentMethod,
            "note" to deposit.note,
            "isFromBazar" to deposit.isFromBazar,
            "bazarId" to deposit.bazarId,
            "createdBy" to uid,
            "createdAt" to System.currentTimeMillis()
        ))

        val auditRef = firestore.collection("messes").document(messId).collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "CREATE",
            "entityType" to "DEPOSIT",
            "entityId" to depId,
            "details" to "Deposit ৳ ${deposit.amountPaisa / 100.0} for ${deposit.memberName}",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun deleteDeposit(messId: String, depositId: String, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        batch.delete(firestore.collection("messes").document(messId).collection("deposits").document(depositId))

        val auditRef = firestore.collection("messes").document(messId).collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "DELETE",
            "entityType" to "DEPOSIT",
            "entityId" to depositId,
            "details" to "Deleted deposit",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun saveBill(messId: String, bill: MonthlyBill, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val billId = if (bill.id.isNotEmpty()) bill.id else UUID.randomUUID().toString()
        val ref = firestore.collection("messes").document(messId).collection("bills").document(billId)

        ref.set(mapOf(
            "id" to billId,
            "messId" to messId,
            "periodId" to bill.periodId,
            "name" to bill.name,
            "category" to bill.category,
            "amountPaisa" to bill.amountPaisa,
            "splitMode" to bill.splitMode.name,
            "includedMemberIds" to bill.includedMemberIds,
            "note" to bill.note,
            "updatedBy" to uid,
            "updatedAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun deleteBill(messId: String, billId: String) {
        firestore.collection("messes").document(messId).collection("bills").document(billId).delete().await()
    }

    suspend fun saveExtraCost(messId: String, cost: ExtraCost, performerName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val costId = if (cost.id.isNotEmpty()) cost.id else UUID.randomUUID().toString()
        val ref = firestore.collection("messes").document(messId).collection("extraCosts").document(costId)

        ref.set(mapOf(
            "id" to costId,
            "messId" to messId,
            "periodId" to cost.periodId,
            "title" to cost.title,
            "amountPaisa" to cost.amountPaisa,
            "participantMemberIds" to cost.participantMemberIds,
            "date" to cost.date,
            "note" to cost.note,
            "createdBy" to uid,
            "createdAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun deleteExtraCost(messId: String, costId: String) {
        firestore.collection("messes").document(messId).collection("extraCosts").document(costId).delete().await()
    }

    suspend fun saveNotice(messId: String, title: String, content: String, authorName: String) {
        val uid = auth.currentUser?.uid ?: ""
        val noticeId = UUID.randomUUID().toString()
        val ref = firestore.collection("messes").document(messId).collection("notices").document(noticeId)

        ref.set(mapOf(
            "id" to noticeId,
            "messId" to messId,
            "title" to title,
            "content" to content,
            "authorName" to authorName,
            "authorId" to uid,
            "createdAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun deleteNotice(messId: String, noticeId: String) {
        firestore.collection("messes").document(messId).collection("notices").document(noticeId).delete().await()
    }

    suspend fun saveMenuItem(messId: String, item: MenuItem) {
        val uid = auth.currentUser?.uid ?: ""
        val ref = firestore.collection("messes").document(messId).collection("menu").document(item.dayOfWeek.lowercase())
        ref.set(mapOf(
            "id" to item.dayOfWeek.lowercase(),
            "messId" to messId,
            "dayOfWeek" to item.dayOfWeek,
            "breakfast" to item.breakfast,
            "lunch" to item.lunch,
            "dinner" to item.dinner,
            "updatedBy" to uid,
            "updatedAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun saveRoutineDay(messId: String, routine: RoutineDay) {
        val uid = auth.currentUser?.uid ?: ""
        val ref = firestore.collection("messes").document(messId).collection("routine").document(routine.dayOfWeek.lowercase())
        ref.set(mapOf(
            "id" to routine.dayOfWeek.lowercase(),
            "messId" to messId,
            "dayOfWeek" to routine.dayOfWeek,
            "memberId" to routine.memberId,
            "memberName" to routine.memberName,
            "assignedBy" to uid,
            "updatedAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun updateMemberRole(messId: String, memberId: String, newRole: MemberRole) {
        val messDoc = firestore.collection("messes").document(messId)
        val memberDoc = messDoc.collection("members").document(memberId)

        firestore.runTransaction { tx ->
            val messSnap = tx.get(messDoc)
            @Suppress("UNCHECKED_CAST")
            val managers = (messSnap.get("managerIds") as? List<String>)?.toMutableList() ?: mutableListOf()
            @Suppress("UNCHECKED_CAST")
            val subManagers = (messSnap.get("subManagerIds") as? List<String>)?.toMutableList() ?: mutableListOf()

            managers.remove(memberId)
            subManagers.remove(memberId)

            when (newRole) {
                MemberRole.MANAGER -> managers.add(memberId)
                MemberRole.SUB_MANAGER -> subManagers.add(memberId)
                MemberRole.MEMBER -> {}
            }

            tx.update(messDoc, mapOf("managerIds" to managers, "subManagerIds" to subManagers))
            tx.update(memberDoc, "role", newRole.name)
        }.await()
    }

    suspend fun updateMemberOpeningBalance(messId: String, memberId: String, balancePaisa: Long) {
        firestore.collection("messes").document(messId).collection("members").document(memberId)
            .update("openingBalancePaisa", balancePaisa).await()
    }

    suspend fun updateMessPeriod(messId: String, newPeriodId: String) {
        firestore.collection("messes").document(messId).update("currentPeriodId", newPeriodId).await()
    }

    suspend fun closeMonthAndStartNew(
        messId: String,
        currentPeriodId: String,
        summary: PeriodCalculationSummary,
        carryOverBalances: Boolean,
        performerName: String
    ) {
        val uid = auth.currentUser?.uid ?: ""
        val batch = firestore.batch()
        val messRef = firestore.collection("messes").document(messId)

        // 1. Freeze current period snapshot
        val periodRef = messRef.collection("periods").document(currentPeriodId)
        batch.update(periodRef, mapOf(
            "isClosed" to true,
            "closedAt" to System.currentTimeMillis(),
            "closedBy" to uid,
            "snapshotTotalMeals" to summary.totalMeals,
            "snapshotMealCostPaisa" to summary.totalBazarCostPaisa,
            "snapshotMealRatePaisa" to summary.mealRatePaisa.toLong(),
            "snapshotTotalDepositsPaisa" to summary.totalDepositsPaisa,
            "snapshotTotalBillsPaisa" to summary.totalBillsPaisa,
            "snapshotMessBalancePaisa" to summary.messBalancePaisa
        ))

        // 2. Compute next month period ID and name
        val parts = currentPeriodId.split("-")
        val curYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val curMonth = parts.getOrNull(1)?.toIntOrNull() ?: 11
        val nextCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, curYear)
            set(Calendar.MONTH, curMonth - 1)
            add(Calendar.MONTH, 1)
        }
        val nextPeriodId = SimpleDateFormat("yyyy-MM", Locale.US).format(nextCal.time)
        val nextPeriodName = SimpleDateFormat("MMMM yyyy", Locale.US).format(nextCal.time)

        val nextPeriodRef = messRef.collection("periods").document(nextPeriodId)
        batch.set(nextPeriodRef, mapOf(
            "id" to nextPeriodId,
            "messId" to messId,
            "year" to nextCal.get(Calendar.YEAR),
            "month" to nextCal.get(Calendar.MONTH) + 1,
            "name" to nextPeriodName,
            "isClosed" to false,
            "snapshotTotalMeals" to 0.0,
            "snapshotMealCostPaisa" to 0L,
            "snapshotMealRatePaisa" to 0L,
            "snapshotTotalDepositsPaisa" to 0L,
            "snapshotTotalBillsPaisa" to 0L,
            "snapshotMessBalancePaisa" to 0L
        ))

        // 3. Update opening balances if carry over is selected
        if (carryOverBalances) {
            for (mCalc in summary.memberCalculations) {
                val mRef = messRef.collection("members").document(mCalc.member.id)
                batch.update(mRef, "openingBalancePaisa", mCalc.currentBalancePaisa)
            }
        }

        // 4. Update mess current period
        batch.update(messRef, "currentPeriodId", nextPeriodId)

        // 5. Audit
        val auditRef = messRef.collection("audit").document()
        batch.set(auditRef, mapOf(
            "id" to auditRef.id,
            "messId" to messId,
            "action" to "CLOSE_MONTH",
            "entityType" to "PERIOD",
            "entityId" to currentPeriodId,
            "details" to "Closed $currentPeriodId, opened $nextPeriodId (Carry over: $carryOverBalances)",
            "performedBy" to uid,
            "performedByName" to performerName,
            "timestamp" to System.currentTimeMillis()
        ))

        batch.commit().await()
    }

    suspend fun deleteMess(messId: String) {
        val uid = auth.currentUser?.uid ?: ""
        val messDoc = firestore.collection("messes").document(messId).get().await()
        val creatorId = messDoc.getString("creatorId")
        if (creatorId != uid) {
            throw IllegalStateException("Only the mess creator can delete the mess.")
        }
        firestore.collection("messes").document(messId).delete().await()
        firestore.collection("users").document(uid).update("activeMessId", "").await()
    }
}
