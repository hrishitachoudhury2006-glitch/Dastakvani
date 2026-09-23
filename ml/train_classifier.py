import sys
sys.stdout.reconfigure(encoding='utf-8')
import os
import json
import re
import random
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, f1_score
from sklearn.model_selection import train_test_split

print("Building balanced bilingual 800-sample civic grievance dataset...")

# Load downloaded datasets
df_muni = pd.read_csv('ml/municipal_complaints.csv')
df_cp = pd.read_csv('ml/cpgrams_central.csv')
df_seed = pd.read_csv('ml/dastakvani_training_dataset.csv')

data_by_class = {
    'Civic Neglect': [],
    'Education': [],
    'Welfare': [],
    'Child Safety': []
}

# Add base seed samples
for _, row in df_seed.iterrows():
    c = row['category']
    if c in data_by_class and str(row['text']) not in data_by_class[c]:
        data_by_class[c].append(str(row['text']))

# Add English Civic Neglect from df_muni up to 60 samples
civic_cats = ['Water', 'Waste', 'Roads', 'Streetlights', 'Drainage', 'Public Cleanliness']
for _, row in df_muni[df_muni['category'].isin(civic_cats)].iterrows():
    txt = str(row.get('text', '')) or (str(row.get('title', '')) + " " + str(row.get('description', '')))
    if len(txt.strip()) > 15 and txt.strip() not in data_by_class['Civic Neglect']:
        data_by_class['Civic Neglect'].append(txt.strip())
        if len(data_by_class['Civic Neglect']) >= 60:
            break

districts = ["गांव", "पंचायत", "मोहल्ले", "कस्बे", "बस्ती", "वार्ड", "टोले", "village", "panchayat", "locality", "ward"]
time_frames = ["पिछले तीन महीने से", "कई दिनों से", "लंबे समय से", "दो हफ्तों से", "for past few months", "since last week", "for months"]

civic_templates = [
    "{dist} की मुख्य सड़क पूरी तरह टूट चुकी है और बड़े बड़े गड्ढे हो गए हैं {time}",
    "{dist} में नाली का गंदा बदबूदार पानी सड़क पर बह रहा है और निकासी बंद है",
    "{dist} का बिजली ट्रांसफार्मर जल गया है और पूरी बस्ती में अंधेरा छाया हुआ है",
    "{dist} में सार्वजनिक चापाकल और नल जल योजना का पानी {time} बंद पड़ा है",
    "{dist} में सड़क किनारे कचरे का भारी अंबार लगा है और सफाई कर्मचारी नहीं आते",
    "{dist} की स्ट्रीट लाइट खराब होने से रात के समय रास्ते में भारी अंधेरा रहता है",
    "{dist} में पीने के पानी की मुख्य पाइपलाइन फट गई है और गंदा पानी नलों में आ रहा है",
    "{dist} में बारिश होते ही सड़क पर दो फीट जलभराव हो जाता है और आवागमन रुक जाता है",
    "{dist} का संपर्क पुलिया टूट चुका है और आवाजाही पूरी तरह ठप हो गई है",
    "{dist} में बिजली का जर्जर तार नीचे लटक रहा है जिससे कभी भी करंट लग सकता है",
    "Main connecting asphalt road in {dist} severely damaged with deep potholes {time}",
    "Open sewage drainage overflowing directly onto public street in {dist}",
    "Electric power transformer burnt out, no electricity in {dist} {time}",
    "Public drinking water handpump dry and defunct causing water crisis in {dist}",
    "Uncollected garbage heaps rotting openly near residential houses in {dist}",
    "Streetlights on village access road non functional for long period in {dist}",
    "Drinking water supply contaminated with leaking sewage pipeline in {dist}",
    "Flooded road and waterlogging due to blocked storm drainage in {dist}",
    "Broken canal bridge disconnecting village transportation link in {dist}",
    "Dangling high tension electric wires touching rooftops in {dist}"
]

