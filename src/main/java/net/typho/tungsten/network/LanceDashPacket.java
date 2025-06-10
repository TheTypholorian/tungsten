package net.typho.tungsten.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.typho.tungsten.item.LanceItem;

import java.util.function.Supplier;

public class LanceDashPacket {
    private final boolean isDashing;

    public LanceDashPacket(boolean isDashing) {
        this.isDashing = isDashing;
    }

    public LanceDashPacket(FriendlyByteBuf buf) {
        this.isDashing = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isDashing);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                Player player = Minecraft.getInstance().player;
                assert player != null;
                LanceItem.dash(isDashing, player, player.getPersistentData());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
