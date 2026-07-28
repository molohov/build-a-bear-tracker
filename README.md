# Build-A-Bear Collection Tracker

Android app for tracking your Build-A-Bear plush collection.

## Features

- Browse a catalog of Build-A-Bears (bundled seed data + wiki sync)
- Tag bears as **Owned**, **Want**, or **Don't want**
- Add **custom bears** with camera/gallery photos and manual metadata
- Create **saved filtered views** by year, color, status, source, and categories
- **Sync catalog** from the [Build-a-Bear Workshop Wiki](https://buildabear.fandom.com)
- **Export** your collection to a ZIP file (JSON + custom images)

## Install

Download the latest APK from [GitHub Releases](https://github.com/molohov/build-a-bear-tracker/releases) and install it on your Android device (enable "Install unknown apps" for your browser or file manager if prompted).

## Requirements

- Android Studio (Ladybug or newer)
- JDK 17+
- Android SDK API 35

## Build from source

```bash
git clone https://github.com/molohov/build-a-bear-tracker.git
cd build-a-bear-tracker
./gradlew assembleDebug   # Windows: gradlew.bat assembleDebug
```

Install the debug APK from `app/build/outputs/apk/debug/app-debug.apk`, or run from Android Studio.

For a signed release APK locally, copy `keystore.properties.example` to `keystore.properties`, generate a keystore, then run `./gradlew assembleRelease`.

## Tech stack

Kotlin, Jetpack Compose, Room, Hilt, Retrofit, WorkManager, Coil

## Data

Catalog metadata is sourced from the Fandom wiki (CC-BY-SA). Collection tags and custom entries are stored locally on-device.

## Plan

See [docs/PLAN.md](docs/PLAN.md) for the full implementation plan and architecture.
