package de.keksuccino.drippyloadingscreen.mixin.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.rendering.splash.SplashCustomizationLayer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = LoadingOverlay.class)
public abstract class MixinLoadingOverlay extends GuiComponent {

	protected Minecraft mc = Minecraft.getInstance();

	@Shadow @Final private ReloadInstance reload;
	@Shadow @Final private Consumer<Optional<Throwable>> onFinish;
	@Shadow @Final private boolean fadeIn;

	private float currentProgressNotShadowed;
	private long fadeOutStartNotShadowed = -1L;
	private long fadeInStartNotShadowed = -1L;

	protected boolean isUpdated = false;
	protected int lastWidth = 0;
	protected int lastHeight = 0;

	//Is called in onRender mixin instead
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"), method = "*")
	private void cancelScreenRendering(Screen screen, PoseStack matrix, int i1, int i2, float f) {}

	//Is called in onRender mixin instead
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "*")
	private void cancelInitScreenCall(Screen s, Minecraft p_96607_, int p_96608_, int p_96609_) {}

	//Is called in onRender mixin instead
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"), method = "*")
	private void cancelSetOverlayCall(Minecraft mc, Overlay overlay) {}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"), method = "*")
	private void cancelFillCall(PoseStack p_93173_, int p_93174_, int p_93175_, int p_93176_, int p_93177_, int p_93178_) {}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIFFIIII)V"), method = "*")
	private void cancelBlitCall(PoseStack p_93161_, int p_93162_, int p_93163_, int p_93164_, int p_93165_, float p_93166_, float p_93167_, int p_93168_, int p_93169_, int p_93170_, int p_93171_) {}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fmlclient/ClientModLoader;renderProgressText()V", remap = false), method = "*")
	private void cancelForgeStatusRendering() {}

	//Is called in onRender mixin instead
	@Redirect(at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"), method = "*", remap = false)
	private void cancelAcceptCall(Consumer c, Object o) {}

	//Is called in onRender mixin instead
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadInstance;checkExceptions()V"), method = "*")
	private void cancelCheckExceptionsCall(ReloadInstance i) {}

	@Inject(at = @At("TAIL"), method = "render")
	protected void onRender(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {

		SplashCustomizationLayer handler = SplashCustomizationLayer.getInstance();

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

		//-------------------------------------

		if (this.fadeIn && this.fadeInStartNotShadowed == -1L) {
			this.fadeInStartNotShadowed = time;
		}

		float f = this.fadeOutStartNotShadowed > -1L ? (float)(time - this.fadeOutStartNotShadowed) / 1000.0F : -1.0F;
		float g = this.fadeInStartNotShadowed > -1L ? (float)(time - this.fadeInStartNotShadowed) / 500.0F : -1.0F;
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
		this.currentProgressNotShadowed = Mth.clamp(this.currentProgressNotShadowed * 0.95F + y * 0.050000012F, 0.0F, 1.0F);

		if (f >= 2.0F) {
			this.resetScale(handler);
			this.mc.setOverlay(null);
		}

		if (this.fadeOutStartNotShadowed == -1L && this.reload.isDone() && (!this.fadeIn || g >= 2.0F)) {
			try {
				this.reload.checkExceptions();
				this.onFinish.accept(Optional.empty());
			} catch (Throwable var23) {
				this.onFinish.accept(Optional.of(var23));
			}

			this.fadeOutStartNotShadowed = Util.getMillis();
			if (this.mc.screen != null) {
				this.mc.screen.init(this.mc, this.mc.getWindow().getGuiScaledWidth(), this.mc.getWindow().getGuiScaledHeight());
			}
		}

		//---------------------------------

		//Give all important fields to the handler so elements can use them (only as getter ofc)
		handler.reload = this.reload;
		handler.onFinish = this.onFinish;
		handler.fadeIn = this.fadeIn;
		handler.currentProgress = this.currentProgressNotShadowed;
		handler.fadeOutStart = this.fadeOutStartNotShadowed;
		handler.fadeInStart = this.fadeInStartNotShadowed;

		//Render the actual loading screen and all customization items
		handler.renderLayer();

	}

	private static void resetScale(SplashCustomizationLayer handler) {
		if (handler.scaled) {

			Minecraft mc = Minecraft.getInstance();
			Window w = mc.getWindow();
			int mcScale = w.calculateScale(mc.options.guiScale, mc.isEnforceUnicode());

			w.setGuiScale((double)mcScale);

			int screenWidth = w.getGuiScaledWidth();
			int screenHeight = w.getGuiScaledHeight();

			mc.screen.init(mc, screenWidth, screenHeight);

			handler.scaled = false;

		}
	}

}
