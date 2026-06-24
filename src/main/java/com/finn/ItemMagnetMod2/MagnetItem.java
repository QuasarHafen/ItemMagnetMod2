package com.finn.ItemMagnetMod2;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.NonNull;

public class MagnetItem extends Item {

    public MagnetItem(Properties properties) {
        super(properties.stacksTo(1)); // Magneten sollten nicht stapelbar sein
    }

    // 1. Rechtsklick zum Ein-/Ausschalten
    @Override
    public net.minecraft.world.@NonNull InteractionResult use(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.@NonNull InteractionHand hand) {
        net.minecraft.world.item.ItemStack realStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean currentState = isActive(realStack);
            setMagnetState(realStack, !currentState);

            if (!currentState) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMagnet aktiviert!"), true);
                // Ein knackiges klicken mit höherem Pitch (1.2F) für "An"
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.LEVER_CLICK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMagnet deaktiviert!"), true);
                // Dasselbe Klicken mit tiefem Pitch (0.8F) für "Aus"
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.LEVER_CLICK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            }
        }

        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(net.minecraft.world.item.@NonNull ItemStack stack, net.minecraft.server.level.@NonNull ServerLevel level, net.minecraft.world.entity.@NonNull Entity entity, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
        if (entity instanceof net.minecraft.world.entity.player.Player player && isActive(stack)) {

            double range = 5.0; // Reichweite von Phase 1
            net.minecraft.world.phys.AABB area = player.getBoundingBox().inflate(range);
            java.util.List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area);

            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                // Nur Items bearbeiten, die noch leben und eingesammelt werden können
                if (item.isAlive() && !item.hasPickUpDelay()) {

                    // 1. Richtungsvektor berechnen & Item anziehen
                    net.minecraft.world.phys.Vec3 motion = player.position().subtract(item.position()).normalize().scale(0.15);
                    item.setDeltaMovement(item.getDeltaMovement().add(motion));

                    // 2. Partikel-Spur erzeugen (Direkt auf 'level' aufrufbar, da es ein ServerLevel ist)
                    level.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.WITCH,       // Lila Magie-Partikel
                            item.getX(), item.getY() + 0.2, item.getZ(),    // Position direkt beim Item
                            3,                                                      // Anzahl der Partikel pro Tick
                            0.1, 0.1, 0.1,                          // Leicht gestreut in alle Richtungen
                            0.0                                                     // Geschwindigkeit (bleiben am Item)
                    );
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