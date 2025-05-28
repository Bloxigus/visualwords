package dev.bloxigus.onespecificvisualword.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.bloxigus.onespecificvisualword.WordReplacer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(SignText.class)
public class SignTextMixin {
    @Shadow
    private @Nullable FormattedCharSequence[] renderMessages;

    @SuppressWarnings("DataFlowIssue") // silly intellij
    @WrapMethod(
            method = "getRenderMessages"
    )
    public FormattedCharSequence[] modifyCharSequence(boolean bl, Function<Component, FormattedCharSequence> function, Operation<FormattedCharSequence[]> original) {
        FormattedCharSequence[] result;
        if (this.renderMessages == null) {
            result = original.call(bl, function);
            for (int i = 0; i < 4; i++) {
                this.renderMessages[i] = WordReplacer.replaceInComponents(this.renderMessages[i]);
            }
        } else {
            result = original.call(bl, function);
        }
        return result;
    }
}
