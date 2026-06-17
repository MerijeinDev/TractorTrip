package farmyard.tractortrip.lab

import androidx.annotation.DrawableRes

object TractorLockedArt {

    @DrawableRes
    fun lockedCardFor(unlockLevel: Int): Int? = when (unlockLevel) {
        10 -> R.drawable.shop_card_locked_10
        15 -> R.drawable.shop_card_locked_15
        20 -> R.drawable.shop_card_locked_20
        25 -> R.drawable.shop_card_locked_25
        30 -> R.drawable.shop_card_locked_30
        else -> null
    }
}
