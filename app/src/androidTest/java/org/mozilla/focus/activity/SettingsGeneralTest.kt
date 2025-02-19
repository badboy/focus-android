/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity

import android.content.res.Configuration
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mozilla.focus.activity.robots.browserScreen
import org.mozilla.focus.activity.robots.homeScreen
import org.mozilla.focus.helpers.MainActivityIntentsTestRule
import org.mozilla.focus.helpers.StringsHelper.EN_FRENCH_LOCALE
import org.mozilla.focus.helpers.StringsHelper.EN_LANGUAGE_MENU_HEADING
import org.mozilla.focus.helpers.StringsHelper.FR_ENGLISH_LOCALE
import org.mozilla.focus.helpers.StringsHelper.FR_GENERAL_HEADING
import org.mozilla.focus.helpers.StringsHelper.FR_HELP
import org.mozilla.focus.helpers.StringsHelper.FR_LANGUAGE_MENU
import org.mozilla.focus.helpers.StringsHelper.FR_LANGUAGE_SYSTEM_DEFAULT
import org.mozilla.focus.helpers.StringsHelper.FR_SETTINGS
import org.mozilla.focus.helpers.TestHelper.exitToTop
import org.mozilla.focus.helpers.TestHelper.verifyTranslatedTextExists
import org.mozilla.focus.testAnnotations.SmokeTest
import java.util.Locale

// Tests for the General settings sub-menu: changing theme, locale and default browser
class SettingsGeneralTest {
    @get: Rule
    var mActivityTestRule = MainActivityIntentsTestRule(showFirstRun = false)

    @get: Rule
    var watcher: TestRule = object : TestWatcher() {
        override fun starting(description: Description) {
            println("Starting test: " + description.methodName)
            if (description.methodName == "frenchLocaleTest") {
                changeLocale("fr")
            }
        }
    }

    @After
    fun tearDown() {
        changeLocale("en")
    }

    @Ignore("Failing , see https://github.com/mozilla-mobile/focus-android/issues/6812")
    @SmokeTest
    @Test
    fun changeThemeTest() {
        homeScreen {
        }.openMainMenu {
        }.openSettings {
        }.openGeneralSettingsMenu {
            verifyThemesList()
            selectDarkTheme()
            verifyThemeApplied(isDarkTheme = true, getThemeState = getUiTheme())
            selectLightTheme()
            verifyThemeApplied(isLightTheme = true, getThemeState = getUiTheme())
            selectDeviceTheme()
            verifyThemeApplied(isLightTheme = true, getThemeState = getUiTheme())
        }
    }

    @Ignore("Failing after refactoring Locale Screen #5293")
    @Test
    fun englishSystemLocaleTest() {
        /* Go to Settings and change language to French*/
        homeScreen {
        }.openMainMenu {
        }.openSettings {
        }.openGeneralSettingsMenu {
            openLanguageSelectionMenu()
            verifyLanguageSelected("System default")
            selectLanguage(EN_FRENCH_LOCALE)
            verifyTranslatedTextExists(FR_LANGUAGE_MENU)
            exitToTop()
        }
        /* Exit to main and see the UI is in French as well */
        homeScreen {
        }.openMainMenu {
            verifyTranslatedTextExists(FR_SETTINGS)
            verifyTranslatedTextExists(FR_HELP)
            /* change back to system locale, verify the locale is changed */
        }.openSettings(FR_SETTINGS) {
        }.openGeneralSettingsMenu(FR_GENERAL_HEADING) {
            openLanguageSelectionMenu(FR_LANGUAGE_MENU)
            selectLanguage(FR_LANGUAGE_SYSTEM_DEFAULT)
            verifyTranslatedTextExists(EN_LANGUAGE_MENU_HEADING)
            exitToTop()
        }
        homeScreen {
        }.openMainMenu {
            verifySettingsButtonExists()
            verifyHelpPageLinkExists()
        }
    }

    @Ignore("Failing, see https://github.com/mozilla-mobile/focus-android/issues/4851")
    @Test
    fun frenchLocaleTest() {
        /* Go to Settings */
        homeScreen {
        }.openMainMenu {
        }.openSettings(FR_SETTINGS) {
        }.openGeneralSettingsMenu(FR_GENERAL_HEADING) {
            openLanguageSelectionMenu(FR_LANGUAGE_MENU)
            verifyLanguageSelected(FR_LANGUAGE_SYSTEM_DEFAULT)
            /* change locale to English, verify the locale is changed */
            selectLanguage(FR_ENGLISH_LOCALE)
            verifyTranslatedTextExists(EN_LANGUAGE_MENU_HEADING)
            exitToTop()
        }
        homeScreen {
        }.openMainMenu {
            verifySettingsButtonExists()
            verifyHelpPageLinkExists()
        }
    }

    @SmokeTest
    @Test
    fun changeDefaultBrowserTest() {
        val supportPageUrl = "https://support.mozilla.org/en-US/kb/set-firefox-focus-default-browser-android"

        homeScreen {
        }.openMainMenu {
        }.openSettings {
        }.openGeneralSettingsMenu {
            clickSetDefaultBrowser()
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                browserScreen {
                    verifyPageURL(supportPageUrl)
                }
            } else {
                verifyAndroidDefaultAppsMenuAppears()
                // for API 24 to 28 we'll skip these steps because the switch doesn't update after
                // returning from Default apps settings, not reproducing manually
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    selectFocusDefaultBrowser()
                    verifySwitchIsToggled(true)
                }
            }
        }
    }

    companion object {
        @Suppress("Deprecation")
        fun changeLocale(locale: String?) {
            val context = InstrumentationRegistry.getInstrumentation()
                .targetContext
            val res = context.applicationContext.resources
            val config = res.configuration
            config.setLocale(Locale(locale!!))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.createConfigurationContext(config)
            } else {
                res.updateConfiguration(config, res.displayMetrics)
            }
        }
    }

    private fun getUiTheme(): Boolean {
        val mode =
            mActivityTestRule.activity.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)

        return when (mode) {
            Configuration.UI_MODE_NIGHT_YES -> true // dark theme is set
            Configuration.UI_MODE_NIGHT_NO -> false // dark theme is not set, using light theme
            else -> false // default option is light theme
        }
    }
}