edu_templates = [
    "{dist} के सरकारी स्कूल में {time} शिक्षक अनुपस्थित रहते हैं और पढ़ाई ठप पड़ी है",
    "{dist} के प्राथमिक विद्यालय में मिड डे मील का खाना बहुत घटिया और बदबूदार दिया जा रहा है",
    "{dist} के कन्या मध्य विद्यालय में छात्राओं के लिए कोई सुरक्षित शौचालय और पानी नहीं है",
    "{dist} के स्कूल की छत बरसात में टपकती है और दीवार में दरारें आ गई हैं",
    "{dist} के विद्यालय में छात्रवृत्ति और पोशाक की सरकारी सहायता राशि अभी तक नहीं दी गई",
    "{dist} के हाई स्कूल में विज्ञान और गणित के मास्टर की भारी कमी है",
    "{dist} के आंगनवाड़ी केंद्र पर बच्चों को सूखा राशन और पोषाहार नहीं मिल रहा है",
    "{dist} के स्कूल में प्रधानाध्यापक टीसी और दाखिले के नाम पर जबरन अवैध वसूली कर रहे हैं",
    "{dist} के सरकारी स्कूल में बच्चों को निःशुल्क किताबें {time} नहीं बांटी गई हैं",
    "{dist} के प्राथमिक विद्यालय का हैंडपंप खराब है और बच्चों को पीने का पानी नहीं मिलता",
    "Government school in {dist} has severe teacher absenteeism {time} affecting education",
    "Midday meal food poison risk due to unhygienic rotten food served in {dist} primary school",
    "Complete lack of functional toilets for girl students in {dist} government secondary school",
    "Dangerous dilapidated school roof leaking heavily during rain in {dist} primary classes",
    "Scholarship and free uniform grants delayed for scheduled caste students in {dist}",
    "Panchayat high school in {dist} running without basic science laboratory and math teachers",
    "Anganwadi center locked and infant nutrition grains stolen by supervisor in {dist}",
    "Principal demanding cash bribes for school transfer certificate and report card in {dist}",
    "Free syllabus books not distributed to school students {time} in {dist}",
    "Dysfunctional drinking water handpump inside government school compound in {dist}"
]

welfare_templates = [
    "{dist} में राशन डीलर हर कार्डधारी से दो किलो अनाज की कटौती करता है और पर्ची नहीं देता",
    "{dist} के बुजुर्गों की वृद्धावस्था पेंशन {time} बैंक खाते में नहीं आई है",
    "{dist} में मनरेगा के तहत तालाब खुदाई का काम किया लेकिन मजदूरी का भुगतान नहीं हुआ",
    "{dist} में प्रधानमंत्री आवास योजना की किस्त जारी करने के नाम पर कर्मचारी रिश्वत मांग रहा है",
    "{dist} में बेसहारा महिला की विधवा पेंशन का आवेदन पत्र ब्लॉक ऑफिस में दबा रखा है",
    "{dist} के महादलित परिवारों का राशन कार्ड सूची से जानबूझकर नाम विलोपित कर दिया गया है",
    "{dist} में सरकार द्वारा आवंटित बासगीत पर्चा वाली जमीन पर भू-माफियाओं ने अवैध कब्जा कर लिया है",
    "{dist} में गरीब मजदूरों को जॉब कार्ड देने से पंचायत रोजगार सेवक साफ इनकार कर रहा है",
    "{dist} में दिव्यांग पेंशन की मासिक सहायता राशि {time} बंद कर दी गई है",
    "{dist} में किसान सम्मान निधि की किस्त खाते में नहीं पहुंची है जबकि केवाईसी हो चुका है",
    "PDS ration quota dealer illegally deducting foodgrains and overcharging poor families in {dist}",
    "Old age social security pension pending disbursement for senior citizens {time} in {dist}",
    "MGNREGA wage payments overdue for poor manual laborers {time} in {dist}",
    "Corrupt housing scheme coordinator demanding illegal gratification for PM Awas grant in {dist}",
    "Widow welfare pension application pending without clearance at block development office in {dist}",
    "Wrongful cancellation of BPL ration cards belonging to landless poor families in {dist}",
    "Legally allotted homestead land title patta forcefully encroached by landlords in {dist}",
    "Refusal to issue or renew MGNREGA employment job card to rural jobseekers in {dist}",
    "Disability support allowance withheld {time} causing extreme hardship in {dist}",
    "Direct benefit transfer installment of agricultural income support failed in {dist}"
]

