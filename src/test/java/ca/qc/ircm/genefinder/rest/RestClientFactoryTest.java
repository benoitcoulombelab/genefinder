/*
 * Copyright (c) 2014 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.genefinder.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Client;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class RestClientFactoryTest {
  private RestClientFactory restClientFactory;
  @Mock
  private RestConfiguration restConfiguration;

  @Before
  public void beforeTest() {
    restClientFactory = new RestClientFactory(restConfiguration);
  }

  @Test
  public void createClient() {
    int timeout = 2000;
    when(restConfiguration.timeout()).thenReturn(timeout);

    Client client = restClientFactory.createClient();

    assertTrue(client instanceof Client);
    assertEquals(timeout, client.getConfiguration().getProperty(ClientProperties.CONNECT_TIMEOUT));
    assertEquals(timeout, client.getConfiguration().getProperty(ClientProperties.READ_TIMEOUT));
  }
}
