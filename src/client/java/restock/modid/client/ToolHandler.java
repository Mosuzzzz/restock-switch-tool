package restock.modid.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Handles automatic tool switching on the client side.
 */
public class ToolHandler {

    /**
     * Finds the best tool in the player's hotbar for breaking the given block
     * and switches to it if a better one is found.
     */
    public static void switchToBestTool(Minecraft client, BlockPos pos) {
        if (client.player == null || client.level == null) return;

        BlockState state = client.level.getBlockState(pos);
        if (state.isAir()) return;

        Inventory inventory = client.player.getInventory();
        int currentSlot = inventory.getSelectedSlot();
        int bestSlot = currentSlot;
        float bestSpeed = inventory.getItem(bestSlot).getDestroySpeed(state);

        // Check all hotbar slots (0-8)
        for (int i = 0; i < 9; i++) {
            if (i == currentSlot) continue;
            
            ItemStack stack = inventory.getItem(i);
            float speed = stack.getDestroySpeed(state);
            
            // If this tool is faster, consider it the new best
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // If we found a better tool, switch to it.
        if (bestSlot != currentSlot) {
            inventory.setSelectedSlot(bestSlot);

            // Tell the server about the slot change. Changing the selected slot
            // client-side only is not enough: the server computes break speed and
            // drops from the slot it last knew about, so without this packet the
            // held item desyncs and the wrong tool is used server-side.
            ClientPacketListener connection = client.getConnection();
            if (connection != null) {
                connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
            }
        }
    }
}
