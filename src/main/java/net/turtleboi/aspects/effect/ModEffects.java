package net.turtleboi.aspects.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.effect.custom.*;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Aspects.MOD_ID);

    public static final RegistryObject<MobEffect> CHILLED = MOB_EFFECTS.register("chilled",
            () -> new ChilledEffect(MobEffectCategory.HARMFUL, 59903));

    public static final RegistryObject<MobEffect> FROZEN = MOB_EFFECTS.register("frozen",
            () -> new FrozenEffect(MobEffectCategory.HARMFUL, 8752371));

    public static final RegistryObject<MobEffect> STUNNED = MOB_EFFECTS.register("stunned",
            () -> new StunnedEffect(MobEffectCategory.HARMFUL, 13676558));

    public static final RegistryObject<MobEffect> SAPPED = MOB_EFFECTS.register("sapped",
            () -> new SappedEffect(MobEffectCategory.HARMFUL, 6422601));

    public static final RegistryObject<MobEffect> VIGOR = MOB_EFFECTS.register("vigor",
            () -> new VigorEffect(MobEffectCategory.BENEFICIAL, 15269961));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
