package com.train.ramarai.postest.ui.auth.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.train.ramarai.postest.R

class EmailEditText : TextInputEditText {
    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setup()
    }

    private fun setup() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }

        })
    }

    private fun validateEmail(email: String) {
        val pattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
        val isValid = pattern.matches(email)
        if (!isValid) showError() else removeError()
    }

    private fun removeError() {
        this.error = null
    }

    private fun showError() {
        this.error = context.getString(R.string.invalid_email)
    }
}