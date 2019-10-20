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

class UsersRepository constructor(private val firebaseDatabase: DatabaseReference,
                                  private val firebaseAuth: FirebaseAuth,
                                  private val storageReference: StorageReference) {

    private val db = firebaseDatabase.child(Constants.USERS)

    fun linkAnonymousUserToCredentials(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)
        val authResult = firebaseAuth.currentUser!!.linkWithCredential(credential)
        handleRegisterUser(authResult) { result -> onResult(result) }
    }

    fun registerUser(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
        handleRegisterUser(authResult) { result -> onResult(result) }
    }

    private fun handleRegisterUser(authResult: Task<AuthResult>, onResult: (Result<FirebaseUser>) -> Unit) {
        authResult.addOnCompleteListener { task ->
            when (task.isSuccessful) {
                true -> onResult(Result.Success(task.result?.user!!))

                else -> {
                    try {
                        throw task.exception!!
                    } catch (weakPassword: FirebaseAuthWeakPasswordException){
                        onResult(Result.Error("Please enter a stronger password"))
                    } catch (userExists: FirebaseAuthUserCollisionException) {
                        onResult(Result.Error("Account already exists. Please log in"))
                    } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {
                        onResult(Result.Error("Incorrect email format"))
                    } catch (e: Exception) {
                        onResult(Result.Error("Error signing up. Please try again"))
                    }
                }
            }
        }
    }

    fun createUserAccount(avatarUri: Uri, user: User, onResult: (Result<User>) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(user.userId!!)

        val uploadTask = storageDb.putFile(avatarUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                onResult(Result.Error("Error signing up. Please try again"))
            }

            // Continue with the task to getBitmap the download URL
            storageDb.downloadUrl

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                user.apply {
                    this.userAvatar = task.result.toString()
                }

                firebaseDatabase.child(Constants.USERS)
                        .child(user.userId!!)
                        .setValue(user)
                        .addOnCompleteListener { createAccountTask ->
                            if (createAccountTask.isSuccessful) onResult(Result.Success(user))
                            else onResult(Result.Error("Error signing up. Please try again"))
                        }
            } else {
                onResult(Result.Error("Error signing up. Please try again"))
            }
        }
    }

    fun loginWithEmailAndPassword(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onResult(Result.Success(task.result?.user!!))
                    } else {
                        try {
                            throw task.exception!!
                        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {
                            onResult(Result.Error("Email or Password incorrect"))
                        } catch (userNull: FirebaseAuthInvalidUserException) {
                            onResult(Result.Error("Account not found. Have you signed up?"))
                        } catch (e: Exception) {
                            onResult(Result.Error("Error signing in. Please try again"))
                        }
                    }
                }
    }

    fun loginWithGoogle(account: GoogleSignInAccount, onResult: (Result<FirebaseUser>) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser!!

                        onResult(Result.Success(task.result?.user!!))

                    } else {
                        onResult(Result.Error("Error signing in. Please try again"))
                    }
                }
    }

    fun fetchUserById(userId: String, onResult: (Result<User>) -> Unit) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                onResult(Result.Error("Error fetching user details"))
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists())
                    onResult(Result.Success(p0.getValue(User::class.java)!!))
                else
                    onResult(Result.Error("Error fetching user details"))
            }
        })
    }

    fun updateUserAvatar(userId: String, avatarUri: Uri, onResult: (Result<String>) -> Unit) {
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

    fun updateUserDetails(userId: String, username: String, bio: String, avatar: String?, onResult: (Result<Boolean>) -> Unit) {
        val userReference = db.child(userId)
        userReference.child(Constants.USER_NAME).setValue(username)
        userReference.child(Constants.USER_BIO).setValue(bio)
        avatar?.let { userReference.child(Constants.USER_AVATAR).setValue(it) }

        onResult(Result.Success(true))
    }

}