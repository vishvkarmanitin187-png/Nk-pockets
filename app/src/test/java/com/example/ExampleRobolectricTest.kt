package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NK Pockets", appName)
  }

  @Test
  fun `login credits welcome bonus and enables jackpot spin`() {
    val viewModel = MainViewModel()
    viewModel.login("9876543210")
    assertTrue(viewModel.isLoggedIn.value)
    assertTrue(viewModel.bonusBalance.value >= 3.0)
    assertTrue(viewModel.bonusBalance.value <= 100.0)

    val spinResult = viewModel.spinJackpot(10.0)
    assertNotNull(spinResult)
    assertTrue(viewModel.progressiveJackpot.value > 87450.00)
  }
}
