package com.cy4.itemchat.network;

import java.util.function.Supplier;

import com.cy4.itemchat.ItemChatFeature;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class LinkItemMessage {
	public ItemStack stack;

	public LinkItemMessage() {
	}

	public LinkItemMessage(ItemStack stack) {
		this.stack = stack;
	}

	public static void encode(LinkItemMessage message, PacketBuffer buffer) {
		buffer.writeItemStack(message.stack);
	}

	public static LinkItemMessage decode(PacketBuffer buffer) {
		return new LinkItemMessage(buffer.readItemStack());
	}

	public static void handle(LinkItemMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> ItemChatFeature.linkItem(context.getSender(), message.stack));
		context.setPacketHandled(true);
	}
}
