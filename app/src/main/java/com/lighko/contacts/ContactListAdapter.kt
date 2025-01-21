package com.lighko.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactListAdapter(private val context: Context,
                         val mList: MutableList<ContactItem>,
                         private val onItemLongClickListener: OnItemLongClickListener) : RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inflate_contact_list, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val itemsViewModel = mList[position]
        // sets the text to the textview from our itemHolder class
        holder.textView.text = itemsViewModel.name

        holder.itemView.setOnClickListener {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, itemsViewModel.id)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClickListener.onItemLongClicked(position)
            true
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.contactName)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(items: MutableList<ContactItem>, clearData: Boolean) {
        if (clearData) {
            mList.clear()
        }
        mList.addAll(items)
        notifyDataSetChanged()
    }
    interface OnItemLongClickListener {
        fun onItemLongClicked(position: Int)
    }


}