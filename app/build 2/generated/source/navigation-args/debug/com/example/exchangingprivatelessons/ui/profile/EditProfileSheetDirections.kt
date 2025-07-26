package com.example.exchangingprivatelessons.ui.profile

import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections

public class EditProfileSheetDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
