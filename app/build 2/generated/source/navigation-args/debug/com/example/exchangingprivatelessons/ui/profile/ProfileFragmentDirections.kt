package com.example.exchangingprivatelessons.ui.profile

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R

public class ProfileFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionProfileToEdit(): NavDirections = ActionOnlyNavDirections(R.id.action_profile_to_edit)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
