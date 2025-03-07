package net.turtleboi.aspects.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
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
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.effect.ModEffects;
import net.turtleboi.aspects.item.ModItems;
import net.turtleboi.aspects.network.payloads.FrozenData;
import net.turtleboi.aspects.network.payloads.ParticleData;
import net.turtleboi.aspects.potion.ModPotions;
import net.turtleboi.aspects.util.*;

import java.util.*;

@EventBusSubscriber(modid = Aspects.MOD_ID)
public class   ModEvents {
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();
        int i = 30;

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
            double arcaniAmplifier = 0;
            if (player.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                arcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
            }
            double arcaneFactor = 1 + (arcaniAmplifier / 4.0);

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
                        int ignitionTime = (int) ((60 * infernumAmplifier) * arcaneFactor);
                        System.out.println("Igniting " + livingEntity.getName() + " for " + ignitionTime / 20 + " seconds");
                        if (arcaneFactor > 1) {
                            int upgradedTicks = (int) (ignitionTime * arcaneFactor);
                            if (livingEntity.isOnFire()) {
                                int currentTicks = livingEntity.getRemainingFireTicks();
                                livingEntity.setRemainingFireTicks(currentTicks + upgradedTicks);
                                System.out.println(
                                        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                                (currentTicks + upgradedTicks) / 20 + " seconds"
                                );
                            } else {
                                livingEntity.igniteForTicks(upgradedTicks);
                                System.out.println(
                                        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                                (upgradedTicks) / 20 + " seconds"
                                );
                            }
                        } else {
                            if (livingEntity.isOnFire()) {
                                int currentTicks = livingEntity.getRemainingFireTicks();
                                livingEntity.setRemainingFireTicks(currentTicks + ignitionTime);
                                System.out.println(
                                        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                                (currentTicks + ignitionTime) / 20 + " seconds"
                                );
                            } else {
                                livingEntity.igniteForTicks(ignitionTime);
                            }
                        }

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
                    ColdAuraRenderer.addAuraForEntity(player, System.currentTimeMillis(), 30, glaciusAmplifier);
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
                    RandomSource random = player.level().getRandom();
                    int count = (int) ((glaciusAmplifier + 1) * 20);
                    for (int i = 0; i < count; i++) {
                        double theta = random.nextDouble() * Math.PI;
                        double phi = random.nextDouble() * 2 * Math.PI;
                        double speed = 0.2 + random.nextDouble() * 0.3;
                        double xSpeed = speed * Math.sin(theta) * Math.cos(phi);
                        double ySpeed = speed * Math.cos(theta);
                        double zSpeed = speed * Math.sin(theta) * Math.sin(phi);
                        double offX = (random.nextDouble() - 0.5) * 0.2;
                        double offY = player.getBbHeight() / 2;
                        double offZ = (random.nextDouble() - 0.5) * 0.2;

                        ParticleData.spawnParticle(
                                ParticleTypes.SNOWFLAKE,
                                player.getX() + offX,
                                player.getY() + offY,
                                player.getZ() + offZ,
                                xSpeed,ySpeed,zSpeed);
                    }
                    setChiller(attacker, player);
                    int chillTicks = (int) ((40 * glaciusAmplifier) * arcaneFactor);
                    attacker.addEffect(new MobEffectInstance(ModEffects.CHILLED, chillTicks, (int) glaciusAmplifier - 1));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMobHurt(LivingDamageEvent.Post event) {
        LivingEntity hurtEntity = event.getEntity();
        if (hurtEntity instanceof LivingEntity livingEntity){
            double hurtArcaniAmplifier = 0;
            if (livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                hurtArcaniAmplifier = livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
            }
            double hurtArcaniFactor = 1 + (hurtArcaniAmplifier / 4.0);

            if (livingEntity.isOnFire()) {
                List<Player> players = livingEntity.level().getEntitiesOfClass(
                        Player.class,
                        livingEntity.getBoundingBox().inflate(25.0),
                        e -> e != livingEntity
                );

                for (Player player : players) {
                    if (!livingEntity.getPersistentData().hasUUID("IgnitedBy")){
                        return;
                    }

                    double playerArcaniAmplifier = 0;
                    if (player.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                        playerArcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
                    } else if (player == hurtEntity) {
                        playerArcaniAmplifier = 0;
                    }
                    double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                    UUID ignitedBy = livingEntity.getPersistentData().getUUID("IgnitedBy");
                    if (event.getSource().type() == livingEntity.level().damageSources().onFire().type()
                            && ignitedBy.equals(player.getUUID()) && player.getAttribute(ModAttributes.INFERNUM_ASPECT) != null) {
                        double infernumAmplifier = player.getAttributeValue(ModAttributes.INFERNUM_ASPECT);
                        if (infernumAmplifier > 0) {
                            //System.out.println("Fire damage amplified by " + infernumAmplifier);
                            livingEntity.hurt(
                                    player.damageSources().magic(),
                                    (float) (((1f * (infernumAmplifier)) * playerArcaniFactor) / hurtArcaniFactor));
                            if (player.getAttribute(ModAttributes.GLACIUS_ASPECT) != null && player.getAttributeValue(ModAttributes.GLACIUS_ASPECT) > 0) {
                                double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                                setChiller(livingEntity, player);
                                hurtEntity.addEffect(
                                        new MobEffectInstance(
                                                ModEffects.CHILLED,
                                                (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                                (int) glaciusAmplifier - 1));
                            }
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
                    double attackerArcaniAmplifier = 0;
                    if (attacker.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                        attackerArcaniAmplifier = attacker.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
                    } else if (attacker == hurtEntity) {
                        attackerArcaniAmplifier = 0;
                    }
                    double attackerArcaniFactor = 1 + (attackerArcaniAmplifier / 4.0);
                    maxDamage = (int) ((10 + (10 * (glaciusAmplifier / 4) * attackerArcaniFactor) / hurtArcaniFactor));
                }

                //System.out.println("Dealing " + maxDamage + " damage to " + hurtEntity);
                hurtEntity.hurt(
                        ModDamageSources.frozenDamage(
                                hurtEntity.level(),
                                null,
                                null),
                        Math.min(maxDamage, entityMaxHealth / 4));
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
                //System.out.println("Spawning " + (entitySize * 60) + " particles for " + hurtEntity.getName());
                RandomSource random = hurtEntity.level().getRandom();
                int count = (int) (entitySize * 60);
                for (int i = 0; i < count; i++) {
                    double offX = (random.nextDouble() - 0.5) * 0.5;
                    double offY = hurtEntity.getBbHeight() / 2;
                    double offZ = (random.nextDouble() - 0.5) * 0.5;
                    double theta = random.nextDouble() * Math.PI;
                    double phi = random.nextDouble() * 2 * Math.PI;
                    double speed = 0.5 + random.nextDouble() * 0.5;
                    double xSpeed = speed * Math.sin(theta) * Math.cos(phi);
                    double ySpeed = speed * Math.cos(theta);
                    double zSpeed = speed * Math.sin(theta) * Math.sin(phi);
                    ParticleOptions particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState());

                    ParticleData.spawnParticle(
                            particle,
                            hurtEntity.getX() + offX,
                            hurtEntity.getY() + offY,
                            hurtEntity.getZ() + offZ,
                            xSpeed, ySpeed, zSpeed);
                }
            }

            if (event.getSource().getEntity() instanceof Player player) {
                double playerArcaniAmplifier = 0;
                if (player.getAttribute(ModAttributes.ARCANI_ASPECT) != null) {
                    playerArcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT).getValue();
                }
                double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                if (event.getSource() == livingEntity.damageSources().onFire()){
                    setIgnitor(livingEntity, player);
                    if (player.getAttribute(ModAttributes.GLACIUS_ASPECT) != null) {
                        double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                        setChiller(livingEntity, player);
                        hurtEntity.addEffect(
                                new MobEffectInstance(
                                        ModEffects.CHILLED,
                                        (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                        (int) glaciusAmplifier - 1));
                    }
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
                        int ignitionTime = 0;
                        if (flameLevel > 0){
                            ignitionTime = 100 * flameLevel;
                        } else if (fireAspectLevel > 0){
                            ignitionTime = 80 * fireAspectLevel;
                        }
                        //System.out.println("Igniting " + hurtEntity.getName() + " for " + ignitionTime / 20 + " seconds");
                        if (playerArcaniFactor > 1) {
                            int upgradedTicks = (int) (ignitionTime * playerArcaniFactor);
                            if (livingEntity.isOnFire()) {
                                int currentTicks = livingEntity.getRemainingFireTicks();
                                livingEntity.setRemainingFireTicks(currentTicks + upgradedTicks);
                                //System.out.println(
                                //        "Increasing ignition time for " + hurtEntity.getName() + " to " +
                                //                (currentTicks + upgradedTicks) / 20 + " seconds"
                                //);
                            } else {
                                livingEntity.igniteForTicks(upgradedTicks);
                                //System.out.println(
                                //        "Increasing ignition time for " + hurtEntity.getName() + " to " +
                                //                (upgradedTicks) / 20 + " seconds"
                                //);
                            }
                        }
                    }
                }

                if (player.getAttribute(ModAttributes.GLACIUS_ASPECT) != null) {
                    double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT);
                    if (glaciusAmplifier > 0 && player.level().getRandom().nextDouble() < 0.5 + (0.05 * glaciusAmplifier)) {
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
                        hurtEntity.addEffect(
                                new MobEffectInstance(
                                        ModEffects.CHILLED,
                                        (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                        (int) glaciusAmplifier - 1));
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
                        //System.out.println(
                        //        Component.translatable(
                        //                livingEntity.getName().getString()) +
                        //                " stunned by " + player + " for " +
                        //                (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor) / 20 + " seconds!"
                        //);
                        hurtEntity.addEffect(
                                new MobEffectInstance(
                                        ModEffects.STUNNED,
                                        (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                        (int) tempestasAmplifier - 1));
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
    public static void onPotionEffectAdded(MobEffectEvent.Added event) {
        LivingEntity livingEntity = event.getEntity();
        Entity sourceEntity = event.getEffectSource();
        MobEffectInstance effectInstance = event.getEffectInstance();
        MobEffectInstance oldInstance = event.getOldEffectInstance();
        //System.out.println(
        //        Component.translatable(
        //                "Added Effect - " +
        //                livingEntity.getName() +
        //                " just got " +
        //                effectInstance.getDescriptionId() +
        //                " for " + effectInstance.getDuration() / 20 + " seconds"));

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
                            return;
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
                            return;
                        }
                    }
                }else {
                    if (beneficialEffect) {
                        if (!targetIsHostile){
                            multiplier = arcaneFactor;
                        } else {
                            livingEntity.removeEffect(effectInstance.getEffect());
                            return;
                        }
                    }
                }
            }

            int newDuration = (int) (effectInstance.getDuration() * multiplier);
            //System.out.println("Added Effect - Multiplier: " + multiplier);
            if (multiplier > 1) {
                //System.out.println("Increasing potion duration by " + ((multiplier - 1) * 100) + "% and total duration: " + (newDuration / 20) + " seconds");
                effectInstance.update(new MobEffectInstance(
                        effectInstance.getEffect(),
                        newDuration,
                        (int) (effectInstance.getAmplifier() + (arcaniAmplifier / 2)),
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
                int newDuration = oldInstance.getDuration() + effectInstance.getDuration();
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
            if (livingEntity.getPersistentData().contains("ChilledBy")) {
                setFrozen(livingEntity, livingEntity.getPersistentData().getUUID("ChilledBy"));
            }
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

    private static void setIgnitor(LivingEntity livingEntity, Entity ignitor){
        if (!livingEntity.getPersistentData().contains("IgnitedBy")) {
            //System.out.println(livingEntity + " ignited by " + ignitor + "!");
            livingEntity.getPersistentData().putUUID("IgnitedBy", ignitor.getUUID());
        }
    }

    private static void setChiller(LivingEntity livingEntity, Entity chiller){
        if (!livingEntity.getPersistentData().contains("ChilledBy")) {
            //System.out.println(livingEntity + " chilled by " + chiller + "!");
            livingEntity.getPersistentData().putUUID("ChilledBy", chiller.getUUID());
        }
    }

    private static void setFrozen(LivingEntity livingEntity, UUID uuid){
        if (!livingEntity.getPersistentData().contains("FrozenBy")) {
            //System.out.println(livingEntity + " was frozen by" + uuid.toString() + "!");
            livingEntity.getPersistentData().putUUID("FrozenBy", uuid);
        }
    }

    private static void setStunner(LivingEntity livingEntity, Entity stunner){
        if (!livingEntity.getPersistentData().contains("StunnedBy")) {
            //System.out.println(livingEntity + " stunned by " + stunner + "!");
            livingEntity.getPersistentData().putUUID("StunnedBy", stunner.getUUID());
        }
    }
}
