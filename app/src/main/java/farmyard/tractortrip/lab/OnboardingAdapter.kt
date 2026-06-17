package farmyard.tractortrip.lab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import farmyard.tractortrip.lab.databinding.ItemOnboardingBinding

data class OnboardingPage(
    val backgroundRes: Int,
    val textRes: Int
)

class OnboardingAdapter(
    private val pages: List<OnboardingPage>,
    private val onNext: (position: Int) -> Unit
) : RecyclerView.Adapter<OnboardingAdapter.PageViewHolder>() {

    inner class PageViewHolder(
        private val binding: ItemOnboardingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(page: OnboardingPage, position: Int) {
            binding.ivBackground.setImageResource(page.backgroundRes)
            binding.tvOnboardingText.setText(page.textRes)

            val isLastPage = position == pages.size - 1
            binding.btnNext.contentDescription = binding.root.context.getString(
                if (isLastPage) R.string.onboarding_play else R.string.onboarding_next
            )

            binding.btnNext.setOnClickListener { onNext(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOnboardingBinding.inflate(inflater, parent, false)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(pages[position], position)
    }

    override fun getItemCount(): Int = pages.size
}
