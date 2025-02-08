package eivitool.value

fun GetResolutionList(w:Int, h:Int): List<Triple<String, Int, Int>> {
    return listOf(
        Triple("100%", w, h),
        Triple("90%", (w * 0.9).toInt(), (h * 0.9).toInt()),
        Triple("80%", (w * 0.8).toInt(), (h * 0.8).toInt()),
        Triple("70%", (w * 0.7).toInt(), (h * 0.7).toInt()),
        Triple("60%", (w * 0.6).toInt(), (h * 0.6).toInt()),
        Triple("50%", (w * 0.5).toInt(), (h * 0.5).toInt()),
        Triple("40%", (w * 0.4).toInt(), (h * 0.4).toInt()),
        Triple("30%", (w * 0.3).toInt(), (h * 0.3).toInt()),
        Triple("20%", (w * 0.2).toInt(), (h * 0.2).toInt()),
        Triple("10%", (w * 0.1).toInt(), (h * 0.1).toInt()),
        Triple("1920x1080", 1920, 1080),
        Triple("1536x864", 1536, 864),
        Triple("1440x810", 1440, 810),
        Triple("1280x720", 1280, 720),
        Triple("1152x648", 1152, 648),
        Triple("1096x616", 1096, 616),
        Triple("960x540", 960, 540),
        Triple("852x480", 852, 480),
        Triple("696x392", 696, 392),
        Triple("640x360", 640, 360)
    )
}