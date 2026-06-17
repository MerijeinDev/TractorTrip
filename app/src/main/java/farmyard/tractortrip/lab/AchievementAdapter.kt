package farmyard.tractortrip.lab

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import farmyard.tractortrip.lab.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val items: List<AchievementItem>
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    private val greyFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })

    inner class ViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AchievementItem) {
            val context = binding.root.context
            binding.tvTitle.setText(item.achievement.titleRes)
            binding.tvDesc.text = context.getString(item.achievement.descRes)

            if (item.unlocked) {
                binding.ivCardBg.colorFilter = null
                binding.ivMedal.colorFilter = null
                binding.tvTitle.alpha = 1f
                binding.tvDesc.alpha = 1f
            } else {
                binding.ivCardBg.colorFilter = greyFilter
                binding.ivMedal.colorFilter = greyFilter
                binding.tvTitle.alpha = 0.75f
                binding.tvDesc.alpha = 0.75f
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
