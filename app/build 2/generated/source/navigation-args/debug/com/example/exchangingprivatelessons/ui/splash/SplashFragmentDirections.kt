package com.example.exchangingprivatelessons.ui.splash

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R

public class SplashFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionSplashToAuth(): NavDirections = ActionOnlyNavDirections(R.id.action_splash_to_auth)

    @CheckResult
    public fun actionSplashToHome(): NavDirections = ActionOnlyNavDirections(R.id.action_splash_to_home)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
