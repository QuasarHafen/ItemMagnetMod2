package com.finn.ItemMagnetMod2;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ItemMagnetMod2.MODID)
public final class ItemMagnetMod2 {
    
    // Die eindeutige ID deiner Mod
    public static final String MODID = "itemmagnetmod2";
    
    // Der Logger, um Nachrichten in die Konsole zu schreiben
    private static final Logger LOGGER = LogUtils.getLogger();

    // Ein Register für all unsere Mod-Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Hier registrieren wir unseren Item-Magneten unter Nutzung unserer eigenen MagnetItem-Klasse
    public static final RegistryObject<Item> ITEM_MAGNET = ITEMS.register("item_magnet",
        () -> new MagnetItem(new Item.Properties().stacksTo(1).setId(ITEMS.key("item_magnet")))
    );

    // Der Konstruktor der Mod (hier startet alles beim Laden)
    public ItemMagnetMod2(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // Registriert die Setup-Methode
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        // Sagt Forge, dass es unsere Items laden soll
        ITEMS.register(modBusGroup);

        // Registriert das Event, um das Item in ein Kreativ-Tab einzufügen
        BuildCreativeModeTabContentsEvent.BUS.addListener(ItemMagnetMod2::addCreative);

        // Verknüpfung zur Config-Datei (falls du Einstellungen nutzen willst)
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Item-Magnet Mod: Common Setup wird geladen!");
    }

    // Fügt den Magneten dem "Werkzeuge & Hilfsmittel"-Tab im Kreativmodus hinzu
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ITEM_MAGNET);
        }
    }

    // Client-seitiger Code (Alles für den Ladebildschirm)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Wird im Ladebildschirm aufgerufen – perfekt für Registrierungen von Client-Dingen
            LOGGER.info("Finns Item-Magnet Mod: Client Setup geladen!");
        }
    }

    // Spiel-Events (Alles, was passiert, während das Spiel aktiv läuft)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void onPlayerJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            // Wir prüfen, ob ein Spieler die Welt betritt und ob wir auf dem Client sind
            if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player && event.getLevel().isClientSide()) {
                player.displayClientMessage(
                        Component.literal("§bHallo Finn! Deine Magnet-Mod wurde erfolgreich geladen. §a[Phase 1 Aktiv]"),
                        false
                );
            }
        }
    }
}