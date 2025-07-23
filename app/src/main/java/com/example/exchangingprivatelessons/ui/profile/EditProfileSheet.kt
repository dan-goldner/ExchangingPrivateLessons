package com.example.exchangingprivatelessons.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.BottomSheetEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileSheet : BottomSheetDialogFragment() {

    /* ---------- binding ---------- */
    private var _b: BottomSheetEditProfileBinding? = null
    private val b get() = _b!!

    /* ---------- Shared VM ---------- */
    private val vm by activityViewModels<ProfileViewModel>()

    /* ---------- Launchers ---------- */
    private lateinit var cameraLauncher    : ActivityResultLauncher<Uri>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val galleryLauncher            =
        registerForActivityResult(GetContent()) { uri -> uri?.let(vm::onNewAvatar) }

    /* ---------- lifecycle ---------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher =
            registerForActivityResult(RequestPermission()) { granted ->
                if (granted) launchCamera()
                else Snackbar
                    .make(requireView(), R.string.err_camera_permission, Snackbar.LENGTH_LONG)
                    .show()
            }

        cameraLauncher =
            registerForActivityResult(TakePicture()) { ok ->
                if (ok) vm.pendingCameraUri?.let(vm::onNewAvatar)
            }
    }

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = BottomSheetEditProfileBinding.inflate(i, c, false)
        .also { _b = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(b) {
        /* Prefill */
        /* Prefill */
        vm.user.observe(viewLifecycleOwner) {
            displayEt.setText(it?.displayName)
            bioEt    .setText(it?.bio)

            // חדש: תמונת ברירת‑מחדל
            vm.initPreview(it?.photoUrl)
        }


        /* Avatar preview */
        avatarIv.setOnClickListener { showChooser() }
        removeAvatarBtn.setOnClickListener { vm.onDeleteAvatar() }
        vm.previewAvatar.observe(viewLifecycleOwner) { any ->
            /* התמונה */
            avatarIv.load(
                when (any) {
                    is Uri    -> any
                    is String -> if (any.isBlank()) R.drawable.ic_profile_placeholder else any
                    else      -> R.drawable.ic_profile_placeholder
                }
            ) {
                placeholder(R.drawable.ic_profile_placeholder)
                error      (R.drawable.ic_profile_placeholder)
                crossfade(true)
            }

            /* כיתוב מתחלף */
            avatarHintTv.text = if (any is String && any.isBlank())
                getString(R.string.choose_photo)   // אין תמונה
            else
                getString(R.string.change_photo)   // יש תמונה

            /* כפתור “מחק” מופיע רק אם יש תמונה */
            removeAvatarBtn.isGone = any is String && any.isBlank()
        }



        /* Save */
        saveBtn.setOnClickListener {
            vm.save(displayEt.text.toString(), bioEt.text.toString())
            dismiss()
        }



        deleteAccountBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_account)
                .setMessage(R.string.delete_account_confirm)
                .setPositiveButton(R.string.delete) { _, _ ->
                    vm.deleteMyAccount()     // ← קריאה
                    dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }

    /* ---------- helpers ---------- */

    private fun showChooser() = PopupMenu(requireContext(), b.avatarIv).run {
        menuInflater.inflate(R.menu.menu_avatar, menu)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pick   -> { galleryLauncher.launch("image/*"); true }
                R.id.action_photo  -> { requestCamera();                  true }
                else               -> false
            }
        }; show()
    }

    private fun requestCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                Snackbar.make(requireView(),
                    R.string.err_camera_permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }.show()

            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val uri = vm.createTempCameraUri(requireContext())
        cameraLauncher.launch(uri)
    }
}
