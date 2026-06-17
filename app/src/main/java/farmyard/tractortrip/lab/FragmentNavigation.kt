package farmyard.tractortrip.lab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

private fun Fragment.activityFragmentManager(): FragmentManager =
    requireActivity().supportFragmentManager

fun Fragment.navigateToGame(level: Int) {
    activityFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, GameFragment.newInstance(level))
        .commit()
}

fun Fragment.navigateToLevelSelect() {
    activityFragmentManager().beginTransaction()
        .replace(R.id.fragment_container, LevelSelectFragment())
        .commit()
}

fun Fragment.navigateToMainMenuClearingBackStack() {
    val fm = activityFragmentManager()
    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    fm.beginTransaction()
        .replace(R.id.fragment_container, MainMenuFragment())
        .commit()
}

fun Fragment.dismissChildOverlayBackStack() {
    requireParentFragment().childFragmentManager.popBackStack()
}

fun FragmentActivity.navigateToGame(level: Int) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, GameFragment.newInstance(level))
        .commit()
}
