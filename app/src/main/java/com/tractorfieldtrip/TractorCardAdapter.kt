package com.tractorfieldtrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.tractorfieldtrip.databinding.ItemTractorCardBinding

data class TractorCard(
    val skin: TractorSkin,
    val owned: Boolean,
    val selected: Boolean,
    val canAfford: Boolean,
    val shopUnlocked: Boolean
)

class TractorCardAdapter(
    private val cards: List<TractorCard>,
    private val onSelect: (TractorSkin) -> Unit,
    private val onPurchase: (TractorSkin) -> Unit
) : RecyclerView.Adapter<TractorCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(
        private val binding: ItemTractorCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: TractorCard, position: Int) {
            val context = binding.root.context

            (binding.root.layoutParams as MarginLayoutParams).apply {
                marginEnd = if (position == cards.size - 1) 0 else dp(12, context.resources.displayMetrics.density)
            }

            if (!card.shopUnlocked) {
                bindLocked(card, context)
                return
            }
            bindUnlocked(card, context)
        }

        private fun bindLocked(card: TractorCard, context: android.content.Context) {
            binding.ivCardFrame.scaleType = ImageView.ScaleType.FIT_XY
            binding.ivCardFrame.setImageResource(R.drawable.shop_card_bg_locked)

            binding.tvCardTitle.visibility = View.VISIBLE
            binding.tvCardTitle.text = context.getString(R.string.shop_card_level_label, card.skin.unlockLevel)

            binding.ivCardTractor.visibility = View.VISIBLE
            binding.ivCardTractor.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.ivCardTractor.setImageResource(R.drawable.ic_lock)

            binding.btnCardAction.visibility = View.VISIBLE
            binding.ivCardActionPlate.setImageResource(R.drawable.btn_locked)
            binding.tvCardAction.visibility = View.GONE
            binding.ivCardActionCoin.visibility = View.GONE

            binding.cardRoot.setOnClickListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.shop_locked_level, card.skin.unlockLevel),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun bindUnlocked(card: TractorCard, context: android.content.Context) {
            binding.ivCardFrame.scaleType = ImageView.ScaleType.FIT_XY
            binding.ivCardFrame.setImageResource(R.drawable.shop_card_bg_v)

            binding.tvCardTitle.visibility = View.VISIBLE
            binding.tvCardTitle.setText(card.skin.nameRes)

            binding.ivCardTractor.visibility = View.VISIBLE
            binding.ivCardTractor.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.ivCardTractor.setImageResource(card.skin.spriteRes)

            binding.btnCardAction.visibility = View.VISIBLE

            when {
                card.selected -> {
                    binding.ivCardActionPlate.setImageResource(R.drawable.btn_selected)
                    binding.tvCardAction.visibility = View.GONE
                    binding.ivCardActionCoin.visibility = View.GONE
                    binding.cardRoot.setOnClickListener(null)
                }
                card.owned -> {
                    binding.ivCardActionPlate.setImageResource(R.drawable.btn_select)
                    binding.tvCardAction.visibility = View.GONE
                    binding.ivCardActionCoin.visibility = View.GONE
                    binding.cardRoot.setOnClickListener { onSelect(card.skin) }
                }
                else -> {
                    binding.ivCardActionPlate.setImageResource(R.drawable.btn_buy_bg)
                    binding.tvCardAction.visibility = View.VISIBLE
                    binding.tvCardAction.text = card.skin.price.toString()
                    binding.ivCardActionCoin.visibility = View.VISIBLE
                    binding.cardRoot.setOnClickListener {
                        if (card.canAfford) {
                            onPurchase(card.skin)
                        } else {
                            Toast.makeText(context, R.string.shop_not_enough_coins, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        private fun dp(value: Int, density: Float): Int = (value * density).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTractorCardBinding.inflate(inflater, parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], position)
    }

    override fun getItemCount(): Int = cards.size
}
