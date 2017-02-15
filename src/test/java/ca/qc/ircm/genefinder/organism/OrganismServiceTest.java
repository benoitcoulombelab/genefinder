/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.genefinder.organism;

import static ca.qc.ircm.genefinder.organism.QOrganism.organism;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class OrganismServiceTest {
  private OrganismService organismService;
  @PersistenceContext
  private EntityManager entityManager;
  @Inject
  private JPAQueryFactory jpaQueryFactory;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    organismService = new OrganismService(entityManager, jpaQueryFactory);
  }

  private Optional<Organism> find(Collection<Organism> organisms, int id) {
    return organisms.stream().filter(o -> o.getId() == id).findAny();
  }

  @Test
  public void get_9606() {
    Organism organism = organismService.get(9606);
    assertEquals((Integer) 9606, organism.getId());
    assertEquals("Homo Sapiens", organism.getName());
  }

  @Test
  public void get_10090() {
    Organism organism = organismService.get(10090);
    assertEquals((Integer) 10090, organism.getId());
    assertEquals("Mus Musculus", organism.getName());
  }

  @Test
  public void get_Null() {
    assertNull(organismService.get(null));
  }

  @Test
  public void all() {
    List<Organism> organisms = organismService.all();

    assertEquals(2, organisms.size());
    Optional<Organism> optionalOrganism = find(organisms, 9606);
    assertTrue(optionalOrganism.isPresent());
    Organism organism = optionalOrganism.get();
    assertEquals((Integer) 9606, organism.getId());
    assertEquals("Homo Sapiens", organism.getName());
    optionalOrganism = find(organisms, 10090);
    assertTrue(optionalOrganism.isPresent());
    organism = optionalOrganism.get();
    assertEquals((Integer) 10090, organism.getId());
    assertEquals("Mus Musculus", organism.getName());
  }

  @Test
  public void containsAny_False() {
    jpaQueryFactory.delete(organism).execute();

    boolean value = organismService.containsAny();

    assertFalse(value);
  }

  @Test
  public void containsAny_True() {
    boolean value = organismService.containsAny();

    assertTrue(value);
  }

  @Test
  public void insert() throws Throwable {
    Integer id = 9796;
    String name = "Equus caballus";
    Organism organism = new Organism(id, name);

    organismService.insert(organism);

    entityManager.flush();
    organism = entityManager.find(Organism.class, id);
    assertNotNull(organism);
    assertEquals(id, organism.getId());
    assertEquals(name, organism.getName());
  }

  @Test(expected = PersistenceException.class)
  public void insert_Duplicate() throws Throwable {
    Organism organism = new Organism(9606, "Homo Sapiens");

    organismService.insert(organism);

    entityManager.flush();
  }

  @Test
  public void update() throws Throwable {
    Integer id = 9606;
    String name = "Equus caballus";
    Organism organism = organismService.get(id);
    organism.setName(name);

    organismService.update(organism);

    entityManager.flush();
    organism = entityManager.find(Organism.class, id);
    assertNotNull(organism);
    assertEquals(id, organism.getId());
    assertEquals(name, organism.getName());
  }

  @Test
  public void delete_One() throws Throwable {
    List<Organism> organisms = new ArrayList<>();
    organisms.add(organismService.get(9606));

    organismService.delete(organisms);

    entityManager.flush();
    assertNull(entityManager.find(Organism.class, 9606));
  }

  @Test
  public void delete_Multiple() throws Throwable {
    List<Organism> organisms = new ArrayList<>();
    organisms.add(organismService.get(9606));
    organisms.add(organismService.get(10090));

    organismService.delete(organisms);

    entityManager.flush();
    assertNull(entityManager.find(Organism.class, 9606));
    assertNull(entityManager.find(Organism.class, 10090));
  }
}
