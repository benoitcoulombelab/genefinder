package ca.qc.ircm.genefinder.ncbi;

import ca.qc.ircm.genefinder.data.FindGenesParameters;

/**
 * Parameters for finding genes in data files.
 */
public class ProteinMappingParametersFromFindGenesParameters implements ProteinMappingParameters {
    private final FindGenesParameters delegate;

    public ProteinMappingParametersFromFindGenesParameters(FindGenesParameters delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isGeneId() {
        return delegate.isGeneId();
    }

    @Override
    public boolean isGeneDetails() {
        return delegate.isGeneName() || delegate.isGeneSummary() || delegate.isGeneSynonyms();
    }

    @Override
    public boolean isSequence() {
        return delegate.isProteinMolecularWeight();
    }

    @Override
    public boolean isMolecularWeight() {
        return delegate.isProteinMolecularWeight();
    }
}
