package com.pushup.notificationreader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pushup.notificationreader.data.NotificationEntity
import com.pushup.notificationreader.databinding.ItemNotificationBinding

class NotificationAdapter :
    ListAdapter<NotificationEntity, NotificationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationEntity) {
            binding.tvAppName.text = item.packageName
            binding.tvTimestamp.text = item.formattedDateTime()
            binding.tvTitle.text = item.title
            binding.tvContent.text = item.text

            if (item.subText.isNotEmpty()) {
                binding.tvSubText.text = item.subText
                binding.tvSubText.visibility = View.VISIBLE
            } else {
                binding.tvSubText.visibility = View.GONE
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(old: NotificationEntity, new: NotificationEntity): Boolean {
            return old.id == new.id
        }

        override fun areContentsTheSame(old: NotificationEntity, new: NotificationEntity): Boolean {
            return old == new
        }
    }
}
