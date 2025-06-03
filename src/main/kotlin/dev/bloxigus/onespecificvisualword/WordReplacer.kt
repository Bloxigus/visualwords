package dev.bloxigus.onespecificvisualword

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import dev.bloxigus.onespecificvisualword.Config.Companion.config
import dev.bloxigus.onespecificvisualword.StyledLetter.Companion.cacheKey
import dev.bloxigus.onespecificvisualword.StyledLetter.Companion.replaceWith
import dev.bloxigus.onespecificvisualword.StyledLetter.Companion.toFormattedCharSink
import dev.bloxigus.onespecificvisualword.StyledLetter.Companion.toUnstyledString
import dev.bloxigus.onespecificvisualword.mixin.StyleAccessor
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

object WordReplacer {
    fun parseComponentFromString(string: String): MutableComponent? {
        return Component.Serializer.fromJson(string, RegistryAccess.EMPTY)
    }
    fun parseStringFromComponent(component: Component): String {
        return Component.Serializer.toJson(component, RegistryAccess.EMPTY)
    }

    private var cachedTrieRoot: TrieNode<StyledLetter>? = null

    @JvmStatic
    fun updateCachedConfig() {
        val styledLetterConfig = mutableMapOf<List<StyledLetter>,List<StyledLetter>>()

        for (replacement in config.words) {
            styledLetterConfig[replacement.getReplaceStyledLetters()] = replacement.getWithStyledLetters()
        }

        cachedTrieRoot = TrieNode()

        for ((key, value) in styledLetterConfig) {
            var node = cachedTrieRoot!!
            for (letter in key) {
                node = node.children.computeIfAbsent(letter) { TrieNode() }
            }
            node.replacement = value
        }
    }

    private val timeLimitedCache: Cache<Int, List<StyledLetter>> = CacheBuilder.newBuilder()
        .expireAfterWrite(5.minutes.toJavaDuration())
        .build()

    private fun getCached(input: List<StyledLetter>): List<StyledLetter>? {

        val ctr = cachedTrieRoot ?: return null

        return timeLimitedCache.get(input.cacheKey()) {
            input.replaceWith(ctr)
        }
    }

    @JvmStatic
    fun replaceInComponents(original: FormattedCharSequence): FormattedCharSequence {
        val styledLetters = StyledLetter.fromFormattedCharSequence(original)

        return getCached(styledLetters)?.toFormattedCharSink() ?: original
    }

    @JvmStatic
    fun replaceInString(original: String): String {
        val styledLetters = StyledLetter.fromString(original)

        return getCached(styledLetters)?.toUnstyledString() ?: original
    }
}

data class TrieNode<T>(
    val children: MutableMap<T, TrieNode<T>> = mutableMapOf(),
    var replacement: List<T>? = null
)

data class StyledLetter (
    val style: Style,
    val character: Int,
    val x: Int = 0
) {
    fun matches(other: StyledLetter): Boolean {
        if (other.character != character) return false

        if ((this.style as StyleAccessor).bold != null) {
            if ((other.style as StyleAccessor).bold != (this.style as StyleAccessor).bold) return false
        }
        if ((this.style as StyleAccessor).italic != null) {
            if ((other.style as StyleAccessor).italic != (this.style as StyleAccessor).italic) return false
        }
        if ((this.style as StyleAccessor).strikethrough != null) {
            if ((other.style as StyleAccessor).strikethrough != (this.style as StyleAccessor).strikethrough) return false
        }
        if ((this.style as StyleAccessor).color != null) {
            if ((other.style as StyleAccessor).color != (this.style as StyleAccessor).color) return false
        }
        if ((this.style as StyleAccessor).underlined != null) {
            if ((other.style as StyleAccessor).underlined != (this.style as StyleAccessor).underlined) return false
        }
        if ((this.style as StyleAccessor).obfuscated != null) {
            if ((other.style as StyleAccessor).obfuscated != (this.style as StyleAccessor).obfuscated) return false
        }
        if ((this.style as StyleAccessor).shadowColor != null) {
            if ((other.style as StyleAccessor).shadowColor != (this.style as StyleAccessor).shadowColor) return false
        }
        return true
    }

    companion object {
        @JvmStatic
        fun fromString(component: String): List<StyledLetter> {
            val styledLetters = mutableListOf<StyledLetter>()
            for (letter in component) {
                styledLetters.add(
                    StyledLetter(
                        Style.EMPTY,
                        letter.code
                    )
                )
            }
            return styledLetters
        }
        @JvmStatic
        fun fromComponent(component: MutableComponent): List<StyledLetter> {
            val styledLetters = mutableListOf<StyledLetter>()
            component.visualOrderText.accept { i, style, j ->
                styledLetters.add(StyledLetter(style, j, i))
            }
            return styledLetters
        }
        @JvmStatic
        fun fromFormattedCharSequence(component: FormattedCharSequence): List<StyledLetter> {
            val styledLetters = mutableListOf<StyledLetter>()
            component.accept { i, style, j ->
                styledLetters.add(StyledLetter(style, j, i))
            }
            return styledLetters
        }
        fun List<StyledLetter>.toFormattedCharSink(): FormattedCharSequence {
            return FormattedCharSequence { formattedCharSink ->
                for (letter in this) {
                    formattedCharSink.accept(letter.x, letter.style, letter.character)
                }
                true
            }
        }
        fun List<StyledLetter>.toUnstyledString(): String {
            return buildString {
                for (letter in this@toUnstyledString) {
                    this.append(Character.toChars(letter.character))
                }
            }
        }

        fun List<StyledLetter>.replaceWith(trieRoot: TrieNode<StyledLetter>): List<StyledLetter> {

            val result = mutableListOf<StyledLetter>()
            var i = 0
            while (i < this.size) {
                var node = trieRoot
                var j = i
                var lastMatch: Pair<Int, List<StyledLetter>>? = null

                while (j < this.size) {
                    val letter = this[j]
                    val next = node.children.entries.firstOrNull { it.key.matches(letter) } ?: break
                    node = next.value
                    if (node.replacement != null) {
                        lastMatch = j + 1 to node.replacement!!.withStyle(this[i].style)
                    }
                    j++
                }

                if (lastMatch != null) {
                    result.addAll(lastMatch.second)
                    i = lastMatch.first
                } else {
                    result.add(this[i])
                    i++
                }
            }

            return result
        }

        private fun List<StyledLetter>.withStyle(style: Style): List<StyledLetter> {
            return this.map {
                StyledLetter(
                    it.style.applyTo(style),
                    it.character,
                    it.x
                )
            }
        }

        fun List<StyledLetter>.cacheKey(): Int {
            var value = 0
            for (styledLetter in this) {
                value = value * 31 + styledLetter.character
                value = value * 31 + styledLetter.style.hashCode()
            }
            return value
        }
    }
}