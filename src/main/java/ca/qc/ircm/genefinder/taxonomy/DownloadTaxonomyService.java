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

package ca.qc.ircm.genefinder.taxonomy;

import ca.qc.ircm.genefinder.annotation.NcbiConfiguration;
import ca.qc.ircm.genefinder.ftp.FtpService;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.inject.Inject;

/**
 * Downloads taxonomy.
 */
@Component
public class DownloadTaxonomyService {
  private static final Logger logger = LoggerFactory.getLogger(DownloadTaxonomyService.class);
  private static final Charset FASTA_CHARSET = Charset.forName("UTF-8");
  @Inject
  private NcbiConfiguration ncbiConfiguration;
  @Inject
  private FtpService ftpService;

  protected DownloadTaxonomyService() {
  }

  protected DownloadTaxonomyService(NcbiConfiguration ncbiConfiguration, FtpService ftpService) {
    this.ncbiConfiguration = ncbiConfiguration;
    this.ftpService = ftpService;
  }

  /**
   * Downloads taxonomy and returns all taxons ids of the children of specified taxon.
   *
   * @param taxonId
   *          taxon
   * @param progressBar
   *          records progression
   * @param locale
   *          user's locale
   * @return returns all taxons ids of the children of specified taxon
   * @throws IOException
   *           could not download taxonomy
   * @throws InterruptedException
   *           download taxonomy was interrupted
   */
  public Set<Integer> children(Integer taxonId, ProgressBar progressBar, Locale locale)
      throws IOException, InterruptedException {
    MessageResources resources = new MessageResources(DownloadTaxonomyService.class, locale);
    FTPClient client = client(progressBar.step(0.05), resources);
    try {
      Map<Integer, Taxon> taxonomy =
          downloadAndParseTaxonomyIds(client, progressBar.step(0.90), resources, locale);
      Taxon taxon = taxonomy.get(taxonId);
      List<Taxon> children = children(taxon);
      progressBar.setProgress(1.0);
      return children.stream().map(child -> child.getId()).collect(Collectors.toSet());
    } finally {
      if (client.isConnected()) {
        client.disconnect();
      }
    }
  }

  private List<Taxon> children(Taxon taxon) {
    List<Taxon> children = new ArrayList<>();
    children.addAll(taxon.getChildren());
    for (Taxon child : taxon.getChildren()) {
      children.addAll(children(child));
    }
    return children;
  }

  private FTPClient client(ProgressBar progressBar, MessageResources resources) throws IOException {
    String ftpServer = ncbiConfiguration.ftp();
    progressBar.setMessage(resources.message("ftp.connect", ftpServer));
    FTPClient client = ftpService.anonymousConnect(ftpServer);
    progressBar.setProgress(1.0);
    return client;
  }

  private Map<Integer, Taxon> downloadAndParseTaxonomyIds(FTPClient client, ProgressBar progressBar,
      MessageResources resources, Locale locale) throws IOException {
    String taxonomyFilename = ncbiConfiguration.taxonomy();
    String taxonomyNodesFilename = ncbiConfiguration.taxonomyNodes();
    progressBar.setMessage(resources.message("download.taxonomy", taxonomyFilename));
    logger.debug("download and parse taxonomy ids from {}", taxonomyFilename);
    Path taxonomyFile = ftpService.localFile(taxonomyFilename);
    ftpService.downloadFile(client, taxonomyFilename, taxonomyFile, progressBar.step(0.6), locale);
    progressBar.setProgress(0.6);
    progressBar.setMessage(resources.message("parse.taxonomy.nodes"));
    Map<Integer, Taxon> mappedTaxonomy = null;
    try (ZipInputStream input = new ZipInputStream(Files.newInputStream(taxonomyFile))) {
      boolean nodesFound = false;
      ZipEntry entry;
      while ((entry = input.getNextEntry()) != null) {
        if (entry.getName().equals(taxonomyNodesFilename)) {
          nodesFound = true;
          BufferedReader reader = new BufferedReader(new InputStreamReader(input, FASTA_CHARSET));
          mappedTaxonomy = parseTaxonomyNodes(reader);
        }
      }
      if (!nodesFound) {
        throw new IOException(
            "Did not find file " + taxonomyNodesFilename + " in " + taxonomyFilename);
      }
    }
    progressBar.setProgress(1.0);
    return mappedTaxonomy;
  }

  private Map<Integer, Taxon> parseTaxonomyNodes(BufferedReader reader) throws IOException {
    Map<Integer, Taxon> taxonomy = new HashMap<>();
    String line;
    while ((line = reader.readLine()) != null) {
      String[] columns = line.split("\t");
      Integer id = Integer.valueOf(columns[0]);
      Integer parentId = Integer.valueOf(columns[2]);
      if (!taxonomy.containsKey(parentId)) {
        taxonomy.put(parentId, new Taxon(parentId));
      }
      if (!taxonomy.containsKey(id)) {
        taxonomy.put(id, new Taxon(id));
      }
      Taxon taxon = taxonomy.get(id);
      taxon.setParent(taxonomy.get(parentId));
      if (taxon.getParent().getId() == taxon.getId()) {
        taxon.setParent(null);
      }
      if (taxon.getParent() != null) {
        taxon.getParent().getChildren().add(taxon);
      }
      taxon.setRank(columns[4]);
      taxonomy.put(id, taxon);
    }
    return taxonomy;
  }
}
