package com.raya.v2ray.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.raya.v2ray.R
import com.raya.v2ray.extension.toast
import com.raya.v2ray.extension.toastError
import com.raya.v2ray.extension.toastSuccess
import com.raya.v2ray.handler.AngConfigManager

class ScScannerActivity : BaseActivity() {

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scanQRCode.launch(Intent(this, ScannerActivity::class.java))
        } else {
            toast(R.string.toast_permission_denied)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_none)
        importQRcode()
    }

    private fun importQRcode(): Boolean {
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        return true
    }

    private val scanQRCode = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val scanResult = it.data?.getStringExtra("SCAN_RESULT").orEmpty()
            val (count, countSub) = AngConfigManager.importBatchConfig(scanResult, "", false)

            if (count + countSub > 0) {
                toastSuccess(R.string.toast_success)
            } else {
                toastError(R.string.toast_failure)
            }

            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}