# Fresco

> Fruit & vegetable classifier for Android and iOS, built with Kotlin Multiplatform.

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4?logo=jetpackcompose&logoColor=white)
![ONNX Runtime](https://img.shields.io/badge/ONNX%20Runtime-1.24.3-005CED)
![Core ML](https://img.shields.io/badge/Core%20ML-iOS%2016%2B-000000?logo=apple&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-3.4.2-087CFA?logo=ktor&logoColor=white)
![SQLDelight](https://img.shields.io/badge/SQLDelight-2.3.2-F57C00)
![Android minSdk](https://img.shields.io/badge/minSdk-24-green?logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## Overview

Fresco uses **EfficientNet-B0** to classify fruits and vegetables from the camera, then enriches results with nutritional data from [Open Food Facts](https://world.openfoodfacts.org) and recipe suggestions from [TheMealDB](https://www.themealdb.com). Everything is cached locally with SQLDelight.

- Shared UI and business logic via Compose Multiplatform
- On-device inference — ONNX Runtime on Android, Core ML on iOS
- Clean Architecture: UI → ViewModel → UseCase → Repository
- Dependency injection with Koin

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.3.20 |
| UI | Compose Multiplatform | 1.10.3 |
| ML (Android) | ONNX Runtime | 1.24.3 |
| ML (iOS) | Core ML | native |
| Camera (Android) | CameraX | 1.6.0 |
| Camera (iOS) | AVFoundation | native |
| Networking | Ktor Client | 3.4.2 |
| Local Storage | SQLDelight | 2.3.2 |
| DI | Koin | 4.2.0 |
| Coroutines | kotlinx.coroutines | 1.10.2 |
| Serialization | kotlinx.serialization | 1.10.0 |

---

## Getting Started

### Prerequisites

- Android Studio Meerkat or later
- Xcode 15+
- JDK 11

### Android

```shell
./gradlew :composeApp:assembleDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device.

---

## Project Structure

```
composeApp/src/
├── commonMain/       # Shared UI, ViewModels, domain, data, DI
├── androidMain/      # CameraX, ONNX Runtime, Android platform impl
└── iosMain/          # AVFoundation, Core ML, iOS platform impl
iosApp/               # Xcode project + EfficientNetB0.mlpackage
```

---

## License

MIT
