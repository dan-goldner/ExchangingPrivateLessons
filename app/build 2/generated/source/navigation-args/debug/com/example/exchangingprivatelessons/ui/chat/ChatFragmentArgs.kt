package com.example.exchangingprivatelessons.ui.chat

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.String
import kotlin.jvm.JvmStatic

public data class ChatFragmentArgs(
  public val chatId: String,
  public val peerName: String,
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("chatId", this.chatId)
    result.putString("peerName", this.peerName)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("chatId", this.chatId)
    result.set("peerName", this.peerName)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): ChatFragmentArgs {
      bundle.setClassLoader(ChatFragmentArgs::class.java.classLoader)
      val __chatId : String?
      if (bundle.containsKey("chatId")) {
        __chatId = bundle.getString("chatId")
        if (__chatId == null) {
          throw IllegalArgumentException("Argument \"chatId\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"chatId\" is missing and does not have an android:defaultValue")
      }
      val __peerName : String?
      if (bundle.containsKey("peerName")) {
        __peerName = bundle.getString("peerName")
        if (__peerName == null) {
          throw IllegalArgumentException("Argument \"peerName\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"peerName\" is missing and does not have an android:defaultValue")
      }
      return ChatFragmentArgs(__chatId, __peerName)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): ChatFragmentArgs {
      val __chatId : String?
      if (savedStateHandle.contains("chatId")) {
        __chatId = savedStateHandle["chatId"]
        if (__chatId == null) {
          throw IllegalArgumentException("Argument \"chatId\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"chatId\" is missing and does not have an android:defaultValue")
      }
      val __peerName : String?
      if (savedStateHandle.contains("peerName")) {
        __peerName = savedStateHandle["peerName"]
        if (__peerName == null) {
          throw IllegalArgumentException("Argument \"peerName\" is marked as non-null but was passed a null value")
        }
      } else {
        throw IllegalArgumentException("Required argument \"peerName\" is missing and does not have an android:defaultValue")
      }
      return ChatFragmentArgs(__chatId, __peerName)
    }
  }
}
