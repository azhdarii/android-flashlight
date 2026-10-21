# چراغ قوه / App10

A simple Android flashlight app written in Kotlin with Jetpack Compose.

![image](https://github.com/user-attachments/assets/33ed66e0-e49d-4f63-869d-119701da61be)

## Features

- **Torch** — switch the camera flash on and off.
- **Strobe** — flashing effect with an adjustable period.
- **Colour lamp** — fullscreen solid colour screen, with a set of presets and a full HSV picker.
- **Keeps working in the background** — a foreground service keeps the torch alive while you
  are in another app or the screen is off, with a notification to switch it off again.
- **Theme selection** — light, dark or follow the system, persisted across restarts.
- **Tapsell ads** — optional banner and interstitial, see [Ads](#ads).

## Architecture

The project is split into layers, so that the UI, the platform integration and the state
handling are independently testable.

```
app/src/main/java/com/azhdar/app10/
├── App10Application.kt      Application + hand written dependency container
├── MainActivity.kt          Single activity host, theme and window handling
├── core/
│   ├── ads/AdsManager.kt    Tapsell SDK wrapper
│   ├── flashlight/
│   │   ├── TorchController  Owns the camera2 torch, publishes its state
│   │   ├── TorchDriver      Interface that makes the torch testable
│   │   └── StrobeController The flashing effect, independent of the camera
│   └── service/TorchService Foreground service that keeps the torch alive
├── data/SettingsRepository  DataStore backed user preferences
└── ui/
    ├── FlashlightViewModel  Translates user intents into controller calls
    ├── components/          Reusable composables
    ├── navigation/          Navigation graph and routes
    ├── screens/             Home, colour chooser, colour picker, solid colour
    └── theme/               Material 3 theme, colours and typography
```

`TorchController` and `StrobeController` live in the application scope, so the torch keeps
its state across configuration changes and while the app is in the background. The UI only
observes their `StateFlow`s and never touches the camera itself.

## Building

The project is a standard Gradle project:

```bash
cd app10
./gradlew assembleDebug
```

Requirements: JDK 17, Android SDK with platform 34.

## Ads

The ad zone ids are read from `local.properties` (or from `-P` flags), so a build without
them simply runs without ads instead of failing:

```properties
TAPSELL_APP_KEY=your-tapsell-app-key
TAPSELL_BANNER_ZONE=your-banner-zone-id
TAPSELL_INTERSTITIAL_ZONE=your-interstitial-zone-id
TAPSELL_REWARDED_ZONE=your-rewarded-zone-id
```

## Permissions

The app deliberately asks for as little as possible:

| Permission | Why |
| --- | --- |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Required by the ad SDK. |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps the torch alive while the app is backgrounded. |
| `POST_NOTIFICATIONS` | Shows the foreground service notification. Requested at runtime, best effort. |

The camera **permission is not requested**. The camera2 torch API does not need it: neither
`CameraManager#setTorchMode` nor the native camera service performs a permission or app-op
check for the torch. The foreground service deliberately uses the `specialUse` type instead
of the `camera` type, because the latter would force the `CAMERA` runtime permission to be
granted on Android 14+ for no reason.

## Tests

```bash
cd app10
./gradlew testDebugUnitTest
```

The unit tests cover the torch and strobe state machines, the strobe timing and the
navigation routes. The strobe is tested against a fake torch, so the timing and the state
handling can be verified without a device.

## Download

- CafeBazaar: https://cafebazaar.ir/app/com.azhdar.app10
- GitHub: https://github.com/azhdarii/android-flashlight/tree/main/app10/app/release
