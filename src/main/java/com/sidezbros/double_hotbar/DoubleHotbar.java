package com.sidezbros.double_hotbar;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.time.Instant;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DoubleHotbar.MOD_ID)
public class DoubleHotbar {
    public static final String MOD_ID = "double_hotbar";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static KeyMapping keyMapping;
    private final boolean[] hotbarKeys = new boolean[10];
    private final long[] timer = new long[10];
    private boolean alreadySwapped = false;


    public static final ResourceLocation WOOSH_SOUND_ID = ResourceLocation.fromNamespaceAndPath("double_hotbar", "woosh");
    public static DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "double_hotbar");
    public static final DeferredHolder<SoundEvent, SoundEvent> WOOSH_SOUND_EVENT = SOUND_EVENTS.register("woosh", () -> SoundEvent.createVariableRangeEvent(WOOSH_SOUND_ID));

    @SuppressWarnings("DataFlowIssue")
    public DoubleHotbar(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.getEventBus().addListener(this::onClientSetup);
        modContainer.getEventBus().addListener(this::registerKeyBindings);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> AutoConfig.getConfigScreen(DHModConfig.class, parent).get());

        NeoForge.EVENT_BUS.addListener(this::clientTick);

        SOUND_EVENTS.register(modEventBus);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        DHModConfig.init();
    }

    private void clientTick(ClientTickEvent.Post event) {
        var player = Minecraft.getInstance().player;
        var client = Minecraft.getInstance();
        if (client.player == null) return;
        if (DHModConfig.INSTANCE.holdToSwap) {
            if (keyMapping.isDown() != this.hotbarKeys[9]) {
                this.hotbarKeys[9] = keyMapping.isDown();
                if (keyMapping.isDown()) {
                    timer[9] = Instant.now().toEpochMilli();
                } else {
                    if (Instant.now().toEpochMilli() - timer[9] < DHModConfig.INSTANCE.holdTime) {
                        this.swapStack(player, !DHModConfig.INSTANCE.holdToSwapBar, player.getInventory().selected);
                    } else {
                        this.alreadySwapped = false;
                    }
                }
            }
            if (!this.alreadySwapped && keyMapping.isDown()
                    && Instant.now().toEpochMilli() - timer[9] > DHModConfig.INSTANCE.holdTime) {
                this.swapStack(player, DHModConfig.INSTANCE.holdToSwapBar, player.getInventory().selected);
                this.alreadySwapped = true;
            }
        } else {
            while (keyMapping.consumeClick()) {
                this.swapStack(player, true, 0);
            }
        }
        if (DHModConfig.INSTANCE.allowDoubleTap) {
            for (int i = 0; i < 9; i++) {
                if (client.options.keyHotbarSlots[i].isDown() != this.hotbarKeys[i]) {
                    this.hotbarKeys[i] = client.options.keyHotbarSlots[i].isDown();
                    if (client.options.keyHotbarSlots[i].isDown()) {
                        if (Instant.now().toEpochMilli() - timer[i] < DHModConfig.INSTANCE.doubleTapWindow) {
                            this.swapStack(client.player, false, i);
                            timer[i] = 0;
                        } else {
                            timer[i] = Instant.now().toEpochMilli();
                        }
                    }
                }
            }
        }
    }

    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        keyMapping = new KeyMapping(
                "key.double_hotbar.swap",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                "category.double_hotbar.keybinds"
        );
        event.register(keyMapping);
    }

    public void swapStack(Player player, boolean fullRow, int slot) {
        MultiPlayerGameMode interactionManager = Minecraft.getInstance().gameMode;
        int inventoryRow = DHModConfig.INSTANCE.inventoryRow * 9;
        boolean playSound = false;

        if (interactionManager == null || DHModConfig.INSTANCE.disableMod) {
            return;
        }

        if (fullRow) {
            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getItem(i) != player.getInventory().getItem(inventoryRow + i)) {
                    interactionManager.handleInventoryMouseClick(player.inventoryMenu.containerId, inventoryRow + i, i, ClickType.SWAP, player);
                    playSound = true;
                }
            }
        } else if (player.getInventory().getItem(slot) != player.getInventory().getItem(inventoryRow + slot)) {
            interactionManager.handleInventoryMouseClick(player.inventoryMenu.containerId, inventoryRow + slot, slot, ClickType.SWAP, player);
            playSound = true;
        }


        if (playSound) {
            player.playSound(WOOSH_SOUND_EVENT.get(), 0.01f * DHModConfig.INSTANCE.wooshVolume, 1f);
        }
    }

}
