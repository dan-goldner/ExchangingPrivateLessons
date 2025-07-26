package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R
import kotlin.Int
import kotlin.String

public class LessonListFragmentDirections private constructor() {
  private data class ActionLessonListToAddEditLesson(
    public val lessonId: String? = "",
  ) : NavDirections {
    public override val actionId: Int = R.id.action_lessonList_to_addEditLesson

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("lessonId", this.lessonId)
        return result
      }
  }

  private data class ActionLessonListToLessonDetails(
    public val lessonId: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.action_lessonList_to_lessonDetails

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("lessonId", this.lessonId)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionLessonListToAddEditLesson(lessonId: String? = ""): NavDirections = ActionLessonListToAddEditLesson(lessonId)

    @CheckResult
    public fun actionLessonListToLessonDetails(lessonId: String): NavDirections = ActionLessonListToLessonDetails(lessonId)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
