package de.keksuccino.drippyloadingscreen.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.resources.IAsyncReloader;
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

@Mixin(value = ResourceLoadProgressGui.class)
public abstract class MixinResourceLoadProgressGui extends AbstractGui {

	protected Minecraft mc = Minecraft.getInstance();

	@Shadow @Final private IAsyncReloader asyncReloader;
	@Shadow @Final private Consumer<Optional<Throwable>> completedCallback;
	@Shadow @Final private boolean reloading;
	@Shadow private long fadeOutStart;
	@Shadow private long fadeInStart;
	@Shadow private float progress;

	protected boolean isUpdated = false;
	protected int lastWidth = 0;
	protected int lastHeight = 0;

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	protected void onRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

		SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

		int screenWidth = this.mc.getMainWindow().getScaledWidth();
		int screenHeight = this.mc.getMainWindow().getScaledHeight();
		long time = Util.milliTime();

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

		if (this.reloading && (this.asyncReloader.asyncPartDone() || this.mc.currentScreen != null) && this.fadeInStart == -1L) {
			this.fadeInStart = time;
		}

		float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
		float f1 = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
		if (f >= 1.0F) {
			if (this.mc.currentScreen != null) {
				//TODO übernehmen
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.currentScreen.render(matrix, 0, 0, partialTicks);
				}
			}
		} else if (this.reloading) {
			if (this.mc.currentScreen != null && f1 < 1.0F) {
				//TODO übernehmen
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.currentScreen.render(matrix, mouseX, mouseY, partialTicks);
				}
			}
		}

		float f3 = this.asyncReloader.estimateExecutionSpeed();
		this.progress = MathHelper.clamp(this.progress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);

		if (f >= 2.0F) {
			//TODO übernehmen
			this.resetScale(handler);
			this.mc.setLoadingGui(null);
		}

		if (this.fadeOutStart == -1L && this.asyncReloader.fullyDone() && (!this.reloading || f1 >= 2.0F)) {
			this.fadeOutStart = Util.milliTime();
			try {
				this.asyncReloader.join();
				this.completedCallback.accept(Optional.empty());
			} catch (Throwable throwable) {
				this.completedCallback.accept(Optional.of(throwable));
			}

			if (this.mc.currentScreen != null) {
				this.mc.currentScreen.init(this.mc, screenWidth, screenHeight);
			}
		}

		//Give all important fields to the handler so elements can use them (only as getter ofc)
		handler.asyncReloader = this.asyncReloader;
		handler.completedCallback = this.completedCallback;
		handler.reloading = this.reloading;
		handler.fadeOutStart = this.fadeOutStart;
		handler.fadeInStart = this.fadeInStart;
		handler.progress = this.progress;

		//Render the actual loading screen and all customization items
		handler.renderLayer();

	}

	//TODO übernehmen
	private static void resetScale(SplashCustomizationLayer handler) {
		if (handler.scaled) {

			Minecraft mc = Minecraft.getInstance();
			MainWindow w = mc.getMainWindow();
			int mcScale = w.calcGuiScale(mc.gameSettings.guiScale, mc.getForceUnicodeFont());

			w.setGuiScale((double)mcScale);

			int screenWidth = w.getScaledWidth();
			int screenHeight = w.getScaledHeight();

			mc.currentScreen.init(mc, screenWidth, screenHeight);

			handler.scaled = false;

		}
	}

}
