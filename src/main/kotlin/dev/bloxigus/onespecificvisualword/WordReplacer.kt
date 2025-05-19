package dev.bloxigus.onespecificvisualword

import dev.bloxigus.onespecificvisualword.Config.words
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence

object WordReplacer {
    fun parseComponentFromString(string: String): MutableComponent? {
        return Component.Serializer.fromJson(string, RegistryAccess.EMPTY)
    }
    fun parseStringFromComponent(component: Component): String {
        return Component.Serializer.toJson(component, RegistryAccess.EMPTY)
    }
    @JvmStatic
    fun fromCharSequence(original: FormattedCharSequence): FormattedCharSequence {
        var lastStyle = Style.EMPTY
        val string = StringBuilder()
        val components: MutableList<FormattedCharSequence> = mutableListOf()
        original.accept { i, style, j ->
            if (lastStyle != style) {
                formattedReplace(components, string.toString(), lastStyle)
                string.clear()
                lastStyle = style
            }
            string.append(j.toChar())
            true
        }
        formattedReplace(components, string.toString(), lastStyle)
        return FormattedCharSequence.fromList(components)
    }
    @JvmStatic
    fun fromString(original: String): String {
        val string = StringBuilder()

        var index = 0
        var match = original.findAnyOf(words.keys, index)
        while (match != null) {
            val before = original.substring(index, match.first)
            string.append(before)

            val matchComponent = words[match.second]

            if (matchComponent != null) {
                string.append(formattedCharSequenceToString(matchComponent.visualOrderText))
            }
            index = match.first + match.second.length
            match = original.findAnyOf(words.keys, index + 1)
        }

        val after = original.substring(index, original.length)
        string.append(after)

        return string.toString()
    }
    private fun formattedCharSequenceToString(original: FormattedCharSequence): String {
        val string = StringBuilder()
        original.accept { i, style, j ->
            string.append(j.toChar())
            true
        }
        return string.toString()
    }
    private fun formattedReplace(list: MutableList<FormattedCharSequence>, string: String, style: Style) {
        var index = 0
        var match = string.findAnyOf(words.keys, index)
        while (match != null) {
            val before = string.substring(index, match.first)
            if (before.isNotEmpty()) list.add(before.styled(style).visualOrderText)

            var matchComponent = words[match.second]

            if (matchComponent != null) {
                if (matchComponent.style == Style.EMPTY) {
                    matchComponent = matchComponent.copy().withStyle(style) ?: return
                }
                if (style.isItalic && !matchComponent.style.isItalic) {
                    matchComponent = matchComponent.copy().withStyle(matchComponent.style.withItalic(true)) ?: return
                }
                if (style.isBold && !matchComponent.style.isBold) {
                    matchComponent = matchComponent.copy().withStyle(matchComponent.style.withBold(true)) ?: return
                }
                list.add(matchComponent.visualOrderText)
            }

            index = match.first + match.second.length
            match = string.findAnyOf(words.keys, index + 1)
        }
        val after = string.substring(index, string.length)
        if (after.isNotEmpty()) list.add(after.styled(style).visualOrderText)
    }
}

fun String.styled(style: Style): MutableComponent {
    return Component.literal(this).withStyle(style)
}