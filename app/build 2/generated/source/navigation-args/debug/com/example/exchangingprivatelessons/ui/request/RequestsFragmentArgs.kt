package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class RequestsFragmentArgs(
  public val mode: String = "RECEIVED",
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
    public fun fromBundle(bundle: Bundle): RequestsFragmentArgs {
      bundle.setClassLoader(RequestsFragmentArgs::class.java.classLoader)
      val __mode : String?
      if (bundle.containsKey("mode")) {
        __mode = bundle.getString("mode")
        if (__mode == null) {
          throw IllegalArgumentException("Argument \"mode\" is marked as non-null but was passed a null value.")
        }
      } else {
        __mode = "RECEIVED"
      }
      return RequestsFragmentArgs(__mode)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): RequestsFragmentArgs {
      val __mode : String?
      if (savedStateHandle.contains("mode")) {
        __mode = savedStateHandle["mode"]
        if (__mode == null) {
          throw IllegalArgumentException("Argument \"mode\" is marked as non-null but was passed a null value")
        }
      } else {
        __mode = "RECEIVED"
      }
      return RequestsFragmentArgs(__mode)
    }
  }
}
