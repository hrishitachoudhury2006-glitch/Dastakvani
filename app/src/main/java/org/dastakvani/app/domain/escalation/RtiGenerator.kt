package org.dastakvani.app.domain.escalation

import org.dastakvani.app.domain.model.Complaint
import org.dastakvani.app.domain.routing.RoutingEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RtiGenerator {

    /**
     * Generates a formal Right to Information (RTI) application draft under
     * Section 6(1) of the Right to Information Act, 2005.
     * Clearly labeled as a structured legal draft.
     */
    fun generateRtiDraft(complaint: Complaint): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val filingDate = dateFormat.format(Date(complaint.timestamp))
        val routing = RoutingEngine.resolveRouting(complaint.category, complaint.location.district)

        return """
======================================================================
[विधिक प्रारूप - DRAFT RTI APPLICATION UNDER SECTION 6(1) OF RTI ACT 2005]
======================================================================

सेवा में (To),
लोक सूचना पदाधिकारी (Public Information Officer - PIO)
कार्यालय: ${routing.departmentName}
जिला: ${complaint.location.district}, बिहार

विषय: जन शिकायत संख्या ${complaint.id} के निस्तारण और विभागीय कार्रवाई के संबंध में सूचना।
(Subject: Information regarding resolution and departmental action on Grievance ID: ${complaint.id})

महोदय / महोदया (Sir/Madam),

मैं, सूचना का अधिकार अधिनियम, 2005 की धारा 6(1) के अंतर्गत निम्नलिखित प्रमाणित सूचनाएं निर्धारित समय-सीमा (30 दिवस) के भीतर प्राप्त करने हेतु यह आवेदन प्रस्तुत कर रहा हूँ:

1. संदर्भ शिकायत विवरण:
   - शिकायत संख्या (Complaint ID): ${complaint.id}
   - शिकायत दर्ज तिथि (Filing Date): $filingDate
   - शिकायत का विषय/श्रेणी (Category): ${complaint.category.displayNameHi}
   - घटनास्थल / क्षेत्र (Location): ${complaint.location.address}, जिला ${complaint.location.district}
   - नागरिक का मूल वक्तव्य (Citizen Grievance): "${complaint.transcript}"

2. मांगी गई आवश्यक सूचनाएं (Information Requested):
   क) उक्त शिकायत संख्या ${complaint.id} पर संबंधित विभाग द्वारा अब तक की गई दैनिक कार्रवाई विवरण (Daily Progress Report) की प्रमाणित प्रति प्रदान करें।
   ख) इस मामले की जांच किस सक्षम पदाधिकारी/कर्मचारी को सौंपी गई थी, उनका पदनाम एवं उनके द्वारा प्रस्तुत जांच आख्या (Inquiry Report) की प्रति उपलब्ध कराएं।
   ग) नागरिक अधिकार पत्र (Citizens' Charter) / बिहार लोक शिकायत निवारण अधिकार अधिनियम के अनुसार उक्त समस्या के समाधान की निर्धारित समय-सीमा क्या है और देरी के लिए कौन से पदाधिकारी उत्तरदायी हैं?
   घ) यदि समस्या का समाधान अभी तक नहीं हुआ है, तो विभाग द्वारा इस पर अंतिम कार्रवाई कब तक पूर्ण कर ली जाएगी?

3. आवेदन शुल्क:
   - निर्धारित आवेदन शुल्क ₹10 का पोस्टल ऑर्डर / ई-चालान संलग्न है (गरीबी रेखा बीपीएल कार्डधारकों हेतु निःशुल्क)।

प्रार्थी:
दस्तक वाणी जन उत्तरदायित्व प्रणाली (Dastak Vani Accountability Platform)
नागरिक संदर्भ: ${complaint.id}
दिनांक: ${dateFormat.format(Date())}
स्थान: ${complaint.location.district}, बिहार
======================================================================
""".trimIndent()
    }
}
