package com.example.exchangingprivatelessons.ui.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.activityViewModels
import com.example.exchangingprivatelessons.databinding.BottomSheetEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditProfileBinding? = null
    private val binding get() = _binding!!
    private val vm: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = BottomSheetEditProfileBinding.inflate(i, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(binding) {
        /* pre‑fill */
        vm.user.value?.let { u ->
            displayEt.setText(u.displayName)
            bioEt.setText(u.bio)
            photoEt.setText(u.photoUrl)
        }

        saveBtn.setOnClickListener {
            vm.save(
                displayEt.text.toString(),
                bioEt.text.toString(),
                photoEt.text.toString().ifBlank { null }
            )
            dismiss()
        }
    }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}
