package com.lighko.contacts

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

class ContactUtils(private val context: Context) {

    fun deleteContact(contactId: String) {
        val contentResolver: ContentResolver = context.contentResolver
        val contactUri: Uri = ContactsContract.RawContacts.CONTENT_URI.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .build()

        val selection = ContactsContract.RawContacts._ID + " = ?"
        val selectionArgs = arrayOf(contactId)

        contentResolver.delete(contactUri, selection, selectionArgs)
    }
}