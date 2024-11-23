package com.train.ramarai.postest.utils

import android.app.AlertDialog
import android.content.Context
import com.train.ramarai.postest.R

class DialogUtils(private val context: Context) {
    fun dialogSuccess(title: String, message: String, positiveButtonAction: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _ ->
                positiveButtonAction.invoke()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    fun dialogError(title: String, message: String?) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(context.getString(R.string.try_again)) { _, _ ->
                // Retry action if needed
            }
            setCancelable(false)
            create()
            show()
        }
    }

    fun dialogDefault(title: String, message: String, positiveButtonAction: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Ya") {_,_ ->
                positiveButtonAction.invoke()
            }
            setNegativeButton("Tidak") { _, _ ->

            }
            create()
            show()
        }
    }
}