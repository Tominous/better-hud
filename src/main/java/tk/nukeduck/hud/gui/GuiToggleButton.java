package tk.nukeduck.hud.gui;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import tk.nukeduck.hud.util.ISaveLoad;

public class GuiToggleButton extends GuiButton implements ISaveLoad {
	public boolean updateText = false;
	public String unlocalized;

	public GuiToggleButton(int buttonId, int x, int y, String buttonText, boolean updateText) {
		super(buttonId, x, y, buttonText);
		unlocalized = buttonText;
		this.updateText = updateText;
		updateText();
	}

	public GuiToggleButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, boolean updateText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		unlocalized = buttonText;
		if(this.updateText = updateText) updateText();
	}

	private boolean value = false;

	public void set(boolean value) {
		this.value = value;
		if(updateText) updateText();
	}

	public boolean get() {
		return value;
	}

	public void toggle() {
		set(!get());
	}

	private void updateText() {
		displayString = I18n.format(unlocalized) + ": " + (get() ? ChatFormatting.GREEN : ChatFormatting.RED) + I18n.format(get() ? "options.on" : "options.off");
	}

	@Override
	protected int getHoverState(boolean mouseOver) {
		return get() && enabled ? 2 : super.getHoverState(mouseOver);
	}

	@Override
	public String save() {
		return String.valueOf(get());
	}

	@Override
	public void load(String save) {
		value = Boolean.parseBoolean(save);
	}
}
