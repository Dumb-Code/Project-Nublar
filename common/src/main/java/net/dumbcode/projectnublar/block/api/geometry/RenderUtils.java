package net.dumbcode.projectnublar.block.api.geometry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Arrays;

public class RenderUtils {
    public static void drawCubeoid(PoseStack stack, Vec3 si, Vec3 ei, VertexConsumer buff) {
        PoseStack.Pose pose = stack.last();

        Vector3f s = new Vector3f((float) si.x(), (float) si.y(), (float) si.z());
        Vector3f e = new Vector3f((float) ei.x(), (float) ei.y(), (float) ei.z());

//        buff.(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buff.addVertex(pose, s.x(), e.y(), s.z()).setNormal(pose, 0, 1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), e.y(), e.z()).setNormal(pose, 0, 1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), e.z()).setNormal(pose, 0, 1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), s.z()).setNormal(pose, 0, 1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), e.z()).setNormal(pose, 0, -1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), s.z()).setNormal(pose, 0, -1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), s.z()).setNormal(pose, 0, -1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), e.z()).setNormal(pose, 0, -1, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), e.z()).setNormal(pose, 1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), e.z()).setNormal(pose, 1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), s.z()).setNormal(pose, 1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), s.z()).setNormal(pose, 1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), e.z()).setNormal(pose, -1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), e.y(), e.z()).setNormal(pose, -1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), e.y(), s.z()).setNormal(pose, -1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), s.z()).setNormal(pose, -1, 0, 0).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), e.y(), e.z()).setNormal(pose, 0, 0, 1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), e.z()).setNormal(pose, 0, 0, 1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), e.z()).setNormal(pose, 0, 0, 1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), e.z()).setNormal(pose, 0, 0, 1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), s.y(), s.z()).setNormal(pose, 0, 0, -1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, s.x(), e.y(), s.z()).setNormal(pose, 0, 0, -1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), e.y(), s.z()).setNormal(pose, 0, 0, -1).setColor(1F, 1F, 1F, 1F);
        buff.addVertex(pose, e.x(), s.y(), s.z()).setNormal(pose, 0, 0, -1).setColor(1F, 1F, 1F, 1F);
    }

//    public static void drawSpacedCube(double ulfx, double ulfy, double ulfz, double ulbx, double ulby, double ulbz, double urfx, double urfy, double urfz, double urbx, double urby, double urbz, double dlfx, double dlfy, double dlfz, double dlbx, double dlby, double dlbz, double drfx, double drfy, double drfz, double drbx, double drby, double drbz, double uu, double uv, double du, double dv, double lu, double lv, double ru, double rv, double fu, double fv,double bu, double bv, double tw,double th, double td, VertexConsumer buff) {
//        drawSpacedCube(buff, ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urfx, urfy, urfz, urbx, urby, urbz, dlfx, dlfy, dlfz, dlbx, dlby, dlbz, drfx, drfy, drfz, drbx, drby, drbz, uu, uv, du, dv, lu, lv, ru, rv, fu, fv, bu, bv, tw, th, td);
//    }

