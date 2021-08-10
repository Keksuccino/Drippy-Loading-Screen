package de.keksuccino.drippyloadingscreen.customization.rendering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ItemRenderUtils extends AbstractGui {

    public static void renderGuiItem(int x, int y, float partialTicks, ItemStack stack, float scale, boolean renderStackSize, boolean renderDurability) {

        Minecraft mc = Minecraft.getInstance();

        x = (int) (x / scale);
        y = (int) (y / scale);

        if (!stack.isEmpty()) {
            RenderSystem.pushMatrix();
            RenderSystem.scalef(scale, scale, scale);

            float f = (float)stack.getAnimationsToGo() - partialTicks;
            if (f > 0.0F) {
                RenderSystem.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                RenderSystem.translatef((float)(x + 8), (float)(y + 12), 0.0F);
                RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                RenderSystem.translatef((float)(-(x + 8)), (float)(-(y + 12)), 0.0F);
            }

            //Renders the actual item and effects(?)
            mc.getItemRenderer().renderItemAndEffectIntoGuiWithoutEntity(stack, x, y);
            if (f > 0.0F) {
                RenderSystem.popMatrix();
            }

            //Renders the stack size and durab bar
            renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, null, renderStackSize, renderDurability);

            RenderSystem.popMatrix();
        }

    }

    protected static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, boolean renderStackSize, boolean renderDurability) {
        if (!stack.isEmpty()) {
            MatrixStack matrixstack = new MatrixStack();
            if ((stack.getCount() != 1 || text != null) && renderStackSize) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                matrixstack.translate(0.0D, 0.0D, (double)(Minecraft.getInstance().getItemRenderer().zLevel + 200.0F));
                IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
                fr.renderString(s, (float)(xPosition + 19 - 2 - fr.getStringWidth(s)), (float)(yPosition + 6 + 3), 16777215, true, matrixstack.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
                irendertypebuffer$impl.finish();
            }

            if (stack.getItem().showDurabilityBar(stack) && renderDurability) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableAlphaTest();
                RenderSystem.disableBlend();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                double health = stack.getItem().getDurabilityForDisplay(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getRGBDurabilityForDisplay(stack);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableAlphaTest();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;
            float f3 = clientplayerentity == null ? 0.0F : clientplayerentity.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());
            if (f3 > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tessellator tessellator1 = Tessellator.getInstance();
                BufferBuilder bufferbuilder1 = tessellator1.getBuffer();
                draw(bufferbuilder1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }

    protected static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos((double)(x + 0), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + 0), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + height), 0.0D).color(red, green, blue, alpha).endVertex();
        renderer.pos((double)(x + width), (double)(y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }

    public static Item getItemByName(String name) {
        Item i = null;
        if (name.contains(":")) {
            String domain = name.split("[:]", 2)[0];
            String nameItem = name.split("[:]", 2)[1];
            i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(domain, nameItem));
            if (i != null) {
                return i;
            }
        }
        i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        return i;
    }

}
