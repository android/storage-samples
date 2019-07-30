/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.sharingshortcuts;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides the Sharing Shortcuts items to the system.
 * <p>
 * Use the ShortcutManagerCompat to make it work on older Android versions
 * without any extra work needed.
 * <p>
 * Interactions with the ShortcutManager API can happen on any thread.
 */
public class SharingShortcutsManager {

    /**
     * Define maximum number of shortcuts.
     * Don't add more than {@link ShortcutManagerCompat#getMaxShortcutCountPerActivity(Context)}.
     */
    private static final int MAX_SHORTCUTS = 4;

    /**
     * Category name defined in res/xml/shortcuts.xml that accepts data of type text/plain
     * and will trigger {@link SendMessageActivity}
     */
    private static final String CATEGORY_TEXT_SHARE_TARGET =
            "com.example.android.sharingshortcuts.category.TEXT_SHARE_TARGET";

    /**
     * Publish the list of dynamics shortcuts that will be used in Direct Share.
     * <p>
     * For each shortcut, we specify the categories that it will be associated to,
     * the intent that will trigger when opened as a static launcher shortcut,
     * and the Shortcut ID between other things.
     * <p>
     * The Shortcut ID that we specify in the {@link ShortcutInfoCompat.Builder} constructor will
     * be received in the intent as {@link Intent#EXTRA_SHORTCUT_ID}.
     * <p>
     * In this code sample, this method is completely static. We are always setting the same sharing
     * shortcuts. In a real-world example, we would replace existing shortcuts depending on
     * how the user interacts with the app as often as we want to.
     */
    public void pushDirectShareTargets(@NonNull Context context) {
        ArrayList<ShortcutInfoCompat> shortcuts = new ArrayList<>();

        // Category that our sharing shortcuts will be assigned to
        Set<String> contactCategories = new HashSet<>();
        contactCategories.add(CATEGORY_TEXT_SHARE_TARGET);

        // Adding maximum number of shortcuts to the list
        for (int id = 0; id < MAX_SHORTCUTS; ++id) {
            Contact contact = Contact.byId(id);

            // Item that will be sent if the shortcut is opened as a static launcher shortcut
            Intent staticLauncherShortcutIntent = new Intent(Intent.ACTION_DEFAULT);

            // Creates a new Sharing Shortcut and adds it to the list
            // The id passed in the constructor will become EXTRA_SHORTCUT_ID in the received Intent
            shortcuts.add(new ShortcutInfoCompat.Builder(context, Integer.toString(id))
                    .setShortLabel(contact.getName())
                    // Icon that will be displayed in the share target
                    .setIcon(IconCompat.createWithResource(context, contact.getIcon()))
                    .setIntent(staticLauncherShortcutIntent)
                    // Make this sharing shortcut cached by the system
                    // Even if it is unpublished, it can still appear on the sharesheet
                    .setLongLived()
                    .setCategories(contactCategories)
                    // Person objects are used to give better suggestions
                    .setPerson(new Person.Builder()
                            .setName(contact.getName())
                            .build())
                    .build());
        }

        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts);
    }

    /**
     * Remove all dynamic shortcuts
     */
    public void removeAllDirectShareTargets(@NonNull Context context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context);
    }
}
