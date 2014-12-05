package ca.qc.ircm.genefinder.ncbi;

/**
 * Parameters for finding genes in data files.
 */
public interface ProteinMappingParameters {
    public boolean isGeneId();

    public boolean isGeneDetails();

    public boolean isSequence();

    public boolean isMolecularWeight();
}
