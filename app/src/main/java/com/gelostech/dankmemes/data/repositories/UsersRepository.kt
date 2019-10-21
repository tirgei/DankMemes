package com.gelostech.dankmemes.data.repositories

import android.net.Uri
import com.gelostech.dankmemes.data.Result
import com.gelostech.dankmemes.data.models.User
import com.gelostech.dankmemes.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class UsersRepository constructor(private val firebaseDatabase: DatabaseReference,
                                  private val firebaseAuth: FirebaseAuth,
                                  private val storageReference: StorageReference) {

    private val db = firebaseDatabase.child(Constants.USERS)

    suspend fun linkAnonymousUserToCredentials(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)
        val authResult = firebaseAuth.currentUser!!.linkWithCredential(credential)
        onResult(handleRegisterUser(authResult))
    }

    suspend fun registerUser(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
        onResult(handleRegisterUser(authResult))
    }

    private suspend fun handleRegisterUser(authResult: Task<AuthResult>): Result<FirebaseUser> {
        val result = authResult.await()

        return try {
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

    suspend fun loginWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()

        return try {
            val user = result.user!!
             Result.Success(user)
        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
            Result.Error("Email or Password incorrect")
        } catch (userNull: FirebaseAuthInvalidUserException) {
            (Result.Error("Account not found. Have you signed up?"))
        } catch (e: Exception) {
            Result.Error("Error signing in. Please try again")
        }
    }

    suspend fun loginWithGoogle(account: GoogleSignInAccount,
                                onResult: (Result<FirebaseUser>) -> Unit,
                                newUserResult: (Result<Boolean>) -> Unit?) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser!!

                        onResult(Result.Success(task.result?.user!!))
                        newUserResult(Result.Success(isNew))
                    } else {
                        onResult(Result.Error("Error signing in. Please try again"))
                    }
                }
    }

    suspend fun fetchUserById(userId: String, onResult: (Result<User>) -> Unit) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                onResult(Result.Error("Error fetching user details"))
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                    onResult(Result.Success(p0.getValue(User::class.java)!!))
                else
                    onResult(Result.Error("User not found"))
            }
        })
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