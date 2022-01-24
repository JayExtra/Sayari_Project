package com.dev.james.sayariproject.ui.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.james.sayariproject.databinding.SingleImageCardBinding
import com.dev.james.sayariproject.models.articles.Article

class GalleryRecyclerAdapter : ListAdapter<Article , GalleryRecyclerAdapter.GalleryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = SingleImageCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
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
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Article>(){
        override fun areItemsTheSame(oldItem: Article, newItem: Article) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article) =
            oldItem == newItem

    }

}