package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R
import kotlin.Int
import kotlin.String

public class RequestsFragmentDirections private constructor() {
  private data class ActionRequestsToRequestDetail(
    public val requestId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_requests_to_requestDetail

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("requestId", this.requestId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionRequestsToRequestDetail(requestId: String): NavDirections = ActionRequestsToRequestDetail(requestId)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
