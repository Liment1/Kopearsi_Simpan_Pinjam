# Koperasi Simpan Pinjam (Digital Cooperative Application)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue?style=flat-square)
![Platform](https://img.shields.io/badge/Platform-Android-green?style=flat-square)
![Backend](https://img.shields.io/badge/Backend-Firebase-orange?style=flat-square)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-lightgrey?style=flat-square)

## Project Overview

Koperasi Simpan Pinjam is a native Android application designed to modernize the operations of savings and loan cooperatives. By transitioning from manual workflows to a secure digital ecosystem, the application facilitates real-time financial transactions, automated credit assessment, and seamless communication between members and administrators.

The system is built on a robust MVVM architecture, utilizing Firebase for backend services and integrating with external ML models for intelligent credit scoring.

## App Demo

Experience the core flows of the application, including Instant Loan Approval and Real-Time Savings updates.

![Application Demo](./assets/KSP.gif)
*(Note: This demo highlights the member-side loan application and admin approval workflow)*

## Key Features

### 1. Member Module (User)
* **Smart Loan Application**:
    * **AI Credit Scoring**: Integrates with an external Machine Learning model via REST API to assess borrower risk based on demographic and financial data.
    * **Auto-Calculation**: Automatically calculates service fees, installment amounts, and total repayment projections.
* **Digital Savings**:
    * **Real-time Balance**: Live tracking of Principal, Mandatory, and Voluntary savings.
    * **Instant Transactions**: Secure deposit and withdrawal requests with proof-of-payment uploads.
* **Installment Management**:
    * **Flexible Payments**: Options to pay via manual transfer proof or direct deduction from savings balance.
    * **Status Tracking**: Visual indicators for Paid, Pending, and Overdue installments.
* **Profile & Identity**: Secure management of personal data with native camera integration for identity verification.

### 2. Administrator Module (Management)
* **Executive Dashboard**:
    * **Visual Analytics**: Interactive Line Charts (MPAndroidChart) visualizing revenue trends, net profit, and total assets.
    * **Operational Metrics**: Real-time counters for active loans, total savings, and cash flow.
* **Transaction Oversight**:
    * **Approval Workflow**: Review process for deposits and loan applications.
    * **Installment Verification**: validation of transfer proofs submitted by members.
* **Financial Reporting**:
    * **PDF Generation**: Native generation of professional financial reports using Android Canvas and MediaStore API, complying with Android 10+ Scoped Storage standards.
* **Broadcast System**:
    * **Push Notifications**: Integrated Firebase Cloud Messaging (FCM) console to send announcements or urgent updates to all members.
    * **Profit Distribution**: Tools to calculate and distribute SHU (Sisa Hasil Usaha) to members.

## Technical Architecture

The project adheres to the Model-View-ViewModel (MVVM) pattern, ensuring a clear separation of concerns and data-driven UI updates.

### Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin | Primary development language. |
| **UI Framework** | XML / ViewBinding | Native UI components with material design. |
| **Concurrency** | Coroutines & Flow | Asynchronous programming for network and DB operations. |
| **Database** | Cloud Firestore | NoSQL database with atomic batch transactions. |
| **Network (ML)** | Retrofit 2 | Interface for connecting to the Credit Scoring AI Model. |
| **Notifications** | Firebase Cloud Messaging | Remote push notifications and background services. |
| **Storage** | Firebase Storage | Cloud hosting for transaction proofs and profile images. |
| **Charting** | MPAndroidChart | Data visualization libraries for the Admin Dashboard. |
| **Image Loading** | Glide / Coil | Efficient image caching and rendering. |

### Key Implementations

* **Atomic Transactions**: Critical financial operations (like loan disbursement or balance deduction) utilize Firestore `runTransaction` and `WriteBatch` to ensure data integrity.
* **Scoped Storage Compliance**: File exports (PDFs) utilize the `MediaStore` API to ensure compatibility with Android 11+ privacy standards without requiring broad storage permissions.
* **Event-Driven UI**: `LiveData` and `StateFlow` are used to propagate database changes instantly to the UI.

## Installation and Setup

### Prerequisites
* Android Studio Iguana or newer.
* Minimum SDK: API 24 (Android 7.0).
* Target SDK: API 34 (Android 14).
* A valid Firebase project.
* An active API Endpoint for the Credit Scoring Model (optional for UI testing).

### Configuration Steps

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/yourusername/koperasi-simpan-pinjam.git](https://github.com/yourusername/koperasi-simpan-pinjam.git)
    ```

2.  **Firebase Setup**
    * Create a project in the Firebase Console.
    * Add an Android app with package `com.example.project_map`.
    * Enable **Authentication**, **Firestore**, **Storage**, and **Messaging**.
    * Download `google-services.json` to the `app/` directory.

3.  **API Configuration**
    * Navigate to `com.example.project_map.api.ApiClient`.
    * Update the `BASE_URL` to point to your deployed Credit Scoring ML model.

4.  **Build**
    * Sync Gradle dependencies.
    * Run on an emulator or physical device.

## Permissions

The application requests the following permissions for full functionality:

* `INTERNET`: Network access for API and Firebase.
* `CAMERA`: Capturing identity documents.
* `READ_MEDIA_IMAGES`: Accessing gallery for payment proofs.
* `POST_NOTIFICATIONS`: Receiving loan status updates (Android 13+).
* `WRITE_EXTERNAL_STORAGE`: Legacy support for file saving on older Android versions.

## Future Roadmap

* **Server-Side Logic**: Migrating balance calculation from client-side to Firebase Cloud Functions for enhanced security.
* **Biometric Auth**: Implementing Fingerprint/FaceID login.
* **Payment Gateway**: Integration with Xendit/Midtrans for automated Virtual Account payments.
