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
* Search & filter users by **role**, **skill**, **project interests**, **location** or **radius**.

### 📄 Smart Posts

* Users can post **Projects**, **Bugs**, or **Ideas** within their geofence.
* Posts support: **title, description, skills needed, project links, and attachments(media)**.
* Users can **like**, **comment**, **share** post.

### 💬 Real-Time Chat (Secure)

* Fully encrypted real-time messaging using **Signal Protocol**
* Features:

  * **One-to-one & group chats**
  * **Media sharing** (images, files)
  * Preview for **links**.
  * **Priority messages**, **typing indicator**, **online/last seen** and **read receipts**
  * Secure message using **encryption/decryption** delivering confidentiality.

### 📩 Push Notifications

* Powered by **Firebase Cloud Messaging (FCM)**
* Sent when:

  * A new **chat message** arrives
  * A user create a **new post**
  * A user receives a **project request**
  * A post is interacted with **(like/comment)**
  * A user's profile gets **5 new views**
  * A user profile is **rated/verified**
* Handled with custom logic to show user-specific rich notifications with action buttons

### 👥 Profile Showcase

* Rich profile structure with:

  * **GitHub, LinkedIn, Portfolio** links
  * **Skills**, **Experience**, **Education**
  * Work history & open-to-collab status

### ❌ Block & Report System

* Users can block/report others for spam or abuse
* Once blocked stop showing profile photo, online/last seen, cannot send message.
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
| **UI**               | Jetpack Compose          |
| **Architecture**     | MVVM + Clean Architecture + Repository Pattern |
| **State Mgmt**       | StateFlow, LiveData                            |
| **Network**          | Retrofit, REST APIs, Gson                      |
| **DI**               | Dagger-Hilt                             |
| **Images/Media**     | Glide, Cloudinary (uploading media)            |
| **Map & Location**   | Google Maps SDK, Geofencing APIs, GeoFire      |
| **Database**         | Room (offline support), Firebase Firestore     |
| **Storage**          | Firebase Storage, Cloudinary                   |
| **Notifications**    | Firebase Cloud Messaging (FCM)                 |
| **Encryption**       | Signal Protocol (E2EE)                         |
| **Background Tasks** | WorkManager, BroadcastReceiver, AlarmManager   |
| **Hosting**          | Firebase   |

---

## ⚙️ Implementation Details  

### ⛰️ Map System
- **Tech:** Google Maps SDK + GeoFire, Work Manager, Foreground Service  
- Used **geofencing** to detect students/professionals in real-time.  
- Implemented **live map markers** synced with geo fire and Firestore within specified radius and location.  
- Built **advanced filters** (role, skill, project interest, radius) with Firestore composite queries.
- Utilized Work Manager and Foreground Service to track location in background for real time updates(user can off service when not required).
  
Refer - [Map DataSource](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/MapDataSource), [Location Repository](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/MapDataSource),
[Map Screen](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/presentation/main/Home/Map)


### 📄 Smart Posts  
- **Tech:** Firebase Firestore, Room, Cloudinary
- Posts stored under `posts/{postId}` with metadata.  
- Media uploads handled via **Cloudinary**.  
- Real-time listeners (`snapshotListener`) keep feed updated.  
- Likes/comments stored as subcollections (`posts/{postId}/likes` & `posts/{postId}/comments`).  
- Integrated **deep links** for sharing posts externally.
- Can be stored in room using Firebase persistence for offline experience.\

Refer - [Post Repository Impl](https://github.com/Vishal01x/Findr/blob/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/Repository/ProjectRepository.kt), [Post Listing](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/presentation/main/Home/Listing)

### 💬 Real-Time Chat (Secure)  
- **Tech:** Firestore + Signal Protocol  
- End-to-end encrypted chats.  
- Supports **1-to-1 & group chats** under `chats/{chatId}/messages/{messageId}`.
- Media stored in Cloudinary and seemless sharing of photo, video, docs.
- Show Preview of links using metadata like domain, description, title extracted via Jsoup and stored in local cache using room database.
- Features: typing indicators, online status, read receipts using real time firebase and android lifecycle for tracking user session.
- User can be blocked and reported with screenshots of chat or etc. Once blocked can't message further, managed by keeping the block users map in chats and as per perform actions.

Refer -[ Chat Repository](https://github.com/Vishal01x/Findr/blob/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/Repository/FireStoreService.kt), [Chat Screen](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/presentation/main/Home/ChatDetail)

### 📩 Push Notifications  
- **Tech:** Firebase Cloud Messaging, Notification Manager  
- Notification types: **Chat, Post, Project Request, Profile Events**.
- Provide both topic and token based notification.
- Send Payload to server through a retrofit with authorization token that will be recieved by notification manager.
- Custom channels with unique icons & sounds.  
- **Deep links** open directly to screens (chat, post, profile) provided in Pending Intent.  
- Action buttons (Reply/Accept/Reject/Mark Read) via **BroadcastReceivers**.  
- Unread states grouped via **SharedPreferences** for each notification like chat, post, profile, update.

Refer - [Push Notification FCM](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/fcm), [Helper](https://github.com/Vishal01x/Findr/blob/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/Repository/PushNotificationImpl.kt)

### 👥 Profile Showcase  
- **Tech:** Firestore + Jetpack Compose  
- Schema: `name, bio, skills, education, experience, portfolioLinks`.  
- Integrated deep links for external profiles (GitHub, LinkedIn, Portfolio).  
- Profile view tracking under `profiles/{userId}/views/{viewerId}` with timestamps.  
- Built **Compose UI** with reusable skill chips, experience cards, and status badges.
- User can be verified(can check verifiers profile) and rated(takes the avg for rating).

Refer  - [Profile Repo](https://github.com/Vishal01x/Findr/blob/master/app/src/main/java/com/exa/android/reflekt/loopit/data/remote/main/Repository/ProfileRepository.kt), [Profile UI](https://github.com/Vishal01x/Findr/tree/master/app/src/main/java/com/exa/android/reflekt/loopit/presentation/main/profile)

- The whole dependacy injection is manager using **Hilt**.
- Provided settings and help center for user action and customization.


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

* 👉 **450+ Play Store downloads** since deployed
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
* Firebase admin sdk 
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

## 👨‍💻 Contributors & Roles

- **Vishal Dangi** – Android Development + UI/UX
  - Implemented Posting, Real-time Chat, Profile, Notifications, Services
  
- **Kanhaiya kumar** – Android Development + UI/UX
   - Created ui/ux design and user flow.
   - Implemented authentication.
---

## 👤 Author

**Vishal Dangi**
[LinkedIn](https://www.linkedin.com/in/vishal-dangi-14805725b) | [GitHub](https://github.com/Vishal01x) | [Email](mailto:vishaldangi01x@gmail.com)

---

## ✨ If you like this project...

Give it a ⭐ star and share it with your peers!
