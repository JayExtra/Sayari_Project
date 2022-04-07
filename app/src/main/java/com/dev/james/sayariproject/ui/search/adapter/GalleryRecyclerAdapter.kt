package com.dev.james.sayariproject.ui.search.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleImageCardBinding
import com.dev.james.sayariproject.models.articles.Article

class GalleryRecyclerAdapter(
    val action : (String) -> Unit
) : ListAdapter<Article , GalleryRecyclerAdapter.GalleryViewHolder>(DiffCallback()) {
    private lateinit var context : Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = SingleImageCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        context = parent.context
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
       val item = getItem(position)
        if(item != null){
            holder.bind(item)
        }
    }
    inner class GalleryViewHolder(
        private val binding : SingleImageCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article : Article){
            //do operations
            binding.publisherName.text = article.site
            loadImage(article , binding)
            binding.publisherImg.setCircleBackgroundColorResource(R.color.white)
            binding.publisherImg.setImageResource(R.drawable.sayari_logo2)

            binding.root.setOnClickListener {
                action.invoke(context.getString(R.string.feature_message))
            }
        }


        private fun loadImage(article: Article, binding: SingleImageCardBinding) {
            Glide.with(binding.root)
                .load(article.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.imgProgressBar.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                       binding.imgProgressBar.isVisible = false
                        return false
                    }

                })
                .into(binding.cardBgImage)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article) =
            oldItem == newItem

    }

}