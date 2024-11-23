package com.train.ramarai.postest.utils

import android.content.Context
import android.content.Intent
import com.train.ramarai.postest.ui.auth.login.LoginActivity
import com.train.ramarai.postest.ui.auth.register.RegisterActivity
import com.train.ramarai.postest.ui.main.MainActivity

class Utils {

    fun redirectToMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }


    fun toRegister(context: Context) {
        val intent = Intent(context, RegisterActivity::class.java)
        context.startActivity(intent)
    }

    fun redirectToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

}