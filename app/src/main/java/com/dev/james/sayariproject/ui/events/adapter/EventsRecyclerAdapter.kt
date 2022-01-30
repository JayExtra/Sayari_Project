package com.dev.james.sayariproject.ui.events.adapter

import android.graphics.drawable.Drawable
import android.os.Build
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleEventColorBinding
import com.dev.james.sayariproject.models.events.Events
import java.lang.reflect.Array.get
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class EventsRecyclerAdapter : PagingDataAdapter<Events , EventsRecyclerAdapter.EventsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        val binding = SingleEventColorBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return EventsViewHolder(binding)
    }
    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) {
        val item = getItem(position)
        if(item!=null){
         holder.bind(item)
        }
    }

    inner class EventsViewHolder(
        private val binding : SingleEventColorBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(events: Events){
            binding.apply {
                missionSlug.text = events.name
                eventDescription.text = events.description

                setUpExpandableCard(binding)

                setupDate(events , binding)
                loadImage(events , binding)
            }
        }

        private fun setUpExpandableCard(binding: SingleEventColorBinding) {
            binding.apply {
                expandEventBtn.setOnClickListener {
                    if(expandableLayout.visibility == View.GONE){
                        TransitionManager.beginDelayedTransition(eventCard , AutoTransition())
                        expandableLayout.visibility = View.VISIBLE
                        expandEventBtn.text = "COLLAPSE"
                    }else {
                        TransitionManager.beginDelayedTransition(eventCard , AutoTransition())
                        expandableLayout.visibility = View.GONE
                        expandEventBtn.text = "EXPAND"
                    }
                }
            }
        }

        private fun loadImage(events: Events, binding: SingleEventColorBinding) {
            binding.apply {
                Glide.with(binding.root)
                    .load(events.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_broken_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                           eventProgress.isInvisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            eventProgress.isVisible = false
                            return false
                        }
                    })
                    .into(eventImage)
            }
        }

        private fun setupDate(events: Events, binding: SingleEventColorBinding) {
           binding.apply {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                   // only for OREO and newer versions
                   val dateFormat = ZonedDateTime.parse(events.date)

                   val API_TIME_STAMP_PATTERN = "dd-MM-yyyy HH:mm:ss.SSS"

                   val dateTimeFormatter : DateTimeFormatter =
                       DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN, Locale.ROOT)


                   val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

                   // val formattedDate1 = createdDateFormatted.format(DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN))

                   val formattedDate2 = createdDateFormatted.format(dateTimeFormatter)

                   Log.d("ArticlesRv", "setUpDate: $formattedDate2 ")


                   eventDate.text = formattedDate2
               }else {
                   val dateFormat : SimpleDateFormat =
                       SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss")
                   val eDate : Date = dateFormat.parse(events.date)
                   eventDate.text = eDate.toString()
               }
           }
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<Events>() {
        override fun areItemsTheSame(oldItem: Events, newItem: Events): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Events, newItem: Events): Boolean =
            oldItem == newItem

    }

}
