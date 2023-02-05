package me.nathan.desmos.ingame.account;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockFace;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPlaceBlockPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;

/**
 * Ported from Desmos1.0
 */
import java.util.ArrayList;

public class ChestHandler {

    private final Account account;

    public final ArrayList<Position> nearbyChests = new ArrayList<>();
    public Position closestChest = null;

    public ChestHandler(Account account) {
        this.account = account;
    }

    public void packetEvent(PacketReceivedEvent event) {
        /* Handle Receiving Chunk Packets */
        if(event.getPacket() instanceof ServerChunkDataPacket) {
            ServerChunkDataPacket packet = event.getPacket();

            for (CompoundTag tag : packet.getColumn().getTileEntities()) {
                nearbyChests.add(new Position((Integer) tag.get("x").getValue(), (Integer) tag.get("y").getValue(),
                        (Integer) tag.get("z").getValue()));
            }
        }
    }

    public void updateNearestChest() {
        Position tempPos = null;
        for(Position position : nearbyChests) {
            if(tempPos == null) {
                tempPos = position;
            } else {
                double pDiffX = position.getX() - account.getBlockPosition().getX();
                double pDiffZ = position.getZ() - account.getBlockPosition().getZ();

                double tDiffX = tempPos.getX() - account.getBlockPosition().getX();
                double tDiffZ = tempPos.getZ() - account.getBlockPosition().getZ();
                if(Math.abs(Math.sqrt(Math.pow(pDiffX + pDiffZ, 2))) < Math.abs(Math.sqrt(Math.pow(tDiffX + tDiffZ, 2)))) {
                    tempPos = position;
                }
            }
        }
        closestChest = tempPos;
    }

    public void openChest() {
        account.getSession().send(new ClientPlayerPlaceBlockPacket(closestChest, BlockFace.UP, Hand.MAIN_HAND,0,0,0));
    }
}
