package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.api.FossilPiece;
import net.dumbcode.projectnublar.api.FossilPieces;
import net.dumbcode.projectnublar.api.dinosaur.Diet;
import net.dumbcode.projectnublar.api.DinoBehaviourData;
import net.dumbcode.projectnublar.api.Dinosaur;

import java.util.ArrayList;
import java.util.List;

public class DinosaurInit {
 //   public static ResourceKey<Registry<Dinosaur>> DINOSAUR_KEY = ResourceKey.createRegistryKey(Constants.modLoc("dinosaurs"));
    //  public static DeferredRegister<Dinosaur> DINOSAURS = DeferredRegister.create(Constants.MODID, DINOSAUR_KEY);

    public static String TYRANNOSAURUS_REX_ID = "tyrannosaurus_rex";
    public static String VELOCIRAPTOR_ID = "velociraptor";
    public static String DILOPHOSAURUS_ID = "dilophosaurus";
    public static String BRACHIOSAURUS_ID = "brachiosaurus";
    public static String TRICERATOPS_ID = "triceratops";
    public static String GALLIMIMUS_ID = "gallimimus";

    public static final List<String> DINOSAUR_ID = List.of(TYRANNOSAURUS_REX_ID);


    public static List<FossilPiece> getTyrannosaurPieces() {
        List<FossilPiece> tyrannosaurPieces = new ArrayList<>();
        tyrannosaurPieces.addAll(FossilPieces.tyrannosaurPieces);
        tyrannosaurPieces.addAll(FossilPieces.bipedPieces);
        return tyrannosaurPieces;
    }

    public static final Dinosaur TYRANNOSAURUS_REX = new Dinosaur(
            TYRANNOSAURUS_REX_ID, Diet.CARNIVORE,
            new DinoBehaviourData(200.0,300.0,5.0,2.0,1.0,10.0,10,2.0,5.0,1.0,100.0,
                    5.0,3.0,100.0, 10.0,80.0,100.0,4.0,50,1.0,5,5.0,5.0,7,3,2.0,
                    0.9,0.6,0.4,true,true), //Assign AI data
           getTyrannosaurPieces(),"cretaceous");


public static final List<Dinosaur> DINOSAURS_LIST = List.of(TYRANNOSAURUS_REX);

   /*
    public static DeferredSupplier<Dinosaurs.Dinosaure> VELOCIRAPTOR = register("velociraptor", Diet.CARNIVORE,true);
    public static DeferredSupplier<Dinosaurs.Dinosaure> DILOPHOSAURUS = register("dilophosaurus", Diet.CARNIVORE);
    public static DeferredSupplier<Dinosaurs.Dinosaure> GALLIMIMUS = register("gallimimus", Diet.OMNIVORE,true);
    public static DeferredSupplier<Dinosaurs.Dinosaure> BRACHIOSAURUS = register("triceratops", Diet.HERBIVORE,true);
    public static DeferredSupplier<Dinosaurs.Dinosaure> TRICERATOPS = register("triceratops", Diet.HERBIVORE,true);


    */


    public static List<Dinosaur> getList() {
        List<Dinosaur> dinosaurs = new ArrayList<>();
        dinosaurs.addAll(DINOSAURS_LIST);
        return dinosaurs;
    }
    public static Dinosaur byName(String name) {
        for (Dinosaur piece : DinosaurInit.DINOSAURS_LIST) {
            if (piece.name().equals(name)) {
                return piece;
            }
        }
        throw new IllegalArgumentException("Invalid dinosaur name: " + name);
    }
/*
    //Dummy registry
    public static DeferredSupplier<Dinosaur> register(String name) {
        return DINOSAURS.register(name, () -> new Dinosaur(name));
    }
    //Entity Only
    public static DeferredSupplier<Dinosaur> register(String name, Diet diet, DinoBehaviourData behaviourData) {

        return DINOSAURS.register(name, () -> new Dinosaur(name,diet,behaviourData));
    }

    public static DeferredSupplier<Dinosaur> register(String name, Diet diet, DinoBehaviourData behaviourData, List<FossilPieces.FossilPiece> pPieces, String pPeriod) {
        return DINOSAURS.register(name, () -> new Dinosaur(name,diet,behaviourData,pPieces,pPeriod));
    }

    public static void loadClass() {
        DINOSAURS.register();
    }


    public static Codec<Dinosaur> byNameCodec() {
        Codec<Dinosaur> nameCodec = ResourceLocation.CODEC.flatXmap((location) -> Optional.ofNullable(DINOSAURS.getRegistrar().get(location)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + DINOSAUR_KEY + ": " + location)), (dinosaur) -> DINOSAURS.getRegistrar().getKey(dinosaur).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + DINOSAUR_KEY + ":" + dinosaur)));
        Codec<Dinosaur> idCodec = ExtraCodecs.idResolverCodec((dinosaur) -> DINOSAURS.getRegistrar().getKey(dinosaur).isPresent() ? DINOSAURS.getRegistrar().getRawId(dinosaur) : -1, value -> DINOSAURS.getRegistrar().byRawId(value), -1);
        return ExtraCodecs.orCompressed(nameCodec, idCodec);
    }


 */
}
