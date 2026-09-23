package org.dastakvani.app.domain.routing

import org.dastakvani.app.domain.model.ComplaintCategory

data class RoutingDestination(
    val departmentName: String,
    val departmentEmail: String,
    val ngoName: String,
    val ngoEmail: String,
    val escalationAuthority: String,
    val isDemoConfiguration: Boolean = true
)

object RoutingEngine {

    /**
     * Resolves the deterministic dual-channel government & NGO routing destinations
     * based on complaint category and district.
     * Clearly labeled as configured demo/test routing as mandated by Project Rules.
     */
    fun resolveRouting(category: ComplaintCategory, district: String): RoutingDestination {
        val safeDistrict = district.ifBlank { "Bihar Central" }
        return when (category) {
            ComplaintCategory.CHILD_SAFETY -> RoutingDestination(
                departmentName = "जिला बाल संरक्षण इकाई (District Child Protection Unit - DCPU), $safeDistrict",
                departmentEmail = "dcpu-demo-test@bihar.gov.in",
                ngoName = "बचपन बचाओ / प्रथम बाल सुरक्षा नेटवर्क (Child Rights Network), $safeDistrict",
                ngoEmail = "childrights-partner-test@biharngo.org",
                escalationAuthority = "बिहार राज्य बाल अधिकार संरक्षण आयोग (BSCPCR)"
            )
            ComplaintCategory.CIVIC_NEGLECT -> RoutingDestination(
                departmentName = "कार्यपालक अभियंता, ग्रामीण कार्य विभाग / लोक स्वास्थ्य अभियंत्रण विभाग (RWD/PHED), $safeDistrict",
                departmentEmail = "rwd-phed-demo-test@bihar.gov.in",
                ngoName = "ग्राम वाणी / जन चेतना मंच (Civic Action Network), $safeDistrict",
                ngoEmail = "civic-response-test@biharngo.org",
                escalationAuthority = "जिला लोक शिकायत निवारण पदाधिकारी (PGRO), $safeDistrict"
            )
            ComplaintCategory.EDUCATION -> RoutingDestination(
                departmentName = "जिला शिक्षा पदाधिकारी (District Education Officer - DEO), $safeDistrict",
                departmentEmail = "deo-demo-test@bihar.gov.in",
                ngoName = "प्रथम एजुकेशन फाउंडेशन / अक्षर मंच, $safeDistrict",
                ngoEmail = "education-aid-test@biharngo.org",
                escalationAuthority = "निदेशक, प्राथमिक शिक्षा, शिक्षा विभाग, बिहार"
            )
            ComplaintCategory.WELFARE -> RoutingDestination(
                departmentName = "जिला आपूर्ति पदाधिकारी / सहायक निदेशक सामाजिक सुरक्षा (DSO/Social Welfare), $safeDistrict",
                departmentEmail = "dso-welfare-demo-test@bihar.gov.in",
                ngoName = "हकदर्शक / सामाजिक जवाबदेही अभियान (Social Accountability Forum), $safeDistrict",
                ngoEmail = "welfare-advocacy-test@biharngo.org",
                escalationAuthority = "अनुमंडल लोक शिकायत निवारण पदाधिकारी (SDPGRO), $safeDistrict"
            )
        }
    }
}
