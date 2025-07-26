package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class RatingBottomSheetArgs(
  public val lessonId: String,
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
    public fun fromBundle(bundle: Bundle): RatingBottomSheetArgs {
      bundle.setClassLoader(RatingBottomSheetArgs::class.java.classLoader)
      val __lessonId : String?
      if (bundle.containsKey("lessonId")) {
        __lessonId = bundle.getString("lessonId")
        if (__lessonId == null) {
          throw IllegalArgumentException("Argument \"lessonId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"lessonId\" is missing and does not have an android:defaultValue")
      }
      return RatingBottomSheetArgs(__lessonId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): RatingBottomSheetArgs {
      val __lessonId : String?
      if (savedStateHandle.contains("lessonId")) {
        __lessonId = savedStateHandle["lessonId"]
        if (__lessonId == null) {
          throw IllegalArgumentException("Argument \"lessonId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"lessonId\" is missing and does not have an android:defaultValue")
      }
      return RatingBottomSheetArgs(__lessonId)
    }
  }
}
