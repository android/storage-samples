
Android ActionOpenDocument Sample (Kotlin)
========================================

This sample demonstrates how to display PDF document on screen using
the PdfRenderer introduced in Android 5.0 Lollipop.

Introduction
------------

You can now render PDF document pages into bitmap images for printing by using
the new [PdfRenderer][1] class. You must specify a [ParcelFileDescriptor][2]
that is seekable (that is, the content can be randomly accessed) on which the
system writes the the printable content. Your app can obtain a page for
rendering with [openPage()][3], then call [render()][4] to turn the opened
[PdfRenderer.Page][5] into a bitmap.

This sample loads the PDF from assets. Contents of assets are compressed by
default, and the PdfRenderer class cannot open it. In this sample, we work
around this by copying the file into the cache directory.

[1]: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer.html
[2]: https://developer.android.com/reference/android/os/ParcelFileDescriptor.html
[3]: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer.html#openPage(int)
[4]: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer.Page.html#render(android.graphics.Bitmap,%20android.graphics.Rect,%20android.graphics.Matrix,%20int)
[5]: https://developer.android.com/reference/android/graphics/pdf/PdfRenderer.Page.html

Pre-requisites
--------------

- Android SDK 28

Screenshots
-------------

<img src="screenshots/main.png" height="400" alt="Screenshot"/>

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

Support
-------

- Stack Overflow: http://stackoverflow.com/questions/tagged/android

If you've found an error in this sample, please file an issue:
https://github.com/android/storage/issues

Patches are encouraged, and may be submitted by forking this project and
submitting a pull request through GitHub. Please see CONTRIBUTING.md for more details.

License
-------

Copyright 2017 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
