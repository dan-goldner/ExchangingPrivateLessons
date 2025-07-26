package com.example.exchangingprivatelessons.ui.auth

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R

public class AuthFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionAuthToHome(): NavDirections = ActionOnlyNavDirections(R.id.action_auth_to_home)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
