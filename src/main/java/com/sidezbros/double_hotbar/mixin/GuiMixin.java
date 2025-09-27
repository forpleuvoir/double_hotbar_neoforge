package com.sidezbros.double_hotbar.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.sidezbros.double_hotbar.DHModConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Final
    @Shadow
    private static ResourceLocation HOTBAR_SPRITE;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void renderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed);

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 0))
    private void doubleHotbar$renderHotbarFrame(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && !DHModConfig.INSTANCE.disableMod) {
            guiGraphics.blitSprite(HOTBAR_SPRITE, guiGraphics.guiWidth() / 2 - 91, guiGraphics.guiHeight() - 22 - DHModConfig.INSTANCE.shift, 182, 22 - DHModConfig.INSTANCE.renderCrop);
        }
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
    private void doubleHotbar$shiftHotbarSelector(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && DHModConfig.INSTANCE.reverseBars && !DHModConfig.INSTANCE.disableMod) {
            guiGraphics.pose().translate(0, -DHModConfig.INSTANCE.shift, 0);
        }
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private void doubleHotbar$returnHotbarSelector(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && DHModConfig.INSTANCE.reverseBars && !DHModConfig.INSTANCE.disableMod) {
            guiGraphics.pose().translate(0, DHModConfig.INSTANCE.shift, 0);
        }
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"))
    private void doubleHotbar$shiftHotbarItems(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && DHModConfig.INSTANCE.reverseBars && !DHModConfig.INSTANCE.disableMod) {
            guiGraphics.pose().translate(0, -DHModConfig.INSTANCE.shift, 0);
        }
    }

    @Inject(method = "renderItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void doubleHotbar$renderHotbarItems(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo info) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && !DHModConfig.INSTANCE.disableMod) {
            if (DHModConfig.INSTANCE.reverseBars) {
                guiGraphics.pose().translate(0, DHModConfig.INSTANCE.shift, 0);
            }
            int m = 1;
            for (int n2 = 0; n2 < 9; ++n2) {
                int o = guiGraphics.guiWidth() / 2 - 90 + n2 * 20 + 2;
                int p = guiGraphics.guiHeight() - 16 - 3 - (DHModConfig.INSTANCE.reverseBars ? 0 : DHModConfig.INSTANCE.shift);
                this.renderSlot(guiGraphics, o, p, deltaTracker, getCameraPlayer(), getCameraPlayer().getInventory().items.get(n2 + DHModConfig.INSTANCE.inventoryRow * 9), m++);
            }
        }
    }

    //-----------------Other HUD SHIFT-----------------

    @Unique
    private static int doubleHotbar$modifyYShift(Player player, int y) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && !DHModConfig.INSTANCE.disableMod && player != null && !player.isSpectator()) {
            return y - DHModConfig.INSTANCE.shift;
        }
        return y;
    }

    @ModifyVariable(method = "renderJumpMeter", at = @At(value = "STORE"), ordinal = 3)
    public int doubleHotbar$modifyJumpMeterY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderExperienceBar", at = @At(value = "STORE"), ordinal = 4)
    public int doubleHotbar$modifyExperienceBarY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderHealthLevel", at = @At(value = "STORE"), ordinal = 4)
    public int doubleHotbar$modifyHealthLevelY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderArmor", at = @At(value = "STORE"), ordinal = 5)
    private static int doubleHotbar$modifyArmorY(int y, @Local(argsOnly = true) Player player) {
        return doubleHotbar$modifyYShift(player, y);
    }

    @ModifyVariable(method = "renderFoodLevel", at = @At(value = "STORE"), ordinal = 2)
    public int doubleHotbar$modifyFoodLevelY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderAirLevel", at = @At(value = "STORE"), ordinal = 3)
    public int doubleHotbar$modifyAirLevelY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "STORE"), ordinal = 3)
    public int doubleHotbar$modifySelectedItemNameY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @ModifyVariable(method = "renderVehicleHealth", at = @At(value = "STORE"), ordinal = 4)
    public int doubleHotbar$modifyVehicleHealthY(int y) {
        return doubleHotbar$modifyYShift(getCameraPlayer(), y);
    }

    @Inject(method = "renderOverlayMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"))
    public void doubleHotbar$modifyOverlayMessageY(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DHModConfig.INSTANCE.displayDoubleHotbar && !DHModConfig.INSTANCE.disableMod && getCameraPlayer() != null && !getCameraPlayer().isSpectator()) {
            guiGraphics.pose().translate(0, -DHModConfig.INSTANCE.shift, 0);
        }
    }

}
