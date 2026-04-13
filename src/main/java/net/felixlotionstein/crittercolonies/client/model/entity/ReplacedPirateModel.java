package net.felixlotionstein.crittercolonies.client.model.entity;

import net.felixlotionstein.crittercolonies.Crittercolonies;
import net.felixlotionstein.crittercolonies.anim.ReplacedPirateAnim;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ReplacedPirateModel extends DefaultedEntityGeoModel<ReplacedPirateAnim> {
    public ReplacedPirateModel() {
        super(ResourceLocation.fromNamespaceAndPath(Crittercolonies.MODID, "pirate"));
    }
}
