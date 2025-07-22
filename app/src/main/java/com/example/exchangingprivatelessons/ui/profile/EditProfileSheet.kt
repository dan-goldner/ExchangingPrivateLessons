package com.example.exchangingprivatelessons.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.exchangingprivatelessons.R
import com.example.exchangingprivatelessons.databinding.BottomSheetEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditProfileSheet : BottomSheetDialogFragment() {

    /* ---------------- binding ---------------- */
    private var _binding: BottomSheetEditProfileBinding? = null
    private val binding get() = _binding!!

    /* ---------------- VM (shared עם   ProfileFragment) ---------------- */
    private val vm by activityViewModels<ProfileViewModel>()

    /* ---------------- Activity‑result launchers ---------------- */
    private lateinit var cameraLauncher     : ActivityResultLauncher<Uri>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private var tmpPhotoUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { vm.onNewAvatar(it) }
        }

    /* ----------------------------------------------------------------- */
    /*  onCreate – יצירת ה‑launchers                                     */
    /* ----------------------------------------------------------------- */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* ① בקשת CAMERA permission */
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) openCamera()
                else Snackbar
                    .make(requireView(), R.string.err_camera_permission, Snackbar.LENGTH_LONG)
                    .show()
            }

        /* ② הפעלת המצלמה */
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
                if (ok && tmpPhotoUri != null) {
                    vm.onNewAvatar(tmpPhotoUri!!)
                }
            }
    }

    /* ----------------------------------------------------------------- */
    /*  תצוגה                                                             */
    /* ----------------------------------------------------------------- */
    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View = BottomSheetEditProfileBinding.inflate(i, c, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: View, s: Bundle?) = with(binding) {

        /* Prefill שדות */
        vm.user.value?.let {
            displayEt.setText(it.displayName)
            bioEt.setText(it.bio)
        }

        /* תמונת פרופיל | בחירה / צילום / מחיקה */
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

    /* ----------------------------------------------------------------- */
    /*  צילום תמונה – Permission flow                                    */
    /* ----------------------------------------------------------------- */
    fun onTakePhotoClicked() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED ->
                openCamera()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                Snackbar
                    .make(requireView(),
                        R.string.err_camera_permission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .show()

            else ->
                permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val uri = createTempImageUri()
        tmpPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    private fun createTempImageUri(): Uri {
        val file = File.createTempFile("avatar_", ".jpg",
            requireContext().cacheDir).apply { deleteOnExit() }

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    /* ----------------------------------------------------------------- */
    /*  Popup‑menu לבחירה / צילום                                        */
    /* ----------------------------------------------------------------- */
    private fun showChooser() = PopupMenu(requireContext(), binding.avatarIv).run {
        menuInflater.inflate(R.menu.menu_avatar, menu)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_pick  -> { pickImage.launch("image/*"); true }
                R.id.action_photo -> { onTakePhotoClicked();          true }
                else              -> false
            }
        }
        show()
    }
}
