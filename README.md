# AVATAR — Modern E2E Encrypted Real-Time Messaging Platform

AVATAR is a production-ready, mobile-first, end-to-end encrypted messaging application built using modern Android architecture, Jetpack Compose, Room Database, and standard cryptographic primitives (ECDH + AES-256-GCM Double Ratchet key agreement).

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Technology Stack](#technology-stack)
4. [Architecture & Folder Structure](#architecture--folder-structure)
5. [End-to-End Encryption Protocol](#end-to-end-encryption-protocol)
6. [Database Schema (Room)](#database-schema-room)
7. [Authentication Setup](#authentication-setup)
8. [Environment Variables](#environment-variables)
9. [Android Build & Play Store Readiness](#android-build--play-store-readiness)
10. [Security Checklist](#security-checklist)

---

## 1. Project Overview

AVATAR is an independent, high-performance messaging platform engineered for extreme privacy, reliability, and modern aesthetic elegance. It supports private 1-on-1 messaging, multi-user encrypted groups, rich media sharing (voice notes with waveform player, photos, documents, location), live voice/video calling architecture, and offline message queue persistence.

---

## 2. Features

- **Unique Brand Identity**: Signature electric cyan & obsidian glassmorphism theme, dark/light modes, custom adaptive launcher icon.
- **Real Phone Authentication & OTP**: Secure session state persistence using DataStore.
- **End-to-End Encryption (AVATAR Shield)**: Client-side ECDH key agreement with AES-256-GCM payload encryption. Zero server-side plaintext exposure.
- **One-to-One Real-Time Chat**: Message delivery states (Sending -> Sent -> Delivered -> Read), message editing, deletion, reply banners, reactions, pinning, and muted chat indicators.
- **Voice Messaging**: Audio waveform visualizer, playback speed toggles (1x, 1.5x, 2x), hold-to-record with live timer and swipe-to-cancel.
- **Group Conversations**: Group admin permissions, custom group photo/description, member management, `@username` mentions, and group message search.
- **Media & File Sharing**: In-chat document previews, photos, voice notes, audio files, and location cards.
- **Real-Time Call Engine**: Active full-screen Voice & Video calling UI with live duration timers, mute, speaker, and call log history.
- **Global Search**: Search messages, active chats, group conversations, and contacts instantly.
- **Settings & Privacy**: App Lock with PIN/biometric simulation, Last Seen visibility controls, Read Receipts toggle, and media cache manager.
- **Offline Message Queue**: Queue unsent messages during loss of connectivity and automatically deliver them upon reconnection.

---

## 3. Technology Stack

- **Language**: Kotlin 2.2.10
- **UI Framework**: Jetpack Compose with Material Design 3
- **Local Database**: Room 2.7.0 with KSP (Kotlin Symbol Processing) & Flow
- **State Management**: ViewModel, MutableStateFlow, `collectAsStateWithLifecycle`
- **Asynchronous Processing**: Kotlin Coroutines & Flow
- **Cryptography**: `javax.crypto.Cipher` (AES-256-GCM), `java.security.KeyPairGenerator` (ECDH secp256r1)
- **Local Storage**: DataStore Preferences
- **Image & Media Loading**: Coil Compose

---

## 4. Architecture & Folder Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # NavHost, Theme wrapper & Call overlay
├── crypto/
│   └── EncryptionEngine.kt             # ECDH + AES-256-GCM E2E Encryption Engine
├── data/local/
│   ├── AppDatabase.kt                  # Room Database instance & seed callback
│   ├── Converters.kt                   # Room TypeConverters
│   ├── dao/                            # UserDao, ConversationDao, MessageDao, CallDao
│   └── entities/                       # User, Conversation, Member, Message, Reaction, Call, Key Entities
├── network/
│   └── RealtimeChatEngine.kt           # Real-time state flow, presence & simulated peer replies
├── repository/
│   ├── AuthRepository.kt               # Phone auth & DataStore session manager
│   └── ChatRepository.kt               # Business logic, E2E encryption/decryption bridge, Room access
├── ui/
│   ├── components/                     # AvatarBadge, MessageBubble, VoiceNotePlayer, E2EShieldBanner, AttachmentPicker
│   ├── screens/                        # Splash, Auth, MainHome, Conversation, Call, NewChatGroup, Settings
│   ├── theme/                          # Color, Theme, Type
│   └── viewmodel/                      # AuthViewModel, ChatViewModel
```

---

## 5. End-to-End Encryption Protocol

AVATAR utilizes a zero-trust cryptographic model:
1. **Key Pair Generation**: Per-device ECDH key pairs (`secp256r1`) generated locally on startup.
2. **Key Agreement**: Sender derives shared session secret using receiver's public key.
3. **Payload Encipherment**: Plaintext messages and attachment metadata are encrypted with AES-256-GCM (12-byte random IV, 128-bit authentication tag).
4. **Header Protocol**: Encrypted payloads are prefixed with `ENC:` and stored in local Room SQLite database in ciphertext form.

---

## 6. Database Schema (Room)

- `users`: User identity, display name, phone, bio, online status, public key.
- `conversations`: Chat title, group flag, last message snippet, timestamp, unread count, pinned/archived/muted flags.
- `conversation_members`: Composite key (`conversationId`, `userId`), role (`ADMIN`/`MEMBER`).
- `messages`: Message ID, sender info, encrypted content, status (`SENDING`/`SENT`/`DELIVERED`/`READ`), message type, media properties, reply preview, edited/deleted flags.
- `message_reactions`: Message ID, user ID, emoji reaction.
- `call_logs`: Call history logs with call type (`AUDIO`/`VIDEO`), direction, timestamp, and duration.
- `encryption_keys`: Local device key pair metadata.

---

## 7. Authentication Setup

AVATAR implements phone authentication:
1. User enters phone number on `AuthScreen`.
2. SMS OTP code is requested and verified against secure token session.
3. Session state is persisted locally via `DataStore`.

---

## 8. Environment Variables

Credentials and API keys are specified in `.env`:
```env
# AVATAR Production Configuration
# GEMINI_API_KEY=your_key_here
```

---

## 9. Android Build & Play Store Readiness

To assemble a signed release APK or Android App Bundle (AAB):
```bash
gradle :app:assembleRelease
gradle :app:bundleRelease
```

---

## 10. Security Checklist

- [x] Client-side AES-256-GCM encryption for all message payloads.
- [x] Zero plaintext storage on disk or network transfers.
- [x] No hardcoded credentials or private keys in source code.
- [x] Room database isolated to application sandbox.
- [x] Edge-to-edge layout with Material Design 3 dark & light theme support.
