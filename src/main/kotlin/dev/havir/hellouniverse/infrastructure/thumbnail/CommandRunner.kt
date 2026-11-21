package dev.havir.hellouniverse.infrastructure.thumbnail

import java.time.Duration

/** Runs an external program (vips, ffmpeg) and waits for it. */
interface CommandRunner {
    /**
     * @param command the program and its arguments. No shell is used, so
     *   arguments are never interpreted.
     * @throws ThumbnailException when the program can't start or runs longer
     *   than [timeout]; it is then killed.
     */
    fun run(
        command: List<String>,
        timeout: Duration,
    ): CommandResult
}

data class CommandResult(
    val exitCode: Int,
    /** stdout and stderr together, cut to a few KB. */
    val output: String,
) {
    val succeeded: Boolean get() = exitCode == 0
}
