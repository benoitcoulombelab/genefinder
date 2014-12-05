package ca.qc.ircm.genefinder.organism;

import java.util.Collection;
import java.util.List;

/**
 * Services for {@link Organism}.
 */
public interface OrganismService {
    public Organism get(Integer id);

    public List<Organism> all();

    public void insert(Organism organism);

    public void delete(Collection<Organism> organisms);
}
