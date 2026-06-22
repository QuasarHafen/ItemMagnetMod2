package com.finn.ItemMagnetMod2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class MagnetItem extends Item {

    public MagnetItem(Properties properties) {
        super(properties);
    }
    
    // Diese Methode wird jede Sekunde 20-mal aufgerufen, wenn das Item im Inventar liegt
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, EquipmentSlot slot, int slotId) {
        // Die Logik soll nur auf dem Server berechnet werden und der Träger muss ein Spieler sein
        if (!level.isClientSide() && entity instanceof Player player) {
            
            // Der Magnet funktioniert, wenn er ausgewählt ist (Haupthand) oder in der offhand liegt
        	if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
                
                double reichweite = 6.0; // Radius von 6 Blöcken
                
                // Eine unsichtbare Box um den Spieler herum definieren
                AABB suchBox = player.getBoundingBox().inflate(reichweite);
                
                // Alle Items auf dem Boden innerhalb dieser Box finden
                List<ItemEntity> gefundeneItems = level.getEntitiesOfClass(ItemEntity.class, suchBox);
                
                for (ItemEntity itemEntity : gefundeneItems) {
                    if (itemEntity.isAlive() && !itemEntity.hasPickUpDelay()) {

                        Vec3 spielerPos = player.position().add(0, 0.5, 0);
                        Vec3 itemPos = itemEntity.position();

                        Vec3 richtung = spielerPos.subtract(itemPos).normalize();

                        double geschwindigkeit = 0.25;
                        itemEntity.setDeltaMovement(richtung.scale(geschwindigkeit));
                        itemEntity.hurtMarked = true;
                    }
                }
            }
        }

        super.inventoryTick(stack, level, entity, slot, slotId);
    }
}