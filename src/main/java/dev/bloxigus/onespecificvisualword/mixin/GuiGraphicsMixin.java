package dev.bloxigus.onespecificvisualword.mixin;

import dev.bloxigus.onespecificvisualword.WordReplacer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I",
            index = 2,
            at = @At("HEAD"),
            argsOnly = true
    )
    public FormattedCharSequence modifyCharSequence(FormattedCharSequence value) {
        return WordReplacer.fromCharSequence(value);
    }
    @ModifyVariable(
            method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
            index = 2,
            at = @At("HEAD"),
            argsOnly = true
    )
    public String modifyCharSequence(String value) {
        return WordReplacer.fromString(value);
    }
}
