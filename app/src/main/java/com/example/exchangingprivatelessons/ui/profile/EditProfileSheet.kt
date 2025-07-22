package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.BottomSheetEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileSheet : BottomSheetDialogFragment() {

    /* ---------- binding ---------- */
    private var _binding: BottomSheetEditProfileBinding? = null
    private val binding get() = _binding!!

    /* ---------- VM shared עם ProfileFragment ---------- */
    private val vm by activityViewModels<ProfileViewModel>()

    /* ---------- results ---------- */
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { vm.onNewAvatar(it) }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            if (ok) vm.pendingCameraUri?.let { vm.onNewAvatar(it) }
        }

    /* ---------- lifecycle ---------- */

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = BottomSheetEditProfileBinding.inflate(i, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(binding) {

        /* Prefill שדות טקסט */
        vm.user.value?.let {
            displayEt.setText(it.displayName)
            bioEt.setText(it.bio)
        }

        /* Preview + בחירה/צילום/מחיקה */
        avatarIv.setOnClickListener { showChooser() }
        removeAvatarBtn.setOnClickListener { vm.onDeleteAvatar() }

        vm.previewAvatar.observe(viewLifecycleOwner) { any ->
            removeAvatarBtn.isGone = any == ""
            avatarIv.load(any) { crossfade(true) }
        }

        /* Save */
        saveBtn.setOnClickListener {
            vm.save(displayEt.text.toString(), bioEt.text.toString())
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /* ---------- helpers ---------- */

    private fun showChooser() = PopupMenu(requireContext(), binding.avatarIv).run {
        menuInflater.inflate(R.menu.menu_avatar, menu)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pick  -> { pickImage.launch("image/*"); true }
                R.id.action_photo -> {
                    val uri = vm.createTempCameraUri(requireContext())
                    takePicture.launch(uri); true
                }
                else -> false
            }
        }
        show()
    }
}
