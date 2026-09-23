# Dastak Vani — 3 to 5 Minute Final Demo Test Plan

This walkthrough script guides evaluators and testers through demonstrating the end-to-end functionality of Dastak Vani on an Android device or emulator.

---

### Step 1: Launch Application
- **Action**: Tap the **Dastak Vani** app icon on the Android launcher.
- **Expected Result**: The app opens to the White + Blue Civic Home Screen. The title shows "दस्तक वाणी", accompanied by a high-contrast speaker banner and a prominent 140dp microphone button in the center.

### Step 2: Language Switching
- **Action**: Tap the **"English"** button on the top right app bar, then tap **"हिन्दी"**.
- **Expected Result**: All UI labels, buttons, and prompts immediately switch between English and Hindi. Spoken prompts switch locale.

### Step 3: Audio Guidance (Zero-Literacy Proof)
- **Action**: Tap the top audio banner ("सुनें: शिकायत कैसे दर्ज करें").
- **Expected Result**: Android TextToSpeech clearly speaks spoken audio instructions in Hindi: *"दस्तक वाणी में आपका स्वागत है। अपनी समस्या दर्ज कराने के लिए स्क्रीन के बीच में स्थित बड़े माइक बटन को दबाएं..."*.

### Step 4: Microphone Interaction & Recording
- **Action**: Tap the large central **Microphone Button** ("यहाँ दबाकर शिकायत बोलें"). Grant microphone permission if prompted.
- **Expected Result**: The app navigates to the recording screen. The microphone pulse animation ripples with live audio amplitude. Speak a realistic grievance:
  > *"हमारे गांव के प्राथमिक विद्यालय में शिक्षक नहीं आ रहे हैं और मिड डे मील का खाना बहुत खराब दिया जा रहा है।"*
- **Evidence**: Live partial and final transcript appears word-for-word in the transcript card.

### Step 5: On-Device ML Inference & Entity Extraction
- **Action**: Tap the **"बोलना पूरा हुआ • पुष्टि करें"** (Done Speaking) button.
- **Expected Result**: The on-device ML classifier runs in < 2ms directly on Android:
  - **Category**: `शिक्षा / स्कूल (Education)`
  - **Confidence**: `86% सटीक`
  - **Entity Extractor**: Detects village/school locality if mentioned.

### Step 6: Proof of Hearing Spoken Playback
- **Action**: Listen as the phone automatically speaks back the grievance via TTS:
  > *"आप कह रहे हैं कि: हमारे गांव के प्राथमिक विद्यालय में शिक्षक नहीं आ रहे हैं... क्या यह सही है? पुष्टि करने के लिए हाँ दबाएं।"*
- **Expected Result**: The citizen hears their own words confirmed before submission, ensuring zero misunderstanding.

### Step 7: Confirmation & Persistence
- **Action**: Tap the green **"हाँ, यह बिल्कुल सही है • शिकायत दर्ज करें"** button.
- **Expected Result**: Real GPS coordinates are stamped, a unique complaint ID (e.g. `DV-20260924-4521`) is generated, the grievance is saved into persistent storage, and simultaneous routing is triggered.

### Step 8: Submission Success & Audio Readout
- **Action**: On the Success screen, tap **"नंबर सुनें (Audio Readout)"**.
- **Expected Result**: TTS speaks: *"आपकी शिकायत संख्या है DV-20260924-4521"*. Dispatched destinations show the District Education Officer and Pratham Education Foundation.

### Step 9: Anonymized Village Wall (Complaint Box)
- **Action**: Navigate to the **"ग्राम मंच / शिकायत पेटी"** (Village Wall).
- **Expected Result**: The newly submitted complaint appears live in the public feed. Notice:
  - Phone number is completely hidden.
  - Citizen name is masked (`नागरिक स***`).
  - Status chip displays `दर्ज (Submitted)`. Filter chips allow filtering by Education, Civic Neglect, Welfare, or Child Safety.

### Step 10: Volunteer / NGO Portal Action
- **Action**: Tap **"कार्यकर्ता एवं एनजीओ पोर्टल"**. Enter demo volunteer credentials:
  - Email: `volunteer@biharngo.org`
  - Password: `dastak123`
- **Expected Result**: Authenticated dashboard opens showing metrics and area complaints for Madhubani district.
- **Action**: Tap **"कार्यवाही करें / स्थिति बदलें"** on the complaint. Enter field note: *"स्कूल का निरीक्षण किया गया। बीईओ को नोटिस जारी।"*, and tap **"प्रगति पर (In Progress)"**.
- **Expected Result**: Complaint status immediately updates to `IN_PROGRESS`.

### Step 11: Citizen Voice Status Tracking
- **Action**: Return to Home and tap **"शिकायत की स्थिति जांचें"** (Check Status). Enter or speak `DV-20260924-4521`.
- **Expected Result**: Card displays updated status: `कार्य प्रगति पर (In Progress)`. Tap speaker icon to hear status spoken aloud:
  > *"शिकायत संख्या DV-20260924-4521, श्रेणी Education की वर्तमान स्थिति है: कार्य प्रगति पर।"*

### Step 12: Automated Escalation & Legal RTI Draft
- **Action**: In Status Lookup, search for overdue seed complaint `DV-20260905-1829`.
- **Expected Result**: Status shows `विधिक अपील / RTI (Escalated)`. Tap **"विधिक आरटीआई (RTI) प्रारूप देखें"**.
- **Evidence**: Complete, legally formatted RTI application under Section 6(1) of RTI Act 2005 appears with one-tap clipboard copying.

### Step 13: Review Real ML Evidence
- **Action**: Open `docs/ML_MODEL_REPORT.md`.
- **Evidence**: 800-sample balanced dataset, 92.5% accuracy, 0.9256 Macro F1 score on strictly held-out test data, 320 KB on-device model file.
