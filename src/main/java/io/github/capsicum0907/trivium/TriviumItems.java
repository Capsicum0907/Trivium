package io.github.capsicum0907.trivium;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration. One entry per row of {@link PaxelMaterial} and no list of names
 * anywhere: the table is the only place a variant is written down.
 */
public final class TriviumItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Trivium.MODID);

    public static final Map<PaxelMaterial, DeferredItem<PaxelItem>> PAXELS = registerPaxels();

    private TriviumItems() {
    }

    private static Map<PaxelMaterial, DeferredItem<PaxelItem>> registerPaxels() {
        EnumMap<PaxelMaterial, DeferredItem<PaxelItem>> paxels = new EnumMap<>(PaxelMaterial.class);
        for (PaxelMaterial material : PaxelMaterial.values()) {
            paxels.put(material, ITEMS.register(material.itemName(), () -> new PaxelItem(material)));
        }
        return Collections.unmodifiableMap(paxels);
    }
}
