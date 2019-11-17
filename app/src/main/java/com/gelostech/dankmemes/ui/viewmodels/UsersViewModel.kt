package com.gelostech.dankmemes.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.repositories.UsersRepository
import com.gelostech.dankmemes.data.responses.*
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

    private val _observableUserLiveData = MutableLiveData<ObservableUserResponse>()
    val observableUserLiveData: MutableLiveData<ObservableUserResponse>
        get() = _observableUserLiveData

    private val _genericResponseLiveData = MutableLiveData<GenericResponse>()
    val genericResponseLiveData: MutableLiveData<GenericResponse>
        get() = _genericResponseLiveData

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
            _genericResponseLiveData.value = GenericResponse.loading()

            when (val result = repository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _genericResponseLiveData.value = GenericResponse.success(result.data)
                }

                is Result.Error -> {
                    _genericResponseLiveData.value = GenericResponse.error(result.error)
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
     * Function to fetch a User's profile
     * @param userId - ID of the User
     */
    fun fetchObservableUser(userId: String) {
        viewModelScope.launch {
            _observableUserLiveData.value = ObservableUserResponse.loading()

            when (val userResult = repository.fetchObservableUserById(userId)) {
                is Result.Success -> {
                    _observableUserLiveData.value = ObservableUserResponse.success(userResult.data)
                }

                is Result.Error -> {
                    _observableUserLiveData.value = ObservableUserResponse.error(userResult.error)
                }
            }
        }
    }

    /**
     * Function to update User Avatar
     */
    fun updateUserAvatar(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _genericResponseLiveData.value = GenericResponse.loading()

            repository.updateUserAvatar(userId, imageUri) {
                when (it) {
                    is Result.Success -> {
                        _genericResponseLiveData.postValue(GenericResponse.success(true,
                                item = GenericResponse.ITEM_RESPONSE.UPDATE_AVATAR,
                                value = it.data))
                    }

                    is Result.Error -> {
                        _genericResponseLiveData.postValue(GenericResponse.error(it.error))
                    }
                }
            }
        }
    }

    /**
     * Function to update User profile details
     */
    fun updateUserDetails(userId: String, username: String, bio: String, avatar: String?) {
        viewModelScope.launch {
            _genericResponseLiveData.value = GenericResponse.loading()

            when (val result = repository.updateUserDetails(userId, username, bio, avatar)) {
                is Result.Success -> _genericResponseLiveData.value = GenericResponse.success(result.data)

                is Result.Error -> _genericResponseLiveData.value = GenericResponse.error(result.error)
            }
        }
    }

    /**
     * Function to logout User
     */
    fun logout() {
        _genericResponseLiveData.value = GenericResponse.loading()

        when (val result = repository.logout()) {
            is Result.Success -> {
                _genericResponseLiveData.value = GenericResponse.success(result.data)
            }

            is Result.Error -> {
                _genericResponseLiveData.value = GenericResponse.error(result.error)
            }
        }
    }

}