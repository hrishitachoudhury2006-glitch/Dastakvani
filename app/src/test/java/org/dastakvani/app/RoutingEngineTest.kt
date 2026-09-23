package org.dastakvani.app

import org.dastakvani.app.domain.model.ComplaintCategory
import org.dastakvani.app.domain.routing.RoutingEngine
import org.junit.Assert.*
import org.junit.Test

class RoutingEngineTest {

    @Test
    fun testRoutesChildSafetyToDcpuAndChildNgo() {
        val dest = RoutingEngine.resolveRouting(ComplaintCategory.CHILD_SAFETY, "Madhubani")

        assertTrue("Must route to DCPU", dest.departmentName.contains("बाल संरक्षण"))
        assertTrue("Must route to Child Protection NGO", dest.ngoName.contains("बाल सुरक्षा") || dest.ngoName.contains("बचपन बचाओ"))
        assertTrue("Must be labeled as demo configuration", dest.isDemoConfiguration)
    }

    @Test
    fun testRoutesCivicNeglectToPhedAndCivicNgo() {
        val dest = RoutingEngine.resolveRouting(ComplaintCategory.CIVIC_NEGLECT, "Darbhanga")

        assertTrue("Must route to RWD/PHED", dest.departmentName.contains("लोक स्वास्थ्य") || dest.departmentName.contains("ग्रामीण कार्य"))
        assertTrue("Must route to Civic Action Network", dest.ngoName.contains("ग्राम वाणी") || dest.ngoName.contains("जन चेतना"))
    }

    @Test
    fun testRoutesEducationToDeoAndEducationNgo() {
        val dest = RoutingEngine.resolveRouting(ComplaintCategory.EDUCATION, "Madhubani")

        assertTrue("Must route to DEO", dest.departmentName.contains("शिक्षा"))
        assertTrue("Must route to Pratham or Education NGO", dest.ngoName.contains("प्रथम") || dest.ngoName.contains("शिक्षा"))
    }

    @Test
    fun testRoutesWelfareToDsoAndWelfareNgo() {
        val dest = RoutingEngine.resolveRouting(ComplaintCategory.WELFARE, "Sitamarhi")

        assertTrue("Must route to DSO/Social Welfare", dest.departmentName.contains("आपूर्ति") || dest.departmentName.contains("सामाजिक सुरक्षा"))
        assertTrue("Must route to Welfare NGO", dest.ngoName.contains("हकदर्शक") || dest.ngoName.contains("जवाबदेही"))
    }
}
