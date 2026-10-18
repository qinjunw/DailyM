package com.percontext.app.feature.app

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.percontext.app.R
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrandingResourcesTest {
    @Test
    fun displayNameAndNotificationTitlesUseTheMatchingLanguage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(R.string.app_name, context.applicationInfo.labelRes)
        listOf(
            Locale.SIMPLIFIED_CHINESE to "喵记",
            Locale.TRADITIONAL_CHINESE to "喵记",
            Locale.US to "DailyM",
            Locale.FRANCE to "喵记",
        ).forEach { (locale, expectedName) ->
            val configuration = Configuration(context.resources.configuration).apply {
                setLocale(locale)
            }
            val localized = context.createConfigurationContext(configuration)
            assertEquals(expectedName, localized.getString(R.string.app_name))
            listOf(R.string.recording_notification_title, R.string.transcription_notification_title)
                .forEach { resource ->
                    val title = localized.getString(resource)
                    assertTrue(title.contains(expectedName))
                    assertFalse(title.contains("PerContext"))
                }
            assertEquals(
                if (locale.language == "en") "About DailyM" else "关于 喵记",
                localized.getString(R.string.about_title, expectedName),
            )
        }
    }
}