child_safety_templates = [
    "{dist} के ईंट भट्ठे पर नाबालिग मासूम बच्चों से जबरन बंधुआ मजदूरी कराई जा रही है",
    "{dist} में 14 साल की नाबालिग बच्ची का गैरकानूनी बाल विवाह कराया जा रहा है तुरंत रोकें",
    "{dist} से दो छोटे बच्चे {time} लापता हैं और मानव तस्करी का गंभीर अंदेशा है",
    "{dist} के ढाबे और होटल पर 11 साल के बच्चे से 14 घंटे झूठे बर्तन धुलवाए जा रहे हैं और मारते हैं",
    "{dist} में एक संदिग्ध दलाल गरीब परिवार के बच्चों को बहला फुसला कर बाहर ले जाने के चक्कर में है",
    "{dist} में अनाथ बच्चे पर उसके संरक्षक द्वारा जानलेवा शारीरिक प्रताड़ना और अत्याचार किया जा रहा है",
    "{dist} में कबाड़ गोदाम पर नाबालिग बच्चों से खतरनाक कांच और केमिकल का काम कराया जा रहा है",
    "{dist} के बस स्टैंड पर छोटे बच्चों से जबरन भीख मंगवाने वाला गिरोह सक्रिय है",
    "{dist} में मोटर गैराज में छोटे बच्चों को बंधक बनाकर रात दिन काम करवाया जाता है",
    "{dist} की नाबालिग स्कूली छात्रा को आते-जाते समय शोहदे छेड़ते हैं और अपहरण की धमकी देते हैं",
    "Illegal child labor and exploitation of minors detected at local brick kiln in {dist}",
    "Urgent police action needed to stop illegal child marriage of 13-year-old girl in {dist}",
    "Missing minor child report from {dist}, suspected human trafficking racket active",
    "Commercial roadside dhaba employing underage child laborers under abusive conditions in {dist}",
    "Suspected child traffickers attempting to smuggle adolescent village minors from {dist}",
    "Severe domestic physical violence and cruelty reported against orphan child in {dist}",
    "Hazardous scrap collection and toxic waste handling involving small children in {dist}",
    "Organized child begging syndicate exploiting kidnapped children near railway station in {dist}",
    "Minor boy forced into unpaid bonded labor in automobile workshop in {dist}",
    "School girl facing stalking and abduction threats by local anti-social elements in {dist}"
]

# Generate synthetic variations so each category has 200 samples
random.seed(42)
categories = [
    (civic_templates, 'Civic Neglect'),
    (edu_templates, 'Education'),
    (welfare_templates, 'Welfare'),
    (child_safety_templates, 'Child Safety')
]

for tmpl_list, cat in categories:
    needed = 200 - len(data_by_class[cat])
    gen_count = 0
    while gen_count < needed:
        t = random.choice(tmpl_list)
        d = random.choice(districts)
        tf = random.choice(time_frames)
        sentence = t.replace("{dist}", d).replace("{time}", tf)
        if sentence not in data_by_class[cat]:
            data_by_class[cat].append(sentence)
            gen_count += 1

all_rows = []
for cat, txts in data_by_class.items():
    print(f"Class '{cat}': {len(txts)} samples")
    for t in txts[:200]: # exactly 200 per class -> 800 total
        all_rows.append({'text': t, 'category': cat})

df_final = pd.DataFrame(all_rows).sample(frac=1.0, random_state=42).reset_index(drop=True)
print("\nFinal balanced dataset shape:", df_final.shape)
print("Final distribution:\n", df_final['category'].value_counts())

