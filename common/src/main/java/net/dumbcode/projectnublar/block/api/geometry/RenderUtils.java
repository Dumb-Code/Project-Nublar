package net.dumbcode.projectnublar.block.api.geometry;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class RenderUtils {
    public static void drawCubeoid(PoseStack stack, Vec3 si, Vec3 ei, VertexConsumer buff) {
        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();

        Vector3f s = new Vector3f((float) si.x(), (float) si.y(), (float) si.z());
        Vector3f e = new Vector3f((float) ei.x(), (float) ei.y(), (float) ei.z());

//        buff.(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 0, 1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 0, -1, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, -1, 0, 0).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), e.z()).normal(normal, 0, 0, 1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), s.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, s.x(), e.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), e.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
        buff.vertex(pose, e.x(), s.y(), s.z()).normal(normal, 0, 0, -1).color(1F, 1F, 1F, 1F).endVertex();
    }

//    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td, VertexConsumer buff) {
//        drawSpacedCube(buff, ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urfx, urfy, urfz, urbx, urby, urbz, dlfx, dlfy, dlfz, dlbx, dlby, dlbz, drfx, drfy, drfz, drbx, drby, drbz, uu, uv, du, dv, lu, lv, ru, rv, fu, fv, bu, bv, tw, th, td);
//    }

    //ulf, ulb, urf, urb, dlf, dlb, drf, drb
    public static void drawSpacedCube(PoseStack stack, VertexConsumer buff, float r, float g, float b, float a, int light, int overlay, float ulfx, float ulfy, float ulfz, float ulbx, float ulby, float ulbz, float urfx, float urfy, float urfz, float urbx, float urby, float urbz, float dlfx, float dlfy, float dlfz, float dlbx, float dlby, float dlbz, float drfx, float drfy, float drfz, float drbx, float drby, float drbz, float uu, float uv, float du, float dv, float lu, float lv, float ru, float rv, float fu, float fv, float bu, float bv, float tw, float th, float td) {
        Vector3f xNorm = MathUtils.calculateNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calculateNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calculateNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);

        Matrix4f pose = stack.last().pose();
        Matrix3f normal = stack.last().normal();

        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(fu, fv).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(fu, fv + th).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(fu + td, fv + th).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(fu + td, fv).overlayCoords(overlay).uv2(light).normal(normal, xNorm.x(), xNorm.y(), xNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(bu, bv).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(bu, bv + th).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(bu + td, bv + th).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(bu + td, bv).overlayCoords(overlay).uv2(light).normal(normal, -xNorm.x(), -xNorm.y(), -xNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(uu, uv).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(uu, uv + tw).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(uu + td, uv + tw).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(uu + td, uv).overlayCoords(overlay).uv2(light).normal(normal, yNorm.x(), yNorm.y(), yNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(du, dv).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(du, dv + tw).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(du + td, dv + tw).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(du + td, dv).overlayCoords(overlay).uv2(light).normal(normal, -yNorm.x(), -yNorm.y(), -yNorm.z()).endVertex();
        buff.vertex(pose, drfx, drfy, drfz).color(r, g, b, a).uv(ru, rv).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, urfx, urfy, urfz).color(r, g, b, a).uv(ru + th, rv).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, urbx, urby, urbz).color(r, g, b, a).uv(ru + th, rv + tw).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, drbx, drby, drbz).color(r, g, b, a).uv(ru, rv + tw).overlayCoords(overlay).uv2(light).normal(normal, zNorm.x(), zNorm.y(), zNorm.z()).endVertex();
        buff.vertex(pose, ulfx, ulfy, ulfz).color(r, g, b, a).uv(lu, lv).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, dlfx, dlfy, dlfz).color(r, g, b, a).uv(lu + th, lv).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, dlbx, dlby, dlbz).color(r, g, b, a).uv(lu + th, lv + tw).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
        buff.vertex(pose, ulbx, ulby, ulbz).color(r, g, b, a).uv(lu, lv + tw).overlayCoords(overlay).uv2(light).normal(normal, -zNorm.x(), -zNorm.y(), -zNorm.z()).endVertex();
    }

    public static void renderBoxLines(PoseStack stack, VertexConsumer buff, Vector3f[] points, Direction... blocked) { //todo: color params
        renderBoxLines(
                stack, buff,
                Arrays.stream(points).map(Vec3::new).toArray(Vec3[]::new),
                blocked
        );
    }

    public static void renderBoxLines(PoseStack stack, VertexConsumer buff, Vec3[] points, Direction... blocked) { //todo: color params
        renderLineSegment(stack, buff, points, blocked, 0b100, 0b101, 0b111, 0b110);
        renderLineSegment(stack, buff, points, blocked, 0b000, 0b001, 0b011, 0b010);
        renderLineSegment(stack, buff, points, blocked, 0b011, 0b111);
        renderLineSegment(stack, buff, points, blocked, 0b110, 0b010);
        renderLineSegment(stack, buff, points, blocked, 0b001, 0b101);
        renderLineSegment(stack, buff, points, blocked, 0b100, 0b000);
    }

    public static void renderLineSegment(PoseStack stack, VertexConsumer buff, Vec3[] points, Direction[] blocked, int... ints) {
        Matrix4f pose = stack.last().pose();
        over:
        for (int i = 0; i < ints.length; i++) {
            int nextID = (i + 1) % ints.length;
            if (ints.length == 2 && i == 1) {
                break;
            }
            Vec3 vec = points[ints[i]];
            Vec3 next = points[ints[nextID]];
            for (Direction face : blocked) {
                int bit = face.getAxis().ordinal();
                int shifted = (ints[i] >> bit) & 1;
                if (shifted == ((ints[nextID] >> bit) & 1) && shifted == face.getAxisDirection().ordinal()) {
                    continue over;
                }
            }
            buff.vertex(pose, (float) vec.x, (float) vec.y, (float) vec.z).color(0f, 0f, 0f, 0.4f).endVertex();
            buff.vertex(pose, (float) next.x, (float) next.y, (float) next.z).color(0f, 0f, 0f, 0.4f).endVertex();

        }
    }

