package com.example.memory

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class SecureActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var messageInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var encodeBtn: Button
    private lateinit var decodeBtn: Button
    private lateinit var secureTvCharCount: TextView
    private lateinit var tvResult: TextView
    private lateinit var mainLayout: LinearLayout

    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_secure)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Şifreli Steganografi"

        imageView = findViewById(R.id.secureimageView)
        messageInput = findViewById(R.id.securemessageInput)
        passwordInput = findViewById(R.id.securepasswordInput)
        encodeBtn = findViewById(R.id.secureencodeBtn)
        decodeBtn = findViewById(R.id.securedecodeBtn)
        secureTvCharCount = findViewById(R.id.secureTvCharCount)
        tvResult = findViewById(R.id.secureTvResult)
        mainLayout = findViewById(R.id.secureMainLayout)
        val scrollView = findViewById<ScrollView>(R.id.secureActivityScrollView)

        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(0, 0, 0, insets.bottom)
            windowInsets
        }

        val focusListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, v.bottom + 150)
                }, 300)
            }
        }
        messageInput.onFocusChangeListener = focusListener
        passwordInput.onFocusChangeListener = focusListener

        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedBitmap = uriToBitmap(it)
                imageView.setImageBitmap(selectedBitmap)
                val max = calculateMaxChars(selectedBitmap!!)
                secureTvCharCount.text = "0 / $max Karakter"
                mainLayout.requestFocus()
            }
        }

        imageView.setOnClickListener {
            mainLayout.requestFocus()
            galleryLauncher.launch("image/*")
            messageInput.text.clear()
            passwordInput.text.clear()
        }

        // ŞİFRELE VE GİZLE
        encodeBtn.setOnClickListener {
            val msg = "Memory:${messageInput.text}"
            val pass = passwordInput.text.toString()
            val bitmap = selectedBitmap ?: run {
                Toast.makeText(this, "Önce resim seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (messageInput.text.isBlank() || pass.isBlank()) {
                Toast.makeText(this, "Mesaj ve şifre gerekli", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(messageInput.windowToken, 0)
                mainLayout.requestFocus()

                val encrypted = encryptAES(msg, pass)
                val encodedString = Base64.encodeToString(encrypted, Base64.DEFAULT)
                val encodedBitmap = hideMessage(bitmap, encodedString)

                selectedBitmap = encodedBitmap
                imageView.setImageBitmap(encodedBitmap)
                saveBitmap(encodedBitmap)

                Toast.makeText(this, "Şifrelendi ve Gizlendi", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // ÇÖZ
        decodeBtn.setOnClickListener {
            val pass = passwordInput.text.toString()
            val bitmap = selectedBitmap ?: return@setOnClickListener

            if (pass.isBlank()) {
                Toast.makeText(this, "Şifre giriniz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val extracted = extractMessage(bitmap)
                val bytes = Base64.decode(extracted, Base64.DEFAULT)
                val plain = decryptAES(bytes, pass)

                if (plain.startsWith("Memory:")) {
                    tvResult.text = plain.substring(7)
                } else {
                    tvResult.text = plain
                }

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(passwordInput.windowToken, 0)
                mainLayout.requestFocus()

                Toast.makeText(this, "Mesaj Çözüldü", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                tvResult.text = "Şifre yanlış veya veri bozuk"
            }
        }

        // CANLI SAYAÇ
        messageInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectedBitmap?.let {
                    val max = calculateMaxChars(it)
                    val current = s?.length ?: 0

                    // AES + Base64 overhead hesabı
                    val msgWithPrefix = "Memory:$s"
                    val aesOverhead = 16 + 16 + 16 // salt + iv + padding
                    val aesSize = msgWithPrefix.toByteArray().size + aesOverhead
                    val base64Size = ((aesSize + 2) / 3) * 4 // Base64 formülü

                    secureTvCharCount.text = "$current / $max Karakter"

                    if (base64Size > max) {
                        secureTvCharCount.setTextColor(android.graphics.Color.RED)
                        encodeBtn.isEnabled = false
                    } else {
                        secureTvCharCount.setTextColor(android.graphics.Color.WHITE)
                        encodeBtn.isEnabled = true
                    }
                } ?: run {
                    secureTvCharCount.text = "Önce resim seçiniz"
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun calculateMaxChars(bitmap: Bitmap): Int {
        return ((bitmap.width * bitmap.height * 3) / 8) - 50
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { d, _, _ -> d.isMutableRequired = true }
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 60000, 256)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    private fun encryptAES(message: String, password: String): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16).apply { random.nextBytes(this) }
        val iv = ByteArray(16).apply { random.nextBytes(this) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        return salt + iv + cipher.doFinal(message.toByteArray(Charsets.UTF_8))
    }

    private fun decryptAES(data: ByteArray, password: String): String {
        val salt = data.copyOfRange(0, 16)
        val iv = data.copyOfRange(16, 32)
        val cipherText = data.copyOfRange(32, data.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    fun hideMessage(bitmap: Bitmap, message: String): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val msgBytes = message.toByteArray()
        val len = msgBytes.size
        val full = byteArrayOf((len shr 24).toByte(), (len shr 16).toByte(), (len shr 8).toByte(), len.toByte()) + msgBytes
        val totalBits = full.size * 8
        val pixels = IntArray(mutableBitmap.width * mutableBitmap.height)
        mutableBitmap.getPixels(pixels, 0, mutableBitmap.width, 0, 0, mutableBitmap.width, mutableBitmap.height)
        var bitIndex = 0
        for (i in pixels.indices) {
            if (bitIndex >= totalBits) break
            var p = pixels[i]
            var r = (p shr 16) and 0xFF
            var g = (p shr 8) and 0xFF
            var b = p and 0xFF
            for (c in 0..2) {
                if (bitIndex >= totalBits) break
                val bit = (full[bitIndex / 8].toInt() shr (7 - (bitIndex % 8))) and 1
                when (c) {
                    0 -> r = (r and 0xFE) or bit
                    1 -> g = (g and 0xFE) or bit
                    2 -> b = (b and 0xFE) or bit
                }
                bitIndex++
            }
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        mutableBitmap.setPixels(pixels, 0, mutableBitmap.width, 0, 0, mutableBitmap.width, mutableBitmap.height)
        return mutableBitmap
    }

    fun extractMessage(bitmap: Bitmap): String {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val bits = mutableListOf<Int>()
        for (p in pixels) {
            bits.add((p shr 16) and 1); bits.add((p shr 8) and 1); bits.add(p and 1)
        }
        var len = 0
        for (i in 0 until 32) len = (len shl 1) or bits[i]
        val bytes = ByteArray(len)
        for (i in 0 until len) {
            var curr = 0
            for (j in 0 until 8) curr = (curr shl 1) or bits[32 + i * 8 + j]
            bytes[i] = curr.toByte()
        }
        return String(bytes)
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val cv = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "secure_${System.currentTimeMillis()}.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Memory")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}