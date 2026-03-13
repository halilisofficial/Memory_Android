# \# 📸 Memory - Secure Image Steganography

# 

# \*\*Memory\*\*, Android platformu için geliştirilmiş, mesajları görsellerin içine gizlemek amacıyla kullanılan gelişmiş bir steganografi uygulamasıdır. Klasik veri gizleme yöntemlerini \*\*AES-256\*\* şifreleme ile birleştirerek çift katmanlı bir güvenlik sunar.

# 

# ---

# 

# \## 🚀 Özellikler

# 

# \- \*\*Çift Katmanlı Güvenlik:\*\* Mesajları gizlemeden önce AES-256 (PBKDF2) ile şifreleme imkanı.

# \- \*\*LSB (Least Significant Bit) Tekniği:\*\* Verileri görselin en önemsiz bitlerine yerleştirerek görsel kalitesini bozmadan gizleme.

# \- \*\*Dinamik Kapasite Hesaplama:\*\* Seçilen görselin çözünürlüğüne göre saklanabilecek maksimum karakter sayısını anlık hesaplama.

# \- \*\*Modern UI/UX:\*\* Kotlin ile geliştirilmiş, kullanıcı odaklı ve odaklama (focus) sorunları giderilmiş akıcı arayüz.

# \- \*\*Edge-to-Edge Desteği:\*\* Modern Android sürümleriyle tam uyumlu tam ekran deneyimi.

# 

# ---

# 

# \## 🛠 Kullanılan Teknolojiler \& Kütüphaneler

# 

# \- \*\*Dil:\*\* \[Kotlin](https://kotlinlang.org/)

# \- \*\*Güvenlik:\*\* AES (Advanced Encryption Standard), PBKDF2 (Password-Based Key Derivation Function 2)

# \- \*\*Görüntü İşleme:\*\* Bitwise Pixel Manipulation, ImageDecoder (Android P+)

# \- \*\*UI:\*\* XML, ViewCompat (Window Insets API)

# \- \*\*Depolama:\*\* MediaStore API (Görselleri galeriye kaydetme)

# 

# ---

# 

# \## 📖 Kullanım

# 

# \### 1. Normal Steganografi

# \- Görseli seçin.

# \- Gizlemek istediğiniz mesajı yazın.

# \- "Gizle" butonuna basın. Uygulama mesajı doğrudan piksel verilerine işleyecektir.

# 

# \### 2. Şifreli Steganografi (Memory Modu)

# \- Görseli seçin.

# \- Bir \*\*AES Şifresi\*\* belirleyin.

# \- Mesajınızı yazın. Uygulama önce mesajı şifreleyecek, ardından görselin içine gömecektir. 

# \- \*Not: Şifre olmadan mesajın varlığı tespit edilse bile içeriği okunamaz.\*

# 

# ---

# 

# \## 🏗 Kurulum

# 

# Projeyi yerel makinenizde çalıştırmak için şu adımları izleyin:

# 

# 1\. Bu depoyu klonlayın:

# &nbsp;  ```bash

# &nbsp;  git clone \[https://github.com/halilisofficial/Memory\_Android.git](https://github.com/halilisofficial/Memory\_Android.git)

