package ca.qc.ircm.genefinder.annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.genefinder.ApplicationConfiguration;
import ca.qc.ircm.genefinder.net.FtpClientFactory;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.ServiceTestAnnotations;
import ca.qc.ircm.progressbar.ProgressBar;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.maven.scm.command.Command;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

// TODO Test geneSynonyms, geneSummary and molecularWeight
@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DownloadProteinMappingServiceBeanTest {
  private static final String UNIPROT_HOST = "ftp.uniprot.org";
  private static final String UNIPROT_FOLDER =
      "/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes";
  private static final String NCBI_HOST = "ftp.ncbi.nlm.nih.gov";
  private static final String NCBI_GENE_INFO = "/gene/DATA/GENE_INFO/All_Data.gene_info.gz";
  @InjectMocks
  private DownloadProteinMappingServiceBean downloadProteinMappingServiceBean;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Mock
  private FtpClientFactory ftpClientFactory;
  @Mock
  private FTPClient client;
  @Mock
  private ProgressBar progressBar;
  @Mock
  private ApplicationConfiguration applicationConfiguration;
  @Captor
  private ArgumentCaptor<Command> commandCaptor;
  @Captor
  private ArgumentCaptor<Path> pathCaptor;
  private Path annotationsFolder;
  private Path fasta;
  private Path additionalFasta;
  private Path idMapping;
  private Path geneInfo;
  private Locale locale = Locale.ENGLISH;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    downloadProteinMappingServiceBean = new DownloadProteinMappingServiceBean(ftpClientFactory,
        new IdMappingParser(), new GeneInfoParser(), applicationConfiguration);
    when(ftpClientFactory.create()).thenReturn(client);
    annotationsFolder = temporaryFolder.newFolder("annotations").toPath();
    fasta = temporaryFolder.getRoot().toPath().resolve("human.fasta.gz");
    copyAndCompress(Paths.get(getClass().getResource("/annotation/UP000005640_9606.fasta").toURI()),
        fasta);
    additionalFasta = temporaryFolder.getRoot().toPath().resolve("human_additional.fasta.gz");
    copyAndCompress(
        Paths.get(getClass().getResource("/annotation/UP000005640_9606_additional.fasta").toURI()),
        additionalFasta);
    idMapping = temporaryFolder.getRoot().toPath().resolve("human.idmapping.gz");
    copyAndCompress(
        Paths.get(getClass().getResource("/annotation/UP000005640_9606.idmapping").toURI()),
        idMapping);
    geneInfo = temporaryFolder.getRoot().toPath().resolve("human.gene_info.gz");
    copyAndCompress(Paths.get(getClass().getResource("/annotation/Homo_sapiens.gene_info").toURI()),
        geneInfo);
    retrieveFileAnswer(NCBI_GENE_INFO, geneInfo);
    when(progressBar.step(anyDouble())).thenReturn(progressBar);
    when(applicationConfiguration.annotationsFolder()).thenReturn(annotationsFolder);
  }

  private ProteinMapping findMapping(Collection<ProteinMapping> mappings, String proteinId) {
    for (ProteinMapping mapping : mappings) {
      if (proteinId.equals(mapping.getProteinId())) {
        return mapping;
      }
    }
    return null;
  }

  private void copyAndCompress(Path source, Path destination) throws IOException {
    try (OutputStream output = new GZIPOutputStream(Files.newOutputStream(destination))) {
      Files.copy(source, output);
    }
  }

  private void retrieveFileAnswer(final String remote, final Path source) throws IOException {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        OutputStream output = (OutputStream) invocation.getArguments()[1];
        Files.copy(source, output);
        return null;
      }
    }).when(client).retrieveFile(eq(remote), any(OutputStream.class));
  }

  @Test
  public void downloadMappings() throws Throwable {
    when(client.getReplyCode()).thenReturn(FTPReply.COMMAND_OK);
    when(client.login(anyString(), anyString())).thenReturn(true);
    FTPFile mammalian = Mockito.mock(FTPFile.class);
    when(mammalian.getName()).thenReturn("mammalian");
    when(mammalian.isDirectory()).thenReturn(true);
    FTPFile fasta1 = Mockito.mock(FTPFile.class);
    when(fasta1.getName()).thenReturn("UP000005640_9606.fasta.gz");
    when(fasta1.isFile()).thenReturn(true);
    FTPFile additionalFasta1 = Mockito.mock(FTPFile.class);
    when(additionalFasta1.getName()).thenReturn("UP000005640_9606_additional.fasta.gz");
    when(additionalFasta1.isFile()).thenReturn(true);
    FTPFile gene2acc1 = Mockito.mock(FTPFile.class);
    when(gene2acc1.getName()).thenReturn("UP000005640_9606_DNA.gene2acc.gz");
    when(gene2acc1.isFile()).thenReturn(true);
    FTPFile dna1 = Mockito.mock(FTPFile.class);
    when(dna1.getName()).thenReturn("UP000005640_9606_DNA.fasta.gz");
    when(dna1.isFile()).thenReturn(true);
    FTPFile dnaMiss1 = Mockito.mock(FTPFile.class);
    when(dnaMiss1.getName()).thenReturn("UP000005640_9606_DNA.miss.gz");
    when(dnaMiss1.isFile()).thenReturn(true);
    FTPFile idMapping1 = Mockito.mock(FTPFile.class);
    when(idMapping1.getName()).thenReturn("UP000005640_9606.idmapping.gz");
    when(idMapping1.isFile()).thenReturn(true);
    when(client.listFiles()).thenReturn(new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 },
        new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 });
    when(client.changeWorkingDirectory(anyString())).thenReturn(true);
    retrieveFileAnswer(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.fasta.gz", fasta);
    retrieveFileAnswer(UNIPROT_FOLDER + "/mammalian/UP000005640_9606_additional.fasta.gz",
        additionalFasta);
    retrieveFileAnswer(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.idmapping.gz", idMapping);

    final Collection<ProteinMapping> mappings = downloadProteinMappingServiceBean
        .allProteinMappings(new Organism(9606), progressBar, locale);

    verify(progressBar, atLeastOnce()).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(anyString());
    verify(client, atLeastOnce()).connect(UNIPROT_HOST);
    verify(client, atLeastOnce()).connect(NCBI_HOST);
    verify(client, atLeast(2)).login(eq("anonymous"), anyString());
    verify(client, atLeastOnce()).listFiles();
    verify(client, atLeastOnce()).setFileType(FTP.BINARY_FILE_TYPE);
    verify(client).retrieveFile(eq(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.idmapping.gz"),
        any(OutputStream.class));
    verify(client).retrieveFile(eq(NCBI_GENE_INFO), any(OutputStream.class));
    assertEquals(49, mappings.size());
    ProteinMapping mapping = findMapping(mappings, "A0A024RAP8");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A024RAP8", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    assertEquals("killer cell lectin like receptor K1", mapping.getGeneSummary());
    assertEquals("CD314|D12S2489E|KLR|NKG2-D|NKG2D", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "315221164");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("315221164", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    assertEquals("killer cell lectin like receptor K1", mapping.getGeneSummary());
    assertEquals("CD314|D12S2489E|KLR|NKG2-D|NKG2D", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "NP_001186734.1");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("NP_001186734.1", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    assertEquals("killer cell lectin like receptor K1", mapping.getGeneSummary());
    assertEquals("CD314|D12S2489E|KLR|NKG2-D|NKG2D", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "A0A075B6I6");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A075B6I6", mapping.getProteinId());
    assertEquals("MAWSSLLLTLLAHCTGSWAQSVLTQPPSVSGAPGQRVTISCTGSSSNIGAGYVVHWYQQLPGTAPKLLIYGNSNRPSGV"
        + "PDQFSGSKSGTSASLAITGLQSEDEADYYCKAWDNSLNA", mapping.getSequence());
    assertNull(mapping.getGeneId());
    assertNull(mapping.getGeneName());
    assertNull(mapping.getGeneSummary());
    assertNull(mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "A0A075B759");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A075B759", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    assertEquals("peptidylprolyl isomerase A like 4F", mapping.getGeneSummary());
    assertTrue(mapping.getGeneSynonyms().isEmpty());
    mapping = findMapping(mappings, "528881443");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("528881443", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    assertEquals("peptidylprolyl isomerase A like 4F", mapping.getGeneSummary());
    assertTrue(mapping.getGeneSynonyms().isEmpty());
    mapping = findMapping(mappings, "NP_001137504.2");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("NP_001137504.2", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    assertEquals("peptidylprolyl isomerase A like 4F", mapping.getGeneSummary());
    assertTrue(mapping.getGeneSynonyms().isEmpty());
    mapping = findMapping(mappings, "A0AV96");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0AV96", mapping.getProteinId());
    assertEquals("MTAEDSTAAMSSDSAAGSSAKVPEGVAGAPNEAALLALMERTGYSMVQENGQRKYGGPPPGWEGPHPQRGCEVFVGKIP"
        + "RDVYEDELVPVFEAVGRIYELRLMMDFDGKNRGYAFVMYCHKHEAKRAVRELNNYEIRPGRLLGVCCSVDNCRLFIGGI"
        + "PKMKKREEILEEIAKVTEGVLDVIVYASAADKMKNRGFAFVEYESHRAAAMARRKLMPGRIQLWGHQIAVDWAEPEIDV"
        + "DEDVMETVKILYVRNLMIETTEDTIKKSFGQFNPGCVERVKKIRDYAFVHFTSREDAVHAMNNLNGTELEGSCLEVTLA"
        + "KPVDKEQYSRYQKAARGGGAAEAAQQPSYVYSCDPYTLAYYGYPYNALIGPNRDYFVKAGSIRGRGRGAAGNRAPGPRG"
        + "SYLGGYSAGRGIYSRYHEGKGKQQEKGYELVPNLEIPTVNPVAIKPGTVAIPAIGAQYSMFPAAPAPKMIEDGKIHTVE"
        + "HMISPIAVQPDPASAAAAAAAAAAAAAAVIPTVSTPPPFQGRPITPVYTVAPNVQRIPTAGIYGASYVPFAAPATATIA"
        + "TLQKNAAAAAAMYGGYAGYIPQAFPAAAIQVPIPDVYQTY", mapping.getSequence());
    assertEquals((Long) 54502L, mapping.getGeneId());
    assertEquals("RBM47", mapping.getGeneName());
    assertEquals("RNA binding motif protein 47", mapping.getGeneSummary());
    assertEquals("NET18", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "20450941");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("20450941", mapping.getProteinId());
    assertEquals("MTAEDSTAAMSSDSAAGSSAKVPEGVAGAPNEAALLALMERTGYSMVQENGQRKYGGPPPGWEGPHPQRGCEVFVGKIP"
        + "RDVYEDELVPVFEAVGRIYELRLMMDFDGKNRGYAFVMYCHKHEAKRAVRELNNYEIRPGRLLGVCCSVDNCRLFIGGI"
        + "PKMKKREEILEEIAKVTEGVLDVIVYASAADKMKNRGFAFVEYESHRAAAMARRKLMPGRIQLWGHQIAVDWAEPEIDV"
        + "DEDVMETVKILYVRNLMIETTEDTIKKSFGQFNPGCVERVKKIRDYAFVHFTSREDAVHAMNNLNGTELEGSCLEVTLA"
        + "KPVDKEQYSRYQKAARGGGAAEAAQQPSYVYSCDPYTLAYYGYPYNALIGPNRDYFVKAGSIRGRGRGAAGNRAPGPRG"
        + "SYLGGYSAGRGIYSRYHEGKGKQQEKGYELVPNLEIPTVNPVAIKPGTVAIPAIGAQYSMFPAAPAPKMIEDGKIHTVE"
        + "HMISPIAVQPDPASAAAAAAAAAAAAAAVIPTVSTPPPFQGRPITPVYTVAPNVQRIPTAGIYGASYVPFAAPATATIA"
        + "TLQKNAAAAAAMYGGYAGYIPQAFPAAAIQVPIPDVYQTY", mapping.getSequence());
    assertEquals((Long) 54502L, mapping.getGeneId());
    assertEquals("RBM47", mapping.getGeneName());
    assertEquals("RNA binding motif protein 47", mapping.getGeneSummary());
    assertEquals("NET18", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "A0A024R412");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A024R412", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
    assertEquals("neuropilin 2", mapping.getGeneSummary());
    assertEquals("NP2|NPN2|PRO2714|VEGF165R2", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "11934950");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("11934950", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
    assertEquals("neuropilin 2", mapping.getGeneSummary());
    assertEquals("NP2|NPN2|PRO2714|VEGF165R2", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "119590779");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("119590779", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
    assertEquals("neuropilin 2", mapping.getGeneSummary());
    assertEquals("NP2|NPN2|PRO2714|VEGF165R2", mapping.getGeneSynonyms());
  }

  @Test
  public void downloadMappings_SkipDownloadOfRecentFiles() throws Throwable {
    Files.copy(geneInfo, annotationsFolder.resolve(Paths.get(NCBI_GENE_INFO).getFileName()));
    Files.copy(idMapping, annotationsFolder.resolve("UP000005640_9606.idmapping.gz"));
    Files.copy(fasta, annotationsFolder.resolve("UP000005640_9606.fasta.gz"));
    Files.copy(additionalFasta, annotationsFolder.resolve("UP000005640_9606_additional.fasta.gz"));
    when(client.getReplyCode()).thenReturn(FTPReply.COMMAND_OK);
    when(client.login(anyString(), anyString())).thenReturn(true);
    FTPFile mammalian = Mockito.mock(FTPFile.class);
    when(mammalian.getName()).thenReturn("mammalian");
    when(mammalian.isDirectory()).thenReturn(true);
    FTPFile fasta1 = Mockito.mock(FTPFile.class);
    when(fasta1.getName()).thenReturn("UP000005640_9606.fasta.gz");
    when(fasta1.isFile()).thenReturn(true);
    FTPFile additionalFasta1 = Mockito.mock(FTPFile.class);
    when(additionalFasta1.getName()).thenReturn("UP000005640_9606_additional.fasta.gz");
    when(additionalFasta1.isFile()).thenReturn(true);
    FTPFile gene2acc1 = Mockito.mock(FTPFile.class);
    when(gene2acc1.getName()).thenReturn("UP000005640_9606_DNA.gene2acc.gz");
    when(gene2acc1.isFile()).thenReturn(true);
    FTPFile dna1 = Mockito.mock(FTPFile.class);
    when(dna1.getName()).thenReturn("UP000005640_9606_DNA.fasta.gz");
    when(dna1.isFile()).thenReturn(true);
    FTPFile dnaMiss1 = Mockito.mock(FTPFile.class);
    when(dnaMiss1.getName()).thenReturn("UP000005640_9606_DNA.miss.gz");
    when(dnaMiss1.isFile()).thenReturn(true);
    FTPFile idMapping1 = Mockito.mock(FTPFile.class);
    when(idMapping1.getName()).thenReturn("UP000005640_9606.idmapping.gz");
    when(idMapping1.isFile()).thenReturn(true);
    when(client.listFiles()).thenReturn(new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 },
        new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 });
    when(client.changeWorkingDirectory(anyString())).thenReturn(true);
    retrieveFileAnswer(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.idmapping.gz", idMapping);

    final Collection<ProteinMapping> mappings = downloadProteinMappingServiceBean
        .allProteinMappings(new Organism(9606), progressBar, locale);

    verify(progressBar, atLeastOnce()).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(anyString());
    verify(client, atLeastOnce()).connect(UNIPROT_HOST);
    verify(client, atLeastOnce()).login(eq("anonymous"), anyString());
    verify(client, atLeastOnce()).listFiles();
    verify(client, never()).retrieveFile(anyString(), any(OutputStream.class));
    assertEquals(49, mappings.size());
    ProteinMapping mapping = findMapping(mappings, "A0A024RAP8");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A024RAP8", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    mapping = findMapping(mappings, "315221164");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("315221164", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    mapping = findMapping(mappings, "NP_001186734.1");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("NP_001186734.1", mapping.getProteinId());
    assertEquals("MGWIRGRRSRHSWEMSEFHNYNLDLKKSDFSTRWQKQRCPVVKSKCRENASPFFFCCFIAVAMGIRFIIMVTIWSAVFL"
        + "NSLFNQEVQIPLTESYCGPCPKNWICYKNNCYQFFDESKNWYESQASCMSQNASLLKVYSKEDQDLLKLVKSYHWMGLV"
        + "HIPTNGSWQWEDGSILSPNLLTIIEMQKGDCALYASSFKGYIENCSTPNTYICMQRTV", mapping.getSequence());
    assertEquals((Long) 22914L, mapping.getGeneId());
    assertEquals("KLRK1", mapping.getGeneName());
    assertEquals("killer cell lectin like receptor K1", mapping.getGeneSummary());
    assertEquals("CD314|D12S2489E|KLR|NKG2-D|NKG2D", mapping.getGeneSynonyms());
    mapping = findMapping(mappings, "A0A075B6I6");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A075B6I6", mapping.getProteinId());
    assertEquals("MAWSSLLLTLLAHCTGSWAQSVLTQPPSVSGAPGQRVTISCTGSSSNIGAGYVVHWYQQLPGTAPKLLIYGNSNRPSGV"
        + "PDQFSGSKSGTSASLAITGLQSEDEADYYCKAWDNSLNA", mapping.getSequence());
    assertNull(mapping.getGeneId());
    assertNull(mapping.getGeneName());
    mapping = findMapping(mappings, "A0A075B759");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A075B759", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    mapping = findMapping(mappings, "528881443");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("528881443", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    mapping = findMapping(mappings, "NP_001137504.2");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("NP_001137504.2", mapping.getProteinId());
    assertEquals("MVNSVVFFEITRDGKPLGRISIKLFADKIPKTAENFRALSTGEKGFRYKGSCFHRIIPGFMCQGGDFTRPNGTGDKSIY"
        + "GEKFDDENLIRKHTGSGILSMANAGPNTNGSQFFICAAKTEWLDGKHVAFGKVKERVNIVEAMEHFGYRNSKTSKKITI"
        + "ADCGQF", mapping.getSequence());
    assertEquals((Long) 728945L, mapping.getGeneId());
    assertEquals("PPIAL4F", mapping.getGeneName());
    assertEquals("peptidylprolyl isomerase A like 4F", mapping.getGeneSummary());
    assertTrue(mapping.getGeneSynonyms().isEmpty());
    mapping = findMapping(mappings, "A0AV96");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0AV96", mapping.getProteinId());
    assertEquals("MTAEDSTAAMSSDSAAGSSAKVPEGVAGAPNEAALLALMERTGYSMVQENGQRKYGGPPPGWEGPHPQRGCEVFVGKIP"
        + "RDVYEDELVPVFEAVGRIYELRLMMDFDGKNRGYAFVMYCHKHEAKRAVRELNNYEIRPGRLLGVCCSVDNCRLFIGGI"
        + "PKMKKREEILEEIAKVTEGVLDVIVYASAADKMKNRGFAFVEYESHRAAAMARRKLMPGRIQLWGHQIAVDWAEPEIDV"
        + "DEDVMETVKILYVRNLMIETTEDTIKKSFGQFNPGCVERVKKIRDYAFVHFTSREDAVHAMNNLNGTELEGSCLEVTLA"
        + "KPVDKEQYSRYQKAARGGGAAEAAQQPSYVYSCDPYTLAYYGYPYNALIGPNRDYFVKAGSIRGRGRGAAGNRAPGPRG"
        + "SYLGGYSAGRGIYSRYHEGKGKQQEKGYELVPNLEIPTVNPVAIKPGTVAIPAIGAQYSMFPAAPAPKMIEDGKIHTVE"
        + "HMISPIAVQPDPASAAAAAAAAAAAAAAVIPTVSTPPPFQGRPITPVYTVAPNVQRIPTAGIYGASYVPFAAPATATIA"
        + "TLQKNAAAAAAMYGGYAGYIPQAFPAAAIQVPIPDVYQTY", mapping.getSequence());
    assertEquals((Long) 54502L, mapping.getGeneId());
    assertEquals("RBM47", mapping.getGeneName());
    mapping = findMapping(mappings, "20450941");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("20450941", mapping.getProteinId());
    assertEquals("MTAEDSTAAMSSDSAAGSSAKVPEGVAGAPNEAALLALMERTGYSMVQENGQRKYGGPPPGWEGPHPQRGCEVFVGKIP"
        + "RDVYEDELVPVFEAVGRIYELRLMMDFDGKNRGYAFVMYCHKHEAKRAVRELNNYEIRPGRLLGVCCSVDNCRLFIGGI"
        + "PKMKKREEILEEIAKVTEGVLDVIVYASAADKMKNRGFAFVEYESHRAAAMARRKLMPGRIQLWGHQIAVDWAEPEIDV"
        + "DEDVMETVKILYVRNLMIETTEDTIKKSFGQFNPGCVERVKKIRDYAFVHFTSREDAVHAMNNLNGTELEGSCLEVTLA"
        + "KPVDKEQYSRYQKAARGGGAAEAAQQPSYVYSCDPYTLAYYGYPYNALIGPNRDYFVKAGSIRGRGRGAAGNRAPGPRG"
        + "SYLGGYSAGRGIYSRYHEGKGKQQEKGYELVPNLEIPTVNPVAIKPGTVAIPAIGAQYSMFPAAPAPKMIEDGKIHTVE"
        + "HMISPIAVQPDPASAAAAAAAAAAAAAAVIPTVSTPPPFQGRPITPVYTVAPNVQRIPTAGIYGASYVPFAAPATATIA"
        + "TLQKNAAAAAAMYGGYAGYIPQAFPAAAIQVPIPDVYQTY", mapping.getSequence());
    assertEquals((Long) 54502L, mapping.getGeneId());
    assertEquals("RBM47", mapping.getGeneName());
    mapping = findMapping(mappings, "A0A024R412");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("A0A024R412", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
    mapping = findMapping(mappings, "11934950");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("11934950", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
    mapping = findMapping(mappings, "119590779");
    assertNotNull(mapping);
    assertEquals((Integer) 9606, mapping.getTaxonomyId());
    assertEquals("119590779", mapping.getProteinId());
    assertEquals("MDMFPLTWVFLALYFSRHQVRGQPDPPCGGRLNSKDAGYITSPGYPQDYPSHQNCEWIVYAPEPNQKIVLNFNPHFEIE"
        + "KHDCKYDFIEIRDGDSESADLLGKHCGNIAPPTIISSGSMLYIKFTSDYARQGAGFSLRYEIFKTGSEDCSKNFTSPNG"
        + "TIESPGFPEKYPHNLDCTFTILAKPKMEIILQFLIFDLEHDPLQVGEGDCKYDWLDIWDGIPHVGPLIGKYCGTKTPSE"
        + "LRSSTGILSLTFHTDMAVAKDGFSARYYLVHQEPLENFQCNVPLGMESGRIANEQISASSTYSDGRWTPQQSRLHGDDN"
        + "GWTPNLDSNKEYLQVDLRFLTMLTAIATQGAISRETQNGYYVKSYKLEVSTNGEDWMVYRHGKNHKVFQANNDATEVVL"
        + "NKLHAPLLTRFVRIRPQTWHSGIALRLELFGCRVTDAPCSNMLGMLSGLIADSQISASSTQEYLWSPSAARLVSSRSGW"
        + "FPRIPQAQPGEEWLQVDLGTPKTVKGVIIQGARGGDSITAVEARAFVRKFKVSYSLNGKDWEYIQDPRTQQPKLFEGNM"
        + "HYDTPDIRRFDPIPAQYVRVYPERWSPAGIGMRLEVLGCDWTDSKPTVETLGPTVKSEETTTPYPTEEEATECGENCSF"
        + "EDDKDLQLPSGFNCNFDFLEEPCGWMYDHAKWLRTTWASSSSPNDRTFPDDRNFLRLQSDSQREGQYARLISPPVHLPR"
        + "SPVCMEFQYQATGGRGVALQVVREASQESKLLWVIREDQGGEWKHGRIILPSYDMEYQIVFEGVIGKGRSGEIAIDDIR"
        + "ISTDVPLENCMEPISAFAGGTLLPGTEPTVDTVPMQPIPAYWYYVMAAGGAVLVLVSVALALVLHYHRFRYAAKKTDHS"
        + "ITYKTSHYTNGAPLAVEPTLTIKLEQDRGSHC", mapping.getSequence());
    assertEquals((Long) 8828L, mapping.getGeneId());
    assertEquals("NRP2", mapping.getGeneName());
  }

  @Test
  public void downloadMappings_Mouse() throws Throwable {
    when(client.getReplyCode()).thenReturn(FTPReply.COMMAND_OK);
    when(client.login(anyString(), anyString())).thenReturn(true);
    FTPFile mammalian = Mockito.mock(FTPFile.class);
    when(mammalian.getName()).thenReturn("mammalian");
    when(mammalian.isDirectory()).thenReturn(true);
    FTPFile fasta1 = Mockito.mock(FTPFile.class);
    when(fasta1.getName()).thenReturn("UP000005640_9606.fasta.gz");
    when(fasta1.isFile()).thenReturn(true);
    FTPFile additionalFasta1 = Mockito.mock(FTPFile.class);
    when(additionalFasta1.getName()).thenReturn("UP000005640_9606_additional.fasta.gz");
    when(additionalFasta1.isFile()).thenReturn(true);
    FTPFile gene2acc1 = Mockito.mock(FTPFile.class);
    when(gene2acc1.getName()).thenReturn("UP000005640_9606_DNA.gene2acc.gz");
    when(gene2acc1.isFile()).thenReturn(true);
    FTPFile dna1 = Mockito.mock(FTPFile.class);
    when(dna1.getName()).thenReturn("UP000005640_9606_DNA.fasta.gz");
    when(dna1.isFile()).thenReturn(true);
    FTPFile dnaMiss1 = Mockito.mock(FTPFile.class);
    when(dnaMiss1.getName()).thenReturn("UP000005640_9606_DNA.miss.gz");
    when(dnaMiss1.isFile()).thenReturn(true);
    FTPFile idMapping1 = Mockito.mock(FTPFile.class);
    when(idMapping1.getName()).thenReturn("UP000005640_9606.idmapping.gz");
    when(idMapping1.isFile()).thenReturn(true);
    when(client.listFiles()).thenReturn(new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 },
        new FTPFile[] { mammalian },
        new FTPFile[] { fasta1, additionalFasta1, gene2acc1, dna1, dnaMiss1, idMapping1 });
    when(client.changeWorkingDirectory(anyString())).thenReturn(true);
    retrieveFileAnswer(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.idmapping.gz", idMapping);

    final Collection<ProteinMapping> mappings = downloadProteinMappingServiceBean
        .allProteinMappings(new Organism(10090), progressBar, locale);

    verify(progressBar, atLeastOnce()).setProgress(anyDouble());
    verify(progressBar, atLeastOnce()).setMessage(anyString());
    verify(client, atLeastOnce()).connect(UNIPROT_HOST);
    verify(client, atLeastOnce()).connect(NCBI_HOST);
    verify(client, atLeast(2)).login(eq("anonymous"), anyString());
    verify(client, atLeastOnce()).listFiles();
    verify(client, atLeastOnce()).setFileType(FTP.BINARY_FILE_TYPE);
    verify(client, never()).retrieveFile(
        eq(UNIPROT_FOLDER + "/mammalian/UP000005640_9606.idmapping.gz"), any(OutputStream.class));
    verify(client).retrieveFile(eq(NCBI_GENE_INFO), any(OutputStream.class));
    assertEquals(0, mappings.size());
  }
}
