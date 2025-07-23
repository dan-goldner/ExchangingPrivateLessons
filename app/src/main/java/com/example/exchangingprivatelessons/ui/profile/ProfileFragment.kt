package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!

    private val args by navArgs<ProfileFragmentArgs>()
    private val vm   by activityViewModels<ProfileViewModel>()   // ← activity scope

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = FragmentProfileBinding.inflate(i, c, false)
        .also { _b = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(b) {

        vm.setProfileUid(args.uid)

        editFab.isVisible = vm.isMine
        editFab.setOnClickListener { EditProfileSheet().show(parentFragmentManager, null) }

        /* ----- User stream ----- */
        vm.user.observe(viewLifecycleOwner) { u ->
            progressBar.isVisible = u == null
            content    .isVisible = u != null
            u ?: return@observe

            avatarIv.load(
                if (u.photoUrl.isBlank()) R.drawable.ic_profile_placeholder else u.photoUrl
            ) {
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
                crossfade(true)
            }
            nameTv .text = u.displayName
            emailTv.text = u.email
            bioTv  .text = u.bio.ifBlank { getString(R.string.profile_no_bio) }
        }

        /* ----- Sign‑out event ----- */
        vm.signOut.observe(viewLifecycleOwner) { event ->
            event.getOrNull()?.let {
                findNavController().navigate(
                    R.id.authFragment,                    // יעד‑התחברות
                    null,
                    navOptions {
                        popUpTo(findNavController().graph.startDestinationId) {
                            inclusive = true             // נקה BackStack
                        }
                    }
                )
            }
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
