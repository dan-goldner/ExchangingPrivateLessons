package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import kotlin.String
import kotlin.jvm.JvmStatic

public data class AddEditLessonFragmentArgs(
  public val lessonId: String? = "",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("lessonId", this.lessonId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("lessonId", this.lessonId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): AddEditLessonFragmentArgs {
      bundle.setClassLoader(AddEditLessonFragmentArgs::class.java.classLoader)
      val __lessonId : String?
      if (bundle.containsKey("lessonId")) {
        __lessonId = bundle.getString("lessonId")
      } else {
        __lessonId = ""
      }
      return AddEditLessonFragmentArgs(__lessonId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): AddEditLessonFragmentArgs {
      val __lessonId : String?
      if (savedStateHandle.contains("lessonId")) {
        __lessonId = savedStateHandle["lessonId"]
      } else {
        __lessonId = ""
      }
      return AddEditLessonFragmentArgs(__lessonId)
    }
  }
}
