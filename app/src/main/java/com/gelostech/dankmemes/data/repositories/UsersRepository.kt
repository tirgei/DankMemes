package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GoogleLoginResponse
import com.gelostech.dankmemes.data.wrappers.ObservableUser
import com.gelostech.dankmemes.ui.callbacks.StorageUploadListener
import com.gelostech.dankmemes.utils.AppUtils
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.TimeFormatter
import com.gelostech.dankmemes.utils.get
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class UsersRepository constructor(private val firestoreDatabase: FirebaseFirestore,
                                  private val firebaseAuth: FirebaseAuth,
                                  private val storageReference: StorageReference) {

    private val db = firestoreDatabase.collection(Constants.USERS)

    /**
     * Function to register an anonymous User
     * @param email - Email
     * @param password - Password
     */
    suspend fun linkAnonymousUserToCredentials(email: String, password: String): Result<FirebaseUser> {
        val credential = EmailAuthProvider.getCredential(email, password)
        val authResult = firebaseAuth.currentUser!!.linkWithCredential(credential)
        return handleRegisterUser(authResult)
    }

    /**
     * Function to register a User
     * @param email - Email
     * @param password - Password
     */
    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
        return handleRegisterUser(authResult)
    }

    /**
     * Function to handle user register Task
     */
    private suspend fun handleRegisterUser(authResult: Task<AuthResult>): Result<FirebaseUser> {
        return try {
            val result = authResult.await()
            Result.Success(result.user!!)

        } catch (weakPassword: FirebaseAuthWeakPasswordException){
            Result.Error("Please enter a stronger password")

        } catch (userExists: FirebaseAuthUserCollisionException) {
            Result.Error("Account already exists. Please log in")

        } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
            Result.Error("Incorrect email format")

        } catch (e: Exception) {
            Result.Error("Error signing up. Please try again")
        }
    }

    /**
     * Function to create account for new User
     * @param user - The user details to create account for
     * @param avatarUri - Selected avatar file Uri
     */
    suspend fun createUserAccount(avatarUri: Uri, user: User, callback: (Result<User>) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(user.userId!!)
        val errorMessage = "Error signing up. Please try again"

        AppUtils.uploadFileToFirebaseStorage(storageDb, avatarUri, object : StorageUploadListener {
            override fun onFileUploaded(downloadUrl: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    if (downloadUrl.isNullOrEmpty()) {
                        Timber.e("Avatar is not uploaded...")
                        callback(Result.Error(errorMessage))
                    } else {
                        Timber.e("Avatar uploaded...")

                        try {
                            user.apply {
                                user.userAvatar = downloadUrl
                            }

                            db.document(user.userId!!).set(user).await()
                            callback(Result.Success(user))

                        } catch (e: java.lang.Exception) {
                            Timber.e("Error creating User account: ${e.localizedMessage}")
                            callback(Result.Error(errorMessage))
                        }
                    }
                }
            }
        })

    }

    /**
     * Function to login User
     * @param email - Email
     * @param password - Password
     */
    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(result.user!!)

        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
            Timber.e("InvalidCredException: ${wrongPassword.localizedMessage}")
            Result.Error("Email or Password incorrect")

        } catch (userNull: FirebaseAuthInvalidUserException) {
            Timber.e("UserException: ${userNull.localizedMessage}")
            (Result.Error("Account not found. Have you signed up?"))

        } catch (e: Exception) {
            Timber.e("LoginException: ${e.localizedMessage}")
            Result.Error("Error signing in. Please try again")
        }
    }

    /**
     * Function to login with Google
     * @param account - Device Google account
     */
    suspend fun loginWithGoogle(account: GoogleSignInAccount): Result<GoogleLoginResponse> {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()

        return try {
            Result.Success(GoogleLoginResponse.success(result.additionalUserInfo!!.isNewUser, result.user!!))

        } catch (e: java.lang.Exception) {
            Timber.e("Error logging in with Google: ${e.localizedMessage}")
            Result.Error("Error signing in. Please try again")
        }
    }

    /**
     * Function to create account for User registered through Google login
     * @param user - Object of the user details
     */
    suspend fun createGoogleUserAccount(user: User): Result<User> {
        val errorMessage = "Error signing up"

        return try {
            db.document(user.userId!!).set(user).await()
            Result.Success(user)

        } catch (e: Exception) {
            Timber.e("Error creating account for Google user: ${e.localizedMessage}")
            Result.Error(errorMessage)
        }
    }

    /**
     * Function to send email to reset User password
     * @param email - Email to send instructions to
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        val errorMessage = "Error sending password reset email"

        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.Success(true)

        } catch (e: FirebaseAuthInvalidUserException) {
            Timber.e("$errorMessage: ${e.localizedMessage}")
            Result.Error("Account not found. Have you signed up?")

        } catch (e: java.lang.Exception) {
            Timber.e("$errorMessage: ${e.localizedMessage}")
            Result.Error(errorMessage)
        }
    }

    /**
     * Fetch user by ID
     * @param userId - ID of the User
     */
    suspend fun fetchUserById(userId: String): Result<User> {
        return try {
            val user = db.document(userId)
                    .get()
                    .await()

            Result.Success(user.toObject(User::class.java)!!)
        } catch (e: java.lang.Exception) {
            Timber.e("Error fetching User: ${e.localizedMessage}")
            Result.Error("User not found")
        }
    }
     
    /** Fetch user by ID
     * @param userId - ID of the User
     */
    suspend fun fetchObservableUserById(userId: String): Result<ObservableUser> {
        return try {
            val user = db.document(userId)
                    .get()
                    .await()

            Result.Success(ObservableUser(user.id, getObservableUser(user.id)))
        } catch (e: java.lang.Exception) {
            Timber.e("Error fetching User: ${e.localizedMessage}")
            Result.Error("User not found")
        }
    }

    /**
     * Function to upload new user avatar
     * @param userId - ID of the User
     * @param avatarUri - URI of the new avatar
     * @param callback - Callback with the download url
     */
    fun updateUserAvatar(userId: String, avatarUri: Uri, callback: (Result<String>) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(userId)

        AppUtils.uploadFileToFirebaseStorage(storageDb, avatarUri, object : StorageUploadListener {
            override fun onFileUploaded(downloadUrl: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    if (downloadUrl.isNullOrEmpty()) {
                        callback(Result.Error("Error uploading avatar"))
                    } else {
                        callback(Result.Success(downloadUrl))
                    }
                }
            }
        })
    }

    /**
     * Function to update user profile details
     * @param userId - ID of the User
     * @param username - Username of the User
     * @param bio - Bio of the User
     * @param avatar - Avatar of the User
     */
    suspend fun updateUserDetails(userId: String, username: String, bio: String, avatar: String?): Result<Boolean> {
        return try {
            val userReference = db.document(userId)
            userReference.update(Constants.USER_NAME, username).await()
            userReference.update(Constants.USER_BIO, bio).await()
            avatar?.let { userReference.update(Constants.USER_AVATAR, it).await() }

            val dateUpdated = TimeFormatter().getNormalYear(System.currentTimeMillis())
            userReference.update(Constants.DATE_UPDATED, dateUpdated).await()

            Result.Success(true)
        } catch (e: Exception) {
            Timber.e("Error updating user details: ${e.localizedMessage}")
            Result.Error("Error updating profile")
        }

    }

    /**
     * Function to mute user - Users posts won't appear on timeline
     * @param userId - ID of the user
     */
    suspend fun muteUser(userId: String): Result<String> {
        val errorMessage = "Error muting user"

        return try {
            when (val userResult = fetchUserById(userId)) {
                is Result.Error -> Result.Error(userResult.error)

                is Result.Success -> {
                    Timber.e("Muting: ${userResult.data}")
                    val userReference = db.document(userId)
                    userReference.update(Constants.MUTED, true).await()
                    Result.Success(userResult.data.userName.toString())
                }
            }

        } catch (e: Exception) {
            Timber.e("$errorMessage: ${e.localizedMessage}")
            Result.Error(errorMessage)
        }
    }

    /**
     * Function to logout User
     */
    fun logout(): Result<Boolean> {
        return try {
            firebaseAuth.signOut()
            Result.Success(firebaseAuth.currentUser == null)
        } catch (e: Exception) {
            Timber.e("Error logging out: ${e.localizedMessage}")
            Result.Error("Error logging out")
        }
    }

    /**
     * Wrap User object in Observable
     */
    private fun getObservableUser(userId: String): Observable<User> = Observable.create<User> { emitter ->
        db.document(userId).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
                emitter.onError(firebaseFirestoreException)
            else if (documentSnapshot != null && documentSnapshot.exists())
                emitter.onNext(documentSnapshot.toObject(User::class.java)!!)
            else
                emitter.onError(Throwable("User not found"))
        }
    }
}