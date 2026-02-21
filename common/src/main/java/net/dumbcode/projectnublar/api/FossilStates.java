package net.dumbcode.projectnublar.api;

import net.minecraft.util.StringRepresentable;

public class FossilStates {


    public enum StoneType implements StringRepresentable {
        STONE("stone"),
        ANDESITE("andesite"),
        GRANITE("granite"),
        DIORITE("diorite"),
        SANDSTONE("sandstone"),
        DEEPSLATE("deepslate"),
        TERRACOTTA("terracotta"),
        RED_TERRACOTTA("red_terracotta"),
        ORANGE_TERRACOTTA("orange_terracotta"),
        YELLOW_TERRACOTTA("yellow_terracotta"),
        BROWN_TERRACOTTA("brown_terracotta"),
        WHITE_TERRACOTTA("white_terracotta"),
        LIGHT_GRAY_TERRACOTTA("light_gray_terracotta");


        private final String name;
        StoneType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Piece implements StringRepresentable {
        RIBCAGE("ribcage"),
        FOOT("foot"),
        ARM("arm");
        private final String name;

        private Piece(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
    public enum EntityType implements StringRepresentable {
        TYRANNOSAURUS_REX("tyrannosaurus_rex"),
        TRICERATOPS("triceratops");
        private final String name;

        private EntityType(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
