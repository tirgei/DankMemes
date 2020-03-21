package com.gelostech.dankmemes.data.wrappers

import com.gelostech.dankmemes.data.models.User
import io.reactivex.Observable

data class ObservableUser (
        override val id: String,
        val user: Observable<User>
): ItemViewModel {}