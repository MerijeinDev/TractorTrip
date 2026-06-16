package com.tractorfieldtrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tractorfieldtrip.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val items: List<AchievementItem>
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AchievementItem) {
            val context = binding.root.context
            binding.tvTitle.setText(item.achievement.titleRes)
            binding.tvDesc.text = context.getString(
                item.achievement.descRes
            ) + "\n" + context.getString(
                R.string.ach_reward_format,
                item.achievement.coinReward
            )

            if (item.unlocked) {
                binding.cardPanel.setBackgroundResource(R.drawable.bg_achievement_card_enabled)
                val outline = ContextCompat.getColor(context, R.color.text_outline_brown)
                binding.tvTitle.setShadowLayer(4f, 0f, 2f, outline)
            } else {
                binding.cardPanel.setBackgroundResource(R.drawable.bg_achievement_card_locked)
                binding.tvTitle.setShadowLayer(0f, 0f, 0f, 0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
