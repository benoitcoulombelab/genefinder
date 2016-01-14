package ca.qc.ircm.genefinder.ncbi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import ca.qc.ircm.genefinder.ApplicationProperties;
import ca.qc.ircm.genefinder.organism.Organism;
import ca.qc.ircm.genefinder.test.config.Rules;
import ca.qc.ircm.progress_bar.ProgressBar;
import ca.qc.ircm.protein.ProteinService;

public class NcbiServiceBeanTest {
    private NcbiServiceBean ncbiServiceBean;
    @Mock
    private ProteinService proteinService;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private Organism organism;
    @Mock
    private ProgressBar progressBar;
    @Captor
    private ArgumentCaptor<Collection<ProteinMapping>> proteinMappingsCaptor;
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public RuleChain rules = Rules.defaultRules(this).around(temporaryFolder);
    private File home;
    private Locale locale;

    @Before
    public void beforeTest() throws Throwable {
	ncbiServiceBean = new NcbiServiceBean(proteinService, applicationProperties);
	locale = Locale.getDefault();
	home = temporaryFolder.newFolder("home");
	when(applicationProperties.getHome()).thenReturn(home);
	File file = temporaryFolder.newFile("gene2accession.gz");
	try (InputStream input = getClass().getResourceAsStream("/gene2accession.txt");
		OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
	    IOUtils.copy(input, output);
	}
	when(applicationProperties.getProperty("ncbi.gene2accession"))
		.thenReturn(file.toURI().toURL().toExternalForm());
	file = temporaryFolder.newFile("gene_info.gz");
	try (InputStream input = getClass().getResourceAsStream("/gene_info.txt");
		OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
	    IOUtils.copy(input, output);
	}
	when(applicationProperties.getProperty("ncbi.gene_info")).thenReturn(file.toURI().toURL().toExternalForm());
	file = temporaryFolder.newFile("gi_taxid.txt");
	try (InputStream input = getClass().getResourceAsStream("/gi_taxid.txt");
		OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
	    IOUtils.copy(input, output);
	}
	when(applicationProperties.getProperty("ncbi.gi_taxid")).thenReturn(file.toURI().toURL().toExternalForm());
	file = temporaryFolder.newFile("nr.txt");
	try (InputStream input = getClass().getResourceAsStream("/nr.txt");
		OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
	    IOUtils.copy(input, output);
	}
	when(applicationProperties.getProperty("ncbi.nr")).thenReturn(file.toURI().toURL().toExternalForm());
    }

    private Optional<ProteinMapping> find(Collection<ProteinMapping> mappings, int gi) {
	return mappings.stream().filter(pm -> pm.getGi() == gi).findFirst();
    }

    @Test
    public void allProteinMappings_9606() throws Throwable {
	when(organism.getId()).thenReturn(9606);
	when(proteinService.weight(any())).thenReturn(12.5);
	ProteinMappingParametersBean parameters = new ProteinMappingParametersBean();
	parameters.geneId(true);
	parameters.geneDetails(true);
	parameters.sequence(true);
	parameters.molecularWeight(true);

	List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, parameters, progressBar, locale);

