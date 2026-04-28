package restock.modid.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import restock.modid.RestockHandler;

/**
 * Mixin for net.minecraft.world.entity.player.Inventory (Mojang-mapped MC 26.1.2).
 *
 * Uses a tick-based approach to detect when a slot becomes empty.
 * This is more reliable than method hooks for block placement/consumption
 * which might modify item stacks in-place.
 */
@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    public Player player;

    @Shadow
    public abstract ItemStack getItem(int slot);

    @Shadow
    public abstract int getContainerSize();

    /**
     * Stores the item type that was in each slot during the previous tick.
     * We only care about the Item, not the count.
     */
    @Unique
    private final ItemStack[] restockitem$lastState = new ItemStack[100]; // Plenty of space for any inventory size

    @Unique
    private boolean restockitem$initialized = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void restockitem$onTick(CallbackInfo ci) {
        // Server-side only for restocking
        if (player.level().isClientSide()) return;

        int size = this.getContainerSize();
        
        // Ensure array is large enough (though 100 is usually enough for Player inventory)
        if (size > restockitem$lastState.length) return;

        for (int i = 0; i < size; i++) {
            ItemStack current = this.getItem(i);
            ItemStack last = restockitem$lastState[i];

            // If a slot was not empty but is now empty
            if (current.isEmpty() && last != null && !last.isEmpty()) {
                // Only restock hotbar (0-8) and off-hand (40)
                // In Inventory class: 0-8 is hotbar, 40 is offhand
                boolean isHotbar = (i >= 0 && i < 9);
                boolean isOffhand = (i == 40);
                
                if (isHotbar || isOffhand) {
                    restock.modid.RestockItem.LOGGER.info("[RestockItem] Slot {} became empty (was {}). Restocking...", i, last.getItem());
                    RestockHandler.restockForItem(player, i, last);
                    
                    // After restock, update 'last' so we don't trigger again immediately
                    // The restockForItem call will set the slot to the new stack.
                    // We'll capture that in the next tick or update it now.
                    restockitem$lastState[i] = this.getItem(i).copy();
                    continue;
                }
            }

            // Update last known state (copy to avoid modification issues)
            if (!current.isEmpty()) {
                restockitem$lastState[i] = current.copy();
            } else {
                restockitem$lastState[i] = null;
            }
        }
    }
}
