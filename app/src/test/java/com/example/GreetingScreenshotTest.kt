package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.LivePulseBadge
import com.example.ui.theme.YTLiveManagerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun live_badge_screenshot() {
    composeTestRule.setContent {
      YTLiveManagerTheme {
        LivePulseBadge(isLive = true, customText = "LIVE")
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/live_badge.png")
  }
}
