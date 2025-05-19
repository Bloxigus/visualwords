package dev.bloxigus.onespecificvisualword.mixin;

import dev.bloxigus.onespecificvisualword.WordReplacer;
import net.minecraft.client.StringSplitter;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StringSplitter.class)
public class StringSplitterMixin {
    @ModifyVariable(
            method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F",
            index = 1,
            at = @At("HEAD"),
            argsOnly = true
    )
    public FormattedCharSequence modifyStringWidth(FormattedCharSequence value) {
        return WordReplacer.fromCharSequence(value);
    }

    @ModifyVariable(
            method = "stringWidth(Ljava/lang/String;)F",
            index = 1,
            at = @At("HEAD"),
            argsOnly = true
    )
    public String modifyStringWidth(String value) {
        return WordReplacer.fromString(value);
    }
}
