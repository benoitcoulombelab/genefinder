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
