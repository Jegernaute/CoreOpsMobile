package com.example.coreops.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coreops.data.remote.models.UserDto
import com.example.coreops.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val profile: UserDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _state.value = ProfileState.Loading
            repository.getMyProfile().onSuccess { userDto ->
                _state.value = ProfileState.Success(userDto)
            }.onFailure { error ->
                _state.value = ProfileState.Error(error.message ?: "Помилка завантаження профілю")
            }
        }
    }
}