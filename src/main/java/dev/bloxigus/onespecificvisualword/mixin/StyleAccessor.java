package dev.bloxigus.onespecificvisualword.mixin;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Style.class)
public interface StyleAccessor {
    @Accessor
    TextColor getColor();

    @Accessor
    Boolean getItalic();

    @Accessor
    Boolean getBold();

    @Accessor
    Boolean getUnderlined();

    @Accessor
    Boolean getStrikethrough();

    @Accessor
    Boolean getObfuscated();

    @Accessor
    Integer getShadowColor();
}
