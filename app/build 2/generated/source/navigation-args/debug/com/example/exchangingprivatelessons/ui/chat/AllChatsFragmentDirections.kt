package com.example.exchangingprivatelessons.ui.chat

import android.os.Bundle
import androidx.`annotation`.CheckResult
import androidx.navigation.NavDirections
import com.example.exchangingprivatelessons.NavGraphDirections
import com.example.exchangingprivatelessons.R
import kotlin.Int
import kotlin.String

public class AllChatsFragmentDirections private constructor() {
  private data class ActionAllChatsFragmentToChatFragment(
    public val chatId: String,
    public val peerName: String,
  ) : NavDirections {
    public override val actionId: Int = R.id.actionAllChatsFragmentToChatFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putString("chatId", this.chatId)
        result.putString("peerName", this.peerName)
        return result
      }
  }

  public companion object {
    @CheckResult
    public fun actionAllChatsFragmentToChatFragment(chatId: String, peerName: String): NavDirections = ActionAllChatsFragmentToChatFragment(chatId, peerName)

    @CheckResult
    public fun actionGlobalLoginFragment(): NavDirections = NavGraphDirections.actionGlobalLoginFragment()
  }
}
