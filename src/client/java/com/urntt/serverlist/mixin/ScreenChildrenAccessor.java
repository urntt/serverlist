package com.urntt.serverlist.mixin;

import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenChildrenAccessor {
	/** The screen's event listeners in keyboard navigation order. */
	@Accessor("children")
	List<GuiEventListener> serverlist$children();
}
