package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.data.responses.GoogleLoginResponse
import com.gelostech.dankmemes.utils.Constants
import com.gelostech.dankmemes.utils.get
import com.gelostech.dankmemes.utils.execute
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UsersRepository constructor(private val firebaseDatabase: DatabaseReference,
                                  private val firebaseAuth: FirebaseAuth,
                                  private val storageReference: StorageReference) {

    private val db = firebaseDatabase.child(Constants.USERS)

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

    suspend fun createUserAccount(avatarUri: Uri, user: User, onResult: (Result<User>) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(user.userId!!)
        val errorMessage = "Error signing up. Please try again"

        val uploadTask = storageDb.putFile(avatarUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw Exception(errorMessage)
            }

            // Continue with the task to getBitmap the download URL
            storageDb.downloadUrl

        }
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.apply {
                    this.userAvatar = task.result.toString()
                }

                firebaseDatabase.child(Constants.USERS)
                        .child(user.userId!!)
                        .setValue(user)
                        .addOnSuccessListener {
                            onResult(Result.Success(user))
                        }
                        .addOnFailureListener {
                            onResult(Result.Error(errorMessage))
                        }
            } else {
                onResult(Result.Error(errorMessage))
            }
        }
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
            val createUser = db.child(user.userId!!).setValue(user)
                    .execute()
                    .await()

            if (createUser)
                Result.Success(user)
            else
                Result.Error(errorMessage)
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
            val result = firebaseAuth.sendPasswordResetEmail(email).execute().await()

            if (result)
                Result.Success(true)
            else
                Result.Error(errorMessage)

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
        val user = db.child(userId).get().await()

        return try {
            Result.Success(user.getValue(User::class.java)!!)
        } catch (e: java.lang.Exception) {
            Timber.e("Error fetching User: ${e.localizedMessage}")
            Result.Error("User not found")
        }
    }

    suspend fun updateUserAvatar(userId: String, avatarUri: Uri, onResult: (Result<String>) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(userId)

        val uploadTask = storageDb.putFile(avatarUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                onResult(Result.Error("Error updating profile details"))
            }

            // Continue with the task to get the download URL
            storageDb.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful)
                onResult(Result.Success(task.result.toString()))
            else
                onResult(Result.Error("Error fetching user details"))
        }
    }

    suspend fun updateUserDetails(userId: String, username: String, bio: String, avatar: String?, onResult: (Result<Boolean>) -> Unit) {
        val userReference = db.child(userId)
        userReference.child(Constants.USER_NAME).setValue(username)
        userReference.child(Constants.USER_BIO).setValue(bio)
        avatar?.let { userReference.child(Constants.USER_AVATAR).setValue(it) }

        onResult(Result.Success(true))
    }

}