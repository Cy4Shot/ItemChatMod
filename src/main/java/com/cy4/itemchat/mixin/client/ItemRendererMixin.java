package com.cy4.itemchat.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cy4.itemchat.ItemChatFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

	@Accessor(value = "itemColors")
	public abstract ItemColors getItemColors();

	@Inject(method = "renderQuadList", at = @At(value = "HEAD"), cancellable = true)
	public void renderQuads(PoseStack ms, VertexConsumer builder, List<BakedQuad> quads, ItemStack stack,
			int lightmap, int overlay, CallbackInfo ci) {
		if (ItemChatFeature.alphaValue != 1.0F) {
			boolean flag = !stack.isEmpty();
			PoseStack.Pose entry = ms.last();

			for (BakedQuad bakedquad : quads) {
				int i = flag && bakedquad.isTinted() ? getItemColors().getColor(stack, bakedquad.getTintIndex())
						: -1;

				float r = (i >> 16 & 255) / 255.0F;
				float g = (i >> 8 & 255) / 255.0F;
				float b = (i & 255) / 255.0F;
				builder.putBulkData(entry, bakedquad, r, g, b, ItemChatFeature.alphaValue, lightmap, overlay, true);
			}
			ci.cancel();
		}
	}
}