    //ulf, ulb, urf, urb, dlf, dlb, drf, drb
    public static void drawSpacedCube(PoseStack stack, VertexConsumer buff, float r, float g, float b, float a, int light, int overlay, float ulfx, float ulfy, float ulfz, float ulbx, float ulby, float ulbz, float urfx, float urfy, float urfz, float urbx, float urby, float urbz, float dlfx, float dlfy, float dlfz, float dlbx, float dlby, float dlbz, float drfx, float drfy, float drfz, float drbx, float drby, float drbz, float uu, float uv, float du, float dv, float lu, float lv, float ru, float rv, float fu, float fv, float bu, float bv, float tw, float th, float td) {
        Vector3f xNorm = MathUtils.calculateNormalF(urfx, urfy, urfz, drfx, drfy, drfz, dlfx, dlfy, dlfz);
        Vector3f yNorm = MathUtils.calculateNormalF(ulfx, ulfy, ulfz, ulbx, ulby, ulbz, urbx, urby, urbz);
        Vector3f zNorm = MathUtils.calculateNormalF(drfx, drfy, drfz, urfx, urfy, urfz, urbx, urby, urbz);

        PoseStack.Pose pose = stack.last();

        buff.addVertex(pose, urfx, urfy, urfz).setColor(r, g, b, a).setUv(fu, fv).setOverlay(overlay).setLight(light).setNormal(pose, xNorm.x(), xNorm.y(), xNorm.z());
        buff.addVertex(pose, drfx, drfy, drfz).setColor(r, g, b, a).setUv(fu, fv + th).setOverlay(overlay).setLight(light).setNormal(pose, xNorm.x(), xNorm.y(), xNorm.z());
        buff.addVertex(pose, dlfx, dlfy, dlfz).setColor(r, g, b, a).setUv(fu + td, fv + th).setOverlay(overlay).setLight(light).setNormal(pose, xNorm.x(), xNorm.y(), xNorm.z());
        buff.addVertex(pose, ulfx, ulfy, ulfz).setColor(r, g, b, a).setUv(fu + td, fv).setOverlay(overlay).setLight(light).setNormal(pose, xNorm.x(), xNorm.y(), xNorm.z());
        buff.addVertex(pose, drbx, drby, drbz).setColor(r, g, b, a).setUv(bu, bv).setOverlay(overlay).setLight(light).setNormal(pose, -xNorm.x(), -xNorm.y(), -xNorm.z());
        buff.addVertex(pose, urbx, urby, urbz).setColor(r, g, b, a).setUv(bu, bv + th).setOverlay(overlay).setLight(light).setNormal(pose, -xNorm.x(), -xNorm.y(), -xNorm.z());
        buff.addVertex(pose, ulbx, ulby, ulbz).setColor(r, g, b, a).setUv(bu + td, bv + th).setOverlay(overlay).setLight(light).setNormal(pose, -xNorm.x(), -xNorm.y(), -xNorm.z());
        buff.addVertex(pose, dlbx, dlby, dlbz).setColor(r, g, b, a).setUv(bu + td, bv).setOverlay(overlay).setLight(light).setNormal(pose, -xNorm.x(), -xNorm.y(), -xNorm.z());
        buff.addVertex(pose, ulfx, ulfy, ulfz).setColor(r, g, b, a).setUv(uu, uv).setOverlay(overlay).setLight(light).setNormal(pose, yNorm.x(), yNorm.y(), yNorm.z());
        buff.addVertex(pose, ulbx, ulby, ulbz).setColor(r, g, b, a).setUv(uu, uv + tw).setOverlay(overlay).setLight(light).setNormal(pose, yNorm.x(), yNorm.y(), yNorm.z());
        buff.addVertex(pose, urbx, urby, urbz).setColor(r, g, b, a).setUv(uu + td, uv + tw).setOverlay(overlay).setLight(light).setNormal(pose, yNorm.x(), yNorm.y(), yNorm.z());
        buff.addVertex(pose, urfx, urfy, urfz).setColor(r, g, b, a).setUv(uu + td, uv).setOverlay(overlay).setLight(light).setNormal(pose, yNorm.x(), yNorm.y(), yNorm.z());
        buff.addVertex(pose, dlbx, dlby, dlbz).setColor(r, g, b, a).setUv(du, dv).setOverlay(overlay).setLight(light).setNormal(pose, -yNorm.x(), -yNorm.y(), -yNorm.z());
        buff.addVertex(pose, dlfx, dlfy, dlfz).setColor(r, g, b, a).setUv(du, dv + tw).setOverlay(overlay).setLight(light).setNormal(pose, -yNorm.x(), -yNorm.y(), -yNorm.z());
        buff.addVertex(pose, drfx, drfy, drfz).setColor(r, g, b, a).setUv(du + td, dv + tw).setOverlay(overlay).setLight(light).setNormal(pose, -yNorm.x(), -yNorm.y(), -yNorm.z());
        buff.addVertex(pose, drbx, drby, drbz).setColor(r, g, b, a).setUv(du + td, dv).setOverlay(overlay).setLight(light).setNormal(pose, -yNorm.x(), -yNorm.y(), -yNorm.z());
        buff.addVertex(pose, drfx, drfy, drfz).setColor(r, g, b, a).setUv(ru, rv).setOverlay(overlay).setLight(light).setNormal(pose, zNorm.x(), zNorm.y(), zNorm.z());
        buff.addVertex(pose, urfx, urfy, urfz).setColor(r, g, b, a).setUv(ru + th, rv).setOverlay(overlay).setLight(light).setNormal(pose, zNorm.x(), zNorm.y(), zNorm.z());
        buff.addVertex(pose, urbx, urby, urbz).setColor(r, g, b, a).setUv(ru + th, rv + tw).setOverlay(overlay).setLight(light).setNormal(pose, zNorm.x(), zNorm.y(), zNorm.z());
        buff.addVertex(pose, drbx, drby, drbz).setColor(r, g, b, a).setUv(ru, rv + tw).setOverlay(overlay).setLight(light).setNormal(pose, zNorm.x(), zNorm.y(), zNorm.z());
        buff.addVertex(pose, ulfx, ulfy, ulfz).setColor(r, g, b, a).setUv(lu, lv).setOverlay(overlay).setLight(light).setNormal(pose, -zNorm.x(), -zNorm.y(), -zNorm.z());
        buff.addVertex(pose, dlfx, dlfy, dlfz).setColor(r, g, b, a).setUv(lu + th, lv).setOverlay(overlay).setLight(light).setNormal(pose, -zNorm.x(), -zNorm.y(), -zNorm.z());
        buff.addVertex(pose, dlbx, dlby, dlbz).setColor(r, g, b, a).setUv(lu + th, lv + tw).setOverlay(overlay).setLight(light).setNormal(pose, -zNorm.x(), -zNorm.y(), -zNorm.z());
        buff.addVertex(pose, ulbx, ulby, ulbz).setColor(r, g, b, a).setUv(lu, lv + tw).setOverlay(overlay).setLight(light).setNormal(pose, -zNorm.x(), -zNorm.y(), -zNorm.z());
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
            buff.addVertex(pose, (float) vec.x, (float) vec.y, (float) vec.z).setColor(0f, 0f, 0f, 0.4f);
            buff.addVertex(pose, (float) next.x, (float) next.y, (float) next.z).setColor(0f, 0f, 0f, 0.4f);

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

    public static void renderBorderExclusive(GuiGraphicsExtractor stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        renderBorder(stack, left - borderSize, top - borderSize, right + borderSize, bottom + borderSize, borderSize, borderColor);
    }

    public static void renderBorder(GuiGraphicsExtractor stack, int left, int top, int right, int bottom, int borderSize, int borderColor) {
        stack.fill(left, top, right, top + borderSize, borderColor);
        stack.fill(left, bottom, right, bottom - borderSize, borderColor);
        stack.fill(left, top, left + borderSize, bottom, borderColor);
        stack.fill(right, top, right - borderSize, bottom, borderColor);
    }

    public static void drawTexturedQuad(PoseStack stack, VertexConsumer buffer, float left, float top, float right, float bottom, float minU, float minV, float maxU, float maxV, float zLevel) {

//        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        Matrix4f pose = stack.last().pose();

        buffer.addVertex(pose, left, top, zLevel).setUv(minU, minV);
        buffer.addVertex(pose, left, bottom, zLevel).setUv(minU, maxV);
        buffer.addVertex(pose, right, bottom, zLevel).setUv(maxU, maxV);
        buffer.addVertex(pose, right, top, zLevel).setUv(maxU, minV);

//        Tessellator.getInstance().draw();
    }

    public static void draw256Texture(Identifier rl, GuiGraphicsExtractor stack, int x, int y, int u, int v, int sizeX, int sizeU) {
        stack.blit(RenderPipelines.GUI_TEXTURED, rl, x, y, u, v, sizeX, sizeU, 256, 256);
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