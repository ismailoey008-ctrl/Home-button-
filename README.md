# Home Circle ⭕

Aplikasi Android pengganti tombol Home fisik/layar yang **ringan**, **elegan**, dan **sangat responsif**, dirancang khusus untuk Android 8+ (Oreo hingga versi terbaru).

## ✨ Fitur Utama

- 🏠 **Fungsi Tombol Home Cepat:** Ketukan tunggal langsung membawa ke layar utama tanpa lag.
- 🎯 **Desain Murni Lingkaran:** Tampilan minimalis bersih tanpa ikon rumit:
  - *Ring* (Cincin Modern)
  - *Dot* (Lingkaran dengan Titik Pusat / gaya AssistiveTouch)
  - *Solid* (Lingkaran Penuh)
  - *Double Ring* (Cincin Ganda)
- 🎚️ **Pengaturan Transparansi (Opacity):**
  - Transparansi saat disentuh (Active Opacity: 10% - 100%)
  - Transparansi saat diam (Idle Opacity: 10% - 100%)
  - Otomatis memudar (*auto-fade*) setelah 3 detik tidak disentuh agar tidak menutupi konten layar.
- ⚡ **Sangat Ringan & Hemat Memori:**
  - Menggunakan overlay Canvas perangkat keras kustom (*hardware-accelerated*), bukan view hierarchy yang berat.
  - Ukuran file instalasi sangat kecil dan efisien baterai.
- 🧲 **Responsif & Gestur Cerdas:**
  - *Snap to Edge:* Menempel otomatis dan mulus ke tepi layar kiri/kanan setelah digeser.
  - Umpan balik getaran halus (*haptic feedback*) saat disentuh.
  - Dukungan aksi opsional untuk Ketuk Ganda (*Double Tap*) dan Tekan Lama (*Long Press*): Recent Apps, Kunci Layar, Tarik Notifikasi, atau Buka Pengaturan.
- 📱 **Kompatibel Android 8+:**
  - Menggunakan `TYPE_APPLICATION_OVERLAY` dan `NotificationChannel`.
  - Dilengkapi fitur **Quick Settings Tile** di panel notifikasi untuk mengaktifkan/menonaktifkan tombol dengan cepat.

## 🛠️ Teknologi yang Digunakan

- **Bahasa:** Kotlin
- **UI:** Jetpack Compose (Material 3) untuk pengaturan & Custom Hardware-Accelerated View untuk floating overlay
- **Penyimpanan Lokal:** Room Database & Shared Preferences
- **Layanan Latar Belakang:** Android Foreground Service & Optional Accessibility Service
- **Arsitektur:** MVVM (Model-View-ViewModel) + Repository Pattern

## 🚀 Cara Menjalankan

1. Buka proyek ini di **Android Studio** (Koala / Ladybug atau versi terbaru).
2. Sinkronkan Gradle (`Sync Project with Gradle Files`).
3. Jalankan aplikasi pada emulator atau perangkat fisik (Android 8.0 / API 26 ke atas).
4. Berikan izin **Tampilkan di Atas Aplikasi Lain** (*Display Over Other Apps*).
