package ru.cherryngine.lib.jackson.serializer.impl

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import jakarta.inject.Singleton
import net.minestom.server.item.Material
import net.minestom.server.utils.NamespaceID
import ru.cherryngine.lib.jackson.serializer.JacksonDeserializer
import ru.cherryngine.lib.jackson.serializer.JacksonSerializer

@Singleton
class MaterialSerializationProcessor : JacksonSerializer<Material>, JacksonDeserializer<Material> {
    override fun serialize(value: Material, gen: JsonGenerator) {
        gen.writeString(value.key().value().uppercase())
    }

    override fun deserialize(parser: JsonParser): Material {
        return Material.fromNamespaceId(NamespaceID.from(parser.valueAsString.lowercase()))!!
    }
}
