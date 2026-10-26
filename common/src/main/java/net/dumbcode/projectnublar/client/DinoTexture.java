package net.dumbcode.projectnublar.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
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
        return this.resourceId();
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
    /**
     * The image comes from the in-memory PNG data, not from a resource pack; the 26.2 texture
     * manager uploads it. Without data the texture is the missing texture.
     */
    @Override
    public TextureContents loadContents(ResourceManager manager) {
        NativeImage image = asNative();
        return image == null ? TextureContents.createMissing() : new TextureContents(image, null);
    }
}
