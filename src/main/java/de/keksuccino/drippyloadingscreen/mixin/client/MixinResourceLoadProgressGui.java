package de.keksuccino.drippyloadingscreen.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.items.v2.audio.ACIHandler;
import de.keksuccino.drippyloadingscreen.customization.placeholdervalues.PlaceholderTextValueHelper;
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

	@Shadow @Final private IAsyncReloader reload;
	@Shadow @Final private Consumer<Optional<Throwable>> onFinish;
	@Shadow @Final private boolean fadeIn;
	@Shadow private long fadeOutStart;
	@Shadow private long fadeInStart;
	@Shadow private float currentProgress;

	protected boolean isUpdated = false;
	protected int lastWidth = 0;
	protected int lastHeight = 0;

	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	protected void onRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {

		SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

		if (DrippyLoadingScreen.isAuudioLoaded()) {
			ACIHandler.onRenderOverlay(handler);
		}

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

		//--------------------------------------

		if (this.fadeIn && (this.reload.isApplying() || this.mc.screen != null) && this.fadeInStart == -1L) {
			this.fadeInStart = time;
		}

		float f = this.fadeOutStart > -1L ? (float)(time - this.fadeOutStart) / 1000.0F : -1.0F;
		float f1 = this.fadeInStart > -1L ? (float)(time - this.fadeInStart) / 500.0F : -1.0F;
		if (f >= 1.0F) {
			if (this.mc.screen != null) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.screen.render(matrix, 0, 0, partialTicks);
				}
			}
		} else if (this.fadeIn) {
			if (this.mc.screen != null && f1 < 1.0F) {
				if (!DrippyLoadingScreen.isFancyMenuLoaded() && handler.fadeOut) {
					this.mc.screen.render(matrix, mouseX, mouseY, partialTicks);
				}
			}
		}

		float f3 = this.reload.getActualProgress();
		this.currentProgress = MathHelper.clamp(this.currentProgress * 0.95F + f3 * 0.050000012F, 0.0F, 1.0F);

		if (f >= 2.0F) {
			this.resetScale(handler);
			this.mc.setOverlay(null);
		}

		if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f1 >= 2.0F)) {
			this.fadeOutStart = Util.getMillis();
			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable throwable) {
				this.onFinish.accept(Optional.of(throwable));
			}

			if (this.mc.screen != null) {
				this.mc.screen.init(this.mc, screenWidth, screenHeight);
			}
		}

		//---------------------------------

		//Give all important fields to the handler so elements can use them (only as getter ofc)
		handler.asyncReloader = this.reload;
		handler.completedCallback = this.onFinish;
		handler.reloading = this.fadeIn;
		handler.fadeOutStart = this.fadeOutStart;
		handler.fadeInStart = this.fadeInStart;
		handler.progress = this.currentProgress;

		PlaceholderTextValueHelper.currentLoadingProgressValue = "" + (int)(this.currentProgress * 100.0F);

		//Render the actual loading screen and all customization items
		handler.renderLayer();

	}

	private static void resetScale(SplashCustomizationLayer handler) {
		if (handler.scaled) {

			Minecraft mc = Minecraft.getInstance();
			MainWindow w = mc.getWindow();
			int mcScale = w.calculateScale(mc.options.guiScale, mc.isEnforceUnicode());

			w.setGuiScale((double)mcScale);

			int screenWidth = w.getGuiScaledWidth();
			int screenHeight = w.getGuiScaledHeight();

			if (mc.screen != null) {
				mc.screen.init(mc, screenWidth, screenHeight);
			}

			handler.scaled = false;

		}
	}

}
