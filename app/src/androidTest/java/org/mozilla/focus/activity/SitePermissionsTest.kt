/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.focus.activity.robots.searchScreen
import org.mozilla.focus.helpers.FeatureSettingsHelper
import org.mozilla.focus.helpers.MainActivityFirstrunTestRule
import org.mozilla.focus.helpers.MockWebServerHelper
import org.mozilla.focus.helpers.TestHelper.grantAppPermission
import org.mozilla.focus.testAnnotations.SmokeTest

class SitePermissionsTest {
    private lateinit var webServer: MockWebServer
    private val featureSettingsHelper = FeatureSettingsHelper()
    /* Test page created and handled by the Mozilla mobile test-eng team */
    private val permissionsPage = "https://mozilla-mobile.github.io/testapp/permissions"
    private val testPageSubstring = "https://mozilla-mobile.github.io:443"

    @get: Rule
    val mActivityTestRule = MainActivityFirstrunTestRule(showFirstRun = false)

    @Before
    fun setUp() {
        featureSettingsHelper.setCfrForTrackingProtectionEnabled(false)
        webServer = MockWebServer().apply {
            dispatcher = MockWebServerHelper.AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        webServer.shutdown()
        featureSettingsHelper.resetAllFeatureFlags()
    }

    @Test
    fun testLocationSharingNotAllowed() {
        searchScreen {
        }.loadPage(permissionsPage) {
            clickGetLocationButton()
            verifyLocationPermissionPrompt(testPageSubstring)
            denySitePermissionRequest()
            verifyPageContent("User denied geolocation prompt")
        }
    }

    @Ignore("Needs mocking location for Firebase - to do: https://github.com/mozilla-mobile/mobile-test-eng/issues/585")
    @SmokeTest
    @Test
    fun testLocationSharingAllowed() {
        searchScreen {
        }.loadPage(permissionsPage) {
            clickGetLocationButton()
            verifyLocationPermissionPrompt(testPageSubstring)
            allowSitePermissionRequest()
            grantAppPermission()
            verifyPageContent("longitude")
            verifyPageContent("latitude")
        }
    }

    @SmokeTest
    @Test
    fun allowCameraPermissionsTest() {
        searchScreen {
        }.loadPage(permissionsPage) {
            clickGetCameraButton()
            grantAppPermission()
            verifyCameraPermissionPrompt(testPageSubstring)
            allowSitePermissionRequest()
            verifyPageContent("Camera allowed")
        }
    }

    @SmokeTest
    @Test
    fun denyCameraPermissionsTest() {
        searchScreen {
        }.loadPage(permissionsPage) {
            clickGetCameraButton()
            grantAppPermission()
            verifyCameraPermissionPrompt(testPageSubstring)
            denySitePermissionRequest()
            verifyPageContent("Camera not allowed")
        }
    }
}