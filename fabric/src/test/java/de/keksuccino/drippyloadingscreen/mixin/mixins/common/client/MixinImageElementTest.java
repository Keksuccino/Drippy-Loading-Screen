package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class MixinImageElementTest {

    @Test
    void usesFallbackBeforeResourceShadersAreInstalled() throws Exception {
        assertFalse(hasShaders(null));
    }

    @ParameterizedTest
    @CsvSource({"false, false, false", "true, false, false", "false, true, false", "true, true, true"})
    void requiresBothShaderStages(boolean vertex, boolean fragment, boolean expected) throws Exception {
        EnumSet<ShaderType> stages = EnumSet.noneOf(ShaderType.class);
        if (vertex) stages.add(ShaderType.VERTEX);
        if (fragment) stages.add(ShaderType.FRAGMENT);
        assertEquals(expected, hasShaders(source(stages)));
    }

    @Test
    void rechecksAvailabilityAfterResourceReloads() throws Exception {
        EnumSet<ShaderType> stages = EnumSet.of(ShaderType.VERTEX, ShaderType.FRAGMENT);
        ShaderSource source = source(stages);
        assertTrue(hasShaders(source));
        stages.remove(ShaderType.FRAGMENT);
        assertFalse(hasShaders(source));
        stages.add(ShaderType.FRAGMENT);
        assertTrue(hasShaders(source));
        assertFalse(hasShaders(null));
        assertFalse(hasShaders(source(EnumSet.noneOf(ShaderType.class))));
    }

    private static ShaderSource source(EnumSet<ShaderType> stages) {
        return (ShaderSource) Proxy.newProxyInstance(ShaderSource.class.getClassLoader(), new Class<?>[]{ShaderSource.class}, (proxy, method, args) -> {
            // A source-only probe must never compile a pipeline, resolve includes, or close the source.
            assertEquals("getShader", method.getName());
            assertEquals(Identifier.withDefaultNamespace("core/fancymenu_gui_smooth_image_rect"), args[0]);
            return stages.contains(args[1]) ? "void main() {}" : null;
        });
    }

    private static boolean hasShaders(ShaderSource source) throws Exception {
        // Exercise the actual mixin guard without bootstrapping Minecraft or transforming its classes.
        Method guard = MixinImageElement.class.getDeclaredMethod("hasSmoothImageShaders_Drippy", ShaderSource.class);
        guard.setAccessible(true);
        return (boolean) guard.invoke(null, source);
    }

}
