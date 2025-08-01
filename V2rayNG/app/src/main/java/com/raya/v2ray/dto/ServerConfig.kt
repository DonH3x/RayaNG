package com.raya.v2ray.dto

import com.raya.v2ray.AppConfig.TAG_BLOCKED
import com.raya.v2ray.AppConfig.TAG_DIRECT
import com.raya.v2ray.AppConfig.TAG_PROXY

data class ServerConfig(
    val configVersion: Int = 3,
    val configType: EConfigType,
    var subscriptionId: String = "",
    val addedTime: Long = System.currentTimeMillis(),
    var remarks: String = "",
    val outboundBean: V2rayConfig.OutboundBean? = null,
    var fullConfig: V2rayConfig? = null
) {
    companion object {
        fun create(configType: EConfigType): ServerConfig {
            when (configType) {
                EConfigType.VMESS,
                EConfigType.VLESS ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                vnext = listOf(
                                    V2rayConfig.OutboundBean.OutSettingsBean.VnextBean(
                                        users = listOf(V2rayConfig.OutboundBean.OutSettingsBean.VnextBean.UsersBean())
                                    )
                                )
                            ),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()
                        )
                    )

                EConfigType.CUSTOM ->
                    return ServerConfig(configType = configType)

                EConfigType.SHADOWSOCKS,
                EConfigType.SOCKS,
                EConfigType.HTTP,
                EConfigType.TROJAN,
                EConfigType.HYSTERIA2 ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                servers = listOf(V2rayConfig.OutboundBean.OutSettingsBean.ServersBean())
                            ),
                            streamSettings = V2rayConfig.OutboundBean.StreamSettingsBean()
                        )
                    )

                EConfigType.WIREGUARD ->
                    return ServerConfig(
                        configType = configType,
                        outboundBean = V2rayConfig.OutboundBean(
                            protocol = configType.name.lowercase(),
                            settings = V2rayConfig.OutboundBean.OutSettingsBean(
                                secretKey = "",
                                peers = listOf(V2rayConfig.OutboundBean.OutSettingsBean.WireGuardBean())
                            )
                        )
                    )
            }
        }
    }

    fun getProxyOutbound(): V2rayConfig.OutboundBean? {
        if (configType != EConfigType.CUSTOM) {
            return outboundBean
        }
        return fullConfig?.getProxyOutbound()
    }

    fun getAllOutboundTags(): MutableList<String> {
        if (configType != EConfigType.CUSTOM) {
            return mutableListOf(TAG_PROXY, TAG_DIRECT, TAG_BLOCKED)
        }
        fullConfig?.let { config ->
            return config.outbounds.map { it.tag }.toMutableList()
        }
        return mutableListOf()
    }
}
