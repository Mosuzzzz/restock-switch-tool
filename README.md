# RestockItem & AutoTool Mod

A lightweight Fabric mod for Minecraft 26.1.2 that enhances the survival experience by automating inventory management and tool selection.

## 🚀 Features

### 1. Auto-Restock
Never run out of blocks while building again! 
- Automatically refills your **active hotbar slot** (or off-hand) from your main inventory when a stack is depleted.
- **Smart Detection:** Works with block placement, food consumption, and item usage.
- **Safety Mode:** Automatically pauses while you are moving items in your inventory to prevent interference with manual organization.

### 2. Auto-Tool
Always use the right tool for the job.
- When you start breaking a block, the mod instantly switches your active slot to the **best tool** in your hotbar.
- Supports Pickaxes, Axes, Shovels, and more.
- **Hotbar Only:** Only switches between items in your hotbar (slots 1-9) to keep your main inventory organized.

## 🛠️ Requirements
- **Minecraft:** 26.1.2
- **Fabric Loader:** 0.19.2+
- **Java:** 26

## 📦 Installation
1. Ensure you have **Fabric Loader** installed for 26.1.2.
2. Download the `restockitem-1.0.0.jar` from the `build/libs` folder.
3. Place the JAR file in your Minecraft `mods` folder.

## 🔨 Development
This project uses Gradle. To build the mod from source:
```bash
./gradlew build
```
The resulting JAR will be in `build/libs/`.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

