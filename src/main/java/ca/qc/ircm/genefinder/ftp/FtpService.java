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

package ca.qc.ircm.genefinder.ftp;

import ca.qc.ircm.genefinder.ApplicationConfiguration;
import ca.qc.ircm.progressbar.ProgressBar;
import ca.qc.ircm.utils.MessageResources;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * Services for FTP protocol.
 */
@Component
public class FtpService {
  private static final Logger logger = LoggerFactory.getLogger(FtpService.class);
  private static final int FTP_DEFAULT_TIMEOUT = 120000;
  private static final int MEGA_BYTE_SIZE = 1024 * 1024;
  @Inject
  private FtpClientFactory ftpClientFactory;
  @Inject
  private ApplicationConfiguration applicationConfiguration;

  protected FtpService() {
  }

  protected FtpService(FtpClientFactory ftpClientFactory,
      ApplicationConfiguration applicationConfiguration) {
    this.ftpClientFactory = ftpClientFactory;
    this.applicationConfiguration = applicationConfiguration;
  }

  /**
   * Creates an instance of FTPClient.
   *
   * @return FTPClient
   */
  public FTPClient createClient() {
    return ftpClientFactory.create();
  }

  /**
   * Creates an instance of FTPClient that is anonymously connected to remote FTP server.
   *
   * @param ftpServer
   *          remote FTP server URL
   * @return FTPClient that is anonymously connected to remote FTP server
   * @throws IOException
   *           could not connect to remote FTP server
   */
  public FTPClient anonymousConnect(String ftpServer) throws IOException {
    FTPClient client = ftpClientFactory.create();
    logger.debug("connect to FTP server {}", ftpServer);
    client.setConnectTimeout(FTP_DEFAULT_TIMEOUT);
    client.setDefaultTimeout(FTP_DEFAULT_TIMEOUT);
    client.connect(ftpServer);
    int reply = client.getReplyCode();
    if (!FTPReply.isPositiveCompletion(reply)) {
      throw new IOException("Could not connect to FTP server " + ftpServer);
    }
    String username = "anonymous";
    String password = "";
    logger.debug("login on FTP server {}", ftpServer);
    if (!client.login(username, password)) {
      throw new IOException("Could not login on FTP server " + ftpServer);
    }
    client.enterLocalPassiveMode();
    client.setDataTimeout(FTP_DEFAULT_TIMEOUT);
    client.setControlKeepAliveTimeout(300);
    return client;
  }

  /**
   * Lists all files in directory and it's sub-directories.
   *
   * @param client
   *          FTP client
   * @param directory
   *          directory where to start walking
   * @return all files in directory and it's sub-directories
   * @throws IOException
   *           could not access FTP server
   */
  public List<String> walkTree(FTPClient client, String directory) throws IOException {
    logger.debug("walking FTP tree starting from {}", directory);
    List<String> files = new ArrayList<>();
    for (FTPFile file : client.listFiles(directory)) {
      String filename = directory + "/" + file.getName();
      if (file.isDirectory()) {
        files.addAll(walkTree(client, filename));
      } else {
        files.add(filename);
      }
    }
    return files;
  }

  /**
   * Returns local file with same path and filename as remote file.
   *
   * @param remoteFile
   *          remote file
   * @return local file with same path and filename as remote file
   */
  public Path localFile(String remoteFile) {
    Path path = Paths.get(remoteFile.replaceAll("^\\\\*", ""));
    if (path.getRoot() != null) {
      path = path.getRoot().relativize(path);
    }
    return applicationConfiguration.download().resolve(path);
  }

  /**
   * Download a file to local drive.
   *
   * @param client
   *          FTP client
   * @param remoteFile
   *          remote file to download
   * @param localFile
   *          local file where to download
   * @param progressBar
   *          records progression
   * @param locale
   *          user's locale
   * @throws IOException
   *           could not access FTP server
   */
  public void downloadFile(FTPClient client, String remoteFile, Path localFile,
      ProgressBar progressBar, Locale locale) throws IOException {
    logger.debug("getting file size and modified date for {}", remoteFile);
    FTPFile[] files = client.listFiles(remoteFile);
    if (files.length != 1) {
      throw new IllegalStateException("ftp returned more than one file for " + remoteFile);
    }
    FTPFile file = files[0];
    boolean download = false;
    if (!Files.exists(localFile)) {
      download = true;
    } else if (file.getSize() != Files.size(localFile)) {
      download = true;
    } else if (file.getTimestamp().toInstant().minus(1, ChronoUnit.HALF_DAYS)
        .isAfter(Files.getLastModifiedTime(localFile).toInstant())) {
      download = true;
    }
    if (download) {
      logger.debug("downloading file {} to {}", remoteFile, localFile);
      Files.createDirectories(localFile.getParent());
      client.setFileType(FTP.BINARY_FILE_TYPE);
      DownloadProgressionThread progressThread =
          new DownloadProgressionThread(localFile, file.getSize(), progressBar, locale);
      try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(localFile))) {
        progressThread.start();
        client.retrieveFile(remoteFile, output);
      } finally {
        progressThread.interrupt();
      }
    } else {
      logger.debug("skipping download of file {}, file on disk {} is the same", remoteFile,
          localFile);
    }
    progressBar.setProgress(1.0);
  }

  private static class DownloadProgressionThread extends Thread {
    private final Path file;
    private final long size;
    private final ProgressBar progressBar;
    private final Locale locale;

    DownloadProgressionThread(Path file, long size, ProgressBar progressBar, Locale locale) {
      this.setDaemon(true);
      this.file = file;
      this.size = Math.max(size, 1);
      this.progressBar = progressBar;
      this.locale = locale;
    }

    @Override
    public void run() {
      MessageResources resources = new MessageResources(FtpService.class, locale);
      String filename = file.getFileName().toString();
      while (true) {
        try {
          long currentSize = Files.size(file);
          progressBar.setMessage(resources.message("download", currentSize / MEGA_BYTE_SIZE,
              size / MEGA_BYTE_SIZE, filename));
          progressBar.setProgress((double) currentSize / size);
        } catch (IOException e) {
          // Ignore.
        }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }
}
