# 🔍 Findr – A Networking Platform - Live at Play Store

&#x20;

## 📱 Download APP - from Play store

> [⬇️ Download Findr](https://play.google.com/store/apps/details?id=com.exa.android.reflekt)
> ✅ 450+ downloads | ⏰ 65+ active projects tracked via Firebase

---

## 🧠 Overview

**Findr** is a smart collaboration platform that helps people **discover, connect, and work together** based on skills, roles, and interests at any physical location or event. It combines the power of **real-time geolocation, secure chat, and project networking** into a seamless mobile experience.

### 💡 Use Cases:

* College fests, hackathons, tech conferences
* Co-working spaces, incubation centers
* Student clubs or organizations
* Professional networking in a city or community

---

## 🧣 Key Features

### ⛰️ Nearby Discovery & Filtering

* Locate professionals nearby using **geofencing** and **live map view**.
* Search & filter users by **role**, **skill**, **project interests**, or **radius**.

### 📄 Smart Posts

* Users can post **Projects**, **Bugs**, or **Ideas** within their geofence.
* Posts support: **title, description, skills needed, project links, and attachments**.

### 👥 Profile Showcase

* Rich profile structure with:

  * **GitHub, LinkedIn, Portfolio** links
  * **Skills**, **Experience**, **Education**
  * Work history & open-to-collab status

### 💬 Real-Time Chat (Secure)

* Fully encrypted real-time messaging using **Signal Protocol**
* Features:

  * **One-to-one & group chats**
  * **Media sharing** (images, files)
  * **Message scheduling**
  * **Priority messages**, **typing indicator**, and **read receipts**

### 📩 Push Notifications

* Powered by **Firebase Cloud Messaging (FCM)**
* Sent when:

  * A new message arrives
  * A user receives a project request
  * A post is interacted with (like/comment)
* Handled with custom logic to show user-specific rich notifications with action buttons

### ❌ Block & Report System

* Users can block/report others for spam or abuse
* Firebase functions handle moderation flow (flag, count, restrict)

### 🔍 Request to Join Project

* Every project post allows users to send a **Join Request**
* Request includes an optional message and profile preview
* Project creators receive **push notifications** and can **accept/decline** from within the app
* Status of requests is visible in both user's dashboards
* Firebase Firestore handles request creation, status updates, and sync

---

## 📚 Tech Stack

| Layer                | Technologies Used                              |
| -------------------- | ---------------------------------------------- |
| **Language**         | Kotlin                                         |
| **UI**               | Jetpack Compose, Material Design 3             |
| **Architecture**     | MVVM + Clean Architecture + Repository Pattern |
| **State Mgmt**       | StateFlow, LiveData                            |
| **Network**          | Retrofit, REST APIs, Gson                      |
| **DI**               | Dagger-Hilt, Koin                              |
| **Images/Media**     | Glide, Cloudinary (uploading media)            |
| **Map & Location**   | Google Maps SDK, Geofencing APIs, GeoFire      |
| **Database**         | Room (offline support), Firebase Firestore     |
| **Storage**          | Firebase Storage, Cloudinary                   |
| **Notifications**    | Firebase Cloud Messaging (FCM)                 |
| **Encryption**       | Signal Protocol (E2EE)                         |
| **Background Tasks** | WorkManager, BroadcastReceiver, AlarmManager   |
| **Hosting**          | Firebase, AWS Lambda (for APIs & moderation)   |

---

<p float="left">
  <img src="https://github.com/user-attachments/assets/e0190839-8d90-4a10-ba2e-53d479024ab4" width="200" />
  <img src = "https://github.com/user-attachments/assets/81b47513-4e02-44b5-930e-684296e76a30" width = "200"/>
  <img src="https://github.com/user-attachments/assets/fcaf49e0-67c0-4b95-abba-15e7721ba0c3" width="200" />
  <img src="https://github.com/user-attachments/assets/b8cc8047-8c8f-4cf8-b090-116927a8817f" width="200" />
  <img src="https://github.com/user-attachments/assets/7bef83d0-3104-47c9-a3ef-574b59bd6ada" width="200" />
  <img src="https://github.com/user-attachments/assets/7d0fe6a1-4ccb-4fc6-b153-978a515de500" width="200" />
  <img src="https://github.com/user-attachments/assets/a8ec88ff-2d8a-48a2-9cee-036c65445235" width="200" />
  <img src="https://github.com/user-attachments/assets/26e34cc5-8ec2-49b0-a742-54d4c3808a54" width="200" />
</p>


## 🚀 Metrics

* 👉 **350+ APK downloads** since launch
* 📊 **65+ active projects tracked** via Firebase
* 💬 Dozens of connections and chats initiated daily

---

## 🛍️ Setup Guide

### ✈ Requirements

* Android Studio Arctic Fox or higher
* Kotlin 1.8+
* Firebase Project
* Google Maps API Key
* Cloudinary account

### 🔧 Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project > Enable **Firestore**, **Firebase Auth**, **Cloud Messaging**, **Storage**
3. Download `google-services.json` and add to `/app` directory
4. Add Firebase SDKs to `build.gradle` (app)

```gradle
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-messaging'
implementation 'com.google.firebase:firebase-storage'
```

### 📊 Google Maps + Geofencing

1. Get API Key from [Google Cloud Console](https://console.cloud.google.com)
2. Enable APIs: Maps SDK, Geofencing API, Places API
3. Add to `local.properties` or manifest:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
```

### 🚀 Cloudinary Setup

1. Create account at [Cloudinary](https://cloudinary.com)
2. Get API Key & Secret
3. Use `Cloudinary.upload` via their Kotlin SDK or HTTP endpoint for media upload

### 🔢 Optional Tools

* [Postman](https://www.postman.com/) for testing APIs
* [Firebase Emulator Suite](https://firebase.google.com/docs/emulator-suite) for local testing

---

## 📈 Future Enhancements

* 🔍 Skill-based AI matching (ML model integration)
* Alumni interaction
* 🎬 Video intro profiles
* 🪝 Event-based networking filters

---

## 🤝 Contributions

Pull requests are welcome! For major changes, please open an issue first to discuss what you would like to change.

---

## 👤 Author

**Vishal Dangi**
[LinkedIn](https://www.linkedin.com/in/vishal-dangi-14805725b) | [GitHub](https://github.com/Vishal01x) | [Email](mailto:vishaldangi01x@gmail.com)

---

## ✨ If you like this project...

Give it a ⭐ star and share it with your peers!
