package com.dev.james.sayariproject.ui.search.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.dev.james.sayariproject.databinding.SingleMissionCardBinding
import com.dev.james.sayariproject.models.discover.ActiveMissions
import de.hdodenhof.circleimageview.CircleImageView

class MissionsRecyclerAdapter(
    private val action : (String) -> Unit
) : ListAdapter<ActiveMissions , MissionsRecyclerAdapter.MissionsViewHolder>(DiffCallback())  {
    private lateinit var context : Context

    inner class MissionsViewHolder(
        private val binding : SingleMissionCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mission: ActiveMissions, patch: CircleImageView){
            binding.apply {
                missionName.text = mission.title
            }
            loadImage(binding , mission , patch)
            binding.root.setOnClickListener {
                action.invoke(context.getString(R.string.feature_message))
            }
        }

        private fun loadImage(
            binding: SingleMissionCardBinding,
            mission: ActiveMissions,
            patch: CircleImageView
        ) {
            var dominantColor: Int = 0

            binding.apply {

                Glide.with(binding.root)
                    .load(mission.missionPatch)
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
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val drawable = resource as BitmapDrawable
                            val bitmap = drawable.bitmap

                            Palette.Builder(bitmap).generate {
                                it?.let { palette ->
                                    dominantColor = palette.getDominantColor(
                                        ContextCompat.getColor(root.context , R.color.white)
                                    )
                                   missionCard.setCardBackgroundColor(dominantColor)
                                }
                            }
                            return false
                        }

                    })
                    .into(patch)

            }

        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ActiveMissions>(){
        override fun areItemsTheSame(oldItem: ActiveMissions, newItem: ActiveMissions) : Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ActiveMissions, newItem: ActiveMissions): Boolean =
            oldItem == newItem

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionsViewHolder {
        val binding = SingleMissionCardBinding.inflate(LayoutInflater.from(parent.context) ,parent ,  false)
        context = parent.context
        return MissionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MissionsViewHolder, position: Int) {
        val missionItem = getItem(position)
        val patch = holder.itemView.findViewById<CircleImageView>(R.id.missionPatch)
        if(missionItem!=null){
            holder.bind(missionItem , patch)
        }

    }
}