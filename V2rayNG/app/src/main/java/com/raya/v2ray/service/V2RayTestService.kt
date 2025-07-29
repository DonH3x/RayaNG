package com.raya.v2ray.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.raya.v2ray.AppConfig.MSG_MEASURE_CONFIG
import com.raya.v2ray.AppConfig.MSG_MEASURE_CONFIG_CANCEL
import com.raya.v2ray.AppConfig.MSG_MEASURE_CONFIG_NUI
import com.raya.v2ray.AppConfig.MSG_MEASURE_CONFIG_NUI_SUCCESS
import com.raya.v2ray.AppConfig.MSG_MEASURE_CONFIG_SUCCESS
import com.raya.v2ray.dto.EConfigType
import com.raya.v2ray.extension.serializable
import com.raya.v2ray.handler.MmkvManager
import com.raya.v2ray.handler.SpeedtestManager
import com.raya.v2ray.handler.V2rayConfigManager
import com.raya.v2ray.util.MessageUtil
import com.raya.v2ray.util.PluginUtil
import com.raya.v2ray.util.Utils
import go.Seq
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import libv2ray.Libv2ray
import java.util.concurrent.Executors

class V2RayTestService : Service() {
    private val realTestScope by lazy { CoroutineScope(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()).asCoroutineDispatcher()) }

    /**
     * Initializes the V2Ray environment.
     */
    override fun onCreate() {
        super.onCreate()
        Seq.setContext(this)
        Libv2ray.initCoreEnv(Utils.userAssetPath(this), Utils.getDeviceIdForXUDPBaseKey())
    }

    /**
     * Handles the start command for the service.
     * @param intent The intent.
     * @param flags The flags.
     * @param startId The start ID.
     * @return The start mode.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getIntExtra("key", 0)) {
            MSG_MEASURE_CONFIG -> {
                measureConfig(intent)
            }

            MSG_MEASURE_CONFIG_NUI -> {
                measureConfig(intent, true)
            }

            MSG_MEASURE_CONFIG_CANCEL -> {
                realTestScope.coroutineContext[Job]?.cancelChildren()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Binds the service.
     * @param intent The intent.
     * @return The binder.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Starts the real ping test.
     * @param guid The GUID of the configuration.
     * @return The ping result.
     */
    private fun startRealPing(guid: String): Long {
        val retFailure = -1L

        val config = MmkvManager.decodeServerConfig(guid) ?: return retFailure
        if (config.configType == EConfigType.HYSTERIA2) {
            val delay = PluginUtil.realPingHy2(this, config)
            return delay
        } else {
            val configResult = V2rayConfigManager.getV2rayConfig4Speedtest(this, guid)
            if (!configResult.status) {
                return retFailure
            }
            return SpeedtestManager.realPing(configResult.content)
        }
    }

    private fun measureConfig(intent: Intent, selectBest: Boolean = false) {
        val guid = intent.serializable<String>("content") ?: ""
        realTestScope.launch {
            val result = startRealPing(guid)
            if (selectBest) {
                MessageUtil.sendMsg2UI(this@V2RayTestService, MSG_MEASURE_CONFIG_NUI_SUCCESS, Pair(guid, result))
            } else {
                MessageUtil.sendMsg2UI(this@V2RayTestService, MSG_MEASURE_CONFIG_SUCCESS, Pair(guid, result))
            }
        }
    }
}
