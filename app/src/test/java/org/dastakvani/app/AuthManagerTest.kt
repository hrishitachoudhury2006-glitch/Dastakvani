package org.dastakvani.app

import org.dastakvani.app.data.auth.AuthManager
import org.junit.Assert.*
import org.junit.Test

class AuthManagerTest {

    @Test
    fun testValidVolunteerLoginSucceeds() {
        AuthManager.logout()
        assertFalse(AuthManager.isAuthenticated)

        val result = AuthManager.login("volunteer@biharngo.org", "dastak123")
        assertTrue("Login must succeed for valid volunteer", result.isSuccess)
        assertTrue("User must be authenticated", AuthManager.isAuthenticated)
        assertEquals("Madhubani", result.getOrNull()?.assignedDistrict)
    }

    @Test
    fun testInvalidLoginFails() {
        AuthManager.logout()
        val result = AuthManager.login("random@unknown.org", "wrongpass")
        assertTrue("Login must fail for invalid credentials", result.isFailure)
        assertFalse(AuthManager.isAuthenticated)
    }
}
