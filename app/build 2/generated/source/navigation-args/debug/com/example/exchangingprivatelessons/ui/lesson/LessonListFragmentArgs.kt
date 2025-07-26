package com.example.exchangingprivatelessons.ui.lesson

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class LessonListFragmentArgs(
  public val mode: String = "AVAILABLE",
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("mode", this.mode)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("mode", this.mode)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): LessonListFragmentArgs {
      bundle.setClassLoader(LessonListFragmentArgs::class.java.classLoader)
      val __mode : String?
      if (bundle.containsKey("mode")) {
        __mode = bundle.getString("mode")
        if (__mode == null) {
          throw IllegalArgumentException("Argument \"mode\" is marked as non-null but was passed a null value.")
        }
      } else {
        __mode = "AVAILABLE"
      }
      return LessonListFragmentArgs(__mode)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): LessonListFragmentArgs {
      val __mode : String?
      if (savedStateHandle.contains("mode")) {
        __mode = savedStateHandle["mode"]
        if (__mode == null) {
          throw IllegalArgumentException("Argument \"mode\" is marked as non-null but was passed a null value")
        }
      } else {
        __mode = "AVAILABLE"
      }
      return LessonListFragmentArgs(__mode)
    }
  }
}