def normalize_text(text):
    text = str(text).lower()
    text = re.sub(r'[\r\n\t]+', ' ', text)
    text = re.sub(r'[^\w\s\u0900-\u097F]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    return text

df_final['clean_text'] = df_final['text'].apply(normalize_text)

# Save the final training dataset
df_final.to_csv('ml/dastakvani_training_dataset.csv', index=False)

# Train/Val/Test Split: 70% Train (560), 15% Val (120), 15% Test (120)
X_train_val, X_test, y_train_val, y_test = train_test_split(
    df_final['clean_text'], df_final['category'], test_size=0.15, random_state=42, stratify=df_final['category']
)
X_train, X_val, y_train, y_val = train_test_split(
    X_train_val, y_train_val, test_size=0.1765, random_state=42, stratify=y_train_val
)

print(f"\nSplits: Train={len(X_train)}, Val={len(X_val)}, Test={len(X_test)}")

vectorizer = TfidfVectorizer(
    token_pattern=r'(?u)[a-zA-Z0-9\u0900-\u097F]+',
    ngram_range=(1, 2),
    min_df=2,
    max_features=3000,
    sublinear_tf=True
)
X_train_vec = vectorizer.fit_transform(X_train)
X_val_vec = vectorizer.transform(X_val)
X_test_vec = vectorizer.transform(X_test)

clf = LogisticRegression(C=3.0, max_iter=500, random_state=42, solver='lbfgs')
clf.fit(X_train_vec, y_train)

test_preds = clf.predict(X_test_vec)
acc = accuracy_score(y_test, test_preds)
macro_f1 = f1_score(y_test, test_preds, average='macro')
weighted_f1 = f1_score(y_test, test_preds, average='weighted')

print(f"\n==========================================")
print(f"=== TEST SET EVALUATION METRICS ===")
print(f"==========================================")
print(f"Accuracy:    {acc:.4f} ({acc*100:.2f}%)")
print(f"Macro F1:    {macro_f1:.4f}")
print(f"Weighted F1: {weighted_f1:.4f}")
print("\nClassification Report:\n", classification_report(y_test, test_preds))
print("Confusion Matrix (Classes: {}):\n".format(clf.classes_.tolist()), confusion_matrix(y_test, test_preds, labels=clf.classes_))

# Export parameters to JSON for Android Kotlin runtime
model_export = {
    'classes': clf.classes_.tolist(),
    'vocabulary': {k: int(v) for k, v in vectorizer.vocabulary_.items()},
    'idf': vectorizer.idf_.tolist(),
    'coef': clf.coef_.tolist(),
    'intercept': clf.intercept_.tolist(),
    'metrics': {
        'accuracy': float(acc),
        'macro_f1': float(macro_f1),
        'weighted_f1': float(weighted_f1),
        'test_samples': int(len(X_test)),
        'train_samples': int(len(X_train)),
        'val_samples': int(len(X_val)),
        'vocabulary_size': int(len(vectorizer.vocabulary_))
    }
}

export_path = 'ml/export/dastakvani_ml_model.json'
with open(export_path, 'w', encoding='utf-8') as f:
    json.dump(model_export, f, ensure_ascii=False)

file_size_kb = os.path.getsize(export_path) / 1024
print(f"\nModel exported successfully to {export_path} ({file_size_kb:.1f} KB)")

# Sample test queries
sample_tests = [
    # Hindi queries
    ("मिड डे मील में कीड़ा निकला और शिक्षक स्कूल नहीं आ रहे हैं", "Education"),
    ("गांव की मुख्य सड़क पूरी तरह टूट चुकी है और नाली का गंदा पानी सड़क पर भरा हुआ है", "Civic Neglect"),
    ("राशन डीलर राशन काट लेता है और वृद्धा पेंशन भी बैंक में नहीं आई", "Welfare"),
    ("छोटे बच्चे से ईंट भट्ठे पर जबरन मजदूरी कराई जा रही है", "Child Safety"),
    ("सड़क पर बहुत गड्ढे हैं और बारिश में पानी भर जाता है", "Civic Neglect"),
    ("आंगनवाड़ी केंद्र में पोषाहार नहीं मिलता और सहायिका गायब रहती है", "Education"),
    ("विधवा पेंशन का पैसा एक साल से नहीं आया और राशन भी नहीं मिल रहा", "Welfare"),
    ("नाबालिग लड़की का जबरन बाल विवाह कराया जा रहा है तुरंत रोकें", "Child Safety"),
    
    # English queries
    ("Primary school has no girl toilet and teacher is absent", "Education"),
    ("Potholes on main road and street light is not working", "Civic Neglect"),
    ("Old age pension money not credited and ration dealer overcharging", "Welfare"),
    ("Missing 10 year old child and minor forced into labor at dhaba", "Child Safety"),
    
    # Hinglish queries
    ("School me master nahi aate aur toilet kharab hai", "Education"),
    ("Mohalle me transformer blast ho gaya aur bijli nahi aa rahi", "Civic Neglect"),
    ("MNREGA ki mazdoori ka paisa 3 mahine se pending pada hai", "Welfare"),
    ("Dhabe par 10 saal ke bache se bartan dhulwaya ja raha hai", "Child Safety")
]

print("\n==========================================")
print("=== SAMPLE INFERENCE VERIFICATION ===")
print("==========================================")
all_passed = True
for q, expected in sample_tests:
    cleaned = normalize_text(q)
    vec = vectorizer.transform([cleaned])
    probs = clf.predict_proba(vec)[0]
    best_idx = np.argmax(probs)
    pred_cat = clf.classes_[best_idx]
    conf = probs[best_idx]
    status = "PASS" if pred_cat == expected else "FAIL"
    if status == "FAIL":
        all_passed = False
    print(f"[{status}] '{q[:45]}...' -> {pred_cat} (conf: {conf*100:.1f}%, expected: {expected})")

if all_passed:
    print("\nSUCCESS: ALL 16 COMPREHENSIVE SAMPLE QUERIES PASSED ACCURATELY!")
