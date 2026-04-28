package restock.modid.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import restock.modid.client.ToolHandler;

/**
 * Mixin to trigger auto-tool switching when the player starts breaking a block.
 */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * Injected into startDestroyBlock to switch tools before the breaking process begins.
     */
    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void restockitem$onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        ToolHandler.switchToBestTool(minecraft, pos);
    }

    /**
     * Also hook into continueDestroyBlock for cases where the player moves their 
     * cursor to a new block while holding the button.
     */
    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void restockitem$onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        ToolHandler.switchToBestTool(minecraft, pos);
    }
}
