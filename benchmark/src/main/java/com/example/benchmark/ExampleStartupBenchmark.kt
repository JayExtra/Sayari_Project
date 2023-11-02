package com.example.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/**
 * This is an example startup benchmark.
 *
 * It navigates to the device's home screen, and launches the default activity.
 *
 * Before running this benchmark:
 * 1) switch your app's active build variant in the Studio (affects Studio runs only)
 * 2) add `<profileable android:shell="true" />` to your app's manifest, within the `<application>` tag
 *
 * Run this benchmark from Studio to see startup measurements, and captured system traces
 * for investigating your app's performance.
 */
@RunWith(AndroidJUnit4::class)
class ExampleStartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    companion object{
        const val APP_PACKAGE_NAME = "com.dev.james.sayariproject"
    }

    /*tests app's startup time or TTID (Time to initial display)*/
    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = APP_PACKAGE_NAME ,
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }

    /**/

    @Test
    fun scroll() = benchmarkRule.measureRepeated(
        packageName = APP_PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        iterations = 1,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
        addElementsAndScrollHorizontally()
    }
}

fun MacrobenchmarkScope.addElementsAndScrollHorizontally(){
   // val recyclerViewSelector = UiSelector().resourceId("@id/latesNewsRecyclerView")
    val nextBtn = device.findObject(By.text("next"))
    nextBtn?.click()
    val finishBtn = device.findObject(By.text("finish"))
    finishBtn?.click()
    device.wait(Until.hasObject(By.res("com.dev.james.sayariproject" , "@id/parentPanel")) , 3000)
   /* val okayBtn = device.findObject(
       UiSelector()
           .text("Okay".uppercase())
    )
    okayBtn?.click()

    val allowPermission = device.findObject(
        UiSelector()
            .text("Allow")
    )
    allowPermission?.click()*/

    device.wait(Until.hasObject( By.res(  "com.dev.james.sayariproject", "@android:id/latesNewsRecyclerView")), 3000)
    val rvSelector = UiSelector().resourceId("com.dev.james.sayariproject:id/latesNewsRecyclerView")
    val rv = device.findObject(rvSelector)
    val scrollableRvSelector = rv.selector
    val scrollableRv = UiScrollable(scrollableRvSelector)
    device.waitForIdle()
    scrollableRv.scrollToEnd(2)
    scrollableRv.scrollToBeginning(2)


}