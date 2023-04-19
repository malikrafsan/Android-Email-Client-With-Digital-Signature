package com.fsck.k9.custom_encrypt.key_store

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.InputStream
import java.io.OutputStream

class CryptoManager {
    val privateKeyPath: String = "private_key.txt"
    val publicKeyPath: String = "public_key.txt"

    fun store(str: String, outputStream: OutputStream): ByteArray {
        val bytes = str.toByteArray()
        outputStream.use {
            it.write(bytes)
        }
        return bytes
    }

    fun retrieve(inputStream: InputStream): String {
        return inputStream.use {
            val bytes = ByteArray(it.available())
            it.read(bytes)
            String(bytes)
        }
    }
}
