package net.turtleboi.aspects.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.turtleboi.aspects.Aspects;
import net.turtleboi.aspects.client.renderer.FireAuraRenderer;
import net.turtleboi.aspects.util.AspectUtil;
import net.turtleboi.aspects.util.ModAttributes;
import net.turtleboi.aspects.util.ModTags;

import java.util.List;

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
                if (infernumAmplifier > 0) {
                    FireAuraRenderer.addAura(System.currentTimeMillis(), 30, infernumAmplifier);

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
                    List<LivingEntity> mobs = player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            player.getBoundingBox().inflate(radius),
                            e -> e != player
                    );
                    for (LivingEntity mob : mobs) {
                        mob.igniteForTicks((int) (60 * infernumAmplifier));
                    }
                }
            }
        }
    }

    private static boolean isRune(ItemStack itemStack) {
        return itemStack.is(ModTags.Items.RUNE_ITEMS);
    }
}
