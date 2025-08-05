# Jejak Anak (Child Tracker) 🚀
![Android API](https://img.shields.io/badge/API-24%2B-brightgreen?style=flat-square)

<div style="width: 100%; max-width: 1024px; height: 500px; overflow: hidden; margin: auto; border-radius: 8px;">
<img style="width: 100%; height: 100%; object-fit: cover; object-position: center;" alt="Banner Aplikasi Jejak Anak" src="https://github.com/ghifari21/Jejak-Anak-Android/blob/1d2c1067c61458c044976109cf78208ecb204952/imgs/Jejak%20Anak%20Mockup.png" />
</div>

**Jejak Anak** is a child location tracking application designed to give parents peace of mind. Monitor your child's location in *real-time* and easily set up safe zones.

## 🔥 Features

✅ **Real-time Location Tracking:** Monitor your child's location in real-time.<br/>
✅ **Geofencing:** Create virtual fences and get notified when your child enters or leaves a designated area.<br/>
✅ **Safe & Danger Zones:** Define safe and danger zones for your child.<br/>
✅ **Parent & Child Roles:** The app has two distinct roles, one for the parent and one for the child.<br/>
✅ **Location Sharing:** Children can share their location with their parents.<br/>
✅ **Multi-Child Management:** Parents can add and manage multiple children.<br/>
✅ **Background Service:** Tracking keeps running even when the app is minimized.

## 🛠 Tech Stack

Built with modern Android tech:

- **UI:** [View System (XML)](https://developer.android.com/guide/topics/ui/declaring-layout) & [Material Design 3](https://m3.material.io/)
- **DI:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Networking & Database:** [Firebase Realtime Database](https://firebase.google.com/docs/database)
- **Authentication:** [Firebase Authentication](https://firebase.google.com/docs/auth)
- **Maps:** [Google Maps Platform](https://developers.google.com/maps)
- **Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Architecture:** MVVM + Clean Architecture
- **Image Loading:** [Glide](https://github.com/bumptech/glide)
- **UI States:** [MultiStateView](https://github.com/Kennyc1012/MultiStateView)

## 📸 Screenshots
<div style="display: flex; flex-wrap: wrap; gap: 1%;">
  <img src="https://github.com/ghifari21/Jejak-Anak-Android/blob/1d2c1067c61458c044976109cf78208ecb204952/imgs/Login%20Page.png" alt="Jejak Anak 1" width="24%" />
  <img src="https://github.com/ghifari21/Jejak-Anak-Android/blob/1d2c1067c61458c044976109cf78208ecb204952/imgs/Maps%20Page.png" alt="Jejak Anak 2" width="24%" />
  <img src="https://github.com/ghifari21/Jejak-Anak-Android/blob/1d2c1067c61458c044976109cf78208ecb204952/imgs/Geofence%20List%20Page.png" alt="Jejak Anak 3" width="24%" />
  <img src="https://github.com/ghifari21/Jejak-Anak-Android/blob/1d2c1067c61458c044976109cf78208ecb204952/imgs/Children%20List%20Page.png" alt="Jejak Anak 4" width="24%" />
</div>

## 🚀 Installation
1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/ghifari21/jejak-anak-android.git](https://github.com/ghifari21/jejak-anak-android.git)
    ```
2.  **Open in Android Studio:** Open the cloned repository in Android Studio.
3.  **Firebase Configuration:**
    * Go to the [Firebase Console](https://console.firebase.google.com/).
    * Create a new project or use an existing one.
    * Add an Android app to your Firebase project with the package name `com.gosty.jejakanak`.
    * Download the `google-services.json` file.
    * Place the `google-services.json` file in the `app` directory of the project.
4.  **Google Maps API Key:**
    * Go to the [Google Cloud Console](https://console.cloud.google.com/google/maps-apis/overview).
    * Enable the "Maps SDK for Android".
    * Create an API key.
    * Create a `local.properties` file in the root of the project (if it doesn't exist).
    * Add the following line to your `local.properties` file, replacing `YOUR_API_KEY` with your actual key:
        ```
        MAPS_API_KEY=YOUR_API_KEY
        ```
5.  **Build and Run:** Build and run the app on an emulator or a physical device.

## 🤝 Contributing

Want to contribute? Let’s go!

1.  Fork this repo
2.  Create a new branch, commit & push:
    ```bash
    git checkout -b feature/awesome-feature
    git commit -m "Add some awesome feature"
    git push origin feature/awesome-feature
    ```
3.  Open a pull request 🚀

## 📜 License
This project is licensed under the **MIT License**.

## 💌 Support
Found a bug or have an idea?
- 👉 [Open an issue](https://github.com/ghifari21/jejak-anak-android/issues)

---
Made with ❤️ by [Ghifari](https://github.com/ghifari21)
