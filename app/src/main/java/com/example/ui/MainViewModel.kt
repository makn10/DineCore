package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calc.CalculationService
import com.example.data.calc.PeriodCalculationSummary
import com.example.data.model.AuditLog
import com.example.data.model.BazarEntry
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
import com.example.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = FirestoreRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _currentMess = MutableStateFlow<Mess?>(null)
    val currentMess: StateFlow<Mess?> = _currentMess.asStateFlow()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _periods = MutableStateFlow<List<Period>>(emptyList())
    val periods: StateFlow<List<Period>> = _periods.asStateFlow()

    private val _selectedPeriodId = MutableStateFlow<String>("")
    val selectedPeriodId: StateFlow<String> = _selectedPeriodId.asStateFlow()

    private val _meals = MutableStateFlow<List<MealRecord>>(emptyList())
    val meals: StateFlow<List<MealRecord>> = _meals.asStateFlow()

    private val _bazarEntries = MutableStateFlow<List<BazarEntry>>(emptyList())
    val bazarEntries: StateFlow<List<BazarEntry>> = _bazarEntries.asStateFlow()

    private val _deposits = MutableStateFlow<List<Deposit>>(emptyList())
    val deposits: StateFlow<List<Deposit>> = _deposits.asStateFlow()

    private val _bills = MutableStateFlow<List<MonthlyBill>>(emptyList())
    val bills: StateFlow<List<MonthlyBill>> = _bills.asStateFlow()

    private val _extraCosts = MutableStateFlow<List<ExtraCost>>(emptyList())
    val extraCosts: StateFlow<List<ExtraCost>> = _extraCosts.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _weeklyMenu = MutableStateFlow<List<MenuItem>>(emptyList())
    val weeklyMenu: StateFlow<List<MenuItem>> = _weeklyMenu.asStateFlow()

    private val _routine = MutableStateFlow<List<RoutineDay>>(emptyList())
    val routine: StateFlow<List<RoutineDay>> = _routine.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // Live calculation summary
    private val _coreData = combine(_members, _meals, _bazarEntries) { mList, mealList, bList ->
        Triple(mList, mealList, bList)
    }

    val calculationSummary: StateFlow<PeriodCalculationSummary> = combine(
        _coreData,
        _deposits,
        _bills,
        _extraCosts
    ) { (mList, mealList, bList), dList, billList, eList ->
        CalculationService.calculate(
            members = mList,
            meals = mealList,
            bazarEntries = bList,
            deposits = dList,
            bills = billList,
            extraCosts = eList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PeriodCalculationSummary(
            totalMeals = 0.0,
            totalBazarCostPaisa = 0L,
            mealRatePaisa = 0.0,
            totalDepositsPaisa = 0L,
            totalBillsPaisa = 0L,
            totalExtraCostsPaisa = 0L,
            messBalancePaisa = 0L,
            memberCalculations = emptyList()
        )
    )

    init {
        try {
            repository.auth.addAuthStateListener { auth ->
                _currentUser.value = auth.currentUser
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    observeUserProfile(uid)
                } else {
                    _userProfile.value = null
                    _currentMess.value = null
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainViewModel", "Auth listener setup handled safely: ${e.message}", e)
        }
    }

    private fun observeUserProfile(uid: String) {
        viewModelScope.launch {
            repository.userProfileFlow(uid).collect { profile ->
                _userProfile.value = profile
                val activeMessId = profile?.activeMessId
                if (!activeMessId.isNullOrEmpty()) {
                    observeMess(activeMessId)
                } else {
                    _currentMess.value = null
                }
            }
        }
    }

    private fun observeMess(messId: String) {
        viewModelScope.launch {
            repository.messFlow(messId).collect { mess ->
                _currentMess.value = mess
                if (mess != null) {
                    if (_selectedPeriodId.value.isEmpty() || _selectedPeriodId.value != mess.currentPeriodId) {
                        _selectedPeriodId.value = mess.currentPeriodId
                    }
                    observeMessData(messId, _selectedPeriodId.value)
                }
            }
        }
    }

    fun selectPeriod(periodId: String) {
        _selectedPeriodId.value = periodId
        val messId = _currentMess.value?.id.orEmpty()
        if (messId.isNotEmpty()) {
            observeMessData(messId, periodId)
        }
    }

    private fun observeMessData(messId: String, periodId: String) {
        viewModelScope.launch {
            repository.membersFlow(messId).collect { _members.value = it }
        }
        viewModelScope.launch {
            repository.periodsFlow(messId).collect { _periods.value = it }
        }
        viewModelScope.launch {
            repository.mealsFlow(messId, periodId).collect { _meals.value = it }
        }
        viewModelScope.launch {
            repository.bazarFlow(messId, periodId).collect { _bazarEntries.value = it }
        }
        viewModelScope.launch {
            repository.depositsFlow(messId, periodId).collect { _deposits.value = it }
        }
        viewModelScope.launch {
            repository.billsFlow(messId, periodId).collect { _bills.value = it }
        }
        viewModelScope.launch {
            repository.extraCostsFlow(messId, periodId).collect { _extraCosts.value = it }
        }
        viewModelScope.launch {
            repository.noticesFlow(messId).collect { _notices.value = it }
        }
        viewModelScope.launch {
            repository.weeklyMenuFlow(messId).collect { _weeklyMenu.value = it }
        }
        viewModelScope.launch {
            repository.routineFlow(messId).collect { _routine.value = it }
        }
        viewModelScope.launch {
            repository.auditLogsFlow(messId).collect { _auditLogs.value = it }
        }
    }

    // ---------------- ACTIONS ----------------

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    fun signInWithGoogleIdToken(idToken: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            try {
                val result = repository.auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    repository.ensureUserProfile(
                        userId = user.uid,
                        email = user.email.orEmpty(),
                        displayName = user.displayName.orEmpty(),
                        phone = user.phoneNumber.orEmpty()
                    )
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Google sign in failed"
                )
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter both email and password")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val user = repository.signInWithEmail(trimmedEmail, trimmedPass)
                _currentUser.value = user
                observeUserProfile(user.uid)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Login failed. Please check your credentials."
                )
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String, phone: String, onSuccess: () -> Unit) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()
        val trimmedPhone = phone.trim()

        if (trimmedName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your full name")
            return
        }
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid email address")
            return
        }
        if (trimmedPass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val user = repository.signUpWithEmail(trimmedName, trimmedEmail, trimmedPass, trimmedPhone)
                _currentUser.value = user
                _userProfile.value = UserProfile(
                    id = user.uid,
                    email = trimmedEmail,
                    displayName = trimmedName,
                    phone = trimmedPhone
                )
                observeUserProfile(user.uid)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Account created successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Sign up failed. Please try again."
                )
            }
        }
    }

    fun signInAsGuest(name: String, onSuccess: () -> Unit) {
        val guestName = name.trim().ifEmpty { "Guest Manager" }
        val guestId = "guest_" + System.currentTimeMillis()
        _userProfile.value = UserProfile(
            id = guestId,
            email = "guest@dinecore.local",
            displayName = guestName,
            phone = "01700000000"
        )
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
        onSuccess()
    }

    fun signOut(onSignedOut: () -> Unit) {
        try {
            repository.auth.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        _currentUser.value = null
        _userProfile.value = null
        _currentMess.value = null
        onSignedOut()
    }

    fun createMess(name: String, address: String, creatorName: String, phone: String, onSuccess: () -> Unit) {
        val trimmedName = name.trim()
        val trimmedCreator = creatorName.trim()
        if (trimmedName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter mess name")
            return
        }
        if (trimmedCreator.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter manager name")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val mess = repository.createMess(trimmedName, address.trim(), trimmedCreator, phone.trim())
                _currentMess.value = mess
                val uid = repository.currentUser?.uid ?: _userProfile.value?.id ?: "manager_${System.currentTimeMillis()}"
                
                // Immediately update local user profile
                val updatedProfile = (_userProfile.value ?: UserProfile(id = uid, email = repository.currentUser?.email.orEmpty())).copy(
                    activeMessId = mess.id,
                    displayName = trimmedCreator,
                    phone = phone.trim()
                )
                _userProfile.value = updatedProfile

                // Immediately seed initial manager in members list
                val initialManager = Member(
                    id = uid,
                    messId = mess.id,
                    userId = uid,
                    name = trimmedCreator,
                    phone = phone.trim(),
                    role = MemberRole.MANAGER,
                    isActive = true,
                    openingBalancePaisa = 0L,
                    joinedAt = System.currentTimeMillis()
                )
                _members.value = listOf(initialManager)
                _selectedPeriodId.value = mess.currentPeriodId

                observeMess(mess.id)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Mess created successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to create mess")
            }
        }
    }

    fun joinMess(inviteCode: String, memberName: String, phone: String, onSuccess: () -> Unit) {
        val trimmedCode = inviteCode.trim().uppercase()
        val trimmedName = memberName.trim()
        if (trimmedCode.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter 6-digit invite code")
            return
        }
        if (trimmedName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your name")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val mess = repository.joinMessByInviteCode(trimmedCode, trimmedName, phone.trim())
                _currentMess.value = mess
                val uid = repository.currentUser?.uid ?: _userProfile.value?.id ?: "member_${System.currentTimeMillis()}"
                val updatedProfile = (_userProfile.value ?: UserProfile(id = uid, email = repository.currentUser?.email.orEmpty())).copy(
                    activeMessId = mess.id,
                    displayName = trimmedName,
                    phone = phone.trim()
                )
                _userProfile.value = updatedProfile

                val initialMember = Member(
                    id = uid,
                    messId = mess.id,
                    userId = uid,
                    name = trimmedName,
                    phone = phone.trim(),
                    role = MemberRole.MEMBER,
                    isActive = true,
                    openingBalancePaisa = 0L,
                    joinedAt = System.currentTimeMillis()
                )
                _members.value = listOf(initialMember)
                _selectedPeriodId.value = mess.currentPeriodId

                observeMess(mess.id)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Joined mess successfully!")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to join mess")
            }
        }
    }

    fun saveMeals(mealsToSave: List<MealRecord>, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.saveMeals(messId, mealsToSave, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Meals saved successfully!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to save meals")
            }
        }
    }

    fun saveBazarEntry(entry: BazarEntry, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.saveBazarEntry(messId, entry, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Bazar entry saved!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to save bazar")
            }
        }
    }

    fun deleteBazarEntry(entry: BazarEntry) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.deleteBazarEntry(messId, entry.id, entry.depositId, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Bazar entry removed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to delete")
            }
        }
    }

    fun saveDeposit(deposit: Deposit, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.saveDeposit(messId, deposit, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Deposit saved!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to save deposit")
            }
        }
    }

    fun deleteDeposit(depositId: String) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.deleteDeposit(messId, depositId, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Deposit deleted")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to delete deposit")
            }
        }
    }

    fun saveBill(bill: MonthlyBill, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.saveBill(messId, bill, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Bill saved!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to save bill")
            }
        }
    }

    fun deleteBill(billId: String) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteBill(messId, billId)
                _uiState.value = _uiState.value.copy(successMessage = "Bill removed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to remove bill")
            }
        }
    }

    fun saveExtraCost(cost: ExtraCost, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val performer = _userProfile.value?.displayName ?: "User"
        viewModelScope.launch {
            try {
                repository.saveExtraCost(messId, cost, performer)
                _uiState.value = _uiState.value.copy(successMessage = "Extra cost saved!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to save extra cost")
            }
        }
    }

    fun deleteExtraCost(costId: String) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteExtraCost(messId, costId)
                _uiState.value = _uiState.value.copy(successMessage = "Extra cost removed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to remove")
            }
        }
    }

    fun saveNotice(title: String, content: String, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val author = _userProfile.value?.displayName ?: "Manager"
        viewModelScope.launch {
            try {
                repository.saveNotice(messId, title, content, author)
                _uiState.value = _uiState.value.copy(successMessage = "Notice posted!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to post notice")
            }
        }
    }

    fun deleteNotice(noticeId: String) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteNotice(messId, noticeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to delete notice")
            }
        }
    }

    fun saveMenuItem(item: MenuItem, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.saveMenuItem(messId, item)
                _uiState.value = _uiState.value.copy(successMessage = "Menu updated!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to update menu")
            }
        }
    }

    fun saveRoutineDay(routine: RoutineDay, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.saveRoutineDay(messId, routine)
                _uiState.value = _uiState.value.copy(successMessage = "Routine updated!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to update routine")
            }
        }
    }

    fun updateMemberRole(memberId: String, newRole: MemberRole) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.updateMemberRole(messId, memberId, newRole)
                _uiState.value = _uiState.value.copy(successMessage = "Member role updated!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to update role")
            }
        }
    }

    fun updateMemberOpeningBalance(memberId: String, balancePaisa: Long) {
        val messId = _currentMess.value?.id ?: return
        viewModelScope.launch {
            try {
                repository.updateMemberOpeningBalance(messId, memberId, balancePaisa)
                _uiState.value = _uiState.value.copy(successMessage = "Opening balance updated!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to update opening balance")
            }
        }
    }

    fun closeMonthAndStartNew(carryOver: Boolean, onComplete: () -> Unit = {}) {
        val messId = _currentMess.value?.id ?: return
        val curPeriod = _selectedPeriodId.value
        val summary = calculationSummary.value
        val performer = _userProfile.value?.displayName ?: "Manager"

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.closeMonthAndStartNew(messId, curPeriod, summary, carryOver, performer)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "New month started successfully!")
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to start new month")
            }
        }
    }

    fun updateThemeAndLanguage(themeMode: String, language: String) {
        val uid = _currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                repository.updateThemeAndLanguage(uid, themeMode, language)
            } catch (_: Exception) {}
        }
    }

    fun deleteMess(onDeleted: () -> Unit) {
        val messId = _currentMess.value?.id ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                repository.deleteMess(messId)
                _currentMess.value = null
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Mess deleted")
                onDeleted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Failed to delete mess")
            }
        }
    }
}
