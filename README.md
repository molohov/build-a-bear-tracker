# Build-A-Bear Collection Tracker

Android app for tracking your Build-A-Bear plush collection.

## Features

- Browse a catalog of Build-A-Bears (bundled seed data + wiki sync)
- Tag bears as **Owned**, **Want**, or **Don't want**
- Add **custom bears** with camera/gallery photos and manual metadata
- Create **saved filtered views** by year, color, status, source, and categories
- **Sync catalog** from the [Build-a-Bear Workshop Wiki](https://buildabear.fandom.com)
- **Export** your collection to a ZIP file (JSON + custom images)

## Requirements

- Android Studio (Ladybug or newer)
- JDK 17+
- Android SDK API 35

## Build & Run

```bash
git clone https://github.com/molohov/build-a-bear-tracker.git
cd build-a-bear-tracker
./gradlew assembleDebug   # Windows: gradlew.bat assembleDebug
```

Install the APK from `app/build/outputs/apk/debug/app-debug.apk`, or run from Android Studio.

## Tech stack

Kotlin, Jetpack Compose, Room, Hilt, Retrofit, WorkManager, Coil

## Data

Catalog metadata is sourced from the Fandom wiki (CC-BY-SA). Collection tags and custom entries are stored locally on-device.

## Plan

See [docs/PLAN.md](docs/PLAN.md) for the full implementation plan and architecture.
