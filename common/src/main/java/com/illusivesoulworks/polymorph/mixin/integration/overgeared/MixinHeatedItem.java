package com.illusivesoulworks.polymorph.mixin.integration.overgeared;

import com.illusivesoulworks.polymorph.common.integration.overgeared.OvergearedModule;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Tells the cooling pick whose favourites apply. None of these methods calls another of them.
@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "net.stirdrem.overgeared.heateditem.HeatedItem", remap = false)
public abstract class MixinHeatedItem {

  @Inject(at = @At("HEAD"),
      method = "setCooled(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V")
  private static void polymorph$preSetCooled(ItemStack stack, LivingEntity entity, CallbackInfo ci) {
    OvergearedModule.setCoolingEntity(entity);
  }

  @Inject(at = @At("HEAD"), method = "setCooledSingle")
  private static void polymorph$preSetCooledSingle(ItemStack stack, Player player, CallbackInfo ci) {
    OvergearedModule.setCoolingEntity(player);
  }

  @Inject(at = @At("HEAD"), method = "handleCoolingLivingEntity")
  private static void polymorph$preCoolLiving(ItemStack stack, Level level, LivingEntity entity,
                                              CallbackInfo ci) {
    OvergearedModule.setCoolingEntity(entity);
  }

  @Inject(at = @At("HEAD"), method = "tickItemEntity")
  private static void polymorph$preTickItemEntity(ItemStack stack, Level level, ItemEntity item,
                                                  CallbackInfo ci) {
    OvergearedModule.setCoolingEntity(item.getOwner());
  }

  // Container cooling has no player: clear any entity a failed call above left behind.
  @Inject(at = @At("HEAD"), method = "handleCoolingContainer")
  private static void polymorph$preCoolContainer(CallbackInfoReturnable<Boolean> cir) {
    OvergearedModule.setCoolingEntity(null);
  }

  @Inject(at = @At("RETURN"), method = {
      "setCooled(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V",
      "setCooledSingle", "handleCoolingLivingEntity", "tickItemEntity"}, require = 4)
  private static void polymorph$postCool(CallbackInfo ci) {
    OvergearedModule.setCoolingEntity(null);
  }
}
