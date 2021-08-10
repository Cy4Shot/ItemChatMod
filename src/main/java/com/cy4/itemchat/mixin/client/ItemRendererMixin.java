package com.cy4.itemchat.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cy4.itemchat.ItemChatFeature;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.item.ItemStack;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Accessor
	public abstract ItemColors getItemColors();

	@Inject(method = "renderQuads(Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Ljava/util/List;Lnet/minecraft/item/ItemStack;II)V", at = @At(value = "HEAD"), cancellable = true)
	public void renderQuads(MatrixStack ms, IVertexBuilder builder, List<BakedQuad> quads, ItemStack stack,
			int lightmap, int overlay, CallbackInfo ci) {
		if (ItemChatFeature.alphaValue != 1.0F) {
			boolean flag = !stack.isEmpty();
			MatrixStack.Entry entry = ms.getLast();

			for (BakedQuad bakedquad : quads) {
				int i = flag && bakedquad.hasTintIndex() ? getItemColors().getColor(stack, bakedquad.getTintIndex())
						: -1;

				float r = (i >> 16 & 255) / 255.0F;
				float g = (i >> 8 & 255) / 255.0F;
				float b = (i & 255) / 255.0F;
				builder.addVertexData(entry, bakedquad, r, g, b, ItemChatFeature.alphaValue, lightmap, overlay, true);
			}
			ci.cancel();
		}
	}
}
