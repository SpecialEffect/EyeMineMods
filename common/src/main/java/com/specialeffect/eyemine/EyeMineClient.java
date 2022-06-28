package com.specialeffect.eyemine;

import com.specialeffect.eyemine.client.CreativeClientHelper;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.MainClientHandler;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.mining.ContinuouslyMine;
import com.specialeffect.eyemine.submod.mining.DwellMine;
import com.specialeffect.eyemine.submod.mining.GatherDrops;
import com.specialeffect.eyemine.submod.mining.MineOne;
import com.specialeffect.eyemine.submod.misc.AutoOpenDoors;
import com.specialeffect.eyemine.submod.misc.AutoPillar;
import com.specialeffect.eyemine.submod.misc.ContinuouslyAttack;
import com.specialeffect.eyemine.submod.misc.DefaultConfigForNewWorld;
import com.specialeffect.eyemine.submod.misc.DwellBuild;
import com.specialeffect.eyemine.submod.misc.IronSights;
import com.specialeffect.eyemine.submod.misc.NightVisionHelper;
import com.specialeffect.eyemine.submod.misc.OpenChat;
import com.specialeffect.eyemine.submod.misc.OpenTablesChests;
import com.specialeffect.eyemine.submod.misc.PickBlock;
import com.specialeffect.eyemine.submod.misc.QuickCommands;
import com.specialeffect.eyemine.submod.misc.SwapMinePlace;
import com.specialeffect.eyemine.submod.misc.UseItem;
import com.specialeffect.eyemine.submod.mouse.MouseHandlerMod;
import com.specialeffect.eyemine.submod.movement.AutoFly;
import com.specialeffect.eyemine.submod.movement.AutoJump;
import com.specialeffect.eyemine.submod.movement.Dismount;
import com.specialeffect.eyemine.submod.movement.EasyLadderClimb;
import com.specialeffect.eyemine.submod.movement.MoveWithGaze;
import com.specialeffect.eyemine.submod.movement.MoveWithGaze2;
import com.specialeffect.eyemine.submod.movement.Sneak;
import com.specialeffect.eyemine.submod.movement.Swim;
import com.specialeffect.eyemine.submod.utils.DebugAverageFps;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;

import java.util.ArrayList;
import java.util.List;

public class EyeMineClient {
	public static final List<SubMod> subModList = new ArrayList<>();
	public static boolean setupComplete;
	public static boolean allowMoreOptions = false;
	public static boolean disableCustomNewWorld = false;

	public static void init() {
		//Initialize the first StateOverlay
		MainClientHandler.initialize();

		// Setup all other sub mods
		instantiateSubMods();

		ClientGuiEvent.SET_SCREEN.register(MainClientHandler::onGuiOpen);
		ClientGuiEvent.RENDER_HUD.register(MainClientHandler::onRenderGameOverlayEvent);
		ClientRawInputEvent.KEY_PRESSED.register(CreativeClientHelper::onKeyInput);

		ClientLifecycleEvent.CLIENT_SETUP.register((state) -> {
			if (!Keybindings.keybindings.isEmpty()) {
				for (KeyMapping keyBinding : Keybindings.keybindings) {
					KeyMappingRegistry.register(keyBinding);
				}
			}
		});

		ClientLifecycleEvent.CLIENT_STARTED.register((state) -> {
			setupComplete = true;
			refresh();
		});
	}

	private static void instantiateSubMods() {
		setupSubMod(new ContinuouslyMine());
		setupSubMod(new GatherDrops());
		setupSubMod(new MineOne());
		setupSubMod(new AutoOpenDoors());
		setupSubMod(new AutoPillar());
		setupSubMod(new ContinuouslyAttack());
		setupSubMod(new OpenChat());
		setupSubMod(new OpenTablesChests());
		setupSubMod(new PickBlock());
		setupSubMod(new QuickCommands());
		setupSubMod(new SwapMinePlace());
		setupSubMod(new UseItem());
		setupSubMod(new MouseHandlerMod());
		setupSubMod(new DwellBuild());
		setupSubMod(new DwellMine());
		setupSubMod(new AutoFly());
		setupSubMod(new AutoJump());
		setupSubMod(new Dismount());
		setupSubMod(new EasyLadderClimb());
		setupSubMod(new MoveWithGaze());
		setupSubMod(new MoveWithGaze2());
		setupSubMod(new Sneak());
		setupSubMod(new Swim());
		setupSubMod(new DebugAverageFps());
		setupSubMod(new DefaultConfigForNewWorld());
		setupSubMod(new IronSights());
		setupSubMod(new NightVisionHelper());
	}

	private static void setupSubMod(SubMod mod) {
		mod.onInitializeClient();
		subModList.add(mod);
	}

	public static void refresh() {
		if (setupComplete) {
			for (SubMod child : subModList) {
				if (child instanceof IConfigListener configListener) {
					configListener.syncConfig();
				}
			}
		}
	}
}
