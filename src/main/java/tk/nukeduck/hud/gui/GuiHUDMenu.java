package tk.nukeduck.hud.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import tk.nukeduck.hud.BetterHud;
import tk.nukeduck.hud.element.HudElement;
import tk.nukeduck.hud.network.proxy.ClientProxy;
import tk.nukeduck.hud.util.Colors;
import tk.nukeduck.hud.util.SettingsIO;

public class GuiHUDMenu extends GuiScreen {
	public int buttonOffset = 0;
	public int currentPage = 0;
	public int perPage = 10;

	public void initGui() {
		super.initGui();
		this.mc = Minecraft.getMinecraft();

		this.buttonList.clear();
		//boolean flag = true;

		int hW = this.width / 2;
		int yBase = this.height / 16 + 42;

		this.buttonList.add(new GuiButton(0, hW - 152, yBase - 22, 150, 20, I18n.format("menu.returnToGame")));
		this.buttonList.add(new GuiButton(1, hW - 152, yBase, 98, 20, I18n.format("betterHud.menu.enableAll")));
		this.buttonList.add(new GuiButton(2, hW - 49, yBase, 98, 20, I18n.format("betterHud.menu.disableAll")));
		this.buttonList.add(new GuiButton(3, hW + 54, yBase, 98, 20, I18n.format("betterHud.menu.resetDefaults")));

		this.buttonList.add(new GuiButton(4, hW - 129, height - 20 - height / 16, 98, 20, I18n.format("betterHud.menu.lastPage")));
		((GuiButton) buttonList.get(4)).enabled = false;
		this.buttonList.add(new GuiButton(5, hW + 31, height - 20 - height / 16, 98, 20, I18n.format("betterHud.menu.nextPage")));

		// Global settings button
		this.buttonList.add(new GuiButton(6, hW + 2, yBase - 22, 150, 20, I18n.format("betterHud.menu.settings", HudElement.GLOBAL.getLocalizedName())));

		buttonOffset = this.buttonList.size(); // Set the button offset for actual element buttons

		this.perPage = Math.max(1, (int) Math.floor((height / 8 * 7 - 110) / 24));

		// TODO convert to rolling instead of mult
		for(int i = 0; i < HudElement.ELEMENTS.length; i++) {
			HudElement element = HudElement.ELEMENTS[i];

			GuiToggleButton b = new GuiToggleButton(i * 2 + buttonOffset, hW - 126, height / 16 + 78 + ((i % perPage) * 24), 150, 20, element.getUnlocalizedName(), true);
			b.set(element.isEnabled());
			b.enabled = !element.unsupported;
			
			this.buttonList.add(b);

			GuiButton options = new GuiButton(i * 2 + buttonOffset + 1, hW + 26, height / 16 + 78 + ((i % perPage) * 24), 100, 20, I18n.format("betterHud.menu.options"));
			options.enabled = element.hasSettings();
			this.buttonList.add(options);
		}
		updatePage();
	}

	private void closeMe() {
		mc.displayGuiScreen((GuiScreen)null);
		if(this.mc.currentScreen == null) {
			mc.setIngameFocus();
		}

		if(BetterHud.proxy instanceof ClientProxy) {
			SettingsIO.saveSettings(BetterHud.LOGGER, (ClientProxy)BetterHud.proxy);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == 1) {
			closeMe();
		}
	}

