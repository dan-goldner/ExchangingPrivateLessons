package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class ProfileFragmentArgs(
  public val uid: String?,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("uid", this.uid)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("uid", this.uid)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ProfileFragmentArgs {
      bundle.setClassLoader(ProfileFragmentArgs::class.java.classLoader)
      val __uid : String?
      if (bundle.containsKey("uid")) {
        __uid = bundle.getString("uid")
      } else {
        throw IllegalArgumentException("Required argument \"uid\" is missing and does not have an android:defaultValue")
      }
      return ProfileFragmentArgs(__uid)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): ProfileFragmentArgs {
      val __uid : String?
      if (savedStateHandle.contains("uid")) {
        __uid = savedStateHandle["uid"]
      } else {
        throw IllegalArgumentException("Required argument \"uid\" is missing and does not have an android:defaultValue")
      }
      return ProfileFragmentArgs(__uid)
    }
  }
}
