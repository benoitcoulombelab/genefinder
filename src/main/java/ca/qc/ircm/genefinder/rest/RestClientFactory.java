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

import org.glassfish.jersey.client.ClientProperties;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Factory for REST client.
 */
@Component
public class RestClientFactory {
  @Inject
  private RestConfiguration restConfiguration;

  protected RestClientFactory() {
  }

  protected RestClientFactory(RestConfiguration restConfiguration) {
    this.restConfiguration = restConfiguration;
  }

  public Client createClient() {
    Client client = ClientBuilder.newClient();
    client.property(ClientProperties.CONNECT_TIMEOUT, restConfiguration.timeout());
    client.property(ClientProperties.READ_TIMEOUT, restConfiguration.timeout());
    return client;
  }
}
