package de.keksuccino.drippyloadingscreen.mixin.mixins.common.client;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MixinLoadingOverlayTest {

    @Test
    void logoSuppressionTargetsTheCurrentMinecraftBlitCall() throws IOException {
        ClassNode mixin = readClass("de/keksuccino/drippyloadingscreen/mixin/mixins/common/client/MixinLoadingOverlay");
        var handler = mixin.methods.stream().filter(method -> method.name.equals("cancelOriginalLogoRenderingDrippy")).findFirst().orElseThrow();
        AnnotationNode injection = handler.visibleAnnotations.stream().filter(annotation -> annotation.desc.endsWith("/WrapWithCondition;")).findFirst().orElseThrow();
        AnnotationNode at = (AnnotationNode) ((List<?>) annotationValue(injection, "at")).getFirst();
        String target = (String) annotationValue(at, "target");
        ClassNode overlay = readClass("net/minecraft/client/gui/screens/LoadingOverlay");
        var render = overlay.methods.stream().filter(method -> method.name.equals("extractRenderState")).findFirst().orElseThrow();
        int matches = 0;
        for (var instruction : render.instructions) {
            if (instruction instanceof MethodInsnNode call && target.equals("L" + call.owner + ";" + call.name + call.desc)) {
                matches++;
            }
        }
        // Both halves of the vanilla logo must be intercepted, including the RenderPearl parameter type.
        assertEquals(2, matches);
    }

    private static Object annotationValue(AnnotationNode annotation, String name) {
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (name.equals(annotation.values.get(i))) return annotation.values.get(i + 1);
        }
        throw new AssertionError("Missing annotation member: " + name);
    }

    private static ClassNode readClass(String name) throws IOException {
        try (InputStream stream = MixinLoadingOverlayTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }

}
