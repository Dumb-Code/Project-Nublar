package net.dumbcode.projectnublar.event;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.network.NublarPayloads;
import net.dumbcode.projectnublar.platform.NeoForgePlatformHelper;
import net.dumbcode.projectnublar.registry.EntityInit;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * NeoForge events of the <b>mod bus</b>, on both sides: entity attributes, the serverbound
 * payloads and the entity data serializers.
 *
 * <p>Only mod-bus events go here. FML 11 sends each listener to the bus that its event type
 * belongs to ({@code IModBusEvent} or not), so the bus is a convention of this class, kept apart
 * from the game-bus class {@code NeoForgeGameEvents}.
 */
@EventBusSubscriber(modid = Constants.MODID)
public final class NeoForgeModEvents {

    /** Gives every dinosaur entity type its attributes. */
    @SubscribeEvent
    public static void createAttributes(EntityAttributeCreationEvent event) {
        EntityInit.attributeSuppliers.forEach(p -> event.put(p.entityTypeSupplier().get(), p.factory().get().build()));
    }

    /**
     * Registers every {@link NublarPayloads#SERVERBOUND} payload. Its handler runs on the server
     * thread with the sending player.
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        NublarPayloads.SERVERBOUND.forEach(entry -> registerServerbound(registrar, entry));
    }

    private static <T extends CustomPacketPayload> void registerServerbound(PayloadRegistrar registrar, NublarPayloads.Entry<T> entry) {
        registrar.playToServer(entry.type(), entry.codec(), (payload, context) ->
                context.enqueueWork(() -> entry.handler().accept(payload, (ServerPlayer) context.player())));
    }

    /**
     * Registers the mod's entity data serializers in NeoForge's synced registry; NeoForge refuses
     * them in vanilla's list (see {@code DataSerializerInit}).
     */
    @SubscribeEvent
    public static void registerEntityDataSerializers(RegisterEvent event) {
        event.register(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS,
                helper -> NeoForgePlatformHelper.entityDataSerializers().forEach(helper::register));
    }

    private NeoForgeModEvents() {
    }
}
