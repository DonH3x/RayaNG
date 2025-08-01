package com.raya.v2ray.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.blacksquircle.ui.editorkit.utils.EditorTheme
import com.blacksquircle.ui.language.json.JsonLanguage
import com.raya.v2ray.AppConfig
import com.raya.v2ray.R
import com.raya.v2ray.databinding.ActivityServerCustomConfigBinding
import com.raya.v2ray.dto.EConfigType
import com.raya.v2ray.dto.ProfileItem
import com.raya.v2ray.extension.toast
import com.raya.v2ray.extension.toastSuccess
import com.raya.v2ray.fmt.CustomFmt
import com.raya.v2ray.handler.MmkvManager
import com.raya.v2ray.util.Utils

class ServerCustomConfigActivity : BaseActivity() {
    private val binding by lazy { ActivityServerCustomConfigBinding.inflate(layoutInflater) }

    private val editGuid by lazy { intent.getStringExtra("guid").orEmpty() }
    private val isRunning by lazy {
        intent.getBooleanExtra("isRunning", false)
                && editGuid.isNotEmpty()
                && editGuid == MmkvManager.getSelectServer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        title = getString(R.string.title_server)

        if (!Utils.getDarkModeStatus(this)) {
            binding.editor.colorScheme = EditorTheme.INTELLIJ_LIGHT
        }
        binding.editor.language = JsonLanguage()
        val config = MmkvManager.decodeServerConfig(editGuid)
        if (config != null) {
            bindingServer(config)
        } else {
            clearServer()
        }
    }

    /**
     * Binding selected server config
     */
    private fun bindingServer(config: ProfileItem): Boolean {
        binding.etRemarks.text = Utils.getEditable(config.remarks)
        val raw = MmkvManager.decodeServerRaw(editGuid)
        val configContent = raw.orEmpty()

        binding.editor.setTextContent(Utils.getEditable(configContent))
        return true
    }

    /**
     * clear or init server config
     */
    private fun clearServer(): Boolean {
        binding.etRemarks.text = null
        return true
    }

    /**
     * save server config
     */
    private fun saveServer(): Boolean {
        if (TextUtils.isEmpty(binding.etRemarks.text.toString())) {
            toast(R.string.server_lab_remarks)
            return false
        }

        val profileItem = try {
            CustomFmt.parse(binding.editor.text.toString())
        } catch (e: Exception) {
            Log.e(AppConfig.TAG, "Failed to parse custom configuration", e)
            toast("${getString(R.string.toast_malformed_josn)} ${e.cause?.message}")
            return false
        }

        val config = MmkvManager.decodeServerConfig(editGuid) ?: ProfileItem.create(EConfigType.CUSTOM)
        binding.etRemarks.text.let {
            config.remarks = if (it.isNullOrEmpty()) profileItem?.remarks.orEmpty() else it.toString()
        }
        config.server = profileItem?.server
        config.serverPort = profileItem?.serverPort

        MmkvManager.encodeServerConfig(editGuid, config)
        MmkvManager.encodeServerRaw(editGuid, binding.editor.text.toString())
        toastSuccess(R.string.toast_success)
        finish()
        return true
    }

    /**
     * save server config
     */
    private fun deleteServer(): Boolean {
        if (editGuid.isNotEmpty()) {
            AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    MmkvManager.removeServer(editGuid)
                    finish()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    // do nothing
                }
                .show()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_server, menu)
        val delButton = menu.findItem(R.id.del_config)
        val saveButton = menu.findItem(R.id.save_config)

        if (editGuid.isNotEmpty()) {
            if (isRunning) {
                delButton?.isVisible = false
                saveButton?.isVisible = false
            }
        } else {
            delButton?.isVisible = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.del_config -> {
            deleteServer()
            true
        }

        R.id.save_config -> {
            saveServer()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}
