package com.dev.james.sayariproject.ui.home.adapters

import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
import com.dev.james.sayariproject.databinding.SingleNewsCardBinding
import com.dev.james.sayariproject.databinding.SingleNewsItemBinding
import com.dev.james.sayariproject.models.articles.Article
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ArticlesRecyclerAdapter(
    private val action : (String?) -> Unit
) : PagingDataAdapter<Article,ArticlesRecyclerAdapter.ArticlesViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val binding = SingleNewsCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticlesViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ArticlesViewHolder, position: Int) {
        val currentItem = getItem(position)

        Log.i("RecyclerViewAdapter", "onBindViewHolder: Binding item at position ${position.toString()}")

        if (currentItem != null) {
            holder.binding(currentItem)
        }

    }


    inner class ArticlesViewHolder(private val binding : SingleNewsCardBinding) : RecyclerView.ViewHolder(binding.root){
        @RequiresApi(Build.VERSION_CODES.O)
        fun binding(article : Article) {
           binding.apply {
               newsSubHeadingTxt.text = article.title
               setUpImage(article , binding)
               setUpDate(article , binding)

               root.setOnClickListener {
                   action.invoke(article.url)
               }
           }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun setUpDate(article: Article, binding: SingleNewsCardBinding) {
            val dateFormat = ZonedDateTime.parse(article.date)

            val API_TIME_STAMP_PATTERN = "yyyy-MM-dd_HH:mm:ss.SSS"

            val dateTimeFormatter : DateTimeFormatter =
                DateTimeFormatter.ofPattern("EEE dd-M-yyyy", Locale.ROOT)


            val createdDateFormatted = dateFormat.withZoneSameInstant(ZoneId.of("Africa/Nairobi"))

           // val formattedDate1 = createdDateFormatted.format(DateTimeFormatter.ofPattern(API_TIME_STAMP_PATTERN))

            val formattedDate2 = createdDateFormatted.format(dateTimeFormatter)

            Log.d("ArticlesRv", "setUpDate: $formattedDate2 ")


            binding.daysIndicatorTxt.text = formattedDate2

        }

        private fun setUpImage(article: Article, binding: SingleNewsCardBinding) {

            binding.apply {
                Glide.with(binding.root)
                    .load(article.imageUrl)
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
                            homeProgressBar.isInvisible = true
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            homeProgressBar.isVisible = false
                            return false
                        }
                    })
                    .into(newsImage)
            }


        }
    }


    class DiffCallback : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem == newItem

    }


}