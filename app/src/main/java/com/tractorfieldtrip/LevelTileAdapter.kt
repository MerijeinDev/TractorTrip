package com.tractorfieldtrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tractorfieldtrip.databinding.ItemLevelTileBinding

data class LevelTile(
    val number: Int,
    val unlocked: Boolean,
    val stars: Int
)

class LevelTileAdapter(
    private val levels: List<LevelTile>,
    private val onLevelClick: (LevelTile) -> Unit
) : RecyclerView.Adapter<LevelTileAdapter.LevelViewHolder>() {

    inner class LevelViewHolder(
        private val binding: ItemLevelTileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tile: LevelTile) {
            binding.tvLevelNumber.text = tile.number.toString()
            binding.tvLevelNumber.visibility = View.VISIBLE

            if (tile.unlocked) {
                binding.tileRoot.setBackgroundResource(R.drawable.bg_level_tile_unlocked)
                val outline = ContextCompat.getColor(binding.root.context, R.color.text_outline_brown)
                binding.tvLevelNumber.setShadowLayer(4f, 0f, 2f, outline)
                binding.tileRoot.alpha = 1f
            } else {
                binding.tileRoot.setBackgroundResource(R.drawable.bg_level_tile_locked)
                binding.tvLevelNumber.setShadowLayer(0f, 0f, 0f, 0)
                binding.tileRoot.alpha = 1f
            }

            binding.rowStars.visibility = if (tile.unlocked && tile.stars > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.ivStar1.setImageResource(
                if (tile.stars >= 1) R.drawable.sprite_star else R.drawable.ic_star_empty
            )
            binding.ivStar2.setImageResource(
                if (tile.stars >= 2) R.drawable.sprite_star else R.drawable.ic_star_empty
            )
            binding.ivStar3.setImageResource(
                if (tile.stars >= 3) R.drawable.sprite_star else R.drawable.ic_star_empty
            )

            binding.tileRoot.setOnClickListener { onLevelClick(tile) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLevelTileBinding.inflate(inflater, parent, false)
        return LevelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(levels[position])
    }

    override fun getItemCount(): Int = levels.size
}
