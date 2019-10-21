package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.data.responses.FirebaseUserResponse
import kotlinx.coroutines.launch

class UsersViewModel constructor(private val repository: UsersRepository): ViewModel() {
    private val _loginWithEmailAndPasswordLiveData = MutableLiveData<FirebaseUserResponse>()
    val loginWithEmailAndPasswordLiveData: MutableLiveData<FirebaseUserResponse>
        get() = _loginWithEmailAndPasswordLiveData

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

}