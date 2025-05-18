package com.exa.android.reflekt.loopit.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object ChatCryptoUtil {

    private fun generateKey(chatId: String): SecretKeySpec {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(chatId.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes.copyOf(16), "AES") // Use 128-bit AES
    }

    fun encrypt(message: String, chatId: String): String {
        val secretKey = generateKey(chatId)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedMessage: String, chatId: String): String {
        val secretKey = generateKey(chatId)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
        return String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
    }
}
