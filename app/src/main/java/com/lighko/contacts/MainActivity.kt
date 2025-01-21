package com.lighko.contacts

import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity(), ContactListAdapter.OnItemLongClickListener {

    companion object {
        private const val REQUEST_READ_CONTACTS = 101
        private const val PERMISSION_REQUEST_WRITE_CONTACTS = 102
    }

    private val contacts = ArrayList<ContactItem>()
    // Declare RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var textview: TextView
    private lateinit var edittext: EditText
    private lateinit var contactListAdapter: ContactListAdapter
    var contactId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edittext = findViewById<EditText>(R.id.etSearch)
        textview = findViewById<TextView>(R.id.txtNoData)
        // getting the recyclerview by its id
        recyclerView = findViewById<RecyclerView>(R.id.recyclerVehiclelist)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS
            )
        } else {
            loadContacts()
        }

        edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do something before text changed
            }

            override fun onTextChanged(
                searchvalue: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val searchString = searchvalue?.toString() ?: ""
                if (searchString.length >= 1) {
                    val filteredData = contacts.filter { contacts ->
                        contacts.name?.contains(searchString, ignoreCase = true) == true
                    }.toMutableList()

                    if (filteredData.size == 0) {
                        recyclerView.visibility = View.GONE
                        textview.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        textview.visibility = View.GONE
                        contactListAdapter.updateItems(filteredData, true)
                    }
                } else {
                    loadContacts()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // do something after text changed
            }
        })

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadContacts()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            PERMISSION_REQUEST_WRITE_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    deleteContactFromDevice(contactId)
                } else {
                    Toast.makeText(this, "Permission denied to write contacts", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
        }
    }

    @SuppressLint("Range")
    private fun loadContacts() {
        // this creates a vertical layout Manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(ContactItem(name, phoneNumber, id))
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        contactListAdapter = ContactListAdapter(this, contacts, this)
        recyclerView.adapter = contactListAdapter
    }

    override fun onItemLongClicked(position: Int) {
        deleteContact(position)
    }

    private fun deleteContact(position: Int) {
        val contact = contacts[position]
        // Call method to delete the contact
        // Check if the permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_CONTACTS),
                PERMISSION_REQUEST_WRITE_CONTACTS
            )
        } else {
            contactId = contact.id.toString()
            // Permission is already granted, proceed with contact deletion
            contact.id?.let { deleteContactFromDevice(it) }
        }

        // Remove the contact from the list and update RecyclerView
        contacts.removeAt(position)
        contactListAdapter.notifyItemRemoved(position)
    }

    private fun deleteContactFromDevice(contactId: String) {
        val contactUtils = ContactUtils(applicationContext)
        contactUtils.deleteContact(contactId)
        Toast.makeText(this, "This Contacts deleted now", Toast.LENGTH_SHORT).show()
    }
}