package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.data.responses.FirebaseUserResponse
import com.gelostech.dankmemes.data.responses.UserResponse
import kotlinx.coroutines.launch

class UsersViewModel constructor(private val repository: UsersRepository): ViewModel() {
    private val _loginWithEmailAndPasswordLiveData = MutableLiveData<FirebaseUserResponse>()
    val loginWithEmailAndPasswordLiveData: MutableLiveData<FirebaseUserResponse>
        get() = _loginWithEmailAndPasswordLiveData

    private val _userLiveData = MutableLiveData<UserResponse>()
    val userLiveData: MutableLiveData<UserResponse>
        get() = _userLiveData

    fun loginUserWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _loginWithEmailAndPasswordLiveData.value = FirebaseUserResponse.loading()

            when (val loginResult = repository.loginWithEmailAndPassword(email, password)) {
                is Result.Success -> {
                    _loginWithEmailAndPasswordLiveData.value = FirebaseUserResponse.success(loginResult.data)
                }
                is Result.Error -> {
                    _loginWithEmailAndPasswordLiveData.value = FirebaseUserResponse.error(loginResult.error)
                }
            }
        }
    }

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            _userLiveData.value = UserResponse.loading()

            when (val userResult = repository.fetchUserById(userId)) {
                is Result.Success -> {
                    _userLiveData.value = UserResponse.success(userResult.data)
                }

                is Result.Error -> {
                    _userLiveData.value = UserResponse.error(userResult.error)
                }
            }
        }
    }

}