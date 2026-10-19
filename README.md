# VisionDetect AI

Real-time AI object detection Android app using TensorFlow Lite + CameraX + Clean Architecture.

## Android Studio Version
**Android Studio Panda 4 (2025.3.4)**

## Version Matrix
| Tool | Version |
|---|---|
| AGP | 8.9.0 |
| Gradle | 8.11.1 |
| Kotlin | 2.0.21 |
| KSP | 2.0.21-1.0.27 |
| Hilt | 2.52 |
| compileSdk | 35 |
| minSdk | 29 (Android 10) |

## Setup Steps

1. Open Android Studio Panda → File → Open → select `VisionDetectAI/` folder
2. Set your SDK path in `local.properties`:
   ```
   sdk.dir=/path/to/your/Android/Sdk
   ```
3. Add the TFLite model (app works in demo mode without it):
   ```
   app/src/main/assets/ssd_mobilenet_v1.tflite
   ```
   Download: https://storage.googleapis.com/download.tensorflow.org/models/tflite/coco_ssd_mobilenet_v1_1.0_quant_2018_06_29.zip
   → rename `detect.tflite` to `ssd_mobilenet_v1.tflite`

4. Sync Gradle → Run on a physical Android device (API 29+)

## Architecture
Clean Architecture + MVVM + Hilt DI

```
Presentation → Domain → Data
     ↓             ↓        ↓
Fragments    UseCases   Room DB
ViewModels   Repos      TFLite
StateFlow    Models     DataStore
```

## Key Features
- Real-time object detection at 30+ FPS
- 80 COCO object classes
- Offline — no internet required
- Detection history with Room DB
- Analytics with MPAndroidChart
- Dark/Light mode
- Confidence threshold slider
- GPU delegate support