//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, VertexConsumer buff) {
//        drawTextureAtlasSprite(x, y, sprite, width, height, 0F, 0F, 16F, 16F, buff);
//    }
//
//    public static void drawTextureAtlasSprite(double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV, VertexConsumer buff) {
//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//        buff.pos(x, y + height, 0).uv(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(maxV)).endVertex();
//        buff.pos(x + width, y + height, 0).uv(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(maxV)).endVertex();
//        buff.pos(x + width, y, 0).uv(sprite.getInterpolatedU(maxU), sprite.getInterpolatedV(minV)).endVertex();
//        buff.pos(x, y, 0).uv(sprite.getInterpolatedU(minU), sprite.getInterpolatedV(minV)).endVertex();
//    }

    public static void renderBorderExclusive(GuiGraphics stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        renderBorder(stack, left - borderSize, top - borderSize, right + borderSize, bottom + borderSize, borderSize, borderColor);
    }

    public static void renderBorder(GuiGraphics stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        stack.fill(left, top, right, top + borderSize, borderColor);
        stack.fill(left, bottom, right, bottom - borderSize, borderColor);
        stack.fill(left, top, left + borderSize, bottom, borderColor);
        stack.fill(right, top, right - borderSize, bottom, borderColor);
    }

    public static void drawTexturedQuad(PoseStack stack, VertexConsumer buffer, float left, float top, float right, float bottom, float minU, float minV, float maxU, float maxV, float zLevel) {

//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        Matrix4f pose = stack.last().pose();

        buffer.vertex(pose, left, top, zLevel).uv(minU, minV).endVertex();
        buffer.vertex(pose, left, bottom, zLevel).uv(minU, maxV).endVertex();
        buffer.vertex(pose, right, bottom, zLevel).uv(maxU, maxV).endVertex();
        buffer.vertex(pose, right, top, zLevel).uv(maxU, minV).endVertex();

//        Tessellator.getInstance().draw();
    }

    public static void draw256Texture(ResourceLocation rl, GuiGraphics stack, int x, int y, int u, int v, int sizeX, int sizeU) {
        stack.blit(rl, x, y, 0, u, v, sizeX, sizeU, 256, 256);
    }

    public static void drawTextureAtlasSprite(PoseStack stack, double x, double y, TextureAtlasSprite sprite, double width, double height) {
        drawTextureAtlasSprite(stack, x, y, sprite, width, height, 0F, 0F, 16F, 16F);
    }

    public static void drawTextureAtlasSprite(PoseStack stack, double x, double y, TextureAtlasSprite sprite, double width, double height, double minU, double minV, double maxU, double maxV) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        Matrix4f pose = stack.last().pose();

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pose, (float) x, (float) (y + height), 0).uv(sprite.getU(minU), sprite.getV(maxV)).endVertex();
        bufferbuilder.vertex(pose, (float) (x + width), (float) (y + height), 0).uv(sprite.getU(maxU), sprite.getV(maxV)).endVertex();
        bufferbuilder.vertex(pose, (float) (x + width), (float) y, 0).uv(sprite.getU(maxU), sprite.getV(minV)).endVertex();
        bufferbuilder.vertex(pose, (float) x, (float) y, 0).uv(sprite.getU(minU), sprite.getV(minV)).endVertex();
        tessellator.end();
    }

    public static void drawScaledCustomSizeModalRect(PoseStack stack, int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        Matrix4f pose = stack.last().pose();
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(pose, x, (y + height), 0.0F).uv(u * f, (v + vHeight) * f1).endVertex();
        buffer.vertex(pose, (x + width), (y + height), 0.0F).uv((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        buffer.vertex(pose, (x + width), y, 0.0F).uv((u + uWidth) * f, v * f1).endVertex();
        buffer.vertex(pose, x, y, 0.0F).uv(u * f, v * f1).endVertex();
        tessellator.end();
    }

    public static final int PIXELS_PER_TICK = 1;
    public static final int TICKS_WAIT_AT_END = 2;

//    public static void renderScrollingText(GuiGraphics stack, Component text, float scrollTicks, int x, int y, int width, int color) {
//        Minecraft mc = Minecraft.getInstance();
//        int textWidth = mc.font.width(text);
//
//        if (textWidth < width) {
//            stack.drawString(mc.font, text, x, y, color);
//            return;
//        }
//
//        int textTicks = textWidth / PIXELS_PER_TICK;
//        int totalTicks = textTicks + TICKS_WAIT_AT_END;
//        float internalScrollTicks = scrollTicks % totalTicks;
//
//        StencilStack.pushSquareStencil(stack, x, y, x + width, y + mc.font.lineHeight);
//        int pixelsToMove = (int) (internalScrollTicks * PIXELS_PER_TICK);
//        int start = x - pixelsToMove;
//
//        if (internalScrollTicks > textTicks - ((float) width / PIXELS_PER_TICK)) {
//            start = x + width - textWidth;
//        }
//
//        mc.font.draw(stack, text, start, y, color);
//
//        StencilStack.popStencil();
//    }
}