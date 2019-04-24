/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.actionopendocument

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.widget.Button
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for ActionOpenDocument sample.
 */
@RunWith(AndroidJUnit4::class)
class ActionOpenDocumentFragmentTests {

    private lateinit var fragment: ActionOpenDocumentFragment
    private lateinit var btnPrevious: Button
    private lateinit var btnNext: Button

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun before() {
        activityTestRule.activity.supportFragmentManager.beginTransaction()
        fragment = activityTestRule.activity.supportFragmentManager
            .findFragmentByTag(DOCUMENT_FRAGMENT_TAG) as ActionOpenDocumentFragment
    }

    @Test
    fun testActivityTitle() {
        // The title of the activity should be "ActionOpenDocument (1/10)" at first
        val expectedActivityTitle = activityTestRule.activity.getString(
            R.string.app_name_with_index, 1, fragment.pageCount
        )
        assertEquals(expectedActivityTitle, activityTestRule.activity.title)
    }

    @Test
    fun testButtons_previousDisabledAtFirst() {
        setUpButtons()
        // Check that the previous button is disabled at first
        assertFalse(btnPrevious.isEnabled)
        // The next button should be enabled
        assertTrue(btnNext.isEnabled)
    }

    @Test
    fun testButtons_bothEnabledInMiddle() {
        setUpButtons()
        turnPages(1)
        // Two buttons should be both enabled
        assertTrue(btnPrevious.isEnabled)
        assertTrue(btnNext.isEnabled)
    }

    @Test
    fun testButtons_nextDisabledLastPage() {
        setUpButtons()
        val pageCount = fragment.pageCount
        // Click till it reaches the last page
        turnPages(pageCount - 1)
        // Check the page count
        val expectedActivityTitle = activityTestRule.activity.getString(
            R.string.app_name_with_index, pageCount, pageCount
        )
        assertEquals(expectedActivityTitle, activityTestRule.activity.title)
        // The previous button should be enabled
        assertTrue(btnPrevious.isEnabled)
        // Check that the next button is disabled
        assertFalse(btnNext.isEnabled)
    }

    @Test
    fun testOrientationChangePreserveState() {
        activityTestRule.activity.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        setUpButtons()
        turnPages(1)
        val pageCount = fragment.pageCount
        val expectedActivityTitle = activityTestRule.activity
            .getString(R.string.app_name_with_index, 2, pageCount)
        assertEquals(expectedActivityTitle, activityTestRule.activity.title)
        activityTestRule.activity.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
        // Check that the title is the same after orientation change
        assertEquals(expectedActivityTitle, activityTestRule.activity.title)
    }

    /**
     * Prepares references to the buttons "Previous" and "Next".
     */
    private fun setUpButtons() {
        val view = fragment.view ?: return
        btnPrevious = view.findViewById(R.id.previous)
        btnNext = view.findViewById(R.id.next)
    }

    /**
     * Click the "Next" button to turn the pages.
     *
     * @param count The number of times to turn pages.
     */
    private fun turnPages(count: Int) {
        for (i in 0 until count) {
            onView(withId(R.id.next)).perform(click())
        }
    }

}
