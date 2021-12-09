package com.samples.storage.scopedstorage.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

/**
 * An [ActivityResultContract] to request the modification of a media [Uri]
 *
 * @return true if the [Uri] is modifiable, else the user denied the user denied the request (from API 30+)
 */
class ModifyMediaRequest : ActivityResultContract<Uri, Uri?>() {

    override fun createIntent(context: Context, input: Uri): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(ActivityResultContracts.StartIntentSenderForResult.ACTION_INTENT_SENDER_REQUEST)
                .putExtra(
                    ActivityResultContracts.StartIntentSenderForResult.EXTRA_INTENT_SENDER_REQUEST,
                    IntentSenderRequest.Builder(
                        MediaStore.createWriteRequest(
                            context.contentResolver,
                            listOf(input)
                        ).intentSender
                    ).build()
                )
        } else {
            Intent()
        }
    }

    override fun getSynchronousResult(context: Context, input: Uri): SynchronousResult<Uri?>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            null
        } else {
            SynchronousResult(input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return null
//        return resultCode == Activity.RESULT_OK
    }
}