<div align="center">
  <img src="app/src/main/res/drawable/ic_splash_logo.png" width="120" alt="QRDroid Logo" />
  <h1>QRDroid</h1>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-7F52FF.svg?logo=Kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Android-3DDC84.svg?logo=Android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Compose-4285F4.svg?logo=JetpackCompose&logoColor=white" alt="Compose" />
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License" /></a>
  </p>
</div>

**QRDroid** is a modern, fast, and intuitive Android application for scanning and generating QR codes and barcodes. Built 100% in **Kotlin** with **Jetpack Compose** and **Material Design 3 (Material You)**, it provides first-class native support for the Brazilian payment ecosystem (PIX and Boletos), dynamic color customization with an HSV color picker, and ultra-fast on-device computer vision powered by Google ML Kit and CameraX.

---

## Key Features

### Smart Scanner
- **Ultra-Fast Detection:** Real-time on-device barcode and QR code recognition powered by **Google ML Kit Barcode Scanning**.
- **Intuitive Camera Controls:** Smooth linear zoom presets (0.5x, 1x, 2x, 5x), flashlight/torch toggle, front and rear camera switching, and gallery image picker fallback.
- **Dynamic Viewfinder Overlay:** Modern rounded viewfinder with animated scanning laser.
- **Haptic Feedback:** Instant tactile confirmation upon successful code detection.
- **Accessible Permission Flow:** Material 3 camera permission screen with privacy guarantees (100% offline processing), gallery scanning option, and automatic lifecycle detection when returning from system Settings.

### Specialized Parsers (PIX, Boletos & More)
- **PIX (BACEN / EMV Standard):** Extracts receiver name, city, transaction ID, PIX key, and formatted amount (BRL). Instant "Copia e Cola" code copy button.
- **Bank Boletos & Utility Bills:** Supports standard Brazilian bank collection slips and utility invoices (water, electricity, gas, municipal taxes), calculating due dates, amounts, and formatting the standard Linha Digitável.
- **Wi-Fi Networks:** Instant reading of network configurations (SSID, password, encryption type WPA/WEP/Open) and direct connection.
- **URLs & Links:** URL preview with safe opening in the user's default browser.
- **Contacts, Emails, Phone Numbers & Text:** Contextual actions for direct dialing, emailing, sharing, and copying.

### QR Code Generator & Studio
- **High-Resolution Generation:** Crisp QR code rendering powered by **ZXing Core**.
- **Custom HSV Color Picker:** Full foreground and background color customization with a 2D Saturation-Value box, Hue slider, real-time preview, and quick presets.
- **Multiple Content Types:** Plain text, URLs, Wi-Fi networks, PIX payment keys, and contact info.
- **Export & Sharing:** Save QR codes directly to device storage, share via Android Sharesheet with `FileProvider`, or copy code content to clipboard.

### History & Favorites
- **Offline & Private Storage:** Robust on-device SQLite database with zero cloud tracking.
- **Dynamic Search & Filtering:** Real-time search query matching and category filter chips (All, PIX, Boleto, URL, Wi-Fi).
- **Favorites & Management:** Star frequently used codes, copy contents on demand, or remove items individually and in bulk.

---

## Architecture & Tech Stack

The project adheres to modern Android engineering best practices:

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material Design 3](https://m3.material.io/)
- **Typography:** Inter font family integrated with Material 3 typography scale.
- **Camera:** [CameraX](https://developer.android.com/training/camerax) (Lifecycle, Camera2, View)
- **Computer Vision:** [Google ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- **QR Code Engine:** [ZXing Core](https://github.com/zxing/zxing)
- **Navigation:** [Jetpack Navigation 3](https://developer.android.com/guide/navigation)
- **Architecture Pattern:** Clean MVVM (Model-View-ViewModel) + Kotlin Coroutines & StateFlow
- **Local Database:** Native SQLite with Repository pattern

---

## Project Structure

```text
qrdroid/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── kotlin/com/example/qrdroid/
│   │   │   │   ├── MainActivity.kt               # Main application entry point
│   │   │   │   ├── Navigation.kt                 # Navigation graph (Navigation 3)
│   │   │   │   ├── data/                         # Entities, SQLite Helper, and Repository
│   │   │   │   ├── generator/                    # ZXing QR Code generator & image exporter
│   │   │   │   ├── scanner/                      # ML Kit image analyzer & parsers (PIX, Boleto, Wi-Fi)
│   │   │   │   └── ui/                           # Jetpack Compose screens, ViewModels, and M3 Theme
│   │   │   │       ├── create/                   # QR code generation & HSV color picker
│   │   │   │       ├── history/                  # Scan history, search, and favorites
│   │   │   │       ├── main/                     # Main scaffold and bottom navigation
│   │   │   │       ├── scanner/                  # Camera viewfinder, permissions & result bottom sheet
│   │   │   │       └── theme/                    # Color schemes, typography, and theme definitions
│   │   │   └── res/                              # Drawables, mipmaps, Inter fonts, and XML configs
│   │   └── test/                                 # Unit tests for parsers, generators, and ViewModels
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                        # Version catalog for dependencies and plugins
└── README.md
```

---

## Getting Started

### Prerequisites
- **Android Studio Ladybug** (or newer)
- **JDK 17** configured
- Android physical device (recommended for camera testing) or Emulator with virtual camera support (API 24+)

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/carllosnc/qrdroid.git
   cd qrdroid
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle dependencies (**Sync Project with Gradle Files**).

4. Build and install the debug APK on your connected device/emulator:
   ```bash
   ./gradlew assembleDebug
   ```

5. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## License

This project is licensed under the [MIT License](LICENSE).
