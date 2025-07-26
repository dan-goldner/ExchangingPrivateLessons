package com.example.exchangingprivatelessons.ui.lesson

import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections

public class RatingBottomSheetDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
