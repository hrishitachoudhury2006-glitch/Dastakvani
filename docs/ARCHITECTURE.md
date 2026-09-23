# Dastak Vani — Technical Architecture Specification

## 1. Executive Architectural Summary
**Dastak Vani ("The Voice That Knocks")** is an Android-native civic accountability platform architected for marginalized, low-literacy communities in rural North Bihar. The application employs Clean Architecture and MVI/MVVM design patterns to ensure strict separation of concerns, high offline resilience, deterministic accountability routing, and zero-literacy multimodal accessibility.

```
                      +---------------------------------------+
                      |           PRESENTATION LAYER          |
                      |   Jetpack Compose + Material 3 UI     |
                      |     White + Blue Civic Palette        |
                      +-------------------+-------------------+
                                          |
                      +-------------------v-------------------+
                      |            DOMAIN LAYER               |
                      |  - Complaint & Category Data Models   |
                      |  - Entity Extraction Engine           |
                      |  - Deterministic Dual Routing Engine  |
                      |  - Section 6(1) RTI Draft Generator   |
                      +-------------------+-------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
+-------v-------+                 +-------v-------+                 +-------v-------+
|  DATA / ML    |                 | SPEECH & TTS  |                 | PERSISTENCE & |
|  On-Device    |                 | Android STT   |                 | WorkManager   |
|  TF-IDF +     |                 | + Proof-of-   |                 | - Local Room/ |
|  Logistic Reg |                 |   Hearing TTS |                 |   JSON Cache  |
|  Inference    |                 |   Engine      |                 | - Escalation  |
+---------------+                 +---------------+                 +---------------+
```

---

## 2. Layered Component Details

### A. Presentation Layer
- **Framework**: Jetpack Compose with Material 3 design system.
- **Theme**: Civic White + Blue (`#1565C0` Primary Blue, `#0D47A1` Navy, `#FFFFFF` Background, `#F8FAFC` Surface, `#E3F2FD` Containers).
- **Zero-Literacy Design**:
  - Oversized primary touch targets (140dp microphone recording button).
  - High-contrast visual state indicators and animated pulse waveforms.
  - Spoken audio instructions via Text-to-Speech upon single-tap on guidance cards.
  - Bilingual localization: Hindi (`hi_IN`) as primary language with English (`en_IN`) toggle.

### B. Speech Recognition & Proof of Hearing
- **Speech Pipeline**: Native Android `SpeechRecognizer` using `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` configured for `hi-IN` and `en-IN`.
- **Audio Feedback**: Live RMS audio level normalization (-2dB to 10dB mapped to 0..1) driving reactive UI audio waves.
- **Proof of Hearing**: Immediate spoken confirmation via `TtsManager` playing back the interpreted grievance in Hindi before final submission. Complainant can confirm with one tap or immediately re-record.

### C. On-Device Machine Learning Inference Engine
- **Engine**: `GrievanceClassifier.kt`
- **Model Representation**: Serialized JSON parameters (`app/src/main/assets/dastakvani_ml_model.json`, 320 KB).
- **Vectorization**: Sublinear TF-IDF featurizer with unigram and bigram extraction across Devanagari and English Unicode ranges.
- **Classification**: Multinomial Logistic Regression (`C=3.0`, 4 output classes).
- **Latency**: Under 2 milliseconds per query.
- **Safety Net**: Transparent rule-based fallback triggered if model confidence falls below 30%, in strict adherence to Section 20 of project specifications.

### D. Entity Extraction & Data Masking
- **Engine**: `EntityExtractor.kt`
- **Phone Extraction**: Regex parser identifying 10-digit Indian mobile numbers (`[6-9]\d{9}`).
- **Name Extraction**: Contextual phrase parsing for Hindi ("मेरा नाम...", "हमार नाम...") and English ("My name is...").
- **Locality Detection**: Panchayat, village, ward, and district extraction.
- **Privacy Enforcement**:
  - Full redaction of phone numbers in public feeds (`getAnonymizedView()`).
  - Name masking (`नागरिक र***`).
  - Suppression of precise street specifics for high-risk Child Safety grievances.

### E. Dual-Channel Routing Engine
- **Engine**: `RoutingEngine.kt`
- **Mechanism**: Deterministic simultaneous routing to:
  1. Competent Government Authority (District Education Officer, Executive Engineer PHED/RWD, District Supply Officer, District Child Protection Unit).
  2. Active Civil Society / NGO Partner (Pratham Education Foundation, Gram Vaani, Haqdarshak, Bachpan Bachao Network).
- **Honest Configuration**: Demo/test routing endpoints clearly marked.

### F. Escalation & Legal RTI Generator
- **Engine**: `RtiGenerator.kt`
- **Statutory Framework**: Section 6(1) of the Right to Information Act, 2005.
- **Trigger**: Complaints exceeding the 15-day administrative threshold without progress.
- **Background Scheduling**: Android `WorkManager` running periodic 12-hour checks (`EscalationWorker.kt`).

---

## 3. Security, Authorization & Privacy
- **Volunteer / NGO Portal**: Role-based authentication gate. Only authenticated NGO officers can modify grievance status or append field verification notes.
- **Zero Secrets in Code**: API keys and secrets excluded from version control via `.gitignore` and `local.properties`.
- **Zero Data Leakage**: Public endpoints only query `getAnonymizedView()`.
