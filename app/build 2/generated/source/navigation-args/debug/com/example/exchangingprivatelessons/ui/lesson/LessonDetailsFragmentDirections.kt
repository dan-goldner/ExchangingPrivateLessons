package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R
import kotlin.Int
import kotlin.String

public class LessonDetailsFragmentDirections private constructor() {
  private data class ActionLessonDetailsFragmentToAddEditLessonFragment(
    public val lessonId: String? = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_lessonDetailsFragment_to_addEditLessonFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("lessonId", this.lessonId)
        return result
      }
  }

  private data class ActionLessonDetailsFragmentToRatingBottomSheet(
    public val lessonId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_lessonDetailsFragment_to_ratingBottomSheet

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("lessonId", this.lessonId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionLessonDetailsFragmentToAddEditLessonFragment(lessonId: String? = ""): NavDirections = ActionLessonDetailsFragmentToAddEditLessonFragment(lessonId)

    @CheckResult
    public fun actionLessonDetailsFragmentToRatingBottomSheet(lessonId: String): NavDirections = ActionLessonDetailsFragmentToRatingBottomSheet(lessonId)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
