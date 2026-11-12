package com.llamatik.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class SseTest {

    @Test
    fun pipeWritesDeltaThenDoneEvents() = runBlocking {
        val writer = java.io.StringWriter()
        val deltas = Channel<String>(capacity = 1)
        val done = Channel<Unit>(capacity = 1)
        val errors = Channel<String>(capacity = 1)

        deltas.send("hello")
        done.send(Unit)

        Sse.pipe(writer = writer, deltas = deltas, done = done, errors = errors)

        assertEquals(
            "data: {\"event\":\"delta\",\"text\":\"hello\"}\n\n" +
                "data: {\"event\":\"done\"}\n\n",
            writer.toString()
        )
    }

    @Test
    fun pipeWritesErrorEventAndStops() = runBlocking {
        val writer = java.io.StringWriter()
        val deltas = Channel<String>(capacity = 1)
        val done = Channel<Unit>(capacity = 1)
        val errors = Channel<String>(capacity = 1)

        errors.send("boom")

        Sse.pipe(writer = writer, deltas = deltas, done = done, errors = errors)

        assertEquals(
            "data: {\"event\":\"error\",\"message\":\"boom\"}\n\n",
            writer.toString()
        )
    }

    @Test
    fun pipeEscapesJsonPayloads() = runBlocking {
        val writer = java.io.StringWriter()
        val deltas = Channel<String>(capacity = 1)
        val done = Channel<Unit>(capacity = 1)
        val errors = Channel<String>(capacity = 1)
        val text = "quote \" slash \\ newline\n tab\t backspace\b"

        deltas.send(text)
        done.send(Unit)

        Sse.pipe(writer = writer, deltas = deltas, done = done, errors = errors)

        val firstPayload = writer.toString()
            .split("\n\n")
            .first()
            .removePrefix("data: ")

        assertEquals(
            text,
            Json.parseToJsonElement(firstPayload).jsonObject.getValue("text").jsonPrimitive.content
        )
    }
}
