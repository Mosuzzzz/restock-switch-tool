package restock.modid;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Core restock logic (Mojang-mapped MC 26.1.2).
 */
public class RestockHandler {

    /**
     * Restocks an empty hotbar / off-hand slot.
     */
    public static void restockForItem(Player player, int emptySlot, ItemStack itemType) {
        // Server-side only
        if (player.level().isClientSide()) return;

        Inventory inventory = player.getInventory();

        // 1. Only restock the CURRENTLY SELECTED slot or the OFF-HAND.
        // This prevents the mod from "fixing" other hotbar slots automatically
        // which can be annoying if the player intentionally emptied them.
        boolean isSelected = (emptySlot == inventory.getSelectedSlot());
        boolean isOffhand = (emptySlot == Inventory.SLOT_OFFHAND);
        
        if (!isSelected && !isOffhand) return;

        // 2. Avoid restocking if the player has a container screen open (Chest,
        // Crafting Table, etc.). containerMenu is never null; it equals inventoryMenu
        // when no external GUI is open. Any other menu means the player is actively
        // managing items, so we stay out of the way.
        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        // 3. Avoid restocking while the player is dragging an item on their cursor
        // (carried stack), which indicates manual organization in the inventory.
        if (!player.containerMenu.getCarried().isEmpty()) {
            return;
        }

        if (itemType == null || itemType.isEmpty()) return;

        // Confirm slot is still empty
        if (!inventory.getItem(emptySlot).isEmpty()) return;

        // --- Search for a matching stack ---
        // Priority 1: main inventory rows (slots 9–35)
        int foundSlot = -1;
        for (int i = 9; i <= 35; i++) {
            ItemStack candidate = inventory.getItem(i);
            if (!candidate.isEmpty() && candidate.is(itemType.getItem())) {
                foundSlot = i;
                break;
            }
        }

        // Priority 2: other hotbar slots (skip the empty one)
        // Only fallback to other hotbar slots if nothing is in the main inventory.
        if (foundSlot == -1) {
            for (int i = 0; i < 9; i++) {
                if (i == emptySlot) continue;
                ItemStack candidate = inventory.getItem(i);
                if (!candidate.isEmpty() && candidate.is(itemType.getItem())) {
                    foundSlot = i;
                    break;
                }
            }
        }

        if (foundSlot == -1) return;

        // Move the matching stack into the empty slot
        ItemStack stackToMove = inventory.getItem(foundSlot).copy();
        inventory.setItem(emptySlot, stackToMove);
        inventory.setItem(foundSlot, ItemStack.EMPTY);

        // Sync inventory state to the client
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }

        RestockItem.LOGGER.info(
            "[RestockItem] Restocked active slot {} with '{}' (moved from slot {})",
            emptySlot, stackToMove.getItem(), foundSlot
        );
    }
}
