package tk.nukeduck.hud.element.vanilla;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;
import static tk.nukeduck.hud.BetterHud.MANAGER;
import static tk.nukeduck.hud.BetterHud.MC;

import java.util.Arrays;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import tk.nukeduck.hud.BetterHud;
import tk.nukeduck.hud.util.Bounds;
import tk.nukeduck.hud.util.Direction;
import tk.nukeduck.hud.util.bars.StatBar;

public class HealthBar extends Bar {
	public HealthBar() {
		super("health", new Bounds(16, 0, 9, 9), new Bounds(61, 0, 9, 9), new Bounds(52, 0, 9, 9));
	}

	@Override
	public void loadDefaults() {
		super.loadDefaults();
		settings.priority.set(3);
		side.setIndex(0);
	}

	@Override
	protected ElementType getType() {
		return ElementType.HEALTH;
	}

	@Override
	protected int getCurrent() {
		return (int)MC.player.getHealth();
	}

	@Override
	protected int getMaximum() {
		return (int)MC.player.getMaxHealth();
	}

	private long lastSystemTime;
	private int healthUpdateCounter, prevHealth, health;

	private Bounds getIconTexture(IconType type, boolean flash, boolean absorption) {
		int y = MC.world.getWorldInfo().isHardcoreModeEnabled() ? 45 : 0;

		if(type == IconType.BACKGROUND) {
			return new Bounds(flash ? 25 : 16, y, 9, 9);
		} else {
			int x;
			if(absorption) {
				x = 160;
			} else if(MC.player.isPotionActive(MobEffects.POISON)) {
				x = 88;
			} else if(MC.player.isPotionActive(MobEffects.WITHER)) {
				x = 124;
			} else {
				x = 52;
			}

			if(flash) x += 18;
			if(type == IconType.HALF) x += 9;

			return new Bounds(x, y, 9, 9);
		}
	}

	private Bounds renderHealth() {
		MC.getTextureManager().bindTexture(BetterHud.ICONS);
		EntityPlayer player = (EntityPlayer)MC.getRenderViewEntity();
		GlStateManager.enableBlend();

		int health = MathHelper.ceil(player.getHealth());

		int updateCounter = MC.ingameGUI.getUpdateCounter();

		int updateDelta = healthUpdateCounter - updateCounter;
		boolean flash = updateDelta > 0 && updateDelta % 6 >= 3;

		long systemTime = Minecraft.getSystemTime();

		if(health != this.health && player.hurtResistantTime > 0) {
			lastSystemTime = systemTime;
			healthUpdateCounter += health < this.health ? 20 : 10;
		} else if(systemTime > lastSystemTime + 1000) {
			lastSystemTime = systemTime;
			prevHealth = health;
		}

		this.health = health;
		int prevHealth = this.prevHealth;

		int maxHealth   = MathHelper.ceil(player.getMaxHealth());
		int extraHealth = MathHelper.ceil(player.getAbsorptionAmount());

		int rows = BetterHud.ceilDiv(maxHealth + extraHealth, 20);
		int rowSpacing = MathHelper.clamp(12 - rows, 3, 10);

		Bounds bounds = new Bounds(81, rowSpacing * (rows - 1) + 9);
		bounds = MANAGER.positionBar(bounds, Direction.WEST, 1);

		Random random = new Random(updateCounter * 312871);

		int regen = player.isPotionActive(MobEffects.REGENERATION) ? healthUpdateCounter % 25 : -1;

		Bounds bgTexture = getIconTexture(IconType.BACKGROUND, flash, false);
		Bounds halfTexture = getIconTexture(IconType.HALF, flash, false);
		Bounds fullTexture = getIconTexture(IconType.FULL, flash, false);
		float absorbRemaining = extraHealth;

		for(int i = BetterHud.ceilDiv(maxHealth + extraHealth, 2) - 1; i >= 0; i--) {
			//int b0 = (highlight ? 1 : 0);
			int row = MathHelper.ceil((float)(i + 1) / 10.0F) - 1;
			int x = left + i % 10 * 8;
			int y = top - row * rowHeight;

			if (health <= 4) y += rand.nextInt(2);
			if (i == regen) y -= 2;

			drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

			if (highlight)
			{
				if (i * 2 + 1 < healthLast)
					drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
				else if (i * 2 + 1 == healthLast)
					drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
			}

			if (absorbRemaining > 0.0F)
			{
				if (absorbRemaining == extraHealth && extraHealth % 2.0F == 1.0F)
				{
					drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
					absorbRemaining -= 1.0F;
				}
				else
				{
					drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
					absorbRemaining -= 2.0F;
				}
			}
			else
			{
				if (i * 2 + 1 < health)
					drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
				else if (i * 2 + 1 == health)
					drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
			}
		}
	}
}