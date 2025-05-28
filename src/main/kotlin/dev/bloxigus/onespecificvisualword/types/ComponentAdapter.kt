package dev.bloxigus.onespecificvisualword.types

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dev.bloxigus.onespecificvisualword.WordReplacer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class ComponentAdapter : TypeAdapter<MutableComponent>() {
    override fun write(out: JsonWriter?, value: MutableComponent?) {
        value?.let {
            out?.value(
                WordReplacer.parseStringFromComponent(value)
            )
        }
    }

    override fun read(input: JsonReader?): MutableComponent {
        input?.let {
            input.nextString()?.let {
                return WordReplacer.parseComponentFromString(it) ?: Component.empty()
            }
        }
        return Component.empty()
    }
}