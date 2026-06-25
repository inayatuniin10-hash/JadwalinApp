# 📅 Jadwalin - Aplikasi Manajemen Catatan & Penjadwalan

Aplikasi Android berbasis **Java** yang dirancang untuk membantu pengguna mengelola produktivitas harian. Aplikasi ini mengintegrasikan pencatatan agenda, pengelolaan jadwal berbasis kalender, serta manajemen profil pengguna dengan sistem penyimpanan cloud yang efisien dan dioptimalkan untuk perangkat dengan spesifikasi RAM terbatas (RAM 3 GB).

---

## 📺 Demo Aplikasi

Saksikan demonstrasi operasional seluruh fitur utama aplikasi Jadwalin (Autentikasi, Kalender Tugas, Tambah/Edit Catatan dengan Lampiran Gambar, serta Manajemen Akun) melalui tautan berikut:

👉 [**Tonton Video Demo Aplikasi Jadwalin di sini**](https://link-video-demo-anda-di-sini.com)  
*(Durasi video maksimal 5 menit)*

---

## 🛠️ Tech Stack & Library Pihak Ketiga

Aplikasi ini dibangun menggunakan arsitektur Android native dengan daftar teknologi berikut:

* **Bahasa Pemrograman:** Java (JDK 17)
* **User Interface & Arsitektur:** Android Jetpack Components (`ConstraintLayout`, `ScrollView`, `Fragment`, `Activity Results API`)
* **Backend & Database:**
    * **Firebase Authentication:** Mengelola gerbang keamanan pengguna, pendaftaran akun (*Register*), masuk (*Login*), sesi aktif, hingga pembaruan kata sandi.
    * **Google Cloud Firestore:** Database NoSQL *real-time* untuk menyimpan data judul, konten, tanggal, dan tautan gambar catatan pengguna secara aman.
* **Penyimpanan Gambar (Cloud Image Hosting):**
    * **ImgBB API:** Repositori penyimpanan cloud pihak ketiga untuk mengunggah berkas gambar lampiran melalui request HTTP Multipart Form Data.
* **Library Jaringan & Pengolahan Data:**
    * **Retrofit 2:** Klien HTTP *type-safe* untuk menangani komunikasi data biner (`byte[]`) asinkronus ke API ImgBB.
    * **Gson Converter:** Library konversi otomatis untuk mengubah respons JSON dari server menjadi objek Java.
    * **Glide:** Pustaka manajemen visual tangguh untuk merender gambar lokal dari galeri/kamera serta memuat gambar eksternal dari internet dengan sistem *caching* yang mulus.

---

## ⚙️ Optimasi Perangkat (RAM 3 GB)

Aplikasi ini telah dilengkapi dengan sistem manajemen memori khusus pada fitur unggah gambar (`AddCatatanActivity` & `AkunFragment`):
* **Downsampling Resolusi:** Gambar berukuran besar dari kamera/galeri secara otomatis dipangkas setengah dimensinya menggunakan `BitmapFactory.Options.inSampleSize` sebelum diproses.
* **Kompresi Kualitas:** Kualitas gambar ditekan hingga 60% format JPEG untuk menghemat jatah RAM secara drastis saat konversi biner dan menghemat kuota internet.
* **Garbage Collection Manual:** Pemicuan fungsi `Bitmap.recycle()` secara instan setelah data terkirim guna mencegah *Out Of Memory (OOM)* atau aplikasi keluar sendiri (*force close*).

---

## 🚀 Langkah Instalasi & Cara Menjalankan

Ikuti panduan berikut untuk menjalankan proyek ini di lingkungan lokal Anda menggunakan **Android Studio**:

### 1. Kloning Repositori
Buka Terminal atau Git Bash, lalu jalankan perintah berikut:
```bash
git clone https://github.com/inayatuniin10-hash/JadwalinApp.git
