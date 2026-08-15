package io.github.capsicum0907.trivium;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;

/**
 * Entry point. {@link #MODID} must match {@code mod_id} in gradle.properties,
 * which is what the generated neoforge.mods.toml is filled from.
 *
 * <p>There is no config. What a paxel mines and how hard lives in its
 * {@link net.minecraft.world.item.component.Tool} data component, and components
 * are baked when the item is registered — long before a server config is read.
 * A setting here would silently do nothing.
 */
@Mod(Trivium.MODID)
public class Trivium {
    public static final String MODID = "trivium";

    private static final Logger LOGGER = LogUtils.getLogger();

    public Trivium(IEventBus modEventBus, ModContainer modContainer) {
        TriviumItems.ITEMS.register(modEventBus);
        modEventBus.register(this);

        LOGGER.info("Trivium {} loaded.", modContainer.getModInfo().getVersion());
    }

    @SubscribeEvent
    public void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.TOOLS_AND_UTILITIES) {
            return;
        }
        // Enum order, which is the tier order the table is written in.
        for (PaxelMaterial material : PaxelMaterial.values()) {
            event.accept(TriviumItems.PAXELS.get(material));
        }
    }
}
