package com.finn.ItemMagnetMod2;

// WICHTIG: Stelle sicher, dass dies der richtige Import ist!
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext; // Prüfe den genauen Pfad für Context
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting; // Oder net.minecraft.world.item.ChatFormatting
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
// KEIN Import für java.awt.swing...Entity!

public class MagnetItem extends Item {

    public static final double MAGNET_RANGE = 6.0;

    public MagnetItem(Properties properties) {
        super(properties);
    }

    // ... (use Methode bleibt gleich) ...

    // HIER GEÄNDERT: onInventoryTick statt inventoryTick für Versionen < 1.21.4
    @Override
    public void onInventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        // Jetzt sollte der Cast funktionieren, da der richtige Import gesetzt ist
        if (entity instanceof Player player && isActive(stack)) {
            AABB area = player.getBoundingBox().inflate(MAGNET_RANGE);
            List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class, area
            );

            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                if (item.isAlive() && !item.hasPickUpDelay()) {
                    Vec3 motion = player.position().subtract(item.position()).normalize().scale(0.15);
                    item.setDeltaMovement(item.getDeltaMovement().add(motion));

                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.WITCH, item.getX(), item.getY() + 0.2, item.getZ(), 3, 0.1, 0.1, 0.1, 0.0);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Reichweite: " + (int)MAGNET_RANGE + " Blöcke").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        if (isActive(stack)) {
            tooltipComponents.add(Component.literal("Zustand: Aktiv").withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.literal("Zustand: Inaktiv").withStyle(ChatFormatting.RED));
        }

        // super-Aufruf hier weglassen, um Argument-Fehler zu vermeiden
    }

    public boolean isActive(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        // Nutzung von .orElse(false) für Optional
        return tag.getBoolean("active").orElse(false);
    }

    public void setMagnetState(ItemStack stack, boolean active) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putBoolean("active", active);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}