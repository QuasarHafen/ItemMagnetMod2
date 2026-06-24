package com.finn.ItemMagnetMod2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MagnetItem extends Item {

    public MagnetItem(Properties properties) {
        super(properties.stacksTo(1)); // Magneten sollten nicht stapelbar sein
    }

    // 1. Rechtsklick zum Ein-/Ausschalten
    @Override
    public net.minecraft.world.@NonNull InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.@NonNull InteractionHand hand) {
        net.minecraft.world.item.ItemStack realStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // 2. Zustand mit dem echten Item umdrehen
            boolean currentState = isActive(realStack);
            setMagnetState(realStack, !currentState);

            // 3. Nachricht an den Spieler senden
            if (!currentState) {
                // 'true' schickt die Nachricht elegant direkt über die Hotbar (Aktionsleiste)
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMagnet aktiviert!"), true);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMagnet deaktiviert!"), true);
            }
        }

        // 4. Erfolg an Minecraft zurückmelden (steuert die Arm-Animation)
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(@NonNull ItemStack stack, net.minecraft.server.level.@NonNull ServerLevel level, @NonNull Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        // Da 'level' jetzt ein ServerLevel ist, fällt die Client-Abfrage weg!
        if (entity instanceof Player player && isActive(stack)) {

            double range = 5.0; // Reichweite von Phase 1
            AABB area = player.getBoundingBox().inflate(range);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

            for (ItemEntity item : items) {
                // Nur Items anziehen, die noch existieren und eingesammelt werden können
                if (item.isAlive() && !item.hasPickUpDelay()) {
                    // Richtungsvektor berechnen
                    Vec3 motion = player.position().subtract(item.position()).normalize().scale(0.15);
                    // Dem Item den Schubs geben
                    item.setDeltaMovement(item.getDeltaMovement().add(motion));
                }
            }
        }
    }

    // 3. Macht das Item im Inventar magisch leuchtend, wenn es aktiv ist
    @Override
    public boolean isFoil(@NonNull ItemStack stack) {
        return isActive(stack);
    }

    // Hilfsmethode zum Auslesen (angepasst für das 26.2 Optional-System)
    private boolean isActive(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            // .orElse(false) entpackt das Optional<Boolean> sicher zu einem normalen boolean
            return customData.copyTag().getBoolean("active").orElse(false);
        }
        return false;
    }

    private void setMagnetState(ItemStack stack, boolean active) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData ->
                customData.update(tag -> tag.putBoolean("active", active))
        );
    }
}