package net.turtleboi.aspects.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.turtleboi.aspects.Aspects;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Aspects.MOD_ID);

    public static final Holder<MobEffect> CHILLED = MOB_EFFECTS.register("chilled",
            () -> new ChilledEffect(MobEffectCategory.HARMFUL, 59903));

    public static final Holder<MobEffect> FROZEN = MOB_EFFECTS.register("frozen",
            () -> new FrozenEffect(MobEffectCategory.HARMFUL, 8752371));

    public static final Holder<MobEffect> STUNNED = MOB_EFFECTS.register("stunned",
            () -> new StunnedEffect(MobEffectCategory.HARMFUL, 13676558));

    public static final Holder<MobEffect> SAPPED = MOB_EFFECTS.register("sapped",
            () -> new SappedEffect(MobEffectCategory.HARMFUL, 6422601).addAttributeModifier(
                    Attributes.MAX_HEALTH,
                    ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "sapped_max_health"),
                    -2,
                    AttributeModifier.Operation.ADD_VALUE
            ));

    public static final Holder<MobEffect> VIGOR = MOB_EFFECTS.register("vigor",
            () -> new VigorEffect(MobEffectCategory.BENEFICIAL, 15269961).addAttributeModifier(
                    Attributes.MAX_HEALTH,
                    ResourceLocation.fromNamespaceAndPath(Aspects.MOD_ID, "vigor_max_health"),
                    2,
                    AttributeModifier.Operation.ADD_VALUE
            ));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
