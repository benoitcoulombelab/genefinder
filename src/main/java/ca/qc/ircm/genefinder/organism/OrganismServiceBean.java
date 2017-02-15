package ca.qc.ircm.genefinder.organism;

import static ca.qc.ircm.genefinder.organism.QOrganism.organism;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class OrganismServiceBean implements OrganismService {
  private static final Logger logger = LoggerFactory.getLogger(OrganismServiceBean.class);
  @PersistenceContext
  private EntityManager entityManager;
  @Inject
  private JPAQueryFactory queryFactory;

  protected OrganismServiceBean() {
  }

  protected OrganismServiceBean(EntityManager entityManager, JPAQueryFactory queryFactory) {
    this.entityManager = entityManager;
    this.queryFactory = queryFactory;
  }

  /**
   * Returns organism with specified id.
   *
   * @param id
   *          id
   * @return organism with specified id
   */
  @Override
  public Organism get(Integer id) {
    if (id == null) {
      return null;
    }

    return entityManager.find(Organism.class, id);
  }

  /**
   * Returns all organisms.
   *
   * @return all organisms
   */
  @Override
  public List<Organism> all() {
    JPAQuery<Organism> query = queryFactory.select(organism);
    query.from(organism);
    return query.fetch();
  }

  /**
   * Returns true if database contains at least one organism, false otherwise.
   *
   * @return true if database contains at least one organism, false otherwise
   */
  public boolean containsAny() {
    JPAQuery<Organism> query = queryFactory.select(organism);
    query.from(organism);
    return query.fetchCount() > 0;
  }

  @Override
  public void insert(Organism organism) {
    entityManager.persist(organism);
  }

  public void update(Organism organism) {
    entityManager.merge(organism);
  }

  @Override
  public void delete(Collection<Organism> organisms) {
    organisms.forEach(o -> delete(o));
  }

  private void delete(Organism organism) {
    organism = entityManager.merge(organism);
    entityManager.refresh(organism);
    entityManager.remove(organism);
  }
}
