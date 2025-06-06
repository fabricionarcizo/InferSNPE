# InferSNPE App

An Android application for real-time camera-based inference using Qualcomm's SNPE (Snapdragon Neural Processing Engine). The app demonstrates on-device AI model execution with support for CPU, GPU, and DSP acceleration modes. This is a research-oriented optimization toolkit designed for experiments and development for the tutorial `Edge AI in Action: Technologies and Applications` presented during the IEEE/CVF Conference on Computer Vision and Pattern Recognition 2025 (CVPR 2025).

## Project Structure

- `app/` - Main Android application module
  - `src/main/java/` - Application source code
  - `src/main/res/` - Application resources (layouts, drawables, etc.)
  - `src/main/assets/` - App assets
  - `src/main/AndroidManifest.xml` - App manifest
- `build.gradle.kts` - Project-level Gradle build script
- `settings.gradle.kts` - Gradle settings
- `gradle/` - Gradle wrapper and version catalog
- `LICENSE` - Project license

## Features

- Real-time camera preview and inference
- Model selection and hardware acceleration (CPU, GPU, DSP)
- FPS display and annotated output
- Modern Android architecture with Kotlin, ViewBinding, and CameraX

## Requirements

- Android device with ARM64 architecture (arm64-v8a)
- Android 8.0 (API 26) or higher
- Qualcomm SNPE SDK (see below)

## Setup

1. **SNPE SDK**: Download the `snpe-release.aar` from the Qualcomm Developer Network and place it in `app/src/main/libs/`.
   - Example: `<SNPE>/2.34.0.250424/android/snpe-release.aar`
2. **Trained Models**: Place your `.dlc` model files in `app/src/main/assets/`.

## Build Instructions

1. Clone this repository.
2. Open in Android Studio (Arctic Fox or newer recommended).
3. Ensure you have the required SDKs and NDK installed.
4. Build and run on a physical ARM64 device.

Alternatively, from the command line:

```sh
./gradlew assembleDebug
```

## Usage

- Launch the app on your device.
- Grant camera permissions.
- Select the desired model and hardware mode (CPU/GPU/DSP).
- View real-time inference results and FPS overlay.

## Dependencies

- [CameraX](https://developer.android.com/training/camerax)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Material Components](https://material.io/develop/android)
- [Qualcomm SNPE](https://developer.qualcomm.com/software/qualcomm-neural-processing-sdk)

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the terms of the LICENSE file in this repository.

---

**Authors:** Fabricio Batista Narcizo, Elizabete Munzlinger, Sai Narsi Reddy Donthi Reddy, and Shan Ahmed Shaffi.
