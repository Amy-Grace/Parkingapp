# Parking Spot Finder and Payment App

This Android application helps users find available parking spots in malls, hospitals, and parks, with integrated payment options via M-Pesa and PayPal.

## Setup Instructions

Before running the app on a test device, you need to complete the following setup:

### 1. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app with package name `com.example.parkingactivity` 
3. Download the `google-services.json` file
4. Replace the placeholder `google-services.json` file in the app directory with your downloaded file

### 2. Mapbox Setup
1. Create a Mapbox account at [Mapbox](https://www.mapbox.com/)
2. Generate a Mapbox access token in your account dashboard
3. Open `app/src/main/res/values/strings.xml`
4. Replace `YOUR_MAPBOX_ACCESS_TOKEN` with your actual token

### 3. Build and Run
1. Open the project in Android Studio
2. Sync Gradle files
3. Connect a physical device or start an emulator
4. Click Run

## Features
- Real-time parking availability updates
- Location-based parking spot finding
- M-Pesa and PayPal payment integration
- Coupon functionality
- User authentication
- Booking history 