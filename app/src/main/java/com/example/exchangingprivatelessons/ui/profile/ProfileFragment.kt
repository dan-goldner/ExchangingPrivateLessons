package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileFragmentArgs by navArgs()
    private val vm by viewModels<ProfileViewModel>()

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = FragmentProfileBinding.inflate(i, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(binding) {

        /* צפייה בפרופיל */
        vm.setProfileUid(args.uid)
        editFab.isVisible = vm.isMine

        /* עריכה */
        editFab.setOnClickListener { EditProfileSheet().show(childFragmentManager, null) }

        /* מחיקה (לחיצה ארוכה על האווטאר) */
        avatarIv.setOnLongClickListener { vm.deleteMyAccount(); true }

        /* Snackbars */
        vm.snackbar.observe(viewLifecycleOwner) {
            Snackbar.make(root, it, Snackbar.LENGTH_LONG).show()
        }

        /* User‑UI */
        vm.user.observe(viewLifecycleOwner) { u ->
            progressBar.isVisible = u == null
            content.isVisible     = u != null
            u ?: return@observe

            avatarIv.load(u.photoUrl) { crossfade(true) }
            nameTv .text = u.displayName
            emailTv.text = u.email
            bioTv  .text = if (u.bio.isBlank()) getString(R.string.no_bio) else u.bio
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
