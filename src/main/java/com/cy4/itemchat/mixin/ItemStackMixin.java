package com.cy4.itemchat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cy4.itemchat.ItemChatFeature;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
	private void getHoverName(CallbackInfoReturnable<Component> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(ItemChatFeature.createStackComponent((ItemStack) (Object) this,
				(MutableComponent) callbackInfoReturnable.getReturnValue()));
	}
}