	verify(applicationProperties).getProperty("ncbi.gene2accession");
	verify(applicationProperties).getProperty("ncbi.gene_info");
	verify(applicationProperties).getProperty("ncbi.gi_taxid");
	verify(applicationProperties).getProperty("ncbi.nr");
	verify(proteinService).weight(
		"MSMLVVFLLLWGVTWGPVTEAAIFYETQPSLWAESESLLKPLANVTLTCQARLETPDFQLFKNGVAQEPVHLDSPAIKHQFLLTGDTQGRYRCRSGLSTGWTQLSKLLELTGPKSLPAPWLSMAPVSWITPGLKTTAVCRGVLRGVTFLLRREGDHEFLEVPEAQEDVEATFPVHQPGNYSCSYRTDGEGALSEPSATVTIEELAAPPPPVLMHHGESSQVLHPGNKVTLTCVAPLSGVDFQLRRGEKELLVPRSSTSPDRIFFHLNAVALGDGGHYTCRYRLHDNQNGWSGDSAPVELILSDETLPAPEFSPEPESGRALRLRCLAPLEGARFALVREDRGGRRVHRFQSPAGTEALFELHNISVADSANYSCVYVDLKPPFGGSAPSERLELHVDGPPPRPQLRATWSGAVLAGRDAVLRCEGPIPDVTFELLREGETKAVKTVRTPGAAANLELIFVGPQHAGNYRCRYRSWVPHTFESELSDPVELLVAES");
	verify(proteinService).weight("TEAAIFYETQ");
	verify(proteinService).weight("QLFKNGVAQEPV");
	verify(proteinService).weight("ELTGPKSL");
	verify(proteinService).weight("MSMLVVFLLL");
	verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
	verify(progressBar, atLeastOnce()).setMessage(any(String.class));
	assertEquals(21, mappings.size());
	ProteinMapping mapping = find(mappings, 119592981).get();
	assertEquals((Integer) 119592981, mapping.getGi());
	assertEquals((Integer) 1, mapping.getGeneId());
	assertEquals("A1BG", mapping.getGeneName());
	assertEquals("A1B|ABG|GAB|HYST2477", mapping.getGeneSynonyms());
	assertEquals("alpha-1-B glycoprotein", mapping.getGeneSummary());
	assertEquals(
		"MSMLVVFLLLWGVTWGPVTEAAIFYETQPSLWAESESLLKPLANVTLTCQARLETPDFQLFKNGVAQEPVHLDSPAIKHQFLLTGDTQGRYRCRSGLSTGWTQLSKLLELTGPKSLPAPWLSMAPVSWITPGLKTTAVCRGVLRGVTFLLRREGDHEFLEVPEAQEDVEATFPVHQPGNYSCSYRTDGEGALSEPSATVTIEELAAPPPPVLMHHGESSQVLHPGNKVTLTCVAPLSGVDFQLRRGEKELLVPRSSTSPDRIFFHLNAVALGDGGHYTCRYRLHDNQNGWSGDSAPVELILSDETLPAPEFSPEPESGRALRLRCLAPLEGARFALVREDRGGRRVHRFQSPAGTEALFELHNISVADSANYSCVYVDLKPPFGGSAPSERLELHVDGPPPRPQLRATWSGAVLAGRDAVLRCEGPIPDVTFELLREGETKAVKTVRTPGAAANLELIFVGPQHAGNYRCRYRSWVPHTFESELSDPVELLVAES",
		mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
	mapping = find(mappings, 317373553).get();
	assertEquals((Integer) 317373553, mapping.getGi());
	assertEquals((Integer) 1, mapping.getGeneId());
	assertEquals("A1BG", mapping.getGeneName());
	assertEquals("A1B|ABG|GAB|HYST2477", mapping.getGeneSynonyms());
	assertEquals("alpha-1-B glycoprotein", mapping.getGeneSummary());
	assertEquals("TEAAIFYETQ", mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
	mapping = find(mappings, 13661814).get();
	assertEquals((Integer) 13661814, mapping.getGi());
	assertEquals((Integer) 2, mapping.getGeneId());
	assertEquals("A2M", mapping.getGeneName());
	assertEquals("A2MD|CPAMD5|FWP007|S863-7", mapping.getGeneSynonyms());
	assertEquals("alpha-2-macroglobulin", mapping.getGeneSummary());
	assertEquals("QLFKNGVAQEPV", mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
	mapping = find(mappings, 2245376).get();
	assertEquals((Integer) 2245376, mapping.getGi());
	assertEquals((Integer) 9, mapping.getGeneId());
	assertEquals("NAT1", mapping.getGeneName());
	assertEquals("AAC1|MNAT|NAT-1|NATI", mapping.getGeneSynonyms());
	assertEquals("N-acetyltransferase 1 (arylamine N-acetyltransferase)", mapping.getGeneSummary());
	assertEquals("ELTGPKSL", mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
	mapping = find(mappings, 123456).get();
	assertEquals((Integer) 123456, mapping.getGi());
	assertEquals(null, mapping.getGeneId());
	assertEquals(null, mapping.getGeneName());
	assertEquals(null, mapping.getGeneSynonyms());
	assertEquals(null, mapping.getGeneSummary());
	assertEquals("MSMLVVFLLL", mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
    }

    @Test
    public void allProteinMappings_10090() throws Throwable {
	when(organism.getId()).thenReturn(10090);
	ProteinMappingParametersBean parameters = new ProteinMappingParametersBean();
	parameters.geneId(true);
	parameters.geneDetails(true);
	parameters.sequence(true);
	parameters.molecularWeight(true);

	List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, parameters, progressBar, locale);

	verify(applicationProperties).getProperty("ncbi.gene2accession");
	verify(applicationProperties).getProperty("ncbi.gene_info");
	verify(applicationProperties).getProperty("ncbi.gi_taxid");
	verify(applicationProperties).getProperty("ncbi.nr");
	verify(proteinService).weight("VLMHHGESS");
	verify(proteinService).weight(
		"MRRNQLPTPAFLLLFLLLPRDATTATAKPQYVVLVPSEVYSGIPEKACVSLNHVNETVMLSLTLEYAMQQTKLLTDQAVDKDSFYCSPFTISGSPLPYTFITVEIKGPTQRFIKKKSIQIIKAESPVFVQTDKPIYKPGQIVKFRVVSVDISFRPLNETFPVVYIETPKRNRIFQWQNIHLAGGLHQLSFPLSVEPALGIYKVVVQKDSGKKIEHSFEVKEYVLPKF");
	verify(proteinService).weight("FFHLNAVAL");
	verify(proteinService).weight("LEGARFALVRED");
	verify(proteinService).weight("LAAPPPP");
	verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
	verify(progressBar, atLeastOnce()).setMessage(any(String.class));
	assertEquals(26, mappings.size());
	ProteinMapping mapping = find(mappings, 463884).get();
	assertEquals((Integer) 463884, mapping.getGi());
	assertEquals((Integer) 11287, mapping.getGeneId());
	assertEquals("Pzp", mapping.getGeneName());
	assertEquals("A1m|A2m|AI893533|MAM", mapping.getGeneSynonyms());
	assertEquals("pregnancy zone protein", mapping.getGeneSummary());
	assertEquals("VLMHHGESS", mapping.getSequence());
	assertEquals((Integer) 10090, mapping.getTaxonomyId());
	mapping = find(mappings, 148667474).get();
	assertEquals((Integer) 148667474, mapping.getGi());
	assertEquals((Integer) 11287, mapping.getGeneId());
	assertEquals("Pzp", mapping.getGeneName());
	assertEquals("A1m|A2m|AI893533|MAM", mapping.getGeneSynonyms());
	assertEquals("pregnancy zone protein", mapping.getGeneSummary());
	assertEquals(
		"MRRNQLPTPAFLLLFLLLPRDATTATAKPQYVVLVPSEVYSGIPEKACVSLNHVNETVMLSLTLEYAMQQTKLLTDQAVDKDSFYCSPFTISGSPLPYTFITVEIKGPTQRFIKKKSIQIIKAESPVFVQTDKPIYKPGQIVKFRVVSVDISFRPLNETFPVVYIETPKRNRIFQWQNIHLAGGLHQLSFPLSVEPALGIYKVVVQKDSGKKIEHSFEVKEYVLPKF",
		mapping.getSequence());
	assertEquals((Integer) 10090, mapping.getTaxonomyId());
	mapping = find(mappings, 4099097).get();
	assertEquals((Integer) 4099097, mapping.getGi());
	assertEquals((Integer) 11298, mapping.getGeneId());
	assertEquals("Aanat", mapping.getGeneName());
	assertEquals("AA-NAT|Nat-2|Nat4|Snat", mapping.getGeneSynonyms());
	assertEquals("arylalkylamine N-acetyltransferase", mapping.getGeneSummary());
	assertEquals("FFHLNAVAL", mapping.getSequence());
	assertEquals((Integer) 10090, mapping.getTaxonomyId());
	mapping = find(mappings, 81912939).get();
	assertEquals((Integer) 81912939, mapping.getGi());
	assertEquals((Integer) 11302, mapping.getGeneId());
	assertEquals("Aatk", mapping.getGeneName());
	assertEquals("AATYK|aatyk1|mKIAA0641", mapping.getGeneSynonyms());
	assertEquals("apoptosis-associated tyrosine kinase", mapping.getGeneSummary());
	assertEquals("LEGARFALVRED", mapping.getSequence());
	assertEquals((Integer) 10090, mapping.getTaxonomyId());
	mapping = find(mappings, 456789).get();
	assertEquals((Integer) 456789, mapping.getGi());
	assertEquals(null, mapping.getGeneId());
	assertEquals(null, mapping.getGeneName());
	assertEquals(null, mapping.getGeneSynonyms());
	assertEquals(null, mapping.getGeneSummary());
	assertEquals("LAAPPPP", mapping.getSequence());
	assertEquals((Integer) 10090, mapping.getTaxonomyId());
    }

    @Test
    public void allProteinMappings_NoGeneIdAndDetails() throws Throwable {
	when(organism.getId()).thenReturn(9606);
	when(proteinService.weight(any())).thenReturn(12.5);
	ProteinMappingParametersBean parameters = new ProteinMappingParametersBean();
	parameters.geneId(false);
	parameters.geneDetails(false);
	parameters.sequence(true);
	parameters.molecularWeight(true);

	List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, parameters, progressBar, locale);

	verify(applicationProperties, never()).getProperty("ncbi.gene2accession");
	verify(applicationProperties, never()).getProperty("ncbi.gene_info");
	verify(applicationProperties).getProperty("ncbi.gi_taxid");
	verify(applicationProperties).getProperty("ncbi.nr");
	verify(proteinService).weight(
		"MSMLVVFLLLWGVTWGPVTEAAIFYETQPSLWAESESLLKPLANVTLTCQARLETPDFQLFKNGVAQEPVHLDSPAIKHQFLLTGDTQGRYRCRSGLSTGWTQLSKLLELTGPKSLPAPWLSMAPVSWITPGLKTTAVCRGVLRGVTFLLRREGDHEFLEVPEAQEDVEATFPVHQPGNYSCSYRTDGEGALSEPSATVTIEELAAPPPPVLMHHGESSQVLHPGNKVTLTCVAPLSGVDFQLRRGEKELLVPRSSTSPDRIFFHLNAVALGDGGHYTCRYRLHDNQNGWSGDSAPVELILSDETLPAPEFSPEPESGRALRLRCLAPLEGARFALVREDRGGRRVHRFQSPAGTEALFELHNISVADSANYSCVYVDLKPPFGGSAPSERLELHVDGPPPRPQLRATWSGAVLAGRDAVLRCEGPIPDVTFELLREGETKAVKTVRTPGAAANLELIFVGPQHAGNYRCRYRSWVPHTFESELSDPVELLVAES");
	verify(proteinService).weight("TEAAIFYETQ");
	verify(proteinService).weight("QLFKNGVAQEPV");
	verify(proteinService).weight("ELTGPKSL");
	verify(proteinService).weight("MSMLVVFLLL");
	verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
	verify(progressBar, atLeastOnce()).setMessage(any(String.class));
	assertEquals(21, mappings.size());
	ProteinMapping mapping = find(mappings, 119592981).get();
	assertEquals((Integer) 119592981, mapping.getGi());
	assertEquals(null, mapping.getGeneId());
	assertEquals(null, mapping.getGeneName());
	assertEquals(null, mapping.getGeneSynonyms());
	assertEquals(null, mapping.getGeneSummary());
	assertEquals(
		"MSMLVVFLLLWGVTWGPVTEAAIFYETQPSLWAESESLLKPLANVTLTCQARLETPDFQLFKNGVAQEPVHLDSPAIKHQFLLTGDTQGRYRCRSGLSTGWTQLSKLLELTGPKSLPAPWLSMAPVSWITPGLKTTAVCRGVLRGVTFLLRREGDHEFLEVPEAQEDVEATFPVHQPGNYSCSYRTDGEGALSEPSATVTIEELAAPPPPVLMHHGESSQVLHPGNKVTLTCVAPLSGVDFQLRRGEKELLVPRSSTSPDRIFFHLNAVALGDGGHYTCRYRLHDNQNGWSGDSAPVELILSDETLPAPEFSPEPESGRALRLRCLAPLEGARFALVREDRGGRRVHRFQSPAGTEALFELHNISVADSANYSCVYVDLKPPFGGSAPSERLELHVDGPPPRPQLRATWSGAVLAGRDAVLRCEGPIPDVTFELLREGETKAVKTVRTPGAAANLELIFVGPQHAGNYRCRYRSWVPHTFESELSDPVELLVAES",
		mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(12.5, mapping.getMolecularWeight(), 0);
    }

    @Test
    public void allProteinMappings_NoProteinDetails() throws Throwable {
	when(organism.getId()).thenReturn(9606);
	when(proteinService.weight(any())).thenReturn(12.5);
	ProteinMappingParametersBean parameters = new ProteinMappingParametersBean();
	parameters.geneId(true);
	parameters.geneDetails(true);
	parameters.sequence(false);
	parameters.molecularWeight(false);

	List<ProteinMapping> mappings = ncbiServiceBean.allProteinMappings(organism, parameters, progressBar, locale);

	verify(applicationProperties).getProperty("ncbi.gene2accession");
	verify(applicationProperties).getProperty("ncbi.gene_info");
	verify(applicationProperties).getProperty("ncbi.gi_taxid");
	verify(applicationProperties, never()).getProperty("ncbi.nr");
	verify(proteinService, never()).weight(any());
	verify(progressBar, atLeastOnce()).setProgress(any(Double.class));
	verify(progressBar, atLeastOnce()).setMessage(any(String.class));
	assertEquals(21, mappings.size());
	ProteinMapping mapping = find(mappings, 119592981).get();
	assertEquals((Integer) 119592981, mapping.getGi());
	assertEquals((Integer) 1, mapping.getGeneId());
	assertEquals("A1BG", mapping.getGeneName());
	assertEquals("A1B|ABG|GAB|HYST2477", mapping.getGeneSynonyms());
	assertEquals("alpha-1-B glycoprotein", mapping.getGeneSummary());
	assertEquals(null, mapping.getSequence());
	assertEquals((Integer) 9606, mapping.getTaxonomyId());
	assertEquals(null, mapping.getMolecularWeight());
    }
}
