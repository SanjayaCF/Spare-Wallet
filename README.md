# Spare-Wallet

Aplikasi Simulasi E-Wallet.

## Anggota Kelompok

* Rendy Ananta Kristanto / 71220840 (RendyAnantaKristanto & R7007R)
* Yosua Sutanto Putra / 71220841 (yosuasp)
* Leif Sean Kusumo / 71220915 (ZeroAce25)
* Sanjaya Cahyadi Fuad / 71220965 (SanjayaCF)

## Link Figma

[Desain Proyek Spare-Wallet di Figma](https://www.figma.com/design/9GLwQMN1RjMlcPoUT0kyWR/PROJECT-UTS-PROGANDRO?node-id=0-1&p=f&t=fRKTm8buRobCaY6k-0)

## Implementasi Rekomendasi Material Design 3

Berikut adalah tabel yang merangkum implementasi rekomendasi Material Design 3 dari file PDF yang diberikan ke dalam proyek ini:

| Rekomendasi | Status Implementasi | Detail |
| :--- | :--- | :--- |
| **Komponen TextField** | **Done** | Menggunakan `OutlinedTextField` dari Material 3 di beberapa layar seperti Login, Register, Edit Profile, dan Transfer. |
| **Komponen Button** | **Done** | Menggunakan komponen `Button` dan `TextButton` dari Material 3 untuk aksi seperti Login, Register, Edit Profile, dan Logout. |
| **Tipografi** | **Done** | Menggunakan `MaterialTheme.typography` yang didefinisikan di `Type.kt` untuk konsistensi teks di seluruh aplikasi. |
| **Warna** | **Done** | Menggunakan skema warna Material 3 yang didefinisikan di `Color.kt` dan `Theme.kt`, serta menerapkan warna dari `colors.xml` pada tema dasar aplikasi. |
| **Komponen Card** | **Done** | Menggunakan `ElevatedCard` dari Material 3 pada halaman utama (Home) dan riwayat transaksi. |
| **Bottom Navigation** | **Done** | Mengimplementasikan `NavigationBar` dari Material 3 sebagai navigasi bawah utama aplikasi. |
| **Action Icon (IconButton)** | **Done** | Menggunakan `IconButton` dari Material 3 untuk ikon aksi seperti Top Up, Transfer, dan lainnya di halaman utama. |
| **Feedback Status (Snackbar)**| **Done** | Menggunakan `SnackbarHost` dari Material 3 untuk menampilkan pesan feedback kepada pengguna, misalnya saat login gagal atau input tidak valid. |

## Catatan Tambahan

Beberapa fitur yang terhubung langsung dengan database Firebase, seperti **Transfer**, mungkin memerlukan waktu proses yang sedikit lebih lama. Dikarenakan mekanisme untuk mencegah kondisi seperti *race condition* atau pembaruan data yang tidak valid ketika ada beberapa operasi yang berjalan secara bersamaan pada data yang sama, contohnya memastikan saldo pengguna tidak menjadi negatif saat ada dua transaksi penarikan di waktu yang hampir bersamaan.
