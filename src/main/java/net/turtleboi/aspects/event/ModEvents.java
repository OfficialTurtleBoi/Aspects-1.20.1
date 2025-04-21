package net.turtleboi.aspects.event;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.ArcaneAuraRenderer;
import net.turtleboi.aspects.client.renderer.ColdAuraRenderer;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.client.renderer.ShockwaveRenderer;
import net.turtleboi.aspects.util.AspectUtil;
import net.turtleboi.aspects.util.ModAttributes;
import net.turtleboi.aspects.util.ModTags;
import net.turtleboi.turtlecore.effect.CoreEffects;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.SendParticlesS2C;
import net.turtleboi.turtlecore.network.packet.util.SendSoundS2C;
import net.turtleboi.turtlecore.util.EffectApplicationUtil;

import java.util.*;

@Mod.EventBusSubscriber(modid = Aspects.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        String name = event.getName();
        int cost = 30;

        if (isRune(right) && left.getItem() instanceof ArmorItem) {
            ItemStack output = left.copy();

            if (name != null && !StringUtil.isNullOrEmpty(name)) {
                if (!name.equals(left.getHoverName().getString())) {
                    cost += 1;
                    output.setHoverName(Component.literal(name));
                }
            } else if (left.hasCustomHoverName()) {
                cost += 1;
                output.resetHoverName();
            }

            ItemStack aspectRune = right.copy();
            aspectRune.setCount(1);

            String aspectData = AspectUtil.getAspect(left);
            if (aspectData == null) {
                String runeAspect = AspectUtil.getAspectFromRune(aspectRune);
                AspectUtil.setAspect(output, runeAspect);
                //System.out.println("Giving " + output + " the aspect of " + runeAspect);
                event.setMaterialCost(1);
                event.setOutput(output);
                event.setCost(cost);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        AspectUtil.updateAspectAttributes(event.player);
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingDamageEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource().getEntity() instanceof LivingEntity attacker) {
            double arcaniAmplifier = 0;
            if (player.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                arcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
            double arcaneFactor = 1 + (arcaniAmplifier / 4.0);

            if (player.getAttribute(ModAttributes.INFERNUM_ASPECT.get()) != null) {
                double infernumAmplifier = player.getAttributeValue(ModAttributes.INFERNUM_ASPECT.get());
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
                        EffectApplicationUtil.setIgnitor(livingEntity, player);
                        int ignitionTime = (int) ((60 * infernumAmplifier) * arcaneFactor);
                        //System.out.println("Igniting " + livingEntity.getName() + " for " + ignitionTime / 20 + " seconds");
                        if (arcaneFactor > 1) {
                            int upgradedTicks = (int) (ignitionTime * arcaneFactor);
                            if (livingEntity.isOnFire()) {
                                int currentTicks = livingEntity.getRemainingFireTicks();
                                livingEntity.setRemainingFireTicks(currentTicks + upgradedTicks);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (currentTicks + upgradedTicks) / 20 + " seconds"
                                //);
                            } else {
                                livingEntity.setSecondsOnFire(upgradedTicks / 20);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (upgradedTicks) / 20 + " seconds"
                                //);
                            }
                        } else {
                            if (livingEntity.isOnFire()) {
                                int currentTicks = livingEntity.getRemainingFireTicks();
                                livingEntity.setRemainingFireTicks(currentTicks + ignitionTime);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (currentTicks + ignitionTime) / 20 + " seconds"
                                //);
                            } else {
                                livingEntity.setSecondsOnFire(ignitionTime / 20);
                            }
                        }

                        if (infernumAmplifier > 1) {
                            livingEntity.hurt(player.damageSources().onFire(), 1);
                            livingEntity.setLastHurtByPlayer(player);
                        }
                    }
                }
            }

            if (player.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null) {
                double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                if (glaciusAmplifier > 0 && player.level().getRandom().nextDouble() < 0.35 + (0.1 * glaciusAmplifier)) {
                    ColdAuraRenderer.addAuraForEntity(player, System.currentTimeMillis(), 30, (int) glaciusAmplifier);
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

                        CoreNetworking.sendToNear(new SendParticlesS2C(
                                ParticleTypes.SNOWFLAKE,
                                player.getX() + offX,
                                player.getY() + offY,
                                player.getZ() + offZ,
                                xSpeed, ySpeed, zSpeed), player);
                    }
                    EffectApplicationUtil.setChiller(attacker, player);
                    int chillTicks = (int) ((40 * glaciusAmplifier) * arcaneFactor);
                    attacker.addEffect(new MobEffectInstance(CoreEffects.CHILLED.get(), chillTicks, (int) glaciusAmplifier - 1));
                }
            }

            if (player.getAttribute(ModAttributes.TERRA_ASPECT.get()) != null) {
                double terraAmplifier = player.getAttributeValue(ModAttributes.TERRA_ASPECT.get());
                if (terraAmplifier > 0) {
                    if (player.getAttribute(ModAttributes.TEMPESTUS_ASPECT.get()) != null) {
                        double tempestasAmplifier = player.getAttributeValue(ModAttributes.TEMPESTUS_ASPECT.get());
                        if (tempestasAmplifier > 0 && player.level().getRandom().nextDouble() < 1 + (0.05 * ((terraAmplifier + tempestasAmplifier) * arcaneFactor))) {
                            ShockwaveRenderer.triggerShockwave(player, (int) ((terraAmplifier + tempestasAmplifier) * arcaneFactor));

                            AABB area = new AABB(
                                    player.getX() - ((terraAmplifier + tempestasAmplifier) * arcaneFactor),
                                    player.getY() - ((terraAmplifier + tempestasAmplifier) * arcaneFactor),
                                    player.getZ() - ((terraAmplifier + tempestasAmplifier) * arcaneFactor),
                                    player.getX() + ((terraAmplifier + tempestasAmplifier) * arcaneFactor),
                                    player.getY() + ((terraAmplifier + tempestasAmplifier) * arcaneFactor),
                                    player.getZ() + ((terraAmplifier + tempestasAmplifier) * arcaneFactor));
                            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player);
                            if (!entities.isEmpty()) {
                                for (LivingEntity target : entities) {
                                    double dx = player.getX() - target.getX();
                                    double dz = player.getZ() - target.getZ();
                                    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

                                    double knockbackFactor = 1.0 / (horizontalDistance + 0.5);
                                    double maxStrength = (terraAmplifier + tempestasAmplifier) * arcaneFactor;
                                    double horizontalStrength = maxStrength * knockbackFactor * (0.9 + player.level().getRandom().nextDouble() * 0.2);

                                    double normX = horizontalDistance != 0 ? dx / horizontalDistance : 0;
                                    double normZ = horizontalDistance != 0 ? dz / horizontalDistance : 0;

                                    double baseVertical = 0.1;
                                    double maxDistance = 5.0;
                                    double verticalBonus = maxStrength * 0.5 * (1.0 - Mth.clamp(horizontalDistance / maxDistance, 0, 1));
                                    verticalBonus *= (0.9 + player.level().getRandom().nextDouble() * 0.2);
                                    double verticalStrength = baseVertical + verticalBonus;

                                    Vec3 knockback = new Vec3(-normX * horizontalStrength, verticalStrength, -normZ * horizontalStrength);
                                    target.setDeltaMovement(knockback);
                                    target.hurtMarked = true;
                                }
                            }

                            CoreNetworking.sendToNear(new SendSoundS2C(
                                    SoundEvents.DRAGON_FIREBALL_EXPLODE,
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    1.25F,
                                    0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f),
                                    true), player);
                            CoreNetworking.sendToNear(new SendSoundS2C(
                                    SoundEvents.LIGHTNING_BOLT_THUNDER,
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    1.25F,
                                    0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f),
                                    true), player);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurtPre(LivingDamageEvent event) {
        Entity hurtEntity = event.getEntity();
        Entity attackerEntity = event.getSource().getEntity();
        double originalDamage = event.getAmount();
        if (hurtEntity instanceof LivingEntity livingEntity) {
            double hurtArcaniAmplifier = 0;
            if (livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                hurtArcaniAmplifier = livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
            double hurtArcaniFactor = 1 + (hurtArcaniAmplifier / 4.0);

            if (event.getSource().getEntity() instanceof Player player) {
                double playerArcaniAmplifier = 0;
                double playerGlaciusAmplifier = 0;
                double playerTerraAmplifier = 0;
                double playerUmbreAmplifier = 0;
                if (player.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                    playerArcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                }
                double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                if (player.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null) {
                    playerGlaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                    if (playerGlaciusAmplifier > 0) {
                        event.setAmount((float) (originalDamage * ((1 + (playerArcaniFactor / 2)) / hurtArcaniFactor)));
                    }
                }

                if (player.getAttribute(ModAttributes.UMBRE_ASPECT.get()) != null) {
                    playerUmbreAmplifier = player.getAttribute(ModAttributes.UMBRE_ASPECT.get()).getValue();
                    if (playerUmbreAmplifier > 0) {

                    }
                }

                if (player.getAttribute(ModAttributes.TERRA_ASPECT.get()) != null) {
                    playerTerraAmplifier = player.getAttributeValue(ModAttributes.TERRA_ASPECT.get());
                    if (playerTerraAmplifier > 0) {
                        if (playerUmbreAmplifier > 0 && player.level().getRandom().nextDouble() < 0.05 + (0.05 * (playerTerraAmplifier + playerUmbreAmplifier))) {
                            event.setAmount(0);
                            if (attackerEntity instanceof LivingEntity attackerLivingEntity) {
                                attackerLivingEntity.hurt(player.damageSources().magic(), (float) Math.max(1, (originalDamage / 2) * (playerTerraAmplifier + playerUmbreAmplifier)));
                            }
                        }

                        if (playerArcaniAmplifier > 0 && player.level().getRandom().nextDouble() < 0.1 + (0.1 * (playerTerraAmplifier + playerArcaniAmplifier))) {
                            player.addEffect(new MobEffectInstance(
                                    MobEffects.ABSORPTION,
                                    600,
                                    (int) Math.max(0, (originalDamage - 1)),
                                    false,
                                    true,
                                    true
                            ));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityHurtPost(LivingDamageEvent event) {
        Entity hurtEntity = event.getEntity();
        if (hurtEntity instanceof LivingEntity livingEntity) {
            double hurtArcaniAmplifier = 0;
            if (livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                hurtArcaniAmplifier = livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
            double hurtArcaniFactor = 1 + (hurtArcaniAmplifier / 4.0);

            if (livingEntity.isOnFire()) {
                List<Player> players = livingEntity.level().getEntitiesOfClass(
                        Player.class,
                        livingEntity.getBoundingBox().inflate(25.0),
                        e -> e != livingEntity
                );

                for (Player player : players) {
                    if (!livingEntity.getPersistentData().hasUUID("IgnitedBy")) {
                        return;
                    }

                    double playerArcaniAmplifier = 0;
                    if (player.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                        playerArcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                    } else if (player == hurtEntity) {
                        playerArcaniAmplifier = 0;
                    }
                    double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                    UUID ignitedBy = livingEntity.getPersistentData().getUUID("IgnitedBy");
                    if (event.getSource().type() == livingEntity.level().damageSources().onFire().type()
                            && ignitedBy.equals(player.getUUID()) && player.getAttribute(ModAttributes.INFERNUM_ASPECT.get()) != null) {
                        double infernumAmplifier = player.getAttributeValue(ModAttributes.INFERNUM_ASPECT.get());
                        if (infernumAmplifier > 0) {
                            //System.out.println("Fire damage amplified by " + infernumAmplifier);
                            livingEntity.hurt(
                                    player.damageSources().magic(),
                                    (float) (((1f * (infernumAmplifier)) * playerArcaniFactor) / hurtArcaniFactor));

                            if (player.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null && player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get()) > 0) {
                                double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                                EffectApplicationUtil.setChiller(livingEntity, player);
                                livingEntity.addEffect(
                                        new MobEffectInstance(
                                                CoreEffects.CHILLED.get(),
                                                (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                                (int) (glaciusAmplifier - 1 + (playerArcaniFactor / 2))));
                            }

                            if (player.getAttribute(ModAttributes.UMBRE_ASPECT.get()) != null && player.getAttributeValue(ModAttributes.UMBRE_ASPECT.get()) > 0) {
                                double umbreAmplifier = player.getAttributeValue(ModAttributes.UMBRE_ASPECT.get());
                                if (event.getSource().type() == player.level().damageSources().onFire().type()) {
                                    player.heal(
                                            (float) Math.max(1, (
                                                    event.getAmount() + (((1f * (infernumAmplifier)) * playerArcaniFactor) / hurtArcaniFactor)) * (0.1 * umbreAmplifier)));
                                }
                            }

                            if (player.getAttribute(ModAttributes.TERRA_ASPECT.get()) != null) {
                                double terraAmplifier = player.getAttributeValue(ModAttributes.TERRA_ASPECT.get());
                                if (terraAmplifier > 0) {
                                    if (!player.hasEffect(MobEffects.ABSORPTION)) {
                                        //System.out.println("Adding Scorched Earth absorption!");
                                        player.addEffect(
                                                new MobEffectInstance(
                                                        MobEffects.ABSORPTION,
                                                        (int) (200 * terraAmplifier * playerArcaniFactor),
                                                        0,
                                                        false,
                                                        true,
                                                        true
                                                ));
                                    } else if (player.hasEffect(MobEffects.ABSORPTION)) {
                                        if (player.getEffect(MobEffects.ABSORPTION).getAmplifier() < (1 + ((terraAmplifier * infernumAmplifier) * playerArcaniFactor))) {
                                            int newAmplifier = player.getEffect(MobEffects.ABSORPTION).getAmplifier() + 1;
                                            int newDuration = player.getEffect(MobEffects.ABSORPTION).getDuration() + (int) (200 * terraAmplifier * playerArcaniFactor);
                                            player.addEffect(new MobEffectInstance(
                                                    MobEffects.ABSORPTION,
                                                    newDuration,
                                                    newAmplifier,
                                                    false,
                                                    true,
                                                    true
                                            ));
                                        } else if (player.getEffect(MobEffects.ABSORPTION).getAmplifier() >= (1 + ((terraAmplifier * infernumAmplifier) * playerArcaniFactor))) {
                                            int sameAmplifier = player.getEffect(MobEffects.ABSORPTION).getAmplifier();
                                            int sameDuration = player.getEffect(MobEffects.ABSORPTION).getDuration() + (int) (200 * terraAmplifier * playerArcaniFactor);
                                            player.addEffect(new MobEffectInstance(
                                                    MobEffects.ABSORPTION,
                                                    sameDuration,
                                                    sameAmplifier,
                                                    false,
                                                    true,
                                                    true
                                            ));
                                        }
                                    }
                                }
                            }
                        }

                        if (player.getAttribute(ModAttributes.TEMPESTUS_ASPECT.get()) != null) {
                            double tempestasAmplifier = player.getAttributeValue(ModAttributes.TEMPESTUS_ASPECT.get());
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
                                EffectApplicationUtil.setStunner(livingEntity, player);
                                //System.out.println(
                                //        Component.translatable(
                                //                livingEntity.getName().getString()) +
                                //                " stunned by " + player + " for " +
                                //                (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor) / 20 + " seconds!"
                                //);
                                livingEntity.addEffect(
                                        new MobEffectInstance(
                                                CoreEffects.STUNNED.get(),
                                                (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                                (int) tempestasAmplifier - 1));
                            }
                        }
                    }
                }
            }

            if (event.getSource().getEntity() instanceof Player player) {
                double playerArcaniAmplifier = 0;
                if (player.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                    playerArcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                }
                double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                if (event.getSource() == livingEntity.damageSources().onFire()) {
                    EffectApplicationUtil.setIgnitor(livingEntity, player);
                    if (player.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null) {
                        double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                        EffectApplicationUtil.setChiller(livingEntity, player);
                        livingEntity.addEffect(
                                new MobEffectInstance(
                                        CoreEffects.CHILLED.get(),
                                        (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                        (int) (glaciusAmplifier - 1 + (playerArcaniFactor / 2))));
                    }
                }

                ItemStack itemStack = player.getMainHandItem();
                // Retrieve enchantments stored on the item from NBT
                Map<?, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);

                int flameLevel = enchantments.getOrDefault(Enchantments.FLAMING_ARROWS, 0);
                int fireAspectLevel = enchantments.getOrDefault(Enchantments.FIRE_ASPECT, 0);

                if (flameLevel > 0 || fireAspectLevel > 0) {
                    EffectApplicationUtil.setIgnitor(livingEntity, player);
                    int ignitionTime = 0;

                    if (flameLevel > 0) {
                        ignitionTime = 100 * flameLevel;
                    } else if (fireAspectLevel > 0) {
                        ignitionTime = 80 * fireAspectLevel;
                    }

                    // System.out.println("Igniting " + livingEntity.getDisplayName().getString() + " for " + ignitionTime / 20 + " seconds");
                    if (playerArcaniFactor > 1) {
                        int upgradedTicks = (int) (ignitionTime * playerArcaniFactor);
                        if (livingEntity.isOnFire()) {
                            int currentTicks = livingEntity.getRemainingFireTicks();
                            livingEntity.setRemainingFireTicks(currentTicks + upgradedTicks);
                            // System.out.println("Increasing ignition time to " + (currentTicks + upgradedTicks) / 20 + " seconds");
                        } else {
                            livingEntity.setSecondsOnFire(upgradedTicks / 20);
                            // System.out.println("Setting ignition time to " + (upgradedTicks) / 20 + " seconds");
                        }
                    }
                }

                if (player.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null) {
                    double glaciusAmplifier = player.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                    if (glaciusAmplifier > 0) {
                        if (player.level().getRandom().nextDouble() < 0.5 + (0.05 * glaciusAmplifier)) {
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
                            EffectApplicationUtil.setChiller(livingEntity, player);
                            livingEntity.addEffect(
                                    new MobEffectInstance(
                                            CoreEffects.CHILLED.get(),
                                            (int) (((60 * glaciusAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                            (int) (glaciusAmplifier - 1 + (playerArcaniFactor / 2))));
                        }
                    }
                }

                if (player.getAttribute(ModAttributes.TEMPESTUS_ASPECT.get()) != null) {
                    double tempestasAmplifier = player.getAttributeValue(ModAttributes.TEMPESTUS_ASPECT.get());
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
                        EffectApplicationUtil.setStunner(livingEntity, player);
                        //System.out.println(
                        //        Component.translatable(
                        //                livingEntity.getName().getString()) +
                        //                " stunned by " + player + " for " +
                        //                (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor) / 20 + " seconds!"
                        //);
                        livingEntity.addEffect(
                                new MobEffectInstance(
                                        CoreEffects.STUNNED.get(),
                                        (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor),
                                        (int) tempestasAmplifier - 1));
                    }
                }

                if (player.getAttribute(ModAttributes.UMBRE_ASPECT.get()) != null && playerArcaniAmplifier > 0) {
                    double umbreAmplifier = player.getAttributeValue(ModAttributes.UMBRE_ASPECT.get());
                    if (umbreAmplifier > 0 && player.level().getRandom().nextDouble() < 0.05 + ((0.05 * umbreAmplifier) * playerArcaniFactor)) {
                        player.level().playSound(
                                null,
                                hurtEntity.getX(),
                                hurtEntity.getY(),
                                hurtEntity.getZ(),
                                SoundEvents.GENERIC_DRINK,
                                SoundSource.PLAYERS,
                                1.0F,
                                0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f)
                        );

                        //System.out.println(
                        //        Component.translatable(
                        //                livingEntity.getName().getString()) +
                        //                " stunned by " + player + " for " +
                        //                (int) (((20 * tempestasAmplifier) * playerArcaniFactor) / hurtArcaniFactor) / 20 + " seconds!"
                        //);
                        if (!livingEntity.hasEffect(CoreEffects.SAPPED.get())) {
                            livingEntity.addEffect(
                                    new MobEffectInstance(
                                            CoreEffects.SAPPED.get(),
                                            (int) (200 * umbreAmplifier * playerArcaniAmplifier),
                                            0,
                                            false,
                                            true,
                                            true
                                    ));
                        } else if (livingEntity.hasEffect(CoreEffects.SAPPED.get()) && livingEntity.getEffect(CoreEffects.SAPPED.get()).getAmplifier() < ((umbreAmplifier * playerArcaniAmplifier) * 2)) {
                            int newAmplifier = livingEntity.getEffect(CoreEffects.SAPPED.get()).getAmplifier() + 1;
                            int newDuration = livingEntity.getEffect(CoreEffects.SAPPED.get()).getDuration() + (int) (200 * umbreAmplifier * playerArcaniFactor);
                            livingEntity.addEffect(new MobEffectInstance(
                                    CoreEffects.SAPPED.get(),
                                    newDuration,
                                    newAmplifier,
                                    false,
                                    true,
                                    true
                            ));
                        }

                        if (!player.hasEffect(CoreEffects.VIGOR.get())) {
                            player.addEffect(
                                    new MobEffectInstance(
                                            CoreEffects.VIGOR.get(),
                                            (int) (200 * umbreAmplifier * playerArcaniAmplifier),
                                            0,
                                            false,
                                            true,
                                            true
                                    ));
                        } else if (player.hasEffect(CoreEffects.VIGOR.get()) && player.getEffect(CoreEffects.VIGOR.get()).getAmplifier() < ((umbreAmplifier * playerArcaniAmplifier) * 2)) {
                            int newAmplifier = player.getEffect(CoreEffects.VIGOR.get()).getAmplifier() + 1;
                            int newDuration = player.getEffect(CoreEffects.VIGOR.get()).getDuration() + (int) (200 * umbreAmplifier * playerArcaniFactor);
                            player.addEffect(new MobEffectInstance(
                                    CoreEffects.VIGOR.get(),
                                    newDuration,
                                    newAmplifier,
                                    false,
                                    true,
                                    true
                            ));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEntityDeath(LivingDeathEvent event) {
        Entity slainEntity = event.getEntity();
        if (slainEntity instanceof LivingEntity livingEntity) {
            if (livingEntity.isOnFire() && livingEntity.getPersistentData().contains("IgnitedBy")) {
                Player ignitingPlayer = livingEntity.level().getPlayerByUUID(livingEntity.getPersistentData().getUUID("IgnitedBy"));
                double playerArcaniAmplifier = 0;
                if (ignitingPlayer != null && ignitingPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                    playerArcaniAmplifier = ignitingPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                }
                double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);

                if (ignitingPlayer.getAttribute(ModAttributes.INFERNUM_ASPECT.get()) != null && playerArcaniAmplifier > 0) {
                    livingEntity.level().playSound(
                            null,
                            livingEntity.getX(),
                            livingEntity.getY(),
                            livingEntity.getZ(),
                            SoundEvents.DRAGON_FIREBALL_EXPLODE,
                            SoundSource.HOSTILE,
                            1.0F,
                            0.4f / (livingEntity.level().getRandom().nextFloat() * 0.4f + 0.8f)
                    );
                    double offX = (livingEntity.level().random.nextDouble() - 0.5) * 0.5;
                    double offY = livingEntity.getBbHeight() * livingEntity.level().random.nextDouble();
                    double offZ = (livingEntity.level().random.nextDouble() - 0.5) * 0.5;
                    CoreNetworking.sendToNear(new SendParticlesS2C(
                            ParticleTypes.EXPLOSION,
                            livingEntity.getX() + offX,
                            livingEntity.getY() + offY,
                            livingEntity.getZ() + offZ,
                            0, 0, 0), livingEntity);
                    double playerInfernumAmplifier = ignitingPlayer.getAttributeValue(ModAttributes.INFERNUM_ASPECT.get());
                    AABB ignitionArea = new AABB(
                            livingEntity.getX() - (playerInfernumAmplifier * playerArcaniFactor), livingEntity.getY() - (playerInfernumAmplifier * playerArcaniFactor),
                            livingEntity.getZ() - (playerInfernumAmplifier * playerArcaniFactor),
                            livingEntity.getX() + (playerInfernumAmplifier * playerArcaniFactor), livingEntity.getY() + (playerInfernumAmplifier * playerArcaniFactor),
                            livingEntity.getZ() + (playerInfernumAmplifier * playerArcaniFactor));
                    FireAuraRenderer.addAuraForEntity(livingEntity, System.currentTimeMillis(), 30, (playerInfernumAmplifier * playerArcaniFactor));
                    List<LivingEntity> blastedEntities = livingEntity.level().getEntitiesOfClass(
                            LivingEntity.class, ignitionArea, e -> e != ignitingPlayer);
                    for (LivingEntity blastedEntity : blastedEntities) {
                        EffectApplicationUtil.setIgnitor(blastedEntity, ignitingPlayer);
                        int ignitionTime = (int) ((60 * playerInfernumAmplifier) * playerArcaniFactor);
                        //System.out.println("Igniting " + livingEntity.getName() + " for " + ignitionTime / 20 + " seconds");
                        if (playerArcaniFactor > 1) {
                            int upgradedTicks = (int) (ignitionTime * playerArcaniFactor);
                            if (blastedEntity.isOnFire()) {
                                int currentTicks = blastedEntity.getRemainingFireTicks();
                                blastedEntity.setRemainingFireTicks(currentTicks + upgradedTicks);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (currentTicks + upgradedTicks) / 20 + " seconds"
                                //);
                            } else {
                                blastedEntity.setSecondsOnFire(upgradedTicks / 20);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (upgradedTicks) / 20 + " seconds"
                                //);
                            }
                        } else {
                            if (blastedEntity.isOnFire()) {
                                int currentTicks = blastedEntity.getRemainingFireTicks();
                                blastedEntity.setRemainingFireTicks(currentTicks + ignitionTime);
                                //System.out.println(
                                //        "Increasing ignition time for " + livingEntity.getName() + " to " +
                                //                (currentTicks + ignitionTime) / 20 + " seconds"
                                //);
                            } else {
                                blastedEntity.setSecondsOnFire(ignitionTime / 20);
                            }
                        }

                        if (playerInfernumAmplifier > 1) {
                            blastedEntity.hurt(ignitingPlayer.damageSources().onFire(), 1);
                            blastedEntity.setLastHurtByPlayer(ignitingPlayer);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingEntityTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Level level = event.level;
        int simulationDistanceInBlocks = 128;
        Set<LivingEntity> processedEntities = new HashSet<>();

        for (Player player : level.players()) {
            AABB searchBox = player.getBoundingBox().inflate(simulationDistanceInBlocks);
            for (LivingEntity livingEntity : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
                if (!processedEntities.add(livingEntity)) {
                    continue;
                }

                if (!livingEntity.isOnFire() && livingEntity.getPersistentData().contains("IgnitedBy")) {
                    //System.out.println(livingEntity + " extinguished!");
                    livingEntity.getPersistentData().remove("IgnitedBy");
                }

                if (livingEntity instanceof Player playerEntity) {
                    double playerArcaniAmplifier = 0;
                    if (playerEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                        playerArcaniAmplifier = playerEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                    }
                    double playerArcaniFactor = 1 + (playerArcaniAmplifier / 4.0);
                    if (playerEntity.getAttribute(ModAttributes.TERRA_ASPECT.get()) != null && playerArcaniAmplifier > 0) {
                        double playerTerraAmplifier = playerEntity.getAttributeValue(ModAttributes.TERRA_ASPECT.get());
                        if (playerEntity.tickCount % (80 / (playerTerraAmplifier * playerArcaniFactor)) == 0) {
                            playerEntity.heal((float) (playerTerraAmplifier * playerArcaniFactor));
                        }
                    }
                }
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
            if (player.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                arcaniAmplifier = player.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
        } else {
            if (livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                arcaniAmplifier = livingEntity.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
            }
        }

        if (arcaniAmplifier > 0) {
            double arcaneFactor = 1 + (arcaniAmplifier / 4.0);
            double multiplier = 1;
            boolean beneficialEffect = effectInstance.getEffect().isBeneficial();
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
                        if (targetIsHostile) {
                            livingEntity.removeEffect(effectInstance.getEffect());
                            return;
                        }
                    } else {
                        multiplier = arcaneFactor;
                    }
                } else if (sourceIsHostile) {
                    if (beneficialEffect) {
                        if (targetIsHostile) {
                            multiplier = arcaneFactor;
                        } else if (targetIsPlayer) {
                            livingEntity.removeEffect(effectInstance.getEffect());
                            return;
                        }
                    }
                } else {
                    if (beneficialEffect) {
                        if (!targetIsHostile) {
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
                if (livingEntity instanceof Player player) {
                    if (oldInstance == null || oldInstance.getEffect() != effectInstance.getEffect()) {
                        ArcaneAuraRenderer.addAuraForEntity(player, System.currentTimeMillis(), 20, 1);
                        CoreNetworking.sendToNear(new SendSoundS2C(
                                SoundEvents.ILLUSIONER_CAST_SPELL,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1.5F,
                                0.4f / (player.level().getRandom().nextFloat() * 0.4f + 0.8f),
                                true
                        ), player);
                    }
                }
                //System.out.println("[" + System.currentTimeMillis() + "] Increasing " + event.getEffectInstance().getEffect().getRegisteredName() + "duration by " + ((multiplier - 1) * 100) + "% and total duration: " + (newDuration / 20) + " seconds");
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

        if (effectInstance.getEffect() == CoreEffects.CHILLED.get()) {
            if (oldInstance != null) {
                int additional = (effectInstance.getAmplifier() == 0) ? 1 : effectInstance.getAmplifier() + 1;
                int newAmplifier = oldInstance.getAmplifier() + additional;
                int newDuration = oldInstance.getDuration() + effectInstance.getDuration();
                effectInstance.update(new MobEffectInstance(
                        CoreEffects.CHILLED.get(),
                        newDuration,
                        newAmplifier,
                        oldInstance.isAmbient(),
                        oldInstance.isVisible(),
                        oldInstance.showIcon()
                ));
            }

            if (livingEntity.getPersistentData().contains("ChilledBy")) {
                Player chillingPlayer = livingEntity.level().getPlayerByUUID(livingEntity.getPersistentData().getUUID("ChilledBy"));
                if (chillingPlayer.getAttribute(ModAttributes.GLACIUS_ASPECT.get()) != null && chillingPlayer.getAttribute(ModAttributes.UMBRE_ASPECT.get()) != null) {
                    double glaciusAmplifier = chillingPlayer.getAttributeValue(ModAttributes.GLACIUS_ASPECT.get());
                    double umbreAmplifier = chillingPlayer.getAttributeValue(ModAttributes.UMBRE_ASPECT.get());
                    if (glaciusAmplifier > 0 && umbreAmplifier > 0) {
                        double chillerArcaniAmplifier = 0;
                        if (chillingPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()) != null) {
                            chillerArcaniAmplifier = chillingPlayer.getAttribute(ModAttributes.ARCANI_ASPECT.get()).getValue();
                        }
                        double chillingArcaniFactor = 1 + (chillerArcaniAmplifier / 4.0);

                        if (!chillingPlayer.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                            chillingPlayer.addEffect(
                                    new MobEffectInstance(
                                            MobEffects.MOVEMENT_SPEED,
                                            (int) (20 * glaciusAmplifier * umbreAmplifier * chillingArcaniFactor),
                                            0,
                                            false,
                                            true,
                                            true
                                    ));
                        } else if (chillingPlayer.hasEffect(MobEffects.MOVEMENT_SPEED) && chillingPlayer.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() < ((glaciusAmplifier + umbreAmplifier) * (chillingArcaniFactor / 2))) {
                            int newAmplifier = chillingPlayer.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() + 1;
                            int newDuration = chillingPlayer.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() + (int) (20 * glaciusAmplifier * umbreAmplifier * chillingArcaniFactor);
                            chillingPlayer.addEffect(new MobEffectInstance(
                                    MobEffects.MOVEMENT_SPEED,
                                    newDuration,
                                    newAmplifier,
                                    false,
                                    true,
                                    true
                            ));
                        }
                    }
                }
            }
        }

        if (event.getEffectInstance().getEffect() == CoreEffects.FROZEN.get()) {
            if (livingEntity.getPersistentData().contains("ChilledBy")) {
                EffectApplicationUtil.setFrozen(livingEntity, livingEntity.getPersistentData().getUUID("ChilledBy"));
            }
        }
    }

    //@SubscribeEvent
    //public static void onBrewingRecipeRegister(BrewingRecipeRegistry event) {
    //    PotionBrewing.Builder builder = event.getBuilder();
//
    //    builder.addMix(Potions.AWKWARD, Items.BLUE_ICE, ModPotions.CHILLING_POTION);
    //    builder.addMix(ModPotions.CHILLING_POTION, Items.GLOWSTONE_DUST, ModPotions.CHILLING_POTION2);
    //    builder.addMix(ModPotions.CHILLING_POTION2, ModItems.GLACIUS_RUNE.get(), ModPotions.FREEZING_POTION);
    //    builder.addMix(Potions.THICK, ModItems.TEMPESTAS_RUNE.get(), ModPotions.STUNNING_POTION);
    //}

    private static boolean isRune(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.RUNE_ITEMS);
    }
}



