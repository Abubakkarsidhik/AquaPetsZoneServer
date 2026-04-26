package com.aquapetszone.kmp.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
    fun hash(password: String?): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verify(password: String?, hash: String?): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}
