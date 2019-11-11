package com.gelostech.dankmemes.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.data.responses.FirebaseUserResponse
import com.gelostech.dankmemes.data.responses.GenericResponse
import com.gelostech.dankmemes.data.responses.GoogleLoginResponse
import com.gelostech.dankmemes.data.responses.UserResponse
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch
import timber.log.Timber

class UsersViewModel constructor(private val repository: UsersRepository): ViewModel() {
    private val _authLiveData = MutableLiveData<FirebaseUserResponse>()
    val authLiveData: MutableLiveData<FirebaseUserResponse>
        get() = _authLiveData

    private val _loginWithGoogleLiveData = MutableLiveData<GoogleLoginResponse>()
    val loginWithGoogleLiveData: MutableLiveData<GoogleLoginResponse>
        get() = _loginWithGoogleLiveData

    private val _userLiveData = MutableLiveData<UserResponse>()
    val userLiveData: MutableLiveData<UserResponse>
        get() = _userLiveData

    private val _resetPasswordLiveData = MutableLiveData<GenericResponse>()
    val resetPasswordLiveData: MutableLiveData<GenericResponse>
        get() = _resetPasswordLiveData

    private val _logoutLiveData = MutableLiveData<GenericResponse>()
    val logoutLiveData = _logoutLiveData

    /**
     * Function to login User with Email & Password
     * @param email - Email
     * @param password - Password
     */
    fun loginUserWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _authLiveData.value = FirebaseUserResponse.loading()

            when (val loginResult = repository.loginWithEmailAndPassword(email, password)) {
                is Result.Success -> {
                    _authLiveData.value = FirebaseUserResponse.success(loginResult.data)
                }
                is Result.Error -> {
                    _authLiveData.value = FirebaseUserResponse.error(loginResult.error)
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
     * Function to register user
     */
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _authLiveData.value = FirebaseUserResponse.loading()

            when (val authResult = repository.registerUser(email, password)) {
                is Result.Success -> {
                    _authLiveData.value = FirebaseUserResponse.success(authResult.data)
                }

                is Result.Error -> {
                    _authLiveData.value = FirebaseUserResponse.error(authResult.error)
                }
            }
        }
    }

    /**
     * Function to create account for new User
     * @param user - The user details to create account for
     * @param imageUri - Selected avatar file Uri
     */
    fun createUserAccount(user: User, imageUri: Uri) {
        viewModelScope.launch {
            _userLiveData.value = UserResponse.loading()

            repository.createUserAccount(imageUri, user) {
                Timber.e("Creating account..")

                when (it) {
                    is Result.Success -> {
                        _userLiveData.postValue(UserResponse.success(it.data))
                    }

                    is Result.Error -> {
                        _userLiveData.postValue(UserResponse.error(it.error))
                    }
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
     * Function to send reset password email
     */
    fun sendResetPasswordEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordLiveData.value = GenericResponse.loading()

            when (val result = repository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _resetPasswordLiveData.value = GenericResponse.success(result.data)
                }

                is Result.Error -> {
                    _resetPasswordLiveData.value = GenericResponse.error(result.error)
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

    /**
     * Function to logout User
     */
    fun logout() {
        _logoutLiveData.value = GenericResponse.loading()

        when (val result = repository.logout()) {
            is Result.Success -> {
                _logoutLiveData.value = GenericResponse.success(result.data)
            }

            is Result.Error -> {
                _logoutLiveData.value = GenericResponse.error(result.error)
            }
        }
    }

}