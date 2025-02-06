package eivitool.utils

import java.lang.management.ManagementFactory
import com.sun.management.OperatingSystemMXBean

fun getUsageMemory(): String {
    val memoryBean = ManagementFactory.getMemoryMXBean()
    return formatByteSize(memoryBean.heapMemoryUsage.used)
}

fun getUsageCpu(): String {
    val osBean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
    val processCpuLoad = osBean.processCpuLoad * 100
    return "%.2f%%".format(processCpuLoad)
}

fun formatByteSize(sizeInBytes: Long): String {
    return when {
        sizeInBytes < 1024 -> "${sizeInBytes} Byte"
        sizeInBytes < 1024 * 1024 -> String.format("%.2f KiB", sizeInBytes / 1024.0)
        sizeInBytes < 1024 * 1024 * 1024 -> String.format("%.2f MiB", sizeInBytes / (1024.0 * 1024))
        else -> String.format("%.2f GiB", sizeInBytes / (1024.0 * 1024 * 1024))
    }
}