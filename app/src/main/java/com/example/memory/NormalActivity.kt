package com.example.memory

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NormalActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    private lateinit var tvCharCount: TextView
    private lateinit var btnEncode: Button
    private lateinit var btnDecode: Button
    private lateinit var tvResult: TextView

    private lateinit var etMessage: EditText


    private var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_normal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Normal Steganografi"

        imageView = findViewById(R.id.imageView)

        tvResult = findViewById(R.id.tvResult)
        btnEncode = findViewById(R.id.btnEncode)
        btnDecode = findViewById(R.id.btnDecode)
        etMessage = findViewById(R.id.etMessage)
        tvCharCount = findViewById(R.id.tvCharCount)
        val scrollView = findViewById<android.widget.ScrollView>(R.id.normalActivityScrollView)
        etMessage.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Klavye açılması için kısa bir süre bekle ve en aşağı kaydır
                scrollView.postDelayed({
                    scrollView.fullScroll(android.view.View.FOCUS_DOWN)
                }, 300)
            }
        }

        //
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            // ScrollView'un alt kısmına klavye + sistem çubuğu kadar boşluk ekle
            view.setPadding(0, 0, 0, insets.bottom)
            windowInsets
        }
        //

        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    selectedBitmap = uriToBitmap(it)
                    imageView.setImageBitmap(selectedBitmap)
                    val max = calculateMaxChars(selectedBitmap!!)
                    tvCharCount.text = "0 / $max Karakter"
                }
            }

        imageView.setOnClickListener {
            galleryLauncher.launch("image/*")
            etMessage.text.clear()
        }
        /*
                btnEncode.setOnClickListener {
                    selectedBitmap?.let { bitmap ->
                        val encoded = hideMessage(bitmap, "Halil secret messagex")
                        selectedBitmap = encoded
                        imageView.setImageBitmap(encoded)
                        saveBitmap(encoded)
                    }
                }

                */

        btnEncode.setOnClickListener {

            val message = "Memory:${etMessage.text.toString()}"

            if (message.isBlank()) {
                tvResult.text = "Mesaj boş"
                return@setOnClickListener
            }

            val bitmap = selectedBitmap
            if (bitmap == null) {
                tvResult.text = "Önce resim seç"
                return@setOnClickListener
            }

            try {
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etMessage.windowToken, 0)
                val encoded = hideMessage(bitmap, message)

                selectedBitmap = encoded
                imageView.setImageBitmap(encoded)
                saveBitmap(encoded)

                tvResult.text = "Mesaj başarıyla gizlendi"

            } catch (e: Exception) {
                tvResult.text = e.message ?: "Hata oluştu"
            }
        }



        btnDecode.setOnClickListener {
            selectedBitmap?.let { bitmap ->
                val decoded = extractMessage(bitmap)
                val clean = if (decoded.startsWith("Memory:")) decoded.substring(7) else decoded
                tvResult.text = clean
            }
        }

// Yazı değişimini takip eden TextWatcher
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentBitmap = selectedBitmap
                if (currentBitmap != null) {
                    val maxChars = calculateMaxChars(currentBitmap)
                    val currentChars = s?.length ?: 0

                    // "Memory:" prefix dahil gerçek boyut
                    val realSize = "Memory:$s".toByteArray(Charsets.UTF_8).size

                    tvCharCount.text = "$currentChars / $maxChars Karakter"

                    if (realSize > maxChars) {
                        tvCharCount.setTextColor(android.graphics.Color.RED)
                        btnEncode.isEnabled = false
                    } else {
                        tvCharCount.setTextColor(android.graphics.Color.WHITE)
                        btnEncode.isEnabled = true
                    }
                } else {
                    tvCharCount.text = "Önce resim seçiniz"
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        etMessage.addTextChangedListener(textWatcher)
        //

    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    private fun calculateMaxChars(bitmap: Bitmap): Int {
        return ((bitmap.width * bitmap.height * 3)/8)- 50
    }
    private fun uriToBitmap(uri: Uri): Bitmap {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun hideMessage(bitmap: Bitmap, message: String): Bitmap {

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val messageBytes = message.toByteArray(Charsets.UTF_8)
        val messageLength = messageBytes.size

        val lengthBytes = ByteArray(4)
        lengthBytes[0] = (messageLength shr 24).toByte()
        lengthBytes[1] = (messageLength shr 16).toByte()
        lengthBytes[2] = (messageLength shr 8).toByte()
        lengthBytes[3] = messageLength.toByte()

        val fullMessage = lengthBytes + messageBytes
        val totalBits = fullMessage.size * 8

        val width = mutableBitmap.width
        val height = mutableBitmap.height
        val capacity = width * height * 3

        if (totalBits > capacity) {
            throw IllegalArgumentException("Message too large for this image")
        }

        val pixels = IntArray(width * height)
        mutableBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var bitIndex = 0

        for (i in pixels.indices) {

            if (bitIndex >= totalBits) break

            var pixel = pixels[i]

            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF

            for (channel in 0..2) {

                if (bitIndex >= totalBits) break

                val byteIndex = bitIndex / 8
                val bitInByte = 7 - (bitIndex % 8)
                val bit = (fullMessage[byteIndex].toInt() shr bitInByte) and 1

                when (channel) {
                    0 -> r = (r and 0xFE) or bit
                    1 -> g = (g and 0xFE) or bit
                    2 -> b = (b and 0xFE) or bit
                }

                bitIndex++
            }

            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        mutableBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return mutableBitmap
    }

    fun extractMessage(bitmap: Bitmap): String {

        val width = bitmap.width
        val height = bitmap.height

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val bits = mutableListOf<Int>()

        for (pixel in pixels) {
            bits.add((pixel shr 16) and 1)
            bits.add((pixel shr 8) and 1)
            bits.add(pixel and 1)
        }

        var messageLength = 0
        for (i in 0 until 32) {
            messageLength = (messageLength shl 1) or bits[i]
        }

        val messageBitsStart = 32
        val messageBitsEnd = messageBitsStart + messageLength * 8

        if (messageBitsEnd > bits.size) return ""

        val messageBytes = ByteArray(messageLength)

        var byteIndex = 0
        var bitCounter = 0
        var currentByte = 0

        for (i in messageBitsStart until messageBitsEnd) {
            currentByte = (currentByte shl 1) or bits[i]
            bitCounter++

            if (bitCounter == 8) {
                messageBytes[byteIndex] = currentByte.toByte()
                byteIndex++
                bitCounter = 0
                currentByte = 0
            }
        }

        return String(messageBytes, Charsets.UTF_8)
    }

    private fun saveBitmap(bitmap: Bitmap) {

        val filename = "encoded_${System.currentTimeMillis()}.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Memory")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let {
            contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
    }
}
