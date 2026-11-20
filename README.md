# 🌱 Plant Care Reminder — Plant Monitoring Application

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Java%20%7C%20Kotlin-F18E33.svg?style=flat&logo=java)](https://developer.android.com)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-4285F4.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-DAO%20%2F%20Offline--First-009688.svg?style=flat)](https://developer.android.com/topic/architecture)
[![University](https://img.shields.io/badge/University-RUSL-800000.svg?style=flat)](https://www.rjt.ac.lk)

> **Mobile Application Development (ICT3219)**  
> **Bachelor of Information and Communication Technology (BICT) Degree Program**  
> Department of Information and Communication Technology, Faculty of Technology  
> **Rajarata University of Sri Lanka, Mihintale**

---

## 📖 Table of Contents
1. [Project Overview](#-project-overview)
2. [Motivation & Problem Statement](#-motivation--problem-statement)
3. [Key Features](#-key-features)
4. [Application Architecture](#-application-architecture)
5. [Screens & Workflow](#-screens--workflow)
6. [Tech Stack & Development Tools](#-tech-stack--development-tools)
7. [Project Directory Structure](#-project-directory-structure)
8. [Database Schema & Data Model](#-database-schema--data-model)
9. [Smart Reminder & Notification Engine](#-smart-reminder--notification-engine)
10. [Setup & Installation](#-setup--installation)
11. [Testing Strategy](#-testing-strategy)
12. [Project Team — Group OAKSHADE](#-project-team--group-oakshade)

---

## 🌿 Project Overview

The **Plant Monitoring Application** (*Plant Care Reminder*) is an offline-first Android application developed to help plant lovers, home gardeners, and office workers keep their plants healthy and thriving. 

With busy modern routines, plant caretakers frequently struggle to maintain consistent watering schedules. This application provides a lightweight, intuitive, and private solution that operates entirely on-device without requiring user accounts, internet access, or cloud backends.

---

## 🎯 Motivation & Problem Statement

### The Problem
- **Inconsistent Watering**: Overwatering and underwatering are the leading causes of houseplant mortality.
- **Varying Care Needs**: Different plant species (succulents, herbs, tropicals, vegetables) require drastically different watering intervals and care routines.
- **Manual Log Inefficiency**: Traditional paper notes or generic calendar reminders are cumbersome to configure and easily lost.

### The Solution
- **Customized Profiles**: Record each plant's name, category, care notes, and personal photo.
- **Intelligent Countdown**: Real-time status tags (`💧 Today`, `💧 Tomorrow`, `In N days`, or `⚠️ Overdue`).
- **Doze-Compatible Reminders**: System-level alarms that trigger even when the device is locked or in battery-saving mode.
- **Actionable Notifications**: Mark plants as watered or snooze directly from the lock screen notification without opening the app.

---

## ✨ Key Features

- **🌱 My Garden Dashboard**:
  - Grid/list view displaying all user plants with custom photos or category emojis.
  - Multi-filtering by category: *All*, *Favorites*, *Indoor*, *Vegetables*, *Fruits*, *Herbs*, *Succulents*, and *Flowers*.
  - Live empty-state illustration when no plants match the active filter.

- **📅 Smart Watering Reminders**:
  - Configurable frequency (daily up to every 30 days).
  - Exact time-of-day picker (e.g., 09:00 AM).
  - Start date and optional reminder expiration date range.

- **🔔 Rich, Actionable Notifications**:
  - Android 8.0+ High-Priority `NotificationChannel`.
  - Notification action buttons: **✓ Watered** and **⏰ Snooze 1h**.
  - Direct tap opens the plant's dedicated profile screen.

- **🔄 Reboot Persistence (`BootReceiver`)**:
  - Automatically restores all scheduled alarms when the Android device is restarted or powered back on.

- **📷 Photo Picker with Internal Storage Caching**:
  - Modern `ActivityResultContracts.GetContent()` integration.
  - Copies photos to internal app storage (`getFilesDir()/plant_photos`) to guarantee images persist across device reboots and cache cleans.

- **🌗 Dark Mode Support**:
  - In-app theme toggle with instant runtime recreation (`AppCompatDelegate.setDefaultNightMode`).
  - Dark mode choice is remembered across sessions via `SharedPreferences`.

- **🛡️ Android 13+ Notification Permissions**:
  - Proactive rationale dialog and runtime `POST_NOTIFICATIONS` permission request.
  - Diagnostic health status indicator in the Settings menu.

---

## 🏗 Application Architecture

The application adopts an **Offline-First Room Architecture** combining Android Jetpack components:

```
┌────────────────────────────────────────────────────────┐
│                   User Interface (UI)                  │
│   MainActivity   PlantDetailsActivity   AddPlantActivity │
│          SettingsActivity       SplashActivity         │
└──────────────────────────┬─────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│     PlantAdapter        │   │    System Services      │
│  RecyclerView & Cards   │   │  AlarmManager & Alerts  │
└────────────┬────────────┘   └─────────────┬───────────┘
             │                              │
             ▼                              ▼
┌─────────────────────────┐   ┌─────────────────────────┐
│        PlantDao         │   │ WateringReminderReceiver│
│   Room Database (SQL)   │◄──┤      BootReceiver       │
└────────────┬────────────┘   └─────────────────────────┘
             ▼
┌─────────────────────────┐
│   AppDatabase (SQLite)  │
│  Table: 'plants'        │
└─────────────────────────┘
```

---

## 📱 Screens & Workflow

| Screen | Description |
| :--- | :--- |
| **Splash Screen** | Animates logo entrance with `ScaleAnimation` + `AlphaAnimation`, loads dark mode preference, and displays Group OAKSHADE attribution. |
| **Garden Dashboard** | Main hub listing all registered plants with category filter chips, search/sort by favorites, and floating action button to add a plant. |
| **Add / Edit Plant** | Dual-mode form with gallery photo picker, category selector, interval stepper, `DatePickerDialog`, and `TimePickerDialog`. |
| **Plant Details** | Comprehensive profile displaying high-res image, watering countdown, care notes, "Water Now" button, and reminder switch. |
| **Settings Screen** | App configuration, dark mode toggle, live notification permission diagnostics, and developer credits. |

---

## 🛠 Tech Stack & Development Tools

- **Programming Languages**: Java & Kotlin
- **IDE**: Android Studio
- **Min SDK**: API 24 (Android 7.0 Nougat)
- **Target / Compile SDK**: API 34+ (Android 14)
- **Local Persistence**: Android Room Database (v2.6.1)
- **Background Tasks**: Android `AlarmManager` with `RTC_WAKEUP` & `BroadcastReceiver`
- **Notifications**: `NotificationCompat` & `NotificationChannel` (API 26+)
- **Build System**: Gradle with Version Catalog (`libs.versions.toml`)

---

## 📁 Project Directory Structure

```
Plant APP/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/myapplication/
│   │   │   ├── AddPlantActivity.java          # Form for adding/editing plants & reminders
│   │   │   ├── AppDatabase.java               # Room Database singleton class
│   │   │   ├── BootReceiver.java              # Restores alarms on device restart
│   │   │   ├── MainActivity.java              # Garden dashboard & filter chip controller
│   │   │   ├── NotificationHelper.java        # Notification channels & action builder
│   │   │   ├── Plant.java                     # Room Entity data model
│   │   │   ├── PlantAdapter.java              # RecyclerView adapter & view holder
│   │   │   ├── PlantDao.java                  # Room DAO SQL queries & CRUD methods
│   │   │   ├── PlantDetailsActivity.java      # Plant profile & care actions screen
│   │   │   ├── SettingsActivity.java          # Preferences, dark mode & permissions
│   │   │   ├── SplashActivity.java            # Entrance screen with smooth animations
│   │   │   └── WateringReminderReceiver.java  # Alarm handler & scheduling logic
│   │   ├── res/
│   │   │   ├── layout/                        # Activity & item XML layout files
│   │   │   ├── values/                        # Strings, themes, light mode colors
│   │   │   ├── values-night/                  # Dark mode semantic colors
│   │   │   └── xml/                           # Backup & data extraction rules
│   │   └── AndroidManifest.xml                # Activities, receivers & permissions
├── gradle/
│   ├── wrapper/                               # Gradle wrapper properties
│   └── libs.versions.toml                     # Dependency version catalog
└── README.md                                  # Project documentation
```

---

## 🗄 Database Schema & Data Model

The application uses an SQLite database managed via Room (`@Entity(tableName = "plants")`):

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | `INTEGER PRIMARY KEY` | Auto-generated unique plant ID |
| `name` | `TEXT` | Plant common or botanical name |
| `category` | `TEXT` | Category (*Indoor, Vegetables, Fruits, etc.*) |
| `age` | `TEXT` | Age description (e.g., "6 months") |
| `description` | `TEXT` | Care notes and sunlight/fertilizer guidelines |
| `favorite` | `INTEGER` | Boolean flag (0 = false, 1 = true) |
| `wateringFrequency`| `INTEGER` | Days between waterings |
| `lastWateredTimestamp` | `INTEGER` | Epoch millis of most recent watering |
| `imageUri` | `TEXT` | Local absolute file path to saved photo |
| `createdTimestamp` | `INTEGER` | Date and time the record was added |
| `reminderEnabled` | `INTEGER` | Notification toggle state |
| `reminderStartDate`| `INTEGER` | Start date epoch millis |
| `reminderEndDate` | `INTEGER` | Expiration date epoch millis |
| `reminderIntervalDays` | `INTEGER` | Interval count between alarms |
| `reminderHour` | `INTEGER` | Hour of day (0–23) |
| `reminderMinute` | `INTEGER` | Minute of hour (0–59) |

---

## ⏰ Smart Reminder & Notification Engine

### 1. Calculation Algorithm (`calculateNextTriggerTime`)
1. Checks whether the current timestamp has surpassed `reminderEndDate`.
2. Initialises a `Calendar` instance targeting the plant's configured hour and minute.
3. If the computed target is in the past, advances day-by-day in steps of `reminderIntervalDays` until it strictly lands in the future.
4. Verifies the next trigger date does not exceed the expiration boundary.

### 2. Doze Mode Compatibility
Scheduled alarms use `AlarmManager.setAndAllowWhileIdle()` with `AlarmManager.RTC_WAKEUP`:
```java
alarmManager.setAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    nextTriggerMillis,
    pendingIntent
);
```
This guarantees notifications are triggered on schedule even when the user's phone enters Android's battery-saving Doze mode.

---

## 🚀 Setup & Installation

### Prerequisites
- **Android Studio**: Jellyfish (2023.3.1) or newer
- **Java Development Kit (JDK)**: JDK 17 or JDK 21 (bundled with Android Studio JBR)
- **Android Device or Emulator**: Android 7.0 (API 24) or higher

### Steps to Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/Plant-App.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio.
   - Select **Open** and choose the `Plant APP` folder.
3. **Gradle Sync**:
   - Wait for Gradle to download dependencies and sync the project.
4. **Run Application**:
   - Connect an Android device with USB debugging enabled, or launch an AVD emulator.
   - Click the green **Run** button (`Shift + F10`).

---

## 🧪 Testing Strategy

The project followed an **Agile development and validation approach**:

- **Unit & Logic Testing**: Verified calendar interval calculations and timestamp math for edge cases (leap days, midnight transitions).
- **Database CRUD Testing**: Checked insert, query by category, update, and cascading delete operations in Room.
- **Notification Testing**: Confirmed high-priority heads-up banners, vibration, and lock screen behavior across Android 8 through Android 14.
- **Reboot Resilience Testing**: Verified alarms are recreated via simulated reboot broadcasts:
  ```bash
  adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
  ```
- **UI & Accessibility Testing**: Validated color contrast, dark mode readability, dynamic screen densities, and recycled view image state clearing.

---

## 👥 Project Team — Group OAKSHADE

This project was developed for the **Mobile Application Development (ICT3219)** course under the **Bachelor of Information and Communication Technology (BICT)** degree program at the **Department of Information and Communication Technology, Faculty of Technology, Rajarata University of Sri Lanka**.

| Student Name | Index Number | Registration Number |
| :--- | :---: | :---: |
| **P.D.V.K. Chandrathilaka** *(Vikum Kalhara)* | **2000** | **ITT/2022/019** |
| **G.M.D.S.K. Weerathunge** *(Sajith Weerathunga)* | **2092** | **ITT/2022/112** |
| **K.A.A.D.D. Vimukthi** | **2089** | **ITT/2022/109** |
| **W.M.H.D. Weerasekara** | **2090** | **ITT/2022/110** |
| **K.A.S. Rashmika** | **2071** | **ITT/2022/091** |

---

### 📄 License & Attribution
*Developed by Group **OAKSHADE** · Plant Care · 2026 · All rights reserved.*
