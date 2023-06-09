package com.dev.james.sayariproject.utilities.logging

import com.dev.james.sayariproject.BuildConfig
import timber.log.Timber

object CrashAndLog {
    fun setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
                }
            })
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}