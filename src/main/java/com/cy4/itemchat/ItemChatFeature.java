package com.cy4.itemchat;

import java.util.List;

import com.cy4.itemchat.network.LinkItemMessage;
import com.cy4.itemchat.network.ModNetwork;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ItemChat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ItemChatFeature {
	public static boolean renderItemsInChat = true;

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void keyboardEvent(ScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		Options settings = mc.options;
		Screen screen = event.getScreen();
		if(InputConstants.isKeyDown(mc.getWindow().getWindow(), settings.keyChat.getKey().getValue())
				&& screen instanceof AbstractContainerScreen && Screen.hasShiftDown()) {
			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;

			List<? extends GuiEventListener> children = gui.children();
			for(GuiEventListener c : children)
				if(c instanceof EditBox) {
					EditBox tf = (EditBox) c;
					if(tf.isFocused())
						return;
				}

			Slot slot = gui.getSlotUnderMouse();
			if(slot != null && slot.container != null) {
				ItemStack stack = slot.getItem();

				if (!stack.isEmpty()
						&& !MinecraftForge.EVENT_BUS.post(new ClientChatEvent(stack.getDisplayName().getString()))) {
					ModNetwork.CHANNEL.sendToServer(new LinkItemMessage(stack));
					event.setCanceled(true);
				}
			}
		}
	}

	public static void linkItem(Player player, ItemStack item) {
		if(!renderItemsInChat)
			return;

		if(!item.isEmpty() && player instanceof ServerPlayer) {
			Component comp = item.getDisplayName();
			Component fullComp = new TranslatableComponent("chat.type.text", player.getDisplayName(), comp);

			PlayerList players = ((ServerPlayer) player).server.getPlayerList();

			ServerChatEvent event = new ServerChatEvent((ServerPlayer) player, comp.getString(), fullComp);
			if (!MinecraftForge.EVENT_BUS.post(event)) {
				players.broadcastMessage(event.getComponent(), ChatType.CHAT, player.getUUID());

				ServerGamePacketListenerImpl handler = ((ServerPlayer) player).connection;
				int threshold = handler.chatSpamTickCount;
				threshold += 20;

				if (threshold > 200 && !players.isOp(player.getGameProfile()))
					handler.onDisconnect(new TranslatableComponent("disconnect.spam"));

				handler.chatSpamTickCount = threshold;
			}
		}

	}

	private static int chatX, chatY;

	public static MutableComponent createStackComponent(ItemStack stack, MutableComponent component) {
		if (!renderItemsInChat)
			return component;

		Style style = component.getStyle();
		if (stack.getCount() > 64) {
			ItemStack copyStack = stack.copy();
			copyStack.setCount(64);
			style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(copyStack)));
			component.withStyle(style);
		}

		MutableComponent out = new TextComponent("   ");
		out.setStyle(style);
		return out.append(component);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void getChatPos(RenderGameOverlayEvent.Chat event) {
		chatX = event.getPosX();
		chatY = event.getPosY();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void renderSymbols(RenderGameOverlayEvent.Post event) {
		if (!renderItemsInChat)
			return;

		Minecraft mc = Minecraft.getInstance();
		Gui gameGui = mc.gui;
		ChatComponent chatGui = gameGui.getChat();
		if (event.getType() == RenderGameOverlayEvent.ElementType.CHAT) {
			int updateCounter = gameGui.getGuiTicks();
			List<GuiMessage<FormattedCharSequence>> lines = chatGui.trimmedMessages;
			int shift = chatGui.chatScrollbarPos;

			int idx = shift;

			while (idx < lines.size() && (idx - shift) < chatGui.getLinesPerPage()) {
				GuiMessage<FormattedCharSequence> line = lines.get(idx);
				StringBuilder before = new StringBuilder();

				FormattedCharSequence lineProperties = line.getMessage();

				int captureIndex = idx;
				lineProperties.accept((counter_, style, character) -> {
					String sofar = before.toString();
					if (sofar.endsWith("    ")) {
						render(mc, event.getMatrixStack(), chatGui, updateCounter, sofar.substring(0, sofar.length() - 3), line, captureIndex - shift, style);
						return false;
					}
					before.append((char) character);
					return true;
				});

				idx++;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static void render(Minecraft mc, PoseStack pose, ChatComponent chatGui, int updateCounter, String before, GuiMessage<FormattedCharSequence> line, int lineHeight, Style style) {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent != null && hoverEvent.getAction() == HoverEvent.Action.SHOW_ITEM) {
			HoverEvent.ItemStackInfo contents = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);

			ItemStack stack = contents != null ? contents.getItemStack() : ItemStack.EMPTY;

			if (stack.isEmpty())
				stack = new ItemStack(Blocks.BARRIER); // for invalid icon

			int timeSinceCreation = updateCounter - line.getAddedTime();
			if (chatGui.isChatFocused()) timeSinceCreation = 0;

			if (timeSinceCreation < 200) {
				float chatOpacity = (float) mc.options.chatOpacity * 0.9f + 0.1f;
				float fadeOut = Mth.clamp((1 - timeSinceCreation / 200f) * 10, 0, 1);
				float alpha = fadeOut * fadeOut * chatOpacity;

				int x = chatX + 3 + mc.font.width(before);
				int y = chatY - mc.font.lineHeight * lineHeight;

				if (alpha > 0) {
					alphaValue = alpha; // TODO LOW PRIO blocks dont fade out properly

					PoseStack modelviewPose = RenderSystem.getModelViewStack();

					modelviewPose.pushPose();
					modelviewPose.translate(x - 2, y - 2, 0);
					modelviewPose.scale(0.65f, 0.65f, 0.65f);
					mc.getItemRenderer().renderGuiItem(stack, 0, 0);
					modelviewPose.popPose();

					RenderSystem.applyModelViewMatrix();

					alphaValue = 1F;
				}
			}
		}
	}

	public static float alphaValue = 1F;
}
