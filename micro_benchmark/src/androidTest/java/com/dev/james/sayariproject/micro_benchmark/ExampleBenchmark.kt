package com.dev.james.sayariproject.micro_benchmark

import android.util.Log
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class ExampleBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun log() {
        benchmarkRule.measureRepeated {
            Log.d("LogBenchmark", "the cost of writing this log method will be measured")
        }
    }
//
//    // using random with the same seed, so that it generates the same data every run
//    private val random = Random(0)
//
//    // create the array once and just copy it in benchmarks
//    private val unsorted = IntArray(10_000) { random.nextInt() }
//
//    @Test
//    fun benchmark_quickSort() {
//        // ...
//        benchmarkRule.measureRepeated {
//            // copy the array with timing disabled to measure only the algorithm itself
//            listToSort = runWithTimingDisabled { unsorted.copyOf() }
//
//            // sort the array in place and measure how long it takes
//            SortingAlgorithms.quickSort(listToSort)
//        }
//
//        // assert only once not to add overhead to the benchmarks
//        assertTrue(listToSort.isSorted)
//    }
}