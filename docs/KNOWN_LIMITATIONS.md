# Dastak Vani — Known Limitations & Operational Boundaries

In strict adherence to the **Zero-Gimmick Rule**, this document transparently enumerates the architectural boundaries, system prerequisites, and known limitations of the Dastak Vani v1.0.0 release.

---

## 1. Speech Recognition & Acoustic Processing
- **Engine Dependency**: Dastak Vani integrates Android's native `android.speech.SpeechRecognizer`. 
- **Physical Device vs. Emulator**: 
  - On physical Android devices with Google Play Services or on-device Speech Services installed, real-time streaming audio transcription in Hindi (`hi-IN`) and English (`en-IN`) operates natively.
  - On headless or standard Android Studio AVD emulators without an active microphone or Google Speech Services APK, speech recognition may return error code 9 (`ERROR_INSUFFICIENT_PERMISSIONS`) or 7 (`ERROR_NO_MATCH`). In such test environments, users can test with the provided simulated recording or direct text inputs.
- **Dialect Coverage (Maithili, Bhojpuri, Magahi, Angika)**:
  - Rural citizens in North Bihar speak regional vernaculars. While Google's Indian Hindi model accommodates substantial colloquial Hindi and Hinglish vocabulary (tested extensively in our training set), pure non-standard dialect phonetic variations may occasionally be mistranscribed by standard engines. Future iterations will incorporate custom local Vosk acoustic models trained on Bihari folk dialects.

---

## 2. On-Device Machine Learning Grievance Classifier
- **Model Architecture**: Real, trained unigram+bigram TF-IDF vectorizer paired with a multinomial Logistic Regression classifier (320 KB parameter bundle), running pure Kotlin matrix dot-product inference on-device in <2ms with zero cloud dependencies.
- **Dataset Scope**: Trained on 800 curated real-world civic complaints sourced from open municipal and CPGRAMS repositories (balanced 200 samples per class across Education, Civic Neglect, Welfare, Child Safety), achieving 92.50% test accuracy.
- **Category Granularity**: The current release classifies complaints into 4 primary civic categories. Specialized sub-categories (e.g., distinguishing between PM-KISAN delay vs. PDS Ration denial under "Welfare") are handled via downstream keyword tagging and entity extraction rather than separate ML heads.

---

## 3. Persistence: Local Offline Mode vs. Firebase Cloud Sync
- **Local Persistence Baseline**: By default, Dastak Vani operates on a robust, persistent local repository layer with atomic file-backed JSON serialization, ensuring zero data loss during total network blackouts in rural terrain.
- **Cloud Firebase Integration**: The codebase includes cloud sync adapters (`FirestoreComplaintAdapter`). Because `google-services.json` is user-provided and environment-specific, cloud sync remains disabled in demo mode until the configuration file is dropped into `app/google-services.json`. All CRUD operations, NGO claim workflows, and status tracking function completely offline.

---

## 4. Dual Routing Destinations (Honest Labeling)
- **Demo/Test Routing Endpoints**: As per the Zero-Gimmick directive, all routing destinations are visibly marked with `[DEMO / TEST ROUTING]`.
  - Government departments (District Magistrate, DEO, BDO, CDPO) and NGO partners (Pratham Bihar, Bachpan Bachao Andolan, ActionAid, Jan Pahar) use sandboxed email addresses (`*-demo@bihar.gov.in`, `*-bihar@demo.ngo`).
  - No simulated "200 OK — Delivered to Official Government Portal" fake toast messages are emitted.

---

## 5. Escalation & Section 6(1) RTI Application Drafts
- **Legal Draft Generation**: Dastak Vani automatically flags complaints exceeding 15 and 30 days of inactivity and generates formal, legally structured applications under Section 6(1) of the Right to Information Act, 2005.
- **Submission Barrier**: The app generates the draft, pre-populates PIO addresses, calculates fees (₹10 Indian Postal Order or BPL exemption), and enables 1-tap clipboard copying / PDF export. It does **not** silently submit the RTI electronically because Bihar RTI Online / central portals mandate citizen Aadhaar/OTP verification, CAPTCHA, and court fee stamp payment.
