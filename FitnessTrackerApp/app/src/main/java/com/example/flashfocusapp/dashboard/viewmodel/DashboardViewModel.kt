package com.example.flashfocusapp.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashfocusapp.common.ui.FirebaseAuthResponse
import com.example.flashfocusapp.dashboard.model.Flashcard
import com.example.flashfocusapp.repositories.FlashcardRepositoryImpl
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FlashcardRepositoryImpl,
    private val user: FirebaseUser?
) : ViewModel() {

    private val _displayUserNameState = MutableStateFlow("")
    val displayUserNameState: StateFlow<String> = _displayUserNameState

    private val _userSignOut = MutableStateFlow(FirebaseAuthResponse.NONE)
    val userSignOut: StateFlow<FirebaseAuthResponse> = _userSignOut

    /**
     * Create Flashcards
     */
    private val _cardSubjects = MutableStateFlow(listOf("Math", "Science", "English", "History"))
    val cardSubjects: StateFlow<List<String>> = _cardSubjects

    private val _addCardResult = MutableStateFlow("")
    val addCardResult: StateFlow<String> = _addCardResult

    private val _createButtonEnabled = MutableStateFlow(true)
    val createButtonEnabled: StateFlow<Boolean> = _createButtonEnabled

    init {
        updateDisplayUserName()

        viewModelScope.launch {
            repository
        }
    }

    fun signOut() {
        Firebase.auth.signOut()
        _userSignOut.value = FirebaseAuthResponse.SUCCESS
    }

    private fun updateDisplayUserName() {
        _displayUserNameState.value = user?.displayName ?: ""
    }

    fun addFlashcard(question: String, answer: String, difficulty: Int) {
        val flashcard = Flashcard(
            question = question,
            answer = answer,
            difficulty = difficulty,
            dateCreated = System.currentTimeMillis()
        )
        viewModelScope.launch {
            user?.let { user ->
                _createButtonEnabled.value = false
                repository.addCardToDatabase(flashcard = flashcard, uid = user.uid)
                    .collect { result ->
                        when {
                            result.isSuccess -> {
                                _addCardResult.value = "Flashcard added successfully"
                            }

                            else -> {
                                _addCardResult.value = "Error adding flashcard"
                            }
                        }
                        _createButtonEnabled.value = true
                    }
            }
        }
    }

    fun getFlashcards() {
        repository.getFlashcards(Firebase.auth.currentUser?.uid ?: "")
    }

    fun createSubject(subject: String) {

    }
}