package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _b: FragmentProfileBinding? = null
    private val b get() = _b!!
    private val vm: ProfileViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        FragmentProfileBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {

        /* עריכת פרופיל */
        b.editFab.setOnClickListener { EditProfileSheet().show(childFragmentManager, null) }

        /* מחיקה (לחיצה ארוכה) */
        b.avatarIv.setOnLongClickListener { vm.deleteMyAccount(); true }

        /* --- observers --- */
        vm.snackbar.observe(viewLifecycleOwner) {
            Snackbar.make(b.root, it, Snackbar.LENGTH_LONG).show()
        }

        vm.user.observe(viewLifecycleOwner) { u ->
            b.progressBar.isVisible = u == null
            b.content.isVisible     = u != null
            u ?: return@observe

            b.avatarIv.load(u.photoUrl) { crossfade(true) }
            b.nameTv .text = u.displayName
            b.emailTv.text = u.email
            b.bioTv  .text = if (u.bio.isBlank()) getString(R.string.no_bio) else u.bio
        }
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}
