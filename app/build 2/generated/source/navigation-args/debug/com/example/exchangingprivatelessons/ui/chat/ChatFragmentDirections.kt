package com.example.exchangingprivatelessons.ui.chat

import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections

public class ChatFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
