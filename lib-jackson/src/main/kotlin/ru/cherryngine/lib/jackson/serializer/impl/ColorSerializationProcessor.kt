package ru.cherryngine.lib.jackson.serializer.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import jakarta.inject.Singleton
import net.minestom.server.color.Color
import ru.cherryngine.lib.jackson.serializer.JacksonDeserializer
import ru.cherryngine.lib.jackson.serializer.JacksonSerializer

@Singleton
class ColorSerializationProcessor : JacksonSerializer<Color>, JacksonDeserializer<Color> {
    override fun serialize(value: Color, gen: JsonGenerator) {
        gen.writeString("${value.red()}, ${value.green()}, ${value.blue()}")
    }

    override fun deserialize(parser: JsonParser): Color {
        val text = parser.readValueAs(String::class.java)
        val (r, g, b) = text.replace(" ", "").split(",")
        return Color(r.toInt(), g.toInt(), b.toInt())
    }
}