package net.turtleboi.aspects.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.potion.ModPotions;
import net.turtleboi.aspects.util.AspectUtil;
import net.turtleboi.aspects.util.ModAttributes;
import net.turtleboi.aspects.util.ModDamageSources;
import net.turtleboi.aspects.util.ModTags;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@EventBusSubscriber(modid = Aspects.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();
        int i = 5;


        if ((isRune(right)) && (left.getItem() instanceof ArmorItem)) {
            ItemStack output = left.copy();

            if (name != null && !StringUtil.isBlank(name)) {
                if (!name.equals(left.getHoverName().getString())) {
                    i += 1;
                    output.set(DataComponents.CUSTOM_NAME, Component.literal(name));
                }
            }else if (left.has(DataComponents.CUSTOM_NAME)) {
                i += 1;
                output.remove(DataComponents.CUSTOM_NAME);
            }

            ItemStack aspectRune = right.copy();
            aspectRune.setCount(1);

            String aspectData = AspectUtil.getAspect(left);
            if (aspectData == null) {
                String runeAspect = AspectUtil.getAspectFromRune(aspectRune);
                AspectUtil.setAspect(output, runeAspect);

                event.setMaterialCost(1);
                event.setOutput(output);
                event.setCost(i);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            AspectUtil.updateAspectAttributes(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker){
            if (player.getAttribute(ModAttributes.INFERNUM_ASPECT) != null) {
                double infernumAmplifier = player.getAttributeValue(ModAttributes.INFERNUM_ASPECT);
                if (infernumAmplifier > 0 && player.level().getRandom().nextDouble() < 0.35 + (0.1 * infernumAmplifier)) {
                    FireAuraRenderer.addAuraForEntity(player, System.currentTimeMillis(), 30, infernumAmplifier);

                    player.level().playSound(
                            null,
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            SoundEvents.FIRECHARGE_USE,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f)
                    );

                    double radius = 1 + infernumAmplifier;
                    List<LivingEntity> livingEntities = player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            player.getBoundingBox().inflate(radius),
                            e -> e != player
                    );

                    for (LivingEntity livingEntity : livingEntities) {
                        setIgnitor(livingEntity, player);
                        livingEntity.igniteForTicks((int) (60 * infernumAmplifier));
                        if (infernumAmplifier > 1) {
                            livingEntity.hurt(player.damageSources().onFire(), 1);
                            livingEntity.setLastHurtByPlayer(player);
                        }
                    }
                }
            }

            if (player.getAttribute(ModAttributes.GLACIUS_ASPECT) != null) {
                double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                if (glaciusAmplifier > 0 && player.level().getRandom().nextDouble() < 0.35 + (0.1 * glaciusAmplifier)) {

                    player.level().playSound(
                            null,
                            attacker.getX(),
                            attacker.getY(),
                            attacker.getZ(),
                            SoundEvents.PLAYER_HURT_FREEZE,
                            SoundSource.PLAYERS,
                            1.0F,
                            0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f)
                    );
                    setChiller(attacker, player);
                    attacker.addEffect(new MobEffectInstance(ModEffects.CHILLED, (int) (40 * glaciusAmplifier), (int) glaciusAmplifier - 1));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMobHurt(LivingDamageEvent.Post event) {
        LivingEntity hurtEntity = event.getEntity();
        if (hurtEntity instanceof LivingEntity livingEntity){
            if (livingEntity.isOnFire()) {
                List<Player> players = livingEntity.level().getEntitiesOfClass(
                        Player.class,
                        livingEntity.getBoundingBox().inflate(25.0),
                        e -> e != livingEntity
                );

                for (Player player : players) {
                    String ignitedBy = livingEntity.getPersistentData().getString("IgnitedBy");
                    if (event.getSource().type() == livingEntity.level().damageSources().onFire().type()
                            && ignitedBy.equals(player.getUUID().toString()) && player.getAttribute(ModAttributes.INFERNUM_ASPECT) != null) {
                        double infernumAmplifier = player.getAttributeValue(ModAttributes.INFERNUM_ASPECT);
                        if (infernumAmplifier > 0) {
                            //System.out.println("Fire damage amplified by " + infernumAmplifier);
                            livingEntity.hurt(player.damageSources().magic(), (float) (1f * (infernumAmplifier)));
                        }
                    }
                }
            }

            if (hurtEntity.hasEffect(ModEffects.FROZEN) && !event.getSource().typeHolder().equals(ModDamageSources.frozenDamageType(hurtEntity.level()))){
                MobEffectInstance effectInstance = hurtEntity.getEffect(ModEffects.FROZEN);
                hurtEntity.removeEffect(ModEffects.FROZEN);
                hurtEntity.addEffect(new MobEffectInstance(
                        ModEffects.FROZEN,
                        3,
                        effectInstance.getAmplifier(),
                        effectInstance.isAmbient(),
                        effectInstance.isVisible(),
                        effectInstance.showIcon()
                ));
                float entityMaxHealth = hurtEntity.getMaxHealth();
                int maxDamage = 10;
                if (event.getSource().getEntity() instanceof LivingEntity attacker
                        && attacker.getAttribute(ModAttributes.GLACIUS_ASPECT) != null){
                    double glaciusAmplifier = attacker.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                    maxDamage = (int) (10 + (10 * (glaciusAmplifier / 4)));
                }

                if (hurtEntity.getPersistentData().contains("Frozen") && hurtEntity.getPersistentData().getBoolean("Frozen")) {
                    hurtEntity.getPersistentData().remove("Frozen");
                    System.out.println("Dealing " + maxDamage + " damage to " + hurtEntity);
                    hurtEntity.hurt(ModDamageSources.frozenDamage(hurtEntity.level(), null, null), Math.min(maxDamage, entityMaxHealth / 4));
                    hurtEntity.level().playSound(
                            null,
                            hurtEntity.getX(),
                            hurtEntity.getY(),
                            hurtEntity.getZ(),
                            SoundEvents.GLASS_BREAK,
                            SoundSource.AMBIENT,
                            1.25F,
                            0.4f / (hurtEntity.level().getRandom().nextFloat() * 0.4f + 0.8f)
                    );

                    double entitySize = hurtEntity.getBbHeight() * hurtEntity.getBbWidth();
                    System.out.println("Spawning " + (entitySize * 60) + " particles for " + hurtEntity.getName());
                    for (int i = 0; i < entitySize * 60; i++) {
                        double offX = (hurtEntity.level().random.nextDouble() - 0.5) * 0.5;
                        double offY = hurtEntity.getBbHeight() * hurtEntity.level().random.nextDouble();
                        double offZ = (hurtEntity.level().random.nextDouble() - 0.5) * 0.5;
                        ParticleOptions particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());
                        ResourceLocation particleKey = BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType());
                        ParticleData.spawnParticle(
                                particleKey,
                                hurtEntity.getX() + offX,
                                hurtEntity.getY() + offY,
                                hurtEntity.getZ() + offZ);
                    }
                }
            }

            if (event.getSource().getEntity() instanceof Player player) {
                //FireAuraRenderer.addAuraForEntity(hurtEntity, System.currentTimeMillis(), 30, 2);

                if (event.getSource() == livingEntity.damageSources().onFire()){
                    setIgnitor(livingEntity, player);
                }

                ItemStack itemStack = player.getMainHandItem();
                ItemEnchantments enchantsData = itemStack.get(DataComponents.ENCHANTMENTS);
                if (enchantsData instanceof ItemEnchantments enchantments) {
                    int flameLevel = enchantments.entrySet().stream()
                            .filter(entry -> entry.getKey().is(Enchantments.FLAME))
                            .mapToInt(Map.Entry::getValue)
                            .findFirst().orElse(0);

                    int fireAspectLevel = enchantments.entrySet().stream()
                            .filter(entry -> entry.getKey().is(Enchantments.FIRE_ASPECT))
                            .mapToInt(Map.Entry::getValue)
                            .findFirst().orElse(0);
                    if (flameLevel > 0 || fireAspectLevel > 0) {
                        setIgnitor(livingEntity, player);
                    }
                }

                if (player.getAttribute(ModAttributes.GLACIUS_ASPECT) != null) {
                    double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                    if (glaciusAmplifier > 0 && player.level().getRandom().nextDouble() < 0.35 + (0.1 * glaciusAmplifier)) {
                        player.level().playSound(
                                null,
                                hurtEntity.getX(),
                                hurtEntity.getY(),
                                hurtEntity.getZ(),
                                SoundEvents.PLAYER_HURT_FREEZE,
                                SoundSource.PLAYERS,
                                1.0F,
                                0.4f / (hurtEntity.level().getRandom().nextFloat() * 0.4f + 0.8f)
                        );
                        setChiller(livingEntity, player);
                        hurtEntity.addEffect(new MobEffectInstance(ModEffects.CHILLED, (int) (40 * glaciusAmplifier), (int) glaciusAmplifier - 1));
                    }
                }

                if (player.getAttribute(ModAttributes.TEMPESTUS_ASPECT) != null) {
                    double tempestasAmplifier = player.getAttributeValue(ModAttributes.TEMPESTUS_ASPECT);
                    if (tempestasAmplifier > 0 && player.level().getRandom().nextDouble() < 0.05 + (0.05 * tempestasAmplifier)) {
                        player.level().playSound(
                                null,
                                hurtEntity.getX(),
                                hurtEntity.getY(),
                                hurtEntity.getZ(),
                                SoundEvents.LIGHTNING_BOLT_THUNDER,
                                SoundSource.PLAYERS,
                                1.0F,
                                0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f)
                        );
                        setStunner(livingEntity, player);
                        hurtEntity.addEffect(new MobEffectInstance(ModEffects.STUNNED, (int) (40 * tempestasAmplifier), (int) tempestasAmplifier - 1));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEntityTick(EntityTickEvent.Post event){
        if (event.getEntity() instanceof LivingEntity livingEntity){
            if (!livingEntity.isOnFire() && livingEntity.getPersistentData().contains("IgnitedBy")) {
                //System.out.println(livingEntity + " extinguished!");
                livingEntity.getPersistentData().remove("IgnitedBy");
            }
        }
    }

    @SubscribeEvent
    public static void onPotionEffectApplied(MobEffectEvent.Added event) {
        LivingEntity livingEntity = event.getEntity();
        Entity sourceEntity = event.getEffectSource();
        MobEffectInstance effectInstance = event.getEffectInstance();
        MobEffectInstance oldInstance = event.getOldEffectInstance();

        double arcaniAmplifier = 0;
        if (sourceEntity instanceof Player player) {
            if (player.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                arcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
            }
        } else {
            if (livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                arcaniAmplifier = livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
            }
        }

        if (arcaniAmplifier > 0) {
            double arcaneFactor = 1 + (arcaniAmplifier / 4.0);
            double multiplier = 1;
            boolean beneficialEffect = effectInstance.getEffect().value().isBeneficial();
            if (sourceEntity == livingEntity) {
                if (beneficialEffect) {
                    multiplier = arcaneFactor;
                }
            } else {
                boolean sourceIsHostile = sourceEntity instanceof Monster;
                boolean targetIsPlayer = livingEntity instanceof Player;
                boolean targetIsHostile = livingEntity instanceof Monster;
                if (sourceEntity instanceof Player) {
                    if (beneficialEffect) {
                        if (targetIsHostile){
                            livingEntity.removeEffect(effectInstance.getEffect());
                        }
                    } else {
                        multiplier = arcaneFactor;
                    }
                } else if (sourceIsHostile) {
                    if (beneficialEffect) {
                        if (targetIsHostile){
                            multiplier = arcaneFactor;
                        } else if (targetIsPlayer) {
                            livingEntity.removeEffect(effectInstance.getEffect());
                        }
                    }
                }else {
                    if (beneficialEffect) {
                        if (!targetIsHostile){
                            multiplier = arcaneFactor;
                        } else {
                            livingEntity.removeEffect(effectInstance.getEffect());
                        }
                    }
                }
            }

            int newDuration = (int) (effectInstance.getDuration() * multiplier);
            System.out.println("Multiplier: " + multiplier);
            if (multiplier > 1) {
                System.out.println("Increasing potion duration by " + ((multiplier - 1) * 100) + "% and total duration: " + (newDuration / 20) + " seconds");
                effectInstance.update(new MobEffectInstance(
                        effectInstance.getEffect(),
                        newDuration,
                        effectInstance.getAmplifier(),
                        effectInstance.isAmbient(),
                        effectInstance.isVisible(),
                        effectInstance.showIcon()
                ));
            }
        }

        if (effectInstance.getEffect() == ModEffects.CHILLED) {
            if (oldInstance != null) {
                int additional = (effectInstance.getAmplifier() == 0) ? 1 : effectInstance.getAmplifier() + 1;
                int newAmplifier = oldInstance.getAmplifier() + additional;
                int newDuration = Math.max(oldInstance.getDuration(), effectInstance.getDuration());
                effectInstance.update(new MobEffectInstance(
                        ModEffects.CHILLED,
                        newDuration,
                        newAmplifier,
                        oldInstance.isAmbient(),
                        oldInstance.isVisible(),
                        oldInstance.showIcon()
                ));
            }
        }

        if (event.getEffectInstance().getEffect() == ModEffects.FROZEN) {
            setFrozen(livingEntity);
        }
    }

    @SubscribeEvent
    public static void onBrewingRecipeRegister(RegisterBrewingRecipesEvent event){
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.AWKWARD, Items.BLUE_ICE, ModPotions.CHILLING_POTION);
        builder.addMix(ModPotions.CHILLING_POTION, Items.GLOWSTONE_DUST, ModPotions.CHILLING_POTION2);
        builder.addMix(ModPotions.CHILLING_POTION2, ModItems.GLACIUS_RUNE.get(), ModPotions.FREEZING_POTION);
        builder.addMix(Potions.THICK, ModItems.TEMPESTAS_RUNE.get(), ModPotions.STUNNING_POTION);
    }

    private static boolean isRune(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.RUNE_ITEMS);
    }

    private static void setIgnitor(LivingEntity livingEntity, Player player){
        if (!livingEntity.getPersistentData().contains("IgnitedBy")) {
            //System.out.println(livingEntity + " ignited by " + player + "!");
            livingEntity.getPersistentData().putString("IgnitedBy", player.getUUID().toString());
        }
    }

    private static void setChiller(LivingEntity livingEntity, Player player){
        if (!livingEntity.getPersistentData().contains("ChilledBy")) {
            //System.out.println(livingEntity + " chilled by " + player + "!");
            livingEntity.getPersistentData().putString("ChilledBy", player.getUUID().toString());
        }
    }

    private static void setFrozen(LivingEntity livingEntity){
        if (!livingEntity.getPersistentData().contains("Frozen")) {
            System.out.println(livingEntity + " is frozen!");
            livingEntity.getPersistentData().putBoolean("Frozen", true);
        }
    }

    private static void setStunner(LivingEntity livingEntity, Player player){
        if (!livingEntity.getPersistentData().contains("StunnedBy")) {
            //System.out.println(livingEntity + " stunned by " + player + "!");
            livingEntity.getPersistentData().putString("StunnedBy", player.getUUID().toString());
        }
    }
}
