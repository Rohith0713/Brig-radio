package com.campuswave.app.data.network

object ApiConfig {
    // 1. FOR PHYSICAL DEVICE (Both laptop and phone must be on same Wi-Fi)
    const val BASE_URL = "http://10.99.37.110:5000/api/"
const val UPLOADS_URL = "http://10.99.37.110:5000/uploads/"
    
    // 2. FOR ANDROID EMULATOR (Uncomment these and comment the ones above if using Emulator)
    // const val BASE_URL = "http://10.0.2.2:5000/api/"
    // const val UPLOADS_URL = "http://10.0.2.2:5000/uploads/"

    // Agora Configuration
    const val AGORA_APP_ID = "5c987d3664cf418ea548a92bc73dff0b"
    val AGORA_TEMP_TOKEN: String? = null // Paste temp token here if Error 110 persists

    // 100ms.live Configuration
    const val HMS_DEFAULT_ROOM_ID = "YOUR_100MS_ROOM_ID"
}
