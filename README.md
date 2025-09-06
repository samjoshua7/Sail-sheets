# SailSheets 📜⛵

A pirate-themed **digital homework diary app** designed for student
groups and teams.\
Inspired by the idea of a pirate ship (`Blazing Pirates` team), the app
allows users to manage daily tasks and homework assignments in an
organized way --- similar to Google Classroom but more lightweight and
fun.

![App Broucher](https://github.com/samjoshua7/Sail-sheets/blob/main/Sail%20Sheets%20Promo%20Broucher.png?raw=true)

------------------------------------------------------------------------

## 🚀 Project Overview

SailSheets is a **daily task and homework management application**.\
- Only the **admin** (teacher/leader) can directly post homework.\
- Team members can also submit homework posts, but those require admin
approval before becoming visible to everyone.\
- A **date-picker calendar** lets students quickly navigate to specific
days and view tasks.

This app aims to provide a simple and engaging way to **stay on track
with studies and tasks**.

![App Broucher](https://github.com/samjoshua7/Sail-sheets/blob/main/Sail%20Sheets%20Launcher.png?raw=true)

------------------------------------------------------------------------

## ✨ Features

-   **User Authentication**
    -   Login and Register system using Firebase Authentication.
    -   Role-based permissions (admin, permitted user, waiting for
        approval).
-   **Homework Posting**
    -   Admin can post homework directly.\
    -   Non-admin users can submit homework requests (visible only after
        admin approval).
-   **Calendar View**
    -   A date picker allows users to switch between days and view
        corresponding tasks.
-   **Task Management**
    -   View, add, and manage homework in a scrollable list format.\
    -   Posts are structured with title, description, and timestamp.
-   **Pirate-Themed UI**
    -   Fun and engaging interface to make learning enjoyable.

------------------------------------------------------------------------

## 🛠 Tech Stack

-   **Frontend:** Android (Java, XML)\
-   **Backend:** Firebase Realtime Database\
-   **Authentication:** Firebase Authentication\
-   **IDE:** Android Studio

------------------------------------------------------------------------

## 📂 Folder Structure

    SailSheets/
    │
    ├── app/                  # Main Android app source
    │   ├── manifests/        # AndroidManifest.xml
    │   ├── java/             # Application logic (activities, models, adapters)
    │   └── res/              # Layouts, drawables, themes
    │
    ├── build.gradle          # Gradle build configs
    ├── settings.gradle       # Project settings
    └── README.md             # Project documentation

------------------------------------------------------------------------

## ⚙️ Setup & Installation

1.  Clone the repository:

    ``` bash
    git clone https://github.com/yourusername/sailsheets.git
    ```

2.  Open in **Android Studio**.

3.  Add your Firebase configuration:

    -   Download `google-services.json` from Firebase Console.\
    -   Place it in `app/` folder.

4.  Sync Gradle and Run the app.

------------------------------------------------------------------------

## 🌟 Future Enhancements

-   Push Notifications for homework reminders.\
-   File Attachments in homework posts.\
-   Multi-language support.\
-   Web dashboard for teachers/admins.

------------------------------------------------------------------------

## 📜 License

This project is licensed under the **MIT License**.\
Feel free to modify and use it for educational purposes.

------------------------------------------------------------------------

## 👨‍💻 Author

Developed by **Sam Joshua** (Team Blazing Pirates ⚓).\
Aimed to make study planning simple and collaborative.
