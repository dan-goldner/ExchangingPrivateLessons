package com.example.exchangingprivatelessons

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections

public class NavGraphDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_global_loginFragment)
  }
}
