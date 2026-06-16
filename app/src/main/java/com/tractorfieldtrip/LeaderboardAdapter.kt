package com.tractorfieldtrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tractorfieldtrip.databinding.ItemLeaderboardStatBinding

class LeaderboardAdapter(
    private val items: List<LeaderboardStat>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardStatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardStatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.ivIcon.setImageResource(item.iconRes)
        holder.binding.tvTitle.text = item.title
        holder.binding.tvValue.text = item.value
    }

    override fun getItemCount(): Int = items.size
}
