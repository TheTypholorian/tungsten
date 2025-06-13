package net.typho.tungsten.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.typho.tungsten.item.HammerItem;

import java.util.function.Supplier;

public class HammerSlamPacket {
    private final boolean isSlamming;

    public HammerSlamPacket(boolean isSlamming) {
        this.isSlamming = isSlamming;
    }

    public HammerSlamPacket(FriendlyByteBuf buf) {
        this.isSlamming = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isSlamming);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                assert player != null;
                HammerItem.slam(isSlamming, player, player.getPersistentData());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
