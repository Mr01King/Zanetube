# ZaneTube starter

Starter project Android (Kotlin + WebView) dengan:
- YouTube melalui official YouTube embedded player.
- Audio URL langsung melalui Android Media3/ExoPlayer.
- Bubble overlay Android yang dapat digeser dan membuka aplikasi.

## Cara membuka
1. Ekstrak ZIP.
2. Buka folder `ZaneTube` melalui Android Studio.
3. Tunggu Gradle Sync, lalu jalankan ke perangkat Android.
4. Untuk bubble, tekan **Aktifkan bubble** dan izinkan "Display over other apps".

## Versi web (Netlify)
Halaman `app/src/main/assets/index.html` juga dipublikasikan sebagai situs web melalui Netlify (lihat `netlify.toml`). Di web, video YouTube dan audio URL langsung diputar di browser; fitur bubble dan audio latar belakang hanya ada di APK Android.

## Batasan penting
- Ini source project, bukan APK siap pasang.
- YouTube hanya memakai embed resmi. Jangan mengunduh/mengekstrak stream atau mengakali batasan pemutaran YouTube. Background playback YouTube tidak dijamin oleh embed.
- Background audio Android ditujukan untuk URL audio langsung yang legal/diizinkan. Untuk rilis publik, lengkapi notifikasi media, kontrol play/pause/next, audio focus, lifecycle, dan pengujian Android versi target.
- Bubble starter hanya tombol mengambang: geser untuk memindahkan, ketuk untuk membuka aplikasi, tekan lama untuk menutup. Belum menjadi mini-player interaktif.
- Gradle wrapper (`gradlew`) belum disertakan; Android Studio dapat membuatnya saat Gradle Sync.
