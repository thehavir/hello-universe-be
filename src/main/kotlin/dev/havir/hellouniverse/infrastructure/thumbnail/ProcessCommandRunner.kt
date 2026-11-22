package dev.havir.hellouniverse.infrastructure.thumbnail

import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Runs commands with low CPU priority (`nice`) and one vips thread, so the
 * small server stays responsive. Output goes to a temporary file instead of
 * a pipe, so a chatty program can never block while we wait for it.
 */
@Component
class ProcessCommandRunner : CommandRunner {
    override fun run(
        command: List<String>,
        timeout: Duration,
    ): CommandResult {
        val outputFile = Files.createTempFile("command-", ".log")
        try {
            val process =
                try {
                    ProcessBuilder(NICE + command)
                        .redirectErrorStream(true)
                        .redirectOutput(outputFile.toFile())
                        .also { it.environment().putAll(ENVIRONMENT) }
                        .start()
                } catch (e: IOException) {
                    throw ThumbnailException(
                        "Cannot start ${command.first()}",
                        e,
                    )
                }
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly().waitFor()
                throw ThumbnailException(
                    "${command.first()} took longer than $timeout",
                )
            }
            return CommandResult(process.exitValue(), readStart(outputFile))
        } finally {
            Files.deleteIfExists(outputFile)
        }
    }

    private fun readStart(file: java.nio.file.Path): String =
        Files.newInputStream(file).use {
            String(it.readNBytes(MAX_OUTPUT_BYTES))
        }

    private companion object {
        val NICE = listOf("nice", "-n", "10")
        val ENVIRONMENT =
            mapOf(
                "VIPS_CONCURRENCY" to "1",
                // Refuse rarely used, less safe file loaders
                "VIPS_BLOCK_UNTRUSTED" to "1",
            )
        const val MAX_OUTPUT_BYTES = 8 * 1024
    }
}
