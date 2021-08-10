package com.cy4.itemchat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cy4.itemchat.ItemChatFeature;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getTextComponent()Lnet/minecraft/util/text/ITextComponent;", at = @At("RETURN"), cancellable = true)
	private void getTextComponent(CallbackInfoReturnable<ITextComponent> callbackInfoReturnable) {
		System.out.println("Stack component created");
		callbackInfoReturnable.setReturnValue(ItemChatFeature.createStackComponent((ItemStack) (Object) this,
				(IFormattableTextComponent) callbackInfoReturnable.getReturnValue()));
	}
}