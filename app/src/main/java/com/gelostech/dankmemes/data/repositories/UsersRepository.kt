package com.gelostech.dankmemes.data.repositories

import android.net.Uri
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

    fun linkAnonymousUserToCredentials(email: String, password: String, onSuccess: (FirebaseUser) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email, password)
        val authResult = firebaseAuth.currentUser!!.linkWithCredential(credential)
        handleRegisterUser(authResult) { firebaseUser -> onSuccess(firebaseUser) }
    }

    fun registerUser(email: String, password: String, onSuccess: (FirebaseUser) -> Unit) {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
        handleRegisterUser(authResult) { firebaseUser -> onSuccess(firebaseUser) }
    }

    private fun handleRegisterUser(authResult: Task<AuthResult>, onSuccess: (FirebaseUser) -> Unit) {
        authResult.addOnCompleteListener { task ->
            when (task.isSuccessful) {
                true -> onSuccess(task.result?.user!!)

                else -> {
                    try {
                        throw task.exception!!
                    } catch (weakPassword: FirebaseAuthWeakPasswordException){

                    } catch (userExists: FirebaseAuthUserCollisionException) {

                    } catch (malformedEmail: FirebaseAuthInvalidCredentialsException) {

                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    fun createUserAccount(avatarUri: Uri, user: User, onSuccess: (User) -> Unit) {
        val storageDb = storageReference.child(Constants.AVATARS).child(user.userId!!)

        val uploadTask = storageDb.putFile(avatarUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
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
                            if (createAccountTask.isSuccessful) onSuccess(user)
                        }
            } else {

            }
        }
    }

    fun loginWithEmailAndPassword(email: String, password: String, onSuccess: (FirebaseUser) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccess(task.result?.user!!)
                    } else {
                        try {
                            throw task.exception!!
                        } catch (wrongPassword: FirebaseAuthInvalidCredentialsException) {


                        } catch (userNull: FirebaseAuthInvalidUserException) {


                        } catch (e: Exception) {

                        }
                    }
                }
    }

    fun loginWithGoogle(account: GoogleSignInAccount, onSuccess: (FirebaseUser) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser!!

                        onSuccess(task.result?.user!!)

                    } else {

                    }
                }
    }

    fun fetchUserById(userId: String, onSuccess: (User) -> Unit) {
        db.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                onSuccess(p0.getValue(User::class.java)!!)
            }
        })
    }

}