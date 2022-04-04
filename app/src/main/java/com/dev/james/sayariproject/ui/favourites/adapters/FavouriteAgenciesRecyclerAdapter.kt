package com.dev.james.sayariproject.ui.favourites.adapters

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.browse.MediaBrowser
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dev.james.sayariproject.R
import com.dev.james.sayariproject.databinding.SingleCrewMemberCardBinding
import com.dev.james.sayariproject.models.favourites.Result

class FavouriteAgenciesRecyclerAdapter(
    val action : (Result) -> Unit
) : ListAdapter<Result , FavouriteAgenciesRecyclerAdapter.FavAgenciesViewHolder>(DiffUtilCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavAgenciesViewHolder {
        val binding = SingleCrewMemberCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return FavAgenciesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavAgenciesViewHolder, position: Int) {
        val item = getItem(position)
        if(item!=null){
            holder.bind(item)
        }
    }
    inner class FavAgenciesViewHolder(
        private val binding : SingleCrewMemberCardBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(agency : Result){
            binding.root.setOnClickListener {
                action.invoke(agency)
            }

            //set up the rest of the views
            binding.apply {
                astroNameTxt.text = agency.name
                astroAgency.text = agency.country_code
                roleTxt.text = agency.abbrev

                //load image
                loadImage(binding , agency)
            }

        }

        private fun loadImage(binding: SingleCrewMemberCardBinding, agency: Result) {
            binding.apply {
                Glide.with(binding.root)
                    .load(agency.image_url)
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
                            astroImgProgress.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            astroImgProgress.isVisible = false
                            return false
                        }

                    })
                    .into(astroImg)
            }
        }
    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<Result>(){
        override fun areItemsTheSame(oldItem: Result, newItem: Result): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Result, newItem: Result): Boolean =
            oldItem == newItem

    }


}