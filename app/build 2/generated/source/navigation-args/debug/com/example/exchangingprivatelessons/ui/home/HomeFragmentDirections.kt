package com.example.exchangingprivatelessons.ui.home

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R
import kotlin.Int
import kotlin.String

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeToLessonListAvailable(
    public val mode: String = "AVAILABLE",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_lessonList_available

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("mode", this.mode)
        return result
      }
  }

  private data class ActionHomeToLessonListTaken(
    public val mode: String = "TAKEN",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_lessonList_taken

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("mode", this.mode)
        return result
      }
  }

  private data class ActionHomeToLessonListMine(
    public val mode: String = "MINE",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_lessonList_mine

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("mode", this.mode)
        return result
      }
  }

  private data class ActionHomeToRequestsReceived(
    public val mode: String = "RECEIVED",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_requests_received

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("mode", this.mode)
        return result
      }
  }

  private data class ActionHomeToRequestsSent(
    public val mode: String = "SENT",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_home_to_requests_sent

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("mode", this.mode)
        return result
      }
  }

  private data class ActionHomeFragmentToProfileFragment(
    public val uid: String?,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_homeFragment_to_profileFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("uid", this.uid)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionHomeToLessonListAvailable(mode: String = "AVAILABLE"): NavDirections = ActionHomeToLessonListAvailable(mode)

    @CheckResult
    public fun actionHomeToLessonListTaken(mode: String = "TAKEN"): NavDirections = ActionHomeToLessonListTaken(mode)

    @CheckResult
    public fun actionHomeToLessonListMine(mode: String = "MINE"): NavDirections = ActionHomeToLessonListMine(mode)

    @CheckResult
    public fun actionHomeToRequestsReceived(mode: String = "RECEIVED"): NavDirections = ActionHomeToRequestsReceived(mode)

    @CheckResult
    public fun actionHomeToRequestsSent(mode: String = "SENT"): NavDirections = ActionHomeToRequestsSent(mode)

    @CheckResult
    public fun actionHomeFragmentToAllChatsFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_homeFragment_to_allChatsFragment)

    @CheckResult
    public fun actionHomeFragmentToProfileFragment(uid: String?): NavDirections = ActionHomeFragmentToProfileFragment(uid)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
