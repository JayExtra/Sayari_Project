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
import com.dev.james.sayariproject.databinding.SingleNewsItemBinding
import com.dev.james.sayariproject.models.Article
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import kotlin.time.milliseconds

class ArticlesRecyclerAdapter(
    private val action : (String?) -> Unit
) : PagingDataAdapter<Article,ArticlesRecyclerAdapter.ArticlesViewHolder>(DiffCallback()) {

    private val formatter : DateFormat
        get() =
            SimpleDateFormat("dd-MMM-yyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val binding = SingleNewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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


    inner class ArticlesViewHolder(private val binding : SingleNewsItemBinding) : RecyclerView.ViewHolder(binding.root){
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
        private fun setUpDate(article: Article, binding: SingleNewsItemBinding) {
            binding.apply {
                val zonedTime = article.createdDateFormatted

                zonedTime?.let {
                    val date = Date.from(zonedTime.toInstant())
                    val formatedDate = formatter.format(date)
                    binding.daysIndicatorTxt.text = formatedDate
                }

            }
        }

        private fun setUpImage(article: Article, binding: SingleNewsItemBinding) {

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