package eivitool.utils

fun paddingText(path: String, maxLength: Int = 25): String {
    return if (path.length > maxLength) {
        "...${path.takeLast(maxLength)}"
    } else {
        path
    }
}