	private void updatePage() {
		((GuiButton) buttonList.get(4)).enabled = currentPage != 0;
		((GuiButton) buttonList.get(5)).enabled = currentPage != Math.ceil((float) HudElement.ELEMENTS.length / perPage) - 1;

		for(int i = 0; i < HudElement.ELEMENTS.length; i++) {
			int offset = i * 2 + buttonOffset;
			boolean a = i >= (currentPage * perPage) && i < ((currentPage + 1) * perPage);
			((GuiButton) this.buttonList.get(offset)).visible = a;
			((GuiButton) this.buttonList.get(offset + 1)).visible = a;
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;

		if(id < buttonOffset) {
			switch(id) {
			case 0:
				closeMe();
				break;
			case 1:
				setAll(true);
				break;
			case 2:
				setAll(false);
				break;
			case 3:
				BetterHud.proxy.loadDefaults();
				mc.displayGuiScreen(new GuiHUDMenu());
				break;
			case 4:
				currentPage--;
				updatePage();
				break;
			case 5:
				currentPage++;
				updatePage();
				break;
			case 6:
				GuiElementSettings gui = new GuiElementSettings(HudElement.GLOBAL, this);
				mc.displayGuiScreen(gui);
				break;
			}
		} else {
			for(int i = 0; i < HudElement.ELEMENTS.length; i++) {
				HudElement el = HudElement.ELEMENTS[i];
				int offset = i * 2 + buttonOffset;
				if(id == offset) {
					el.toggleEnabled();
					button.displayString = I18n.format("betterHud.menu.settingButton", el.getLocalizedName(), (el.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED) + I18n.format(el.isEnabled() ? "options.on" : "options.off"));
					((GuiToggleButton)button).set(el.isEnabled());
				} else if(id == offset + 1) { // <
					GuiElementSettings gui = new GuiElementSettings(el, this);
					mc.displayGuiScreen(gui);
				}
			}
		}
	}

	private void setAll(boolean enabled) {
		for(int i = 0; i < buttonList.size(); i++) {
			if(buttonList.get(i) instanceof GuiButton) {
				GuiButton b = (GuiButton) buttonList.get(i);
				if(b.id >= buttonOffset && (b.id - buttonOffset) % 2 == 0) {
					HudElement element = HudElement.ELEMENTS[(b.id - buttonOffset) / 2];
					element.setEnabled(enabled);
					b.displayString = I18n.format("betterHud.menu.settingButton", element.getLocalizedName(), (element.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED) + I18n.format(element.isEnabled() ? "options.on" : "options.off"));
					((GuiToggleButton) b).set(element.isEnabled());
				}
			}
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float p_73863_3_) {
		if(currentPage < 0) {
			currentPage = 0;
		}

		if(currentPage > HudElement.ELEMENTS.length / perPage) {
			currentPage = HudElement.ELEMENTS.length / perPage;
		}

		updatePage();

		this.drawDefaultBackground();
		//this.drawRect(0, 60, width, height - 20, RenderUtil.colorARGB(85, 0, 0, 0));
		super.drawScreen(mouseX, mouseY, p_73863_3_);
		
		for(Object obj : this.buttonList) {
			if(!(obj instanceof GuiToggleButton)) continue;
			GuiButton button = (GuiButton) obj;
			
			if(button.isMouseOver() && !button.enabled) {
				List<String> tooltip = new ArrayList<String>();
				tooltip.add(I18n.format("betterHud.unsupported"));
				this.drawHoveringText(tooltip, mouseX, mouseY);
			}
		}
		
		this.drawCenteredString(this.fontRenderer, I18n.format("betterHud.menu.hudSettings"), this.width / 2, height / 16 + 5, 16777215);

		this.drawString(this.fontRenderer, countEnabled(HudElement.ELEMENTS) + "/" + HudElement.ELEMENTS.length + " enabled", 5, 5, Colors.WHITE);

		/*for(Object button : buttonList) {
        	if(button instanceof GuiButton) {
        		GuiButton b = (GuiButton) button;
        		if(b.displayString == "<" && b.visible) {
	        		ExtraGuiElement e = HudElements.elements[((b.id - buttonOffset) + 1) / 3];
	        		int color = e.countModes() > 1 ? RenderUtil.colorRGB(255, 255, 255) : RenderUtil.colorARGB(85, 85, 85, 85);
	        		this.drawCenteredString(BetterHud.fr, I18n.format("betterHud.mode." + e.modeAt(e.mode)), b.xPosition + 53, b.yPosition + 6, color);
        		}
        	}
        }*/

		this.drawCenteredString(this.fontRenderer, I18n.format("betterHud.menu.page", (currentPage + 1) + "/" + (int) Math.ceil((float) HudElement.ELEMENTS.length / perPage)), width / 2, height - height / 16 - 13, Colors.WHITE);
	}

	public int countEnabled(HudElement[] elements) {
		int i = 0;
		for(HudElement element : elements) {
			if(element.isEnabled()) i++;
		}
		return i;
	}

	/*public static ExtraGuiElement[][] divideArray(ExtraGuiElement[] source, int chunksize) {
        ExtraGuiElement[][] ret = new ExtraGuiElement[(int)Math.ceil(source.length / (double)chunksize)][chunksize];
        int start = 0;
        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,start, start + chunksize);
            start += chunksize;
        }
        return ret;
    }*/
}