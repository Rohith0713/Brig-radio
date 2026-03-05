package com.campuswave.app.data.network

object ApiConfig {
    // AWS EC2 Production Server
    const val BASE_URL = "http://15.206.131.110:8000/api/"
    const val UPLOADS_URL = "http://15.206.131.110:8000/uploads/"

    // Local Development (uncomment for local testing, comment out production above)
    // const val BASE_URL = "http://10.99.37.110:5000/api/"
    // const val UPLOADS_URL = "http://10.99.37.110:5000/uploads/"

    // Agora Configuration
    const val AGORA_APP_ID = "5c987d3664cf418ea548a92bc73dff0b"
    val AGORA_TEMP_TOKEN: String? = null // Paste temp token here if Error 110 persists

    // 100ms.live Configuration
    const val HMS_DEFAULT_ROOM_ID = "YOUR_100MS_ROOM_ID"
}
