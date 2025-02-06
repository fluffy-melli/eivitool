package eivitool.value

fun GetResolutionList(): List<Triple<String, Int, Int>> {
    return listOf(
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