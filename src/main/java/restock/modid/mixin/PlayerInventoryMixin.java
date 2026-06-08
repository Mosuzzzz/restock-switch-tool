package restock.modid.mixin;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
     * The item type that occupied each slot during the previous tick, or null if
     * the slot was empty. We only care about the Item identity (to know what to
     * refill with), not the count, so we store a lightweight reference instead of
     * copying every ItemStack each tick — this runs every tick for every player.
     */
    @Unique
    private Item[] restockitem$lastItem = null;

    @Inject(method = "tick", at = @At("TAIL"))
    private void restockitem$onTick(CallbackInfo ci) {
        // Server-side only for restocking
        if (player.level().isClientSide()) return;

        int size = this.getContainerSize();

        // Lazily size the snapshot to the actual inventory size.
        if (restockitem$lastItem == null || restockitem$lastItem.length != size) {
            restockitem$lastItem = new Item[size];
        }

        for (int i = 0; i < size; i++) {
            ItemStack current = this.getItem(i);
            Item last = restockitem$lastItem[i];

            // If a slot was not empty but is now empty
            if (current.isEmpty() && last != null) {
                // Only restock hotbar (0-8) and off-hand (40).
                // In Inventory class: 0-8 is hotbar, 40 is offhand.
                boolean isHotbar = (i >= 0 && i < 9);
                boolean isOffhand = (i == Inventory.SLOT_OFFHAND);

                if (isHotbar || isOffhand) {
                    restock.modid.RestockItem.LOGGER.info("[RestockItem] Slot {} became empty (was {}). Restocking...", i, last);
                    RestockHandler.restockForItem(player, i, new ItemStack(last));
                }
            }

            // Update last known state. After a successful restock the slot is now
            // populated again, so re-reading here keeps the snapshot accurate.
            ItemStack updated = this.getItem(i);
            restockitem$lastItem[i] = updated.isEmpty() ? null : updated.getItem();
        }
    }
}
