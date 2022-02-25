package com.dev.james.sayariproject.ui.iss.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
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
import com.dev.james.sayariproject.models.iss.Astronaut
import com.dev.james.sayariproject.models.iss.Crew

class CrewRecyclerAdapter : ListAdapter<Crew, CrewRecyclerAdapter.CrewViewHolder>(DiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrewViewHolder {
        val binding = SingleCrewMemberCardBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return CrewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CrewViewHolder, position: Int) {
        val item = getItem(position)
        if(item != null){
            holder.bind(item)
        }
    }

    inner class CrewViewHolder(
        private val binding : SingleCrewMemberCardBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(crew: Crew){
            binding.apply {
                //load image into circular image
                loadImage(binding , crew.astronaut)

                //set various strings
                astroNameTxt.text = crew.astronaut.name
                astroAgency.text = crew.astronaut.agency.name
                roleTxt.text = crew.role.role
            }
        }

        private fun loadImage(binding: SingleCrewMemberCardBinding, astronaut: Astronaut) {
            Glide.with(binding.root)
                .load(astronaut.profile_image)
                .centerCrop()
                .error(R.drawable.ic_broken_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.astroImgProgress.isInvisible = true
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.astroImgProgress.isInvisible = true
                        return false
                    }
                })
                .into(binding.astroImg)
        }
    }

    class DiffCallBack : DiffUtil.ItemCallback<Crew>(){
        override fun areItemsTheSame(oldItem: Crew, newItem: Crew) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Crew, newItem: Crew) =
            oldItem == newItem
    }
}