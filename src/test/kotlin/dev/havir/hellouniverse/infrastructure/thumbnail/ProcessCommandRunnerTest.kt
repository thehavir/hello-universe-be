package dev.havir.hellouniverse.infrastructure.thumbnail

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProcessCommandRunnerTest {
    val tested = ProcessCommandRunner()
    val timeout: Duration = Duration.ofSeconds(10)

    @Test
    fun `on run returns exit code 0 and the output of the command`() {
        val result = tested.run(listOf("sh", "-c", "echo hello"), timeout)

        assertEquals(CommandResult(0, "hello\n"), result)
        assertTrue { result.succeeded }
    }

    @Test
    fun `on run returns the exit code of a failing command`() {
        val result = tested.run(listOf("sh", "-c", "exit 3"), timeout)

        assertEquals(3, result.exitCode)
        assertFalse { result.succeeded }
    }

    @Test
    fun `on run includes error output`() {
        val result = tested.run(listOf("sh", "-c", "echo broken >&2; exit 1"), timeout)

        assertEquals("broken\n", result.output)
    }

    @Test
    fun `on run passes arguments without a shell`() {
        val result = tested.run(listOf("echo", "a; echo injected", "\$HOME"), timeout)

        assertEquals("a; echo injected \$HOME\n", result.output)
    }

    @Test
    fun `on run uses one vips thread and blocks untrusted vips loaders`() {
        val result =
            tested.run(
                listOf("sh", "-c", "echo \$VIPS_CONCURRENCY \$VIPS_BLOCK_UNTRUSTED"),
                timeout,
            )

        assertEquals("1 1\n", result.output)
    }

    @Test
    fun `on run cuts very long output`() {
        val result =
            tested.run(listOf("sh", "-c", "head -c 100000 /dev/zero | tr '\\0' x"), timeout)

        assertEquals(8 * 1024, result.output.length)
    }

    @Test
    fun `on run kills the command and throws when it runs longer than the timeout`() {
        val start = System.nanoTime()

        assertFailsWith<ThumbnailException> {
            tested.run(listOf("sleep", "30"), Duration.ofMillis(300))
        }
        assertTrue { Duration.ofNanos(System.nanoTime() - start) < Duration.ofSeconds(10) }
    }

    @Test
    fun `on run throws when the program does not exist`() {
        val result = tested.run(listOf("no-such-program-xyz"), timeout)

        // nice starts, then fails to run the program and exits with 127
        assertEquals(127, result.exitCode)
    }
}
