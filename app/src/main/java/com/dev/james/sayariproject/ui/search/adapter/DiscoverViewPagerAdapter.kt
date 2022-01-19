package com.dev.james.sayariproject.ui.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.models.articles.Article

class DiscoverViewPagerAdapter(
    val article : List<Article>,
    val action : (String) -> Unit
) : RecyclerView.Adapter<DiscoverViewPagerAdapter.DiscViewHolder>() {
    inner class DiscViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.single_top_news_item ,
            parent , false)
        return DiscViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiscViewHolder, position: Int) {
        val articleItem = article[position]

        val topNewsImage = holder.itemView.findViewById<ImageView>(R.id.topNewsImage)
        val topNewsTitle = holder.itemView.findViewById<TextView>(R.id.topNewsHeadingTxt)
        val topNewsButton = holder.itemView.findViewById<Button>(R.id.learnMoreBtn)
        topNewsButton.isGone = true

        Glide.with(holder.itemView.context)
            .load(articleItem.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_broken_image)
            .into(topNewsImage)

        topNewsTitle.text = articleItem.title

        topNewsTitle.setOnClickListener {
            action.invoke(articleItem.url)
        }
    }

    override fun getItemCount(): Int {
        return article.size
    }
}