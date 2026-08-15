package io.github.capsicum0907.trivium.data;

import io.github.capsicum0907.trivium.PaxelMaterial;
import io.github.capsicum0907.trivium.Trivium;
import io.github.capsicum0907.trivium.TriviumItems;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** English names, assembled from the same table the items are registered from. */
public class PaxelLanguage extends LanguageProvider {
    public PaxelLanguage(PackOutput output) {
        super(output, Trivium.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (PaxelMaterial material : PaxelMaterial.values()) {
            add(TriviumItems.PAXELS.get(material).get(), material.displayName());
        }
    }
}
