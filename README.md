<div align="center">

# SkyLens
### A focused, native Android camera experience

[![Android CI](https://github.com/sky-pixel-Sp/SkyLens/actions/workflows/android.yml/badge.svg)](https://github.com/sky-pixel-Sp/SkyLens/actions/workflows/android.yml)

**Kotlin · Jetpack Compose · CameraX · Camera2**

[English](#english) · [Bahasa Indonesia](#bahasa-indonesia)

</div>

---

<a id="english"></a>
## 🇬🇧 English

SkyLens is a native Android camera application built with Kotlin, Jetpack Compose, and CameraX. It focuses on a clean camera workflow, practical manual controls, a built-in capture gallery, and transparent reporting of camera hardware capabilities.

> **Project status — v0.1.2 (early MVP):** this repository contains the Android source code. The installable debug APK is published as a prerelease asset when the latest main-branch CI build passes. This is a testing build, not a production-signed Play Store release. Real-device validation is still required.

### What works in this version

| Feature | Current implementation |
|---|---|
| Live camera preview | CameraX preview with rear/front switching |
| Photo capture | JPEG capture using CameraX's maximum-quality mode |
| Video capture | MP4 recording; FHD is preferred, with an HD fallback |
| Motion Capture | A still JPEG paired with a separate, silent MP4 clip |
| Focus | Tap the preview to trigger focus and metering |
| Zoom | Quick 1×, 2× and 3× controls, limited by the camera's reported zoom range |
| Flash | CameraX flash toggle for still capture |
| PRO controls | Exposure compensation control |
| Camera information | Camera2 metadata for JPEG output size, RAW, manual sensor, OIS, video stabilization and constrained high-speed FPS ranges |
| Gallery | Browse SkyLens photos and open a paired motion clip |
| App language | English and Bahasa Indonesia, switchable in the app and saved on the device |

### Important limitations — please read

SkyLens reports hardware capabilities separately from features that are actually implemented. This avoids presenting an advertised or detected capability as a working camera mode.

- **Motion Capture is not Apple Live Photos.** SkyLens currently saves two separate files with the same filename: a `.jpg` still in `Pictures/SkyLens` and a silent `.mp4` companion in `Movies/SkyLens`.
- **Motion Capture does not use a rolling pre-capture buffer yet.** Recording begins when LIVE mode is armed and stops approximately 1.1 seconds after the still-photo callback. The amount of footage before the shutter therefore depends on how long LIVE mode was active.
- **120/240 fps is not an active recording mode yet.** The information panel only reports constrained high-speed ranges exposed by Camera2. A dedicated high-speed recording pipeline has not been connected to the camera controls.
- **RAW/DNG, Ultra HDR, computational Night mode, Portrait depth processing, AI retouch, and object removal are not implemented in this MVP.** A hardware capability report does not mean that the corresponding capture pipeline exists in SkyLens.
- Video resolution, frame rate, stabilization, dynamic range, and image quality depend on the phone's camera hardware, Android camera implementation, lighting, and thermal conditions.
- Video and Motion Capture are currently **silent**. The app does not request microphone permission.

### Language support

The interface supports:

- **EN** — English
- **ID** — Bahasa Indonesia

Use the `EN` / `ID` control in the camera header to switch languages. The selection is saved locally and restored the next time SkyLens opens. On first launch, SkyLens uses the Android device language when it is Indonesian; otherwise it starts in English. The language switch is also available on the camera-permission screen.

### Requirements

- Android 10 (API 29) or newer
- A device with a camera supported by CameraX
- Android SDK Platform 36 for building this project
- JDK 17 or newer

### Build the debug APK

**Recommended: Android Studio**

1. Install Android Studio and JDK 17 or newer.
2. Install Android SDK Platform 36 from **Tools → SDK Manager**.
3. Open the cloned `SkyLens` folder in Android Studio.
4. Allow Gradle sync to finish.
5. Select the `app` run configuration and run it on a physical Android device.

**Windows command line**

After installing Android SDK Platform 36 and JDK 17 or newer, open a terminal in the project root and run:

```powershell
.\build-debug.bat
```

The script downloads the official Gradle 8.13 distribution into the current user's local SkyLens cache when needed. Set `ANDROID_SDK_ROOT` if your Android SDK is installed in a non-default location.

**Linux / macOS**

With Gradle 8.13 installed and `ANDROID_SDK_ROOT` configured:

```bash
gradle --no-daemon assembleDebug
```

After a successful build, the APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Automated checks

GitHub Actions (`.github/workflows/android.yml`) is configured to:

1. Build the debug APK using JDK 17 and Gradle 8.13.
2. Run JVM unit tests.
3. Run Android Lint.
4. Upload the debug APK as the `skylens-debug-apk` workflow artifact (14-day retention).

A workflow being configured does not mean it has passed. Check the **Actions** tab for the result of the latest run before downloading or installing an APK.

### First device-test checklist

- [ ] Grant camera permission and confirm that the preview opens without crashing.
- [ ] Switch between rear and front cameras several times.
- [ ] Take a photo and verify it appears in `Pictures/SkyLens` and the in-app gallery.
- [ ] Record a normal video, stop it, and verify playback in `Movies/SkyLens`.
- [ ] Test LIVE mode and confirm that both the still JPEG and companion MP4 are saved.
- [ ] Open both files using an external media viewer.
- [ ] Test tap-to-focus, zoom, flash, and exposure compensation.
- [ ] Switch between English and Bahasa Indonesia, close and reopen the app, and confirm the language choice is retained.
- [ ] Open the camera capability panel and compare its report with actual device behavior.
- [ ] Repeat tests after denying permission, backgrounding the app, and restarting the device.
- [ ] Check stability and thermal behavior over repeated captures before wider distribution.

### Project structure

```text
SkyLens/
├── .github/workflows/android.yml
├── app/
│   ├── src/main/
│   │   ├── java/id/sky/skylens/
│   │   │   ├── MainActivity.kt
│   │   │   └── camera/
│   │   │       ├── CameraCapabilities.kt
│   │   │       └── MediaStoreRepository.kt
│   │   ├── res/values/strings.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── build-debug.bat
├── build-debug.ps1
├── CHANGELOG.md
└── README.md
```

### Privacy and permissions

SkyLens currently requests the Android **Camera** permission to show the preview and capture images or video. It does not request microphone permission, and recordings are silent. Captures are saved to the device's shared media storage under the `Pictures/SkyLens` and `Movies/SkyLens` folders. The app has no account sign-in or cloud-upload feature in this MVP.

### Contributing and issue reports

Bug reports are most useful when they include the phone model, Android version, SkyLens version, exact steps to reproduce, expected behavior, actual behavior, and relevant logs. Do not attach private photos or videos unless they are necessary and you are comfortable sharing them.

---

<a id="bahasa-indonesia"></a>
## 🇮🇩 Bahasa Indonesia

SkyLens adalah aplikasi kamera Android native yang dibuat menggunakan Kotlin, Jetpack Compose, dan CameraX. Fokusnya adalah alur kamera yang bersih, kontrol manual yang praktis, galeri hasil foto, serta informasi kemampuan kamera perangkat yang ditampilkan secara transparan.

> **Status proyek — v0.1.2 (MVP tahap awal):** repositori ini berisi source code Android. APK debug yang dapat dipasang diterbitkan sebagai aset prerelease ketika build CI branch main berhasil. Ini merupakan build pengujian, bukan APK produksi yang ditandatangani untuk Play Store. Pengujian langsung di perangkat tetap diperlukan.

### Fitur yang sudah tersedia

| Fitur | Implementasi saat ini |
|---|---|
| Pratinjau kamera | Preview CameraX dengan pergantian kamera belakang/depan |
| Pengambilan foto | Foto JPEG dengan mode kualitas maksimum CameraX |
| Perekaman video | Video MP4; FHD diprioritaskan dengan fallback ke HD |
| Motion Capture | Foto JPEG yang dipasangkan dengan file MP4 terpisah tanpa suara |
| Fokus | Ketuk area preview untuk menjalankan fokus dan metering |
| Zoom | Kontrol cepat 1×, 2×, dan 3× sesuai batas zoom yang dilaporkan kamera |
| Lampu kilat | Pengaturan flash CameraX untuk pengambilan foto |
| Kontrol PRO | Pengaturan kompensasi pencahayaan |
| Informasi kamera | Metadata Camera2 untuk ukuran JPEG, RAW, sensor manual, OIS, stabilisasi video, dan rentang FPS high-speed |
| Galeri | Melihat foto SkyLens dan membuka video gerak pasangannya |
| Bahasa aplikasi | English dan Bahasa Indonesia, dapat diganti dari aplikasi dan disimpan di perangkat |

### Batasan penting — wajib dibaca

SkyLens membedakan informasi kemampuan perangkat keras dari fitur yang benar-benar sudah diterapkan. Dengan begitu, kemampuan yang terdeteksi tidak disajikan seolah-olah sudah menjadi mode kamera yang berfungsi.

- **Motion Capture saat ini bukan Apple Live Photos.** SkyLens menyimpan dua file terpisah dengan nama dasar yang sama: foto `.jpg` di `Pictures/SkyLens` dan video pendamping `.mp4` tanpa suara di `Movies/SkyLens`.
- **Motion Capture belum menggunakan rolling pre-capture buffer.** Perekaman dimulai ketika mode LIVE diaktifkan dan dihentikan sekitar 1,1 detik setelah callback foto diterima. Durasi rekaman sebelum tombol rana ditekan bergantung pada berapa lama mode LIVE sudah aktif.
- **120/240 fps belum menjadi mode perekaman aktif.** Panel informasi hanya menampilkan rentang high-speed yang dilaporkan Camera2. Pipeline perekaman high-speed khusus belum dihubungkan ke kontrol kamera.
- **RAW/DNG, Ultra HDR, Night mode komputasional, pemrosesan kedalaman Portrait, AI retouch, dan penghapusan objek belum diterapkan pada MVP ini.** Laporan kemampuan perangkat keras tidak berarti pipeline pengambilan gambar terkait sudah tersedia di SkyLens.
- Resolusi video, frame rate, stabilisasi, dynamic range, dan kualitas gambar bergantung pada perangkat keras kamera, implementasi kamera Android, pencahayaan, dan suhu perangkat.
- Video dan Motion Capture saat ini **tidak memiliki suara**. Aplikasi tidak meminta izin mikrofon.

### Dukungan bahasa

Antarmuka tersedia dalam:

- **EN** — English
- **ID** — Bahasa Indonesia

Gunakan tombol `EN` / `ID` di bagian atas kamera untuk mengganti bahasa. Pilihan bahasa disimpan secara lokal dan digunakan kembali saat SkyLens dibuka. Pada penggunaan pertama, SkyLens mengikuti bahasa perangkat Android apabila bahasanya Bahasa Indonesia; jika bukan, aplikasi dimulai dalam bahasa Inggris. Pengalih bahasa juga tersedia di layar izin kamera.

### Persyaratan

- Android 10 (API 29) atau lebih baru
- Perangkat dengan kamera yang didukung CameraX
- Android SDK Platform 36 untuk membangun proyek
- JDK 17 atau lebih baru

### Cara build APK debug

**Disarankan: Android Studio**

1. Instal Android Studio dan JDK 17 atau lebih baru.
2. Instal Android SDK Platform 36 melalui **Tools → SDK Manager**.
3. Buka folder hasil clone `SkyLens` di Android Studio.
4. Tunggu proses Gradle sync sampai selesai.
5. Pilih konfigurasi run `app`, lalu jalankan pada perangkat Android fisik.

**Command line Windows**

Setelah Android SDK Platform 36 dan JDK 17 atau lebih baru terpasang, buka terminal pada folder utama proyek lalu jalankan:

```powershell
.\build-debug.bat
```

Script akan mengunduh distribusi resmi Gradle 8.13 ke cache SkyLens milik pengguna Windows apabila belum tersedia. Atur `ANDROID_SDK_ROOT` apabila Android SDK berada di lokasi non-default.

**Linux / macOS**

Dengan Gradle 8.13 terpasang dan `ANDROID_SDK_ROOT` sudah diatur:

```bash
gradle --no-daemon assembleDebug
```

Setelah build berhasil, file APK berada di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Pemeriksaan otomatis

GitHub Actions (`.github/workflows/android.yml`) dikonfigurasi untuk:

1. Build APK debug menggunakan JDK 17 dan Gradle 8.13.
2. Menjalankan unit test JVM.
3. Menjalankan Android Lint.
4. Mengunggah APK debug sebagai artifact workflow bernama `skylens-debug-apk` (tersedia selama 14 hari).

Workflow yang sudah dikonfigurasi tidak otomatis berarti build telah berhasil. Periksa tab **Actions** untuk melihat hasil run terbaru sebelum mengunduh atau memasang APK.

### Checklist pengujian perangkat

- [ ] Berikan izin kamera dan pastikan preview terbuka tanpa crash.
- [ ] Ganti kamera belakang dan depan beberapa kali.
- [ ] Ambil foto dan pastikan hasilnya muncul di `Pictures/SkyLens` serta galeri aplikasi.
- [ ] Rekam video biasa, hentikan, lalu pastikan video dapat diputar dari `Movies/SkyLens`.
- [ ] Uji LIVE dan pastikan foto JPEG serta video MP4 pasangannya tersimpan.
- [ ] Buka kedua file menggunakan pemutar media eksternal.
- [ ] Uji tap-to-focus, zoom, flash, dan kompensasi pencahayaan.
- [ ] Ganti bahasa English dan Indonesia, tutup lalu buka kembali aplikasi, dan pastikan pilihan bahasa tetap tersimpan.
- [ ] Buka panel kemampuan kamera dan bandingkan laporannya dengan perilaku perangkat sebenarnya.
- [ ] Ulangi pengujian setelah menolak izin, memindahkan aplikasi ke latar belakang, dan memulai ulang perangkat.
- [ ] Periksa stabilitas dan suhu perangkat melalui pengambilan berulang sebelum distribusi lebih luas.

### Struktur proyek

```text
SkyLens/
├── .github/workflows/android.yml
├── app/
│   ├── src/main/
│   │   ├── java/id/sky/skylens/
│   │   │   ├── MainActivity.kt
│   │   │   └── camera/
│   │   │       ├── CameraCapabilities.kt
│   │   │       └── MediaStoreRepository.kt
│   │   ├── res/values/strings.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── build-debug.bat
├── build-debug.ps1
├── CHANGELOG.md
└── README.md
```

### Privasi dan izin

SkyLens saat ini meminta izin Android **Kamera** untuk menampilkan preview dan mengambil foto atau video. Aplikasi tidak meminta izin mikrofon sehingga rekaman tidak memiliki suara. Hasil pengambilan gambar disimpan di media penyimpanan bersama perangkat, pada folder `Pictures/SkyLens` dan `Movies/SkyLens`. MVP ini tidak memiliki fitur login akun atau unggah ke cloud.

### Kontribusi dan laporan masalah

Laporan bug akan lebih mudah ditindaklanjuti apabila menyertakan model ponsel, versi Android, versi SkyLens, langkah untuk mengulang masalah, perilaku yang diharapkan, perilaku yang terjadi, dan log yang relevan. Jangan sertakan foto atau video pribadi kecuali benar-benar diperlukan dan kamu bersedia membagikannya.
