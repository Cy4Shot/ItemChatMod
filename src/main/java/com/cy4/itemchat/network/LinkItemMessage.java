package com.cy4.itemchat.network;

import java.util.function.Supplier;

import com.cy4.itemchat.ItemChatFeature;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class LinkItemMessage {
	public ItemStack stack;

	public LinkItemMessage() {
	}

	public LinkItemMessage(ItemStack stack) {
		this.stack = stack;
	}

	public static void encode(LinkItemMessage message, FriendlyByteBuf buffer) {
		buffer.writeItem(message.stack);
	}

	public static LinkItemMessage decode(FriendlyByteBuf buffer) {
		return new LinkItemMessage(buffer.readItem());
	}

	public static void handle(LinkItemMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> ItemChatFeature.linkItem(context.getSender(), message.stack));
		context.setPacketHandled(true);
	}
}
