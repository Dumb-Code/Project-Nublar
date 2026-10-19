package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.gene.Genes;

public interface GeneHolder {
    void setGene(Genes.Gene gene, DinoData data);
}
