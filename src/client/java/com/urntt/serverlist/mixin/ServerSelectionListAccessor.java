package com.urntt.serverlist.mixin;

import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerSelectionList.class)
public interface ServerSelectionListAccessor {
	@Accessor("THREAD_POOL")
	static ThreadPoolExecutor serverlist$threadPool() {
		throw new AssertionError();
	}

	@Accessor("PING_1_SPRITE")
	static Identifier serverlist$ping1Sprite() {
		throw new AssertionError();
	}

	@Accessor("PING_2_SPRITE")
	static Identifier serverlist$ping2Sprite() {
		throw new AssertionError();
	}

	@Accessor("PING_3_SPRITE")
	static Identifier serverlist$ping3Sprite() {
		throw new AssertionError();
	}

	@Accessor("PING_4_SPRITE")
	static Identifier serverlist$ping4Sprite() {
		throw new AssertionError();
	}

	@Accessor("PING_5_SPRITE")
	static Identifier serverlist$ping5Sprite() {
		throw new AssertionError();
	}

	@Accessor("MOVE_UP_SPRITE")
	static Identifier serverlist$moveUpSprite() {
		throw new AssertionError();
	}

	@Accessor("MOVE_UP_HIGHLIGHTED_SPRITE")
	static Identifier serverlist$moveUpHighlightedSprite() {
		throw new AssertionError();
	}

	@Accessor("MOVE_DOWN_SPRITE")
	static Identifier serverlist$moveDownSprite() {
		throw new AssertionError();
	}

	@Accessor("MOVE_DOWN_HIGHLIGHTED_SPRITE")
	static Identifier serverlist$moveDownHighlightedSprite() {
		throw new AssertionError();
	}
}
