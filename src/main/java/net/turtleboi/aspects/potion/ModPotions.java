package net.turtleboi.aspects.potion;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.effect.ModEffects;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, Aspects.MOD_ID);

    public static final RegistryObject<Potion> CHILLING_POTION = POTIONS.register("aspects_chilling_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.CHILLED.get(), 300, 0)));

    public static final RegistryObject<Potion> CHILLING_POTION2 = POTIONS.register("aspects_chilling_potion2",
            () -> new Potion(new MobEffectInstance(ModEffects.CHILLED.get(), 300, 1)));

    public static final RegistryObject<Potion> FREEZING_POTION = POTIONS.register("aspects_freezing_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.FROZEN.get(), 300, 0)));

    public static final RegistryObject<Potion> STUNNING_POTION = POTIONS.register("aspects_stunning_potion",
            () -> new Potion(new MobEffectInstance(ModEffects.STUNNED.get(), 300, 0)));

    public static void register(IEventBus eventBus){
        POTIONS.register(eventBus);
    }
}
