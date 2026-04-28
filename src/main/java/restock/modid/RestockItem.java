package restock.modid;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestockItem implements ModInitializer {
	public static final String MOD_ID = "restockitem";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// RestockItem is fully mixin-driven — no event registration needed here.
		// PlayerInventoryMixin intercepts removeStack() calls and delegates to
		// RestockHandler, which moves a matching stack from the main inventory
		// into any hotbar / off-hand slot that just became empty.
		LOGGER.info("[RestockItem] Loaded — items will be restocked automatically when a hotbar slot runs out.");
	}
}