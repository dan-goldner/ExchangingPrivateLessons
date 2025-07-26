package com.example.exchangingprivatelessons.ui.request

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class RequestDetailSheetArgs(
  public val requestId: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("requestId", this.requestId)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("requestId", this.requestId)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): RequestDetailSheetArgs {
      bundle.setClassLoader(RequestDetailSheetArgs::class.java.classLoader)
      val __requestId : String?
      if (bundle.containsKey("requestId")) {
        __requestId = bundle.getString("requestId")
        if (__requestId == null) {
          throw IllegalArgumentException("Argument \"requestId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"requestId\" is missing and does not have an android:defaultValue")
      }
      return RequestDetailSheetArgs(__requestId)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): RequestDetailSheetArgs {
      val __requestId : String?
      if (savedStateHandle.contains("requestId")) {
        __requestId = savedStateHandle["requestId"]
        if (__requestId == null) {
          throw IllegalArgumentException("Argument \"requestId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"requestId\" is missing and does not have an android:defaultValue")
      }
      return RequestDetailSheetArgs(__requestId)
    }
  }
}
