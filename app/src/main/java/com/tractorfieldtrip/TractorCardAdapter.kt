package com.tractorfieldtrip

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
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

            val lockedArt = TractorLockedArt.lockedCardFor(card.skin.unlockLevel)
            if (!card.shopUnlocked && lockedArt != null) {
                binding.cardRoot.background = null
                binding.cardRoot.setPadding(0, 0, 0, 0)
                binding.ivLockedArt.visibility = View.VISIBLE
                binding.ivLockedArt.setImageResource(lockedArt)
                binding.tvCardTitle.visibility = View.GONE
                binding.ivCardTractor.visibility = View.GONE
                binding.ivCardLock.visibility = View.GONE
                binding.btnCardAction.visibility = View.GONE
                binding.cardRoot.setOnClickListener {
                    Toast.makeText(
                        context,
                        context.getString(R.string.shop_locked_level, card.skin.unlockLevel),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            binding.cardRoot.setPadding(
                dp(10, context.resources.displayMetrics.density),
                dp(10, context.resources.displayMetrics.density),
                dp(10, context.resources.displayMetrics.density),
                dp(10, context.resources.displayMetrics.density)
            )
            binding.cardRoot.setOnClickListener(null)
            binding.ivLockedArt.visibility = View.GONE
            binding.btnCardAction.visibility = View.VISIBLE
            binding.cardRoot.setBackgroundResource(R.drawable.bg_tractor_card_unlocked)
            binding.tvCardTitle.visibility = View.VISIBLE
            binding.tvCardTitle.setText(card.skin.nameRes)
            binding.ivCardTractor.visibility = View.VISIBLE
            binding.ivCardTractor.setImageResource(card.skin.spriteRes)
            binding.ivCardLock.visibility = View.GONE

            when {
                card.selected -> {
                    binding.btnCardAction.setBackgroundResource(R.drawable.bg_next_button)
                    binding.btnCardAction.text = context.getString(R.string.card_action_selected)
                    binding.btnCardAction.setOnClickListener { }
                }
                card.owned -> {
                    binding.btnCardAction.setBackgroundResource(R.drawable.bg_next_button)
                    binding.btnCardAction.text = context.getString(R.string.card_action_select)
                    binding.btnCardAction.setOnClickListener { onSelect(card.skin) }
                }
                else -> {
                    binding.btnCardAction.setBackgroundResource(
                        if (card.canAfford) R.drawable.bg_next_button else R.drawable.bg_card_button_locked
                    )
                    binding.btnCardAction.text = context.getString(
                        R.string.card_action_buy,
                        card.skin.price
                    )
                    binding.btnCardAction.setOnClickListener {
                        if (card.canAfford) {
                            onPurchase(card.skin)
                        } else {
                            Toast.makeText(
                                context,
                                R.string.shop_not_enough_coins,
                                Toast.LENGTH_SHORT
                            ).show()
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
