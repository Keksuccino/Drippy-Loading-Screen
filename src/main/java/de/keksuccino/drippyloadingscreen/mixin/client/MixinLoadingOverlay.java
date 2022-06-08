package de.keksuccino.drippyloadingscreen.mixin.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = LoadingOverlay.class)
public abstract class MixinLoadingOverlay extends GuiComponent {

	protected Minecraft mc = Minecraft.getInstance();

	@Shadow @Final private ReloadInstance reload;
	@Shadow @Final private Consumer<Optional<Throwable>> onFinish;
	@Shadow @Final private boolean fadeIn;
	@Shadow private float currentProgress;
	@Shadow private long fadeOutStart;
	@Shadow private long fadeInStart;

	protected boolean isUpdated = false;
	protected int lastWidth = 0;
	protected int lastHeight = 0;

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	protected void onRender(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {

		SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

		ACIHandler.onRenderOverlay(handler);

		int screenWidth = this.mc.getWindow().getGuiScaledWidth();
		int screenHeight = this.mc.getWindow().getGuiScaledHeight();
		long time = Util.getMillis();

		//Handle customization update on window resize
		if ((lastWidth != screenWidth) || (lastHeight != screenHeight)) {
			isUpdated = false;
		}
		lastWidth = screenWidth;
		lastHeight = screenHeight;
		if (!isUpdated) {
			handler.updateCustomizations();
			isUpdated = true;
		}

		info.cancel();

		//-------------------------------------

		if (this.fadeIn && this.fadeInStart == -1L) {
			this.fadeInStart = time;
		}

		float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
		float g = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
		if (f >= 1.0F) {
			if (this.mc.screen != null) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.screen.render(matrices, 0, 0, delta);
				}
			}

		} else if (this.fadeIn) {
			if (this.mc.screen != null && g < 1.0F) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.screen.render(matrices, mouseX, mouseY, delta);
				}
			}

		}

		float y = this.reload.getActualProgress();
		this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

		if (f >= 2.0F) {
			this.resetScale(handler);
			this.mc.setOverlay(null);
		}

		if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || g >= 2.0F)) {
			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable var23) {
				this.onFinish.accept(Optional.of(var23));
			}

			this.fadeOutStart = Util.getMillis();
			if (this.mc.screen != null) {
				this.mc.screen.init(this.mc, this.mc.getWindow().getGuiScaledWidth(), this.mc.getWindow().getGuiScaledHeight());
			}
		}

		//---------------------------------

		//Give all important fields to the handler so elements can use them (only as getter ofc)
		handler.reload = this.reload;
		handler.onFinish = this.onFinish;
		handler.fadeIn = this.fadeIn;
		handler.currentProgress = this.currentProgress;
		handler.fadeOutStart = this.fadeOutStart;
		handler.fadeInStart = this.fadeInStart;

		PlaceholderTextValueHelper.currentLoadingProgressValue = "" + (int)(this.currentProgress * 100.0F);

		//Render the actual loading screen and all customization items
		handler.renderLayer();

	}

	private static void resetScale(SplashCustomizationLayer handler) {
		if (handler.scaled) {

			Minecraft mc = Minecraft.getInstance();
			Window w = mc.getWindow();
			int mcScale = w.calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());

			w.setGuiScale((double)mcScale);

			int screenWidth = w.getGuiScaledWidth();
			int screenHeight = w.getGuiScaledHeight();

			mc.screen.init(mc, screenWidth, screenHeight);

			handler.scaled = false;

		}
	}

}
