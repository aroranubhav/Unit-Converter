# Unit Converter

![CI](https://github.com/aroranubhav/Unit-Converter/actions/workflows/ci.yml/badge.svg)
![CD](https://github.com/aroranubhav/Unit-Converter/actions/workflows/cd.yml/badge.svg)

A minimal Android app built to demonstrate a production-grade CI/CD pipeline using GitHub Actions and Firebase App Distribution.

---

## Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [App Features](#app-features)
- [Architecture](#architecture)
- [Pipeline Architecture](#pipeline-architecture)
  - [CI Workflow](#ci-workflow)
  - [CD Workflow](#cd-workflow)
- [Branch Strategy](#branch-strategy)
- [Versioning](#versioning)
- [Secrets Setup](#secrets-setup)
- [Local Setup](#local-setup)

---

## About the Project

This project is intentionally minimal in scope — the app logic exists to support meaningful unit tests, and the unit tests exist to power the CI pipeline. The real focus is the end-to-end CI/CD setup:

- Every push to a feature branch and every PR to `main` runs the test suite automatically via CI.
- Every version tag (e.g. `v1.0.0`) triggers a signed release APK build and distributes it to testers via Firebase App Distribution.

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Unit Tests | JUnit4 |
| CI/CD | GitHub Actions |
| Distribution | Firebase App Distribution |
| Signing | Keystore via GitHub Secrets |

---

## App Features

A single screen Unit Converter supporting the following conversions:

**Temperature**
- Celsius ↔ Fahrenheit

**Weight**
- Kilogram ↔ Pounds
- Kilogram ↔ Ounces
- Pounds ↔ Ounces

**Distance**
- Kilometer ↔ Miles

---

## Architecture

```
com.maxi.unitconverter
├── model
│   ├── ConversionCategory.kt   # Enum — Temperature, Weight, Distance
│   └── UnitType.kt             # Enum — all unit types with category mapping
├── viewmodel
│   └── UnitConverterViewModel.kt  # Conversion logic + UI state
└── ui
    └── UnitConverterScreen.kt  # Single Compose screen
```

The `convert()` function in `UnitConverterViewModel` is kept pure (no side effects) to make unit testing straightforward — it takes inputs and returns an output with no dependency on Android or coroutines.

---

## Pipeline Architecture

### CI Workflow

**File:** `.github/workflows/ci.yml`

**Triggers:**
- Push to any `feature/**` branch
- Pull Request targeting `main`

```
Push / PR
    ↓
Checkout code
    ↓
Set up JDK 17
    ↓
Run unit tests (./gradlew testDebugUnitTest)
    ↓
Upload test report as artifact (always, even on failure)
```

The test report is uploaded as a GitHub Actions artifact on every run — accessible from the Actions tab regardless of whether the run passed or failed. This makes debugging failing tests straightforward without needing to reproduce locally.

---

### CD Workflow

**File:** `.github/workflows/cd.yml`

**Triggers:**
- Push of a version tag matching `v*.*.*` (e.g. `v1.0.0`)

```
Tag push (e.g. v1.0.0)
    ↓
Checkout code
    ↓
Set up JDK 17
    ↓
Decode keystore from KEYSTORE_BASE64 secret
    ↓
Build signed release APK (./gradlew assembleRelease)
    ↓
Upload APK to Firebase App Distribution
```

CD is gated on a version tag — it does not run on every merge to `main`. This means a human explicitly decides when to ship by creating and pushing a tag, which is the **Continuous Delivery** model (as opposed to Continuous Deployment where every merge ships automatically).

---

## Branch Strategy

```
feature/your-feature  →  push  →  CI runs
        ↓
  Pull Request to main  →  CI must pass before merge is allowed
        ↓
  Merge to main  →  no automatic deployment
        ↓
  git tag v1.0.0 + push  →  CD triggers → Firebase App Distribution
```

The `main` branch is protected:
- Direct pushes are not allowed
- A PR with passing CI is required before merging

---

## Versioning

Versioning is driven automatically by GitHub Actions:

| Property | Source | Example |
|---|---|---|
| `versionCode` | `github.run_number` — auto-increments on every workflow run | 1, 2, 3... |
| `versionName` | `github.ref_name` — resolves to the tag name on a tag push | `v1.0.0` |

This means you never manually update version numbers in `build.gradle.kts`. To release a new version:

```bash
git tag v1.0.1
git push origin v1.0.1
```

Semantic versioning is followed:

| Segment | When to bump |
|---|---|
| MAJOR (`v2.0.0`) | Breaking changes |
| MINOR (`v1.1.0`) | New features, backwards compatible |
| PATCH (`v1.0.1`) | Bug fixes |

---

## Secrets Setup

The CD pipeline requires the following secrets configured in your GitHub repository under **Settings → Secrets and variables → Actions**:

### Keystore Secrets

| Secret | Description | How to get it |
|---|---|---|
| `KEYSTORE_BASE64` | Base64-encoded release keystore | `base64 -i your-key.jks` in terminal |
| `STORE_PASSWORD` | Keystore store password | Password set during `keytool` generation |
| `KEY_ALIAS` | Keystore key alias | Alias set during `keytool` generation |
| `KEY_PASSWORD` | Keystore key password | Key password set during `keytool` generation |

**Generating the keystore:**

```bash
keytool -genkey -v \
  -keystore unit-converter-release.jks \
  -alias unit-converter \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Encoding the keystore to Base64:**

```bash
# macOS / Linux
base64 -i unit-converter-release.jks

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("unit-converter-release.jks"))
```

> ⚠️ Never commit the `.jks` file to the repository. It is included in `.gitignore`.

---

### Firebase Secrets

| Secret | Description | How to get it |
|---|---|---|
| `FIREBASE_APP_ID` | Your Android app's Firebase App ID | Firebase Console → Project Settings → General → Your apps |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Full contents of the service account JSON | Firebase Console → Project Settings → Service Accounts → Generate new private key |

**Firebase setup steps:**
1. Create a project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add an Android app with your package name
3. Enable **App Distribution** under Release & Monitor
4. Create a tester group under **Testers & Groups**
5. Generate a service account private key under **Project Settings → Service Accounts**

> Note: `google-services.json` is **not required** for this project. Firebase App Distribution is used purely as a delivery mechanism via the GitHub Action — the app itself does not use any Firebase SDKs at runtime.

---

## Local Setup

1. Clone the repository
```bash
git clone https://github.com/aroranubhav/Unit-Converter.git
```

2. Open in Android Studio

3. Run unit tests locally
```bash
./gradlew testDebugUnitTest
```

4. Build a debug APK
```bash
./gradlew assembleDebug
```

> The release build requires signing secrets set as environment variables. For local development, the debug build is sufficient.
