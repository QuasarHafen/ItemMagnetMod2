package com.finn.ItemMagnetMod2;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagnetItem extends net.minecraft.world.item.Item {

    // Die zentrale Reichweite (6 Blöcke laut deiner Liste)
    public static final double MAGNET_RANGE = 6.0;

    public MagnetItem(Properties properties) {
        super(properties);
    }

    // 1. DIE USE-METHODE (Rechtsklick Ein/Aus)
    @Override
    public net.minecraft.world.InteractionResult use(
            net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand
    ) {
        net.minecraft.world.item.ItemStack realStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean currentState = isActive(realStack);
            setMagnetState(realStack, !currentState);

            if (!currentState) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMagnet aktiviert!"), true);
                // Hier gehört SoundSource.PLAYERS ans Ende:
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.LEVER_CLICK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMagnet deaktiviert!"), true);
                // Auch hier gehört SoundSource.PLAYERS ans Ende:
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.LEVER_CLICK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        // Und erst HIER ganz unten wird das InteractionResult zurückgegeben
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    // 2. DIE INVENTORY-TICK-METHODE (Hier lag der Fehler!)
    // Minecraft erwartet hier zwingend: Level, Entity, int (slotId), boolean (isSelected)
    @Override
    public void inventoryTick(
            @NotNull net.minecraft.world.item.ItemStack stack,
            @NotNull net.minecraft.world.level.Level level,
            @NotNull net.minecraft.world.entity.Entity entity,
            int slotId,
            boolean isSelected
        )
    {
        // Wir prüfen erst, ob wir auf dem Server sind und der Magnet aktiv ist
        if (!level.isClientSide() && entity instanceof net.minecraft.world.entity.player.Player player && isActive(stack)) {

            // Da wir sicher auf dem Server sind, casten wir das Level sicher zu ServerLevel für die Partikel
            net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) level;

            AABB area = player.getBoundingBox().inflate(MAGNET_RANGE);
            List<net.minecraft.world.entity.item.ItemEntity> items = serverLevel.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area);

            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                if (item.isAlive() && !item.hasPickUpDelay()) {

                    // Items anziehen
                    Vec3 motion = player.position().subtract(item.position()).normalize().scale(0.15);
                    item.setDeltaMovement(item.getDeltaMovement().add(motion));

                    // Partikel über das ServerLevel senden
                    serverLevel.sendParticles(
                            ParticleTypes.WITCH,
                            item.getX(), item.getY() + 0.2, item.getZ(),
                            3,
                            0.1, 0.1, 0.1,
                            0.0
                    );
                }
            }
        }
    }

    // 3. DIE HOVER-TEXT-METHODE (Phase 3: Reichweite im Tooltip)
    @Override
    public void appendHoverText(
            @NotNull net.minecraft.world.item.ItemStack stack,
            @NotNull net.minecraft.world.item.Item.TooltipContext context,
            @NotNull List<net.minecraft.network.chat.Component> tooltipComponents,
            @NotNull net.minecraft.world.item.TooltipFlag tooltipFlag
    ) {
        // Reichweite anzeigen
        tooltipComponents.add(net.minecraft.network.chat.Component.literal("§7Reichweite: §b" + (int) MagnetItem.MAGNET_RANGE + " Blöcke"));

        // Zustand anzeigen
        if (isActive(stack)) {
            tooltipComponents.add(net.minecraft.network.chat.Component.literal("§7Zustand: §aAktiv"));
        } else {
            tooltipComponents.add(net.minecraft.network.chat.Component.literal("§7Zustand: §cInaktiv"));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // --- Hilfsmethoden für NBT-Daten ---
    public boolean isActive(net.minecraft.world.item.ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.getUnsafe().getBoolean("active");
    }

    public void setMagnetState(net.minecraft.world.item.ItemStack stack, boolean active) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putBoolean("active", active);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
