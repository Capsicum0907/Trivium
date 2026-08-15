package io.github.capsicum0907.trivium.data;

import io.github.capsicum0907.trivium.Trivium;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Everything under {@code src/generated/resources} comes from here. Nothing in
 * that directory is written by hand, so a change to the material table reaches
 * models, recipes and language in one pass instead of three.
 */
@EventBusSubscriber(modid = Trivium.MODID, value = { Dist.CLIENT, Dist.DEDICATED_SERVER })
public final class TriviumDataGen {
    private TriviumDataGen() {
    }

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new PaxelItemModels(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new PaxelLanguage(output));
        generator.addProvider(event.includeServer(), new PaxelRecipes(output, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new TestStructures(output));
    }
}
