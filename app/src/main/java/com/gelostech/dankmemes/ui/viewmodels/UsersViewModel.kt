package com.gelostech.dankmemes.ui.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.data.responses.FirebaseUserResponse
import com.gelostech.dankmemes.data.responses.GoogleLoginResponse
import com.gelostech.dankmemes.data.responses.UserResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch

class UsersViewModel constructor(private val repository: UsersRepository): ViewModel() {
    private val _loginLiveData = MutableLiveData<FirebaseUserResponse>()
    val loginLiveData: MutableLiveData<FirebaseUserResponse>
        get() = _loginLiveData

    private val _loginWithGoogleLiveData = MutableLiveData<GoogleLoginResponse>()
    val loginWithGoogleLiveData: MutableLiveData<GoogleLoginResponse>
        get() = _loginWithGoogleLiveData

    private val _userLiveData = MutableLiveData<UserResponse>()
    val userLiveData: MutableLiveData<UserResponse>
        get() = _userLiveData

    /**
     * Function to login User with Email & Password
     * @param email - Email
     * @param password - Password
     */
    fun loginUserWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _loginLiveData.value = FirebaseUserResponse.loading()

            when (val loginResult = repository.loginWithEmailAndPassword(email, password)) {
                is Result.Success -> {
                    _loginLiveData.value = FirebaseUserResponse.success(loginResult.data)
                }
                is Result.Error -> {
                    _loginLiveData.value = FirebaseUserResponse.error(loginResult.error)
                }
            }
        }
    }

    /**
     * Function to login with Google
     *@param account - Current Google account
     */
    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _loginWithGoogleLiveData.value = GoogleLoginResponse.loading()

            when (val loginResult = repository.loginWithGoogle(account)) {
                is Result.Success -> {
                    _loginWithGoogleLiveData.value = loginResult.data
                }
                is Result.Error -> {
                    _loginWithGoogleLiveData.value = GoogleLoginResponse.error(loginResult.error)
                }
            }
        }

    }

    /**
     * Function to create account for Google sign in User
     * @param user - The user details to create account for
     */
    fun createGoogleUserAccount(user: User) {
        viewModelScope.launch {
            _userLiveData.value = UserResponse.loading()

            when (val userResult = repository.createGoogleUserAccount(user)) {
                is Result.Success -> {
                    _userLiveData.value = UserResponse.success(userResult.data)
                }

                is Result.Error -> {
                    _userLiveData.value = UserResponse.error(userResult.error)
                }
            }
        }
    }

    /**
     * Function to fetch a User's profile
     * @param userId - ID of the User
     */
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