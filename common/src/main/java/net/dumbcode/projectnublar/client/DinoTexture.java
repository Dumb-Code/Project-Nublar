package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DinoTexture extends SimpleTexture {
    private static final Set<UUID> usedIds = new HashSet<>();
    private final ByteBuffer dataRef;

    public static DinoTexture create(Identifier name, ByteBuffer data) {
        return new DinoTexture(name, data);
    }

    private DinoTexture(Identifier location, ByteBuffer data) {
        super(location);
        this.dataRef = data;
    }

    public Identifier getLocation() {
        return this.location;
    }

    public NativeImage asNative(){
        ByteBuffer data = this.dataRef;

        if (data == null)
            return null;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer lwjglData = memoryStack.malloc(data.capacity());
            lwjglData.put(data);
            data.rewind();
            lwjglData.rewind();
            return NativeImage.read(lwjglData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void load(ResourceManager manager) {
        ByteBuffer data = this.dataRef;

        if (data == null)
            return;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer lwjglData = memoryStack.malloc(data.capacity());
            lwjglData.put(data);
            data.rewind();
            lwjglData.rewind();
            NativeImage image = NativeImage.read(lwjglData);

            if (RenderSystem.isOnRenderThreadOrInit()) {
                upload(image);
            } else {
                RenderSystem.recordRenderCall(() -> upload(image));
            }
        } catch (Exception ignored) {
        }
    }

    private void upload(NativeImage image) {
        TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }
}
