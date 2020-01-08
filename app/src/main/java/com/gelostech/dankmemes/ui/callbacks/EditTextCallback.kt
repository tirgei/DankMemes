package com.gelostech.dankmemes.ui.callbacks

import android.text.Editable
import android.text.TextWatcher

interface EditTextCallback {
    fun onTextChanged(text: String)
}

class EditTextListener(private val callback: EditTextCallback): TextWatcher {
    override fun afterTextChanged(p0: Editable?) {
        callback.onTextChanged(p0.toString())
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Not in use
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Not in use
    }
}