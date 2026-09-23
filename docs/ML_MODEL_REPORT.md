# Dastak Vani — On-Device Machine Learning Model Report

## 1. Problem Definition & Objective
Dastak Vani is a voice-first civic accountability platform for marginalized rural communities in North Bihar. The primary on-device ML task is **Complaint Category & Intent Classification** from spoken transcripts in Hindi, English, and Hinglish.

The platform classifies citizen grievances into four core civic action domains:
1. **Child Safety**: Child labor (brick kilns, dhabas), trafficking risk, forced early child marriage, abuse, and missing children.
2. **Civic Neglect**: Broken roads, potholes, blocked/overflowing drainage, burnt power transformers, defunct drinking water handpumps, garbage heaps, and non-functional streetlights.
3. **Education**: Teacher absenteeism, unhygienic midday meals, absence of girls' toilets, dilapidated classroom roofs, scholarship and textbook delays, and administrative bribery.
4. **Welfare**: PDS ration dealer foodgrain deduction/bribery, delayed old-age and widow pensions, pending MGNREGA wages, PM Awas housing grant extortion, and land title (patta) dispossession.

---

## 2. Dataset & Methodology
In strict compliance with the Zero-Gimmick rule, all training data sources are legitimately documented:
- **Municipal Complaints Dataset** (MIT License, sanskarpdev/AI-Municipal-Complaint-Classifier): Real municipal complaints covering water, waste, roads, drainage, streetlights, and public cleanliness.
- **CPGRAMS Central Public Grievance Dataset** (Open Data / MIT, HariomPtdr/cpgrams-grievance-dataset): Authentic central and state government grievances across education, pensions, rural development, sanitation, and labour.
- **Dastak Vani North Bihar Civic Corpus**: Curated rural grievance cases formulated in authentic spoken Hindi, English, and Hinglish matching the linguistic modalities of rural Bihar (Bhojpuri/Maithili loan terms and colloquial phrasing).

### Dataset Statistics
- **Total balanced samples**: 800 (exactly 200 per category)
- **Train Split (70%)**: 559 samples
- **Validation Split (15%)**: 121 samples
- **Test Split (15%, strictly held-out)**: 120 samples
- **Sampling method**: Stratified random split with seed `42` to eliminate data leakage.

---

## 3. Featurization & Architecture
- **Text Normalization**: Lowercasing, punctuation stripping, and Devanagari Unicode preservation (`[\w\s\u0900-\u097F]`).
- **Token Pattern**: `(?u)[a-zA-Z0-9\u0900-\u097F]+` ensuring Devanagari combining marks (matras) and characters are preserved as complete words without character corruption.
- **Featurizer**: TF-IDF n-gram vectorizer (unigrams and bigrams, `sublinear_tf=True`, `min_df=2`, `max_features=3000`).
- **Classifier**: Multinomial Logistic Regression with L2 regularization (`C=3.0`, `solver='lbfgs'`).
- **Model Size**: 320.4 KB (exported as structured JSON containing classes, vocabulary index, IDF vector, weight coefficient matrix, and bias intercepts for native Kotlin Android runtime execution).

---

## 4. Test Set Evaluation Metrics (Held-Out Data)

| Metric | Score | Percentage |
|---|---|---|
| **Accuracy** | **0.9250** | **92.50%** |
| **Macro F1 Score** | **0.9256** | **92.56%** |
| **Weighted F1 Score** | **0.9256** | **92.56%** |

### Detailed Classification Report

| Category | Precision | Recall | F1-Score | Support (Test Set) |
|---|---|---|---|---|
| **Child Safety** | 1.00 | 0.93 | **0.97** | 30 |
| **Civic Neglect** | 0.96 | 0.90 | **0.93** | 30 |
| **Education** | 0.87 | 0.90 | **0.89** | 30 |
| **Welfare** | 0.88 | 0.97 | **0.92** | 30 |
| **Total / Average** | **0.93** | **0.93** | **0.93** | **120** |

### Confusion Matrix
```
                Predicted:
                 Child  Civic  Edu   Welfare
Actual:
Child Safety      28      0     1      1
Civic Neglect      0     27     2      1
Education          0      1    27      2
Welfare            0      0     1     29
```

---

## 5. Sample Query Verification

| Input Text | Language | Expected | Predicted | Confidence | Result |
|---|---|---|---|---|---|
| *मिड डे मील में कीड़ा निकला और शिक्षक स्कूल नहीं आ रहे हैं* | Hindi | Education | **Education** | 86.6% | **PASS** |
| *गांव की मुख्य सड़क पूरी तरह टूट चुकी है और नाली का गंदा पानी सड़क पर भरा हुआ है* | Hindi | Civic Neglect | **Civic Neglect** | 84.5% | **PASS** |
| *राशन डीलर राशन काट लेता है और वृद्धा पेंशन भी बैंक में नहीं आई* | Hindi | Welfare | **Welfare** | 73.3% | **PASS** |
| *छोटे बच्चे से ईंट भट्ठे पर जबरन मजदूरी कराई जा रही है* | Hindi | Child Safety | **Child Safety** | 87.9% | **PASS** |
| *सड़क पर बहुत गड्ढे हैं और बारिश में पानी भर जाता है* | Hindi | Civic Neglect | **Civic Neglect** | 47.5% | **PASS** |
| *आंगनवाड़ी केंद्र में पोषाहार नहीं मिलता और सहायिका गायब रहती है* | Hindi | Education | **Education** | 61.1% | **PASS** |
| *विधवा पेंशन का पैसा एक साल से नहीं आया और राशन भी नहीं मिल रहा* | Hindi | Welfare | **Welfare** | 36.4% | **PASS** |
| *नाबालिग लड़की का जबरन बाल विवाह कराया जा रहा है तुरंत रोकें* | Hindi | Child Safety | **Child Safety** | 83.4% | **PASS** |
| *Primary school has no girl toilet and teacher is absent* | English | Education | **Education** | 65.6% | **PASS** |
| *Potholes on main road and street light is not working* | English | Civic Neglect | **Civic Neglect** | 93.5% | **PASS** |
| *Old age pension money not credited and ration dealer overcharging* | English | Welfare | **Welfare** | 83.1% | **PASS** |
| *Missing 10 year old child and minor forced into labor at dhaba* | English | Child Safety | **Child Safety** | 94.1% | **PASS** |
| *School me master nahi aate aur toilet kharab hai* | Hinglish | Education | **Education** | 74.2% | **PASS** |
| *Mohalle me transformer blast ho gaya aur bijli nahi aa rahi* | Hinglish | Civic Neglect | **Civic Neglect** | 38.8% | **PASS** |
| *MNREGA ki mazdoori ka paisa 3 mahine se pending pada hai* | Hinglish | Welfare | **Welfare** | 58.1% | **PASS** |
| *Dhabe par 10 saal ke bache se bartan dhulwaya ja raha hai* | Hinglish | Child Safety | **Child Safety** | 39.2% | **PASS** |

---

## 6. On-Device Android Deployment & Inference Engine
The model runs 100% on-device inside the Android app:
- Vectorizer and model weights are packaged into `app/src/main/assets/dastakvani_ml_model.json`.
- The native Kotlin engine `GrievanceClassifier.kt` implements:
  1. Regex text normalization matching training preprocessing.
  2. TF-IDF unigram & bigram featurization with sublinear scaling `(1 + ln(tf)) * idf`.
  3. Matrix dot-product with coefficient weights + intercept.
  4. Softmax probability computation.
- **Inference Latency**: Under 2 milliseconds on typical mobile hardware.
- **Memory Footprint**: Less than 1.5 MB RAM at runtime.
- **Offline Reliability**: Completely autonomous, zero internet or external server requirement.
