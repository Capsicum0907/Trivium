package io.github.capsicum0907.trivium.data;

import io.github.capsicum0907.trivium.PaxelMaterial;
import io.github.capsicum0907.trivium.Trivium;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * A model per material. The parent is {@code item/handheld} rather than
 * {@code item/generated}: that is what tilts a tool in the hand, and a paxel held
 * flat like a stick would look wrong beside the three items it replaces.
 */
public class PaxelItemModels extends ItemModelProvider {
    private static final String PARENT = "item/handheld";
    private static final String TEXTURE_FOLDER = "item/";

    public PaxelItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Trivium.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (PaxelMaterial material : PaxelMaterial.values()) {
            withExistingParent(material.itemName(), mcLoc(PARENT))
                    .texture("layer0", modLoc(TEXTURE_FOLDER + material.itemName()));
        }
    }
}
