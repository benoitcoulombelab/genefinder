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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.ApplicationConfiguration;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class FtpServiceTest {
  private FtpService ftpService;
  @Mock
  private FtpClientFactory ftpClientFactory;
  @Mock
  private FTPClient ftpClient;
  @Mock
  private ApplicationConfiguration applicationConfiguration;
  @Mock
  private FTPFile ftpFile;
  @Mock
  private ProgressBar progressBar;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path downloadHome;
  private Instant remoteFileTime =
      Instant.now().minus(2, ChronoUnit.DAYS).with(ChronoField.MICRO_OF_SECOND, 0);
  private int remoteFileSize = 2048;
  private Locale locale = Locale.ENGLISH;

  /**
   * Before tests.
   */
  @Before
  public void beforeTest() {
    ftpService = new FtpService(ftpClientFactory, applicationConfiguration);
    when(ftpClientFactory.create()).thenReturn(ftpClient);
    downloadHome = temporaryFolder.getRoot().toPath();
    when(applicationConfiguration.download()).thenReturn(downloadHome);
    when(ftpFile.getSize()).thenReturn((long) remoteFileSize);
    when(ftpFile.getTimestamp())
        .thenReturn(GregorianCalendar.from(remoteFileTime.atZone(ZoneId.systemDefault())));
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
  }

  private byte[] randomBytes(int size) {
    byte[] bytes = new byte[size];
    new Random().nextBytes(bytes);
    return bytes;
  }

  private byte[] writeRandomBytes(Path output, int size) throws IOException {
    try (OutputStream outputStream = Files.newOutputStream(output)) {
      return writeRandomBytes(outputStream, size);
    }
  }

  private byte[] writeRandomBytes(OutputStream output, int size) throws IOException {
    byte[] bytes = new byte[size];
    new Random().nextBytes(bytes);
    output.write(bytes);
    return bytes;
  }

  @Test
  public void createClient() {
    FTPClient ftpClient = ftpService.createClient();

    verify(ftpClientFactory).create();
    assertEquals(this.ftpClient, ftpClient);
  }

  @Test
  public void anonymousConnect() throws Throwable {
    String ftpServer = "localhost:8080";
    when(ftpClient.getReplyCode()).thenReturn(200);
    when(ftpClient.login(any(), any())).thenReturn(true);
    when(ftpClient.isConnected()).thenReturn(true);

    FTPClient ftpClient = ftpService.anonymousConnect(ftpServer);

    verify(ftpClientFactory).create();
    verify(ftpClient).connect(ftpServer);
    verify(ftpClient).getReplyCode();
    verify(ftpClient).login(eq("anonymous"), any());
    verify(ftpClient).enterLocalPassiveMode();
    assertEquals(this.ftpClient, ftpClient);
  }

  private FTPFile ftpFile(String name, boolean directory) {
    FTPFile file = new FTPFile();
    file.setName(name);
    file.setType(directory ? FTPFile.DIRECTORY_TYPE : FTPFile.FILE_TYPE);
    return file;
  }

  @Test
  public void walkTree() throws Throwable {
    String start = "start";
    FTPFile folder1 = ftpFile("folder1", true);
    FTPFile folder2 = ftpFile("folder2", true);
    FTPFile folder3 = ftpFile("folder3", true);
    FTPFile file01 = ftpFile("a", false);
    FTPFile file02 = ftpFile("b", false);
    FTPFile file11 = ftpFile("c", false);
    FTPFile file12 = ftpFile("d", false);
    FTPFile file21 = ftpFile("e", false);
    FTPFile file22 = ftpFile("f", false);
    FTPFile file23 = ftpFile("g", false);
    FTPFile folder31 = ftpFile("folder31", true);
    FTPFile folder32 = ftpFile("folder32", true);
    FTPFile file31 = ftpFile("h", false);
    FTPFile file311 = ftpFile("i", false);
    FTPFile file312 = ftpFile("j", false);
    FTPFile file321 = ftpFile("k", false);
    Map<String, FTPFile[]> files = new HashMap<>();
    files.put(start, new FTPFile[] { folder1, folder2, folder3, file01, file02 });
    files.put(start + "/" + folder1.getName(), new FTPFile[] { file11, file12 });
    files.put(start + "/" + folder2.getName(), new FTPFile[] { file21, file22, file23 });
    files.put(start + "/" + folder3.getName(), new FTPFile[] { folder31, folder32, file31 });
    files.put(start + "/" + folder3.getName() + "/" + folder31.getName(),
        new FTPFile[] { file311, file312 });
    files.put(start + "/" + folder3.getName() + "/" + folder32.getName(),
        new FTPFile[] { file321 });
    when(ftpClient.listFiles(any())).thenAnswer(i -> {
      String directory = (String) i.getArguments()[0];
      if (files.containsKey(directory)) {
        return files.get(directory);
      } else {
        return new FTPFile[0];
      }
    });

    final List<String> filenames = ftpService.walkTree(ftpClient, "start");

    verify(ftpClient).listFiles(start);
    verify(ftpClient).listFiles(start + "/" + folder1.getName());
    verify(ftpClient).listFiles(start + "/" + folder2.getName());
    verify(ftpClient).listFiles(start + "/" + folder3.getName());
    verify(ftpClient).listFiles(start + "/" + folder3.getName() + "/" + folder31.getName());
    verify(ftpClient).listFiles(start + "/" + folder3.getName() + "/" + folder32.getName());
    assertTrue(filenames.contains(start + "/" + file01.getName()));
    assertTrue(filenames.contains(start + "/" + file02.getName()));
    assertTrue(filenames.contains(start + "/" + folder1.getName() + "/" + file11.getName()));
    assertTrue(filenames.contains(start + "/" + folder1.getName() + "/" + file12.getName()));
    assertTrue(filenames.contains(start + "/" + folder2.getName() + "/" + file21.getName()));
    assertTrue(filenames.contains(start + "/" + folder2.getName() + "/" + file22.getName()));
    assertTrue(filenames.contains(start + "/" + folder2.getName() + "/" + file23.getName()));
    assertTrue(filenames.contains(start + "/" + folder3.getName() + "/" + file31.getName()));
    assertTrue(filenames.contains(
        start + "/" + folder3.getName() + "/" + folder31.getName() + "/" + file311.getName()));
    assertTrue(filenames.contains(
        start + "/" + folder3.getName() + "/" + folder31.getName() + "/" + file312.getName()));
    assertTrue(filenames.contains(
        start + "/" + folder3.getName() + "/" + folder32.getName() + "/" + file321.getName()));
  }

  @Test
  public void localFile() throws Throwable {
    String remoteFile = "pub/taxonomy.zip";
    assertEquals(downloadHome.resolve(remoteFile), ftpService.localFile(remoteFile));
  }

  @Test
  public void localFile_StartSlash() throws Throwable {
    String remoteFile = "pub/taxonomy.zip";
    assertEquals(downloadHome.resolve(remoteFile), ftpService.localFile("/" + remoteFile));
  }

  @Test
  public void localFile_StartBackslash() throws Throwable {
    String remoteFile = "pub\\taxonomy.zip";
    assertEquals(downloadHome.resolve(remoteFile), ftpService.localFile("\\" + remoteFile));
  }

  @Test
  public void downloadFile_LocalNotExists() throws Throwable {
    String remoteFile = "remoteFile";
    Path localFile = downloadHome.resolve("localfile.bin");
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });
    byte[] randomBytes = randomBytes(remoteFileSize);
    doAnswer(i -> {
      OutputStream output = (OutputStream) i.getArguments()[1];
      output.write(randomBytes);
      return null;
    }).when(ftpClient).retrieveFile(any(), any());

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient).setFileType(FTP.BINARY_FILE_TYPE);
    verify(ftpClient).retrieveFile(eq(remoteFile), any());
    assertArrayEquals(randomBytes, Files.readAllBytes(localFile));
    verify(progressBar, atLeast(2)).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(any());
  }

  @Test
  public void downloadFile_LocalNotExists_SubFolder() throws Throwable {
    String remoteFile = "remoteFile";
    Path localFile = downloadHome.resolve("pub/localfile.bin");
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });
    byte[] randomBytes = randomBytes(remoteFileSize);
    doAnswer(i -> {
      OutputStream output = (OutputStream) i.getArguments()[1];
      output.write(randomBytes);
      return null;
    }).when(ftpClient).retrieveFile(any(), any());

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient).setFileType(FTP.BINARY_FILE_TYPE);
    verify(ftpClient).retrieveFile(eq(remoteFile), any());
    assertArrayEquals(randomBytes, Files.readAllBytes(localFile));
    verify(progressBar, atLeast(2)).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(any());
  }

  @Test
  public void downloadFile_LocalExist_WrongSize() throws Throwable {
    final String remoteFile = "remoteFile";
    Path localFile = Files.createFile(downloadHome.resolve("localfile.bin"));
    Files.write(localFile, randomBytes(4096));
    Files.setLastModifiedTime(localFile, FileTime.from(remoteFileTime));
    byte[] randomBytes = randomBytes(remoteFileSize);
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });
    doAnswer(i -> {
      OutputStream output = (OutputStream) i.getArguments()[1];
      output.write(randomBytes);
      return null;
    }).when(ftpClient).retrieveFile(any(), any());

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient).setFileType(FTP.BINARY_FILE_TYPE);
    verify(ftpClient).retrieveFile(eq(remoteFile), any());
    assertArrayEquals(randomBytes, Files.readAllBytes(localFile));
    verify(progressBar, atLeast(2)).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(any());
  }

  @Test
  public void downloadFile_LocalExist_RemoteMoreRecent() throws Throwable {
    final String remoteFile = "remoteFile";
    Path localFile = Files.createFile(downloadHome.resolve("localfile.bin"));
    writeRandomBytes(localFile, remoteFileSize);
    Files.setLastModifiedTime(localFile, FileTime.from(remoteFileTime.minus(1, ChronoUnit.DAYS)));
    byte[] randomBytes = randomBytes(remoteFileSize);
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });
    doAnswer(i -> {
      OutputStream output = (OutputStream) i.getArguments()[1];
      output.write(randomBytes);
      return null;
    }).when(ftpClient).retrieveFile(any(), any());

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient).setFileType(FTP.BINARY_FILE_TYPE);
    verify(ftpClient).retrieveFile(eq(remoteFile), any());
    assertArrayEquals(randomBytes, Files.readAllBytes(localFile));
    verify(progressBar, atLeast(2)).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(any());
  }

  @Test
  public void downloadFile_LocalExist_SameSizeAndDate() throws Throwable {
    final String remoteFile = "remoteFile";
    Path localFile = Files.createFile(downloadHome.resolve("localfile.bin"));
    writeRandomBytes(localFile, remoteFileSize);
    Files.setLastModifiedTime(localFile, FileTime.from(remoteFileTime));
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient, never()).retrieveFile(any(), any());
    verify(progressBar, atLeastOnce()).setProgress(anyDouble());
  }

  @Test
  public void downloadFile_LocalExist_LocalMoreRecent() throws Throwable {
    final String remoteFile = "remoteFile";
    Path localFile = Files.createFile(downloadHome.resolve("localfile.bin"));
    writeRandomBytes(localFile, remoteFileSize);
    Files.setLastModifiedTime(localFile, FileTime.from(remoteFileTime.plus(1, ChronoUnit.DAYS)));
    when(ftpClient.listFiles(any())).thenReturn(new FTPFile[] { ftpFile });

    ftpService.downloadFile(ftpClient, remoteFile, localFile, progressBar, locale);

    verify(ftpClient).listFiles(remoteFile);
    verify(ftpClient, never()).retrieveFile(any(), any());
    verify(progressBar, atLeastOnce()).setProgress(anyDouble());
  }
}
