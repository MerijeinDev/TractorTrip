package farmyard.tractortrip.lab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import farmyard.tractortrip.lab.databinding.FragmentTractorSelectBinding

class TractorSelectFragment : Fragment() {

    private var _binding: FragmentTractorSelectBinding? = null
    private val binding get() = _binding!!

    private var selectedId: Int = TractorCatalog.BASIC_ID
    private var adapter: TractorCardAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTractorSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedId = GameProgress.getSelectedSkinId(requireContext())
        binding.btnBack.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.BUTTON_BACK) }
            parentFragmentManager.popBackStack()
        }
        refreshShop()
    }

    override fun onResume() {
        super.onResume()
        refreshShop()
    }

    private fun refreshShop() {
        val context = requireContext()
        val coins = GameProgress.getCoins(context)
        binding.tvCoinCount.text = GameProgress.formatCoins(coins)

        val real = TractorCatalog.all.map { skin ->
            TractorCard(
                skin = skin,
                owned = GameProgress.isSkinOwned(context, skin.id),
                selected = skin.id == selectedId,
                canAfford = coins >= skin.price,
                shopUnlocked = GameProgress.isSkinUnlockedInShop(context, skin)
            )
        }
        val placeholders = listOf(10, 15, 20, 25, 30, 35, 40).map { level ->
            TractorCard(
                skin = TractorSkin(
                    id = 1000 + level,
                    nameRes = R.string.tractor_basic_name,
                    perkRes = R.string.tractor_basic_perk,
                    spriteRes = R.drawable.ic_lock,
                    price = 0,
                    unlockLevel = level
                ),
                owned = false,
                selected = false,
                canAfford = false,
                shopUnlocked = false
            )
        }
        val cards = real + placeholders

        adapter = TractorCardAdapter(
            cards = cards,
            onSelect = { skin ->
                SoundManager.play(context, SoundManager.Effect.CLICK)
                selectedId = skin.id
                GameProgress.setSelectedSkinId(context, skin.id)
                refreshShop()
            },
            onPurchase = { skin ->
                if (GameProgress.purchaseSkin(context, skin)) {
                    SoundManager.play(context, SoundManager.Effect.COIN)
                    selectedId = skin.id
                    GameProgress.setSelectedSkinId(context, skin.id)
                    Toast.makeText(context, R.string.shop_purchased, Toast.LENGTH_SHORT).show()
                    refreshShop()
                } else {
                    SoundManager.play(context, SoundManager.Effect.NOTIFY, volume = 0.75f)
                    Toast.makeText(context, R.string.shop_not_enough_coins, Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.rvTractors.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTractors.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        _binding = null
    }
}
