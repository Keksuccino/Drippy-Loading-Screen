package de.keksuccino.drippyloadingscreen.mixin.client;

import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LoadingOverlay.class)
public interface IMixinLoadingOverlay {

    @Accessor("reload") public ReloadInstance getReloadDrippy();

    @Accessor("onFinish") public Consumer<Optional<Throwable>> getOnFinishDrippy();

    @Accessor("fadeIn") public boolean getFadeInDrippy();

    @Accessor("currentProgress") public float getCurrentProgressDrippy();

    @Accessor("fadeOutStart") public long getFadeOutStartDrippy();

    @Accessor("fadeInStart") public long getFadeInStartDrippy();

}
