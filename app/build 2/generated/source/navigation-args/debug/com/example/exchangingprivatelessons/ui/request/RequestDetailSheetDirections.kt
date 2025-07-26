package com.example.exchangingprivatelessons.ui.request

import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections

public class RequestDetailSheetDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
