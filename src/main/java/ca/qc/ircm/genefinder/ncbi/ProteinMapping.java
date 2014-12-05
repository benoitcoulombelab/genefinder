package ca.qc.ircm.genefinder.ncbi;

/**
 * Protein linked to a gene.
 */
public class ProteinMapping {
    private Integer gi;
    private Integer geneId;
    private String geneName;
    private String geneSynonyms;
    private String geneSummary;
    private Integer taxonomyId;
    private String sequence;
    private Double molecularWeight;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gi == null) ? 0 : gi.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ProteinMapping))
            return false;
        ProteinMapping other = (ProteinMapping) obj;
        if (gi == null) {
            if (other.gi != null)
                return false;
        } else if (!gi.equals(other.gi))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ProteinMapping [gi=" + gi + ", geneId=" + geneId + ", geneName=" + geneName + ", taxonomyId="
                + taxonomyId + "]";
    }

    public Integer getGi() {
        return gi;
    }

    public void setGi(Integer gi) {
        this.gi = gi;
    }

    public Integer getGeneId() {
        return geneId;
    }

    public void setGeneId(Integer geneId) {
        this.geneId = geneId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public Integer getTaxonomyId() {
        return taxonomyId;
    }

    public void setTaxonomyId(Integer taxonomyId) {
        this.taxonomyId = taxonomyId;
    }

    public String getGeneSynonyms() {
        return geneSynonyms;
    }

    public void setGeneSynonyms(String geneSynonyms) {
        this.geneSynonyms = geneSynonyms;
    }

    public String getGeneSummary() {
        return geneSummary;
    }

    public void setGeneSummary(String geneSummary) {
        this.geneSummary = geneSummary;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public Double getMolecularWeight() {
        return molecularWeight;
    }

    public void setMolecularWeight(Double molecularWeight) {
        this.molecularWeight = molecularWeight;
    }
}
