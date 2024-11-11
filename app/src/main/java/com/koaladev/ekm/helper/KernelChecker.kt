import com.koaladev.ekm.helper.RootChecker

object KernelChecker {
    fun isEvergreenKernel(): Boolean {
        if (!RootChecker.isDeviceRooted()) {
            return false
        }

        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = process.outputStream
            val inputStream = process.inputStream

            outputStream.write("cat /proc/evergreen-kernel\n".toByteArray())
            outputStream.write("exit\n".toByteArray())
            outputStream.flush()

            val output = inputStream.bufferedReader().use { it.readText() }
            process.waitFor()

            output.trim() == "evergreen_kernel_verified"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}