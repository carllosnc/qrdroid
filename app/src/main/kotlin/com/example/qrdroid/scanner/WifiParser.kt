package com.example.qrdroid.scanner

data class WifiData(
    val ssid: String = "",
    val password: String = "",
    val type: String = "WPA",
    val isHidden: Boolean = false
)

object WifiParser {

    fun parse(raw: String): WifiData {
        if (!raw.startsWith("WIFI:", ignoreCase = true)) {
            return WifiData()
        }

        var ssid = ""
        var password = ""
        var type = "WPA"
        var isHidden = false

        // Format is key:value;key:value;;
        val content = raw.substringAfter("WIFI:", "")
        val regex = Regex("([STPH]):((?:\\\\;|[^;])*)")
        val matches = regex.findAll(content)

        for (match in matches) {
            val key = match.groupValues[1]
            val value = match.groupValues[2].replace("\\;", ";").replace("\\:", ":")

            when (key) {
                "S" -> ssid = value
                "P" -> password = value
                "T" -> type = value
                "H" -> isHidden = value.equals("true", ignoreCase = true)
            }
        }

        return WifiData(
            ssid = ssid,
            password = password,
            type = if (type.isBlank()) "Aberta" else type,
            isHidden = isHidden
        )
    }

    fun buildPayload(ssid: String, password: String, type: String = "WPA", isHidden: Boolean = false): String {
        val cleanType = when (type.uppercase()) {
            "WEP" -> "WEP"
            "ABERTA", "NOPASS", "OPEN" -> "nopass"
            else -> "WPA"
        }
        val cleanSsid = escape(ssid)
        val cleanPassword = if (cleanType == "nopass") "" else escape(password)
        val hiddenStr = if (isHidden) "H:true;" else ""

        return "WIFI:T:$cleanType;S:$cleanSsid;P:$cleanPassword;$hiddenStr;"
    }

    private fun escape(text: String): String {
        return text.replace(";", "\\;").replace(":", "\\:")
    }
}
