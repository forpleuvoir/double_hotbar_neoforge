package com.sidezbros.double_hotbar.mixin.compat;

import com.sidezbros.double_hotbar.DHModConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.Overlay.class)
public abstract class HUDOverlayHandler$OverlayMixin {

    @ModifyVariable(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "STORE"), ordinal = 0)
    public int doubleHotbar$modifyExperienceLevelY(int y) {
        var player = Minecraft.getInstance().player;
        if (DHModConfig.INSTANCE.displayDoubleHotbar && !DHModConfig.INSTANCE.disableMod && player != null && !player.isSpectator()) {
            return y - DHModConfig.INSTANCE.shift;
        }
        return y;
    }

}
