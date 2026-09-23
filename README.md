# DASTAK VANI (दस्तक वानी) — "The Voice That Knocks"

> **A Voice-First Civic Accountability Android Platform for Rural North Bihar**  
> *Empowering marginalized, zero-literacy citizens to speak their grievances, verify via audio feedback, categorize on-device with ML, route deterministically to authorities and NGOs, and auto-escalate into formal legal RTI applications.*

---

## 🌟 The Core Mission

In rural North Bihar (districts such as Muzaffarpur, Sitamarhi, Madhubani, Darbhanga, and West Champaran), millions of citizens are effectively barred from civic grievance redressal because existing portals are text-heavy, English/formal Hindi dependent, and bureaucratic.

**DASTAK VANI** bridges this digital divide with a **Voice-First, Offline-First, Zero-Gimmick** architecture:
1. **Citizen Speaks**: Speaks naturally in colloquial Hindi/Bihari dialects.
2. **On-Device ML**: Classifies complaints across 4 civic pillars (**Education**, **Civic Neglect**, **Welfare**, **Child Safety**) using an embedded, 100% on-device TF-IDF + Logistic Regression model (<2ms latency, zero cloud dependency).
3. **Proof-of-Hearing**: Plays back extracted entities and category via native Text-to-Speech in Hindi for low-literacy verification before submission.
4. **Dual Routing**: Transmits grievances deterministically to relevant district authorities and partner NGOs.
5. **Village Wall**: Public, anonymized civic accountability board masking all PII.
6. **Auto-Escalation & RTI**: Monitors resolution deadlines (>15/30 days) and generates formal **Section 6(1) Right to Information (RTI) Act, 2005** legal petitions.

---

## 🏗️ Architecture & Technology Stack

```
[ Citizen Speech Input ] 🎙️ (Native Android SpeechRecognizer + RMS Visualizer)
             │
             ▼
[ On-Device ML Classifier ] 🧠 (TF-IDF + Logistic Regression, 92.50% Acc, 320 KB)
             │
             ├──▶ [ Entity Extractor ] 🏷️ (Name, Phone, Village, Landmark, PII Masking)
             ├──▶ [ TTS Proof-of-Hearing ] 🔊 (Native Android TextToSpeech confirmation)
             │
             ▼
[ Persistent Repository ] 💾 (Local atomic JSON engine; Firestore-ready cloud adapter)
             │
             ├──▶ [ Public Village Wall ] 🏘️ (Anonymized collective grievance stream)
             ├──▶ [ Dual Routing Engine ] 📮 (District Authority + Grassroots NGO)
             ├──▶ [ NGO Volunteer Portal ] 🤝 (Auth, complaint claiming, status transitions)
             └──▶ [ RTI Escalation Worker ] ⚖️ (WorkManager + Section 6(1) RTI Generator)
```

- **Platform**: Android (Min SDK 26, Target SDK 35, Compile SDK 35)
- **Language**: 100% Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose with Material 3 (White + Blue Civic Design System)
- **Machine Learning**: Custom scikit-learn TF-IDF + Multinomial Logistic Regression pipeline exported to compact parameter JSON (`320 KB`), inferred via pure Kotlin dot-product engine in `<2ms`.
- **Speech & Audio**: Android Native `SpeechRecognizer` + RMS amplitude listener + Android `TextToSpeech`
- **Location**: Android Location API + Geocoder fallback
- **Concurrency**: Kotlin Coroutines & Flows
- **Background Tasks**: AndroidX WorkManager for periodic escalation checks

---

## 📊 On-Device ML Model Metrics

The grievance classification model was trained on 800 balanced civic complaints across rural North Bihar categories:

| Metric | Score |
|---|---|
| **Test Accuracy** | **92.50%** |
| **Macro F1 Score** | **0.9256** |
| **Weighted F1 Score** | **0.9256** |
| **Model Size** | **320 KB** (`assets/dastakvani_ml_model.json`) |
| **Inference Latency** | **< 2 ms** (100% on-device, zero cloud calls) |
| **Vocabulary Size** | 2,139 unigrams & bigrams (Hindi & Hinglish) |

For complete dataset attribution, confusion matrix, and training methodology, see [`docs/ML_MODEL_REPORT.md`](docs/ML_MODEL_REPORT.md).

---

## 🚀 Quick Start & Build Instructions

### Prerequisites
- **Android Studio**: Ladybug / Meerkat (or command line Android SDK Platform 35)
- **JDK**: OpenJDK 21 (bundled with Android Studio JBR at `C:\Program Files\Android\Android Studio\jbr`)
- **Gradle**: 8.14.3 (included via `gradlew`)

### Build & Run Tests
```bash
# 1. Run all unit tests
./gradlew test

# 2. Assemble Debug APK
./gradlew assembleDebug
```

The generated APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk (~17.4 MB)
```

### Install onto Connected Device / Emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔑 Demo Credentials & Test Workflow

- **NGO / Volunteer Portal Login**:
  - **Email**: `volunteer@biharngo.org`
  - **Password**: `dastak123`
- **Pre-Seeded Sample Complaints**:
  - `DV-20260901-7F2A`: Welfare (Ration card cut off in Saraiya) — *Overdue (>15 days, eligible for RTI draft)*
  - `DV-20260910-3C1B`: Education (Primary school roof collapsed in Bochahan) — *Investigating*
  - `DV-20260915-8E4D`: Civic Neglect (Handpump dry for 2 months in Sakra) — *Submitted*
  - `DV-20260918-1A9F`: Child Safety (12-year-old forced labor at brick kiln) — *High Priority Action*

For a complete step-by-step 3–5 minute walkthrough, see [`docs/FINAL_DEMO_TEST_PLAN.md`](docs/FINAL_DEMO_TEST_PLAN.md).

---

## 📑 Documentation Index

- 📘 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — System architecture, package breakdown, security & PII handling.
- 🔬 [`docs/ML_MODEL_REPORT.md`](docs/ML_MODEL_REPORT.md) — ML dataset sources, features, evaluation metrics, and on-device deployment.
- 🎬 [`docs/FINAL_DEMO_TEST_PLAN.md`](docs/FINAL_DEMO_TEST_PLAN.md) — Complete demo script and test execution steps.
- ⚠️ [`docs/KNOWN_LIMITATIONS.md`](docs/KNOWN_LIMITATIONS.md) — Honest engineering boundaries (Speech, Emulator vs. Device, Demo routing endpoints).
- ✅ [`docs/FINAL_REVIEW_CHECKLIST.md`](docs/FINAL_REVIEW_CHECKLIST.md) — Complete 20-item P0/P1 verification matrix.

---

## ⚖️ License & Public Good Attribution
Built for community empowerment and transparent civic governance under the MIT License.
Datasets utilized strictly from open public sources (Municipal Complaints & CPGRAMS).
