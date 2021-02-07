package cz.tomashavlicek.mildybackgroundlocation.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import cz.tomashavlicek.mildybackgroundlocation.R
import cz.tomashavlicek.mildybackgroundlocation.shared.IntentCreator

class RequestPermissionDeniedForeverDialogFragment : DialogFragment() {

    companion object {
        private const val TAG = "RequestPermissionDeniedForeverDialogFragment"

        @JvmStatic fun showDialog(fragmentManager: FragmentManager) {
            // Osetreni na Caused by java.lang.IllegalStateException
            // Can not perform this action after onSaveInstanceState
            if (fragmentManager.isStateSaved) {
                // "Cannot show denied forever dialog due to existing save state"
                return
            }
            RequestPermissionDeniedForeverDialogFragment().show(fragmentManager, TAG)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setPositiveButton(R.string.permission_denied_forever_dialog_positive) { _, _ -> openSettingsIntent() }
                .setNeutralButton(R.string.permission_denied_forever_dialog_neutral) { _, _ -> }
                .setMessage(R.string.permission_denied_forever_dialog_body)
                .create()
    }

    private fun openSettingsIntent() {
        startActivity(IntentCreator().createAppSettingsIntent(requireContext()))
    }
}