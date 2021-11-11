package de.keksuccino.drippyloadingscreen.mixin.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = SplashOverlay.class)
public abstract class MixinResourceLoadProgressGui extends DrawableHelper {

	protected MinecraftClient mc = MinecraftClient.getInstance();

	@Shadow @Final private ResourceReload reload;
	@Shadow @Final private Consumer<Optional<Throwable>> exceptionHandler;
	@Shadow @Final private boolean reloading;
	@Shadow private float progress;
	@Shadow private long reloadCompleteTime;
	@Shadow private long reloadStartTime;

	protected boolean isUpdated = false;
	protected int lastWidth = 0;
	protected int lastHeight = 0;

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	protected void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {

		SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

		int screenWidth = this.mc.getWindow().getScaledWidth();
		int screenHeight = this.mc.getWindow().getScaledHeight();
		long time = Util.getMeasuringTimeMs();

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

		if (this.reloading && this.reloadStartTime == -1L) {
			this.reloadStartTime = time;
		}

		float f = this.reloadCompleteTime > -1L ? (float)(time - this.reloadCompleteTime) / 1000.0F : -1.0F;
		float g = this.reloadStartTime > -1L ? (float)(time - this.reloadStartTime) / 500.0F : -1.0F;
		if (f >= 1.0F) {
			if (this.mc.currentScreen != null) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.currentScreen.render(matrices, 0, 0, delta);
				}
			}

		} else if (this.reloading) {
			if (this.mc.currentScreen != null && g < 1.0F) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.currentScreen.render(matrices, mouseX, mouseY, delta);
				}
			}

		}

		float y = this.reload.getProgress();
		this.progress = MathHelper.clamp(this.progress * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

		if (f >= 2.0F) {
			this.resetScale(handler);
			this.mc.setOverlay(null);
		}

		if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || g >= 2.0F)) {
			try {
				this.reload.throwException();
				this.exceptionHandler.accept(Optional.empty());
			} catch (Throwable var23) {
				this.exceptionHandler.accept(Optional.of(var23));
			}

			this.reloadCompleteTime = Util.getMeasuringTimeMs();
			if (this.mc.currentScreen != null) {
				this.mc.currentScreen.init(this.mc, this.mc.getWindow().getScaledWidth(), this.mc.getWindow().getScaledHeight());
			}
		}

		//---------------------------------

		//Give all important fields to the handler so elements can use them (only as getter ofc)
		handler.reload = this.reload;
		handler.exceptionHandler = this.exceptionHandler;
		handler.reloading = this.reloading;
		handler.progress = this.progress;
		handler.reloadCompleteTime = this.reloadCompleteTime;
		handler.reloadStartTime = this.reloadStartTime;

		//Render the actual loading screen and all customization items
		handler.renderLayer();

	}

	private static void resetScale(SplashCustomizationLayer handler) {
		if (handler.scaled) {

			MinecraftClient mc = MinecraftClient.getInstance();
			Window w = mc.getWindow();
			int mcScale = w.calculateScaleFactor(mc.options.guiScale, mc.forcesUnicodeFont());

			w.setScaleFactor((double)mcScale);

			int screenWidth = w.getScaledWidth();
			int screenHeight = w.getScaledHeight();

			mc.currentScreen.init(mc, screenWidth, screenHeight);

			handler.scaled = false;

		}
	}

}
