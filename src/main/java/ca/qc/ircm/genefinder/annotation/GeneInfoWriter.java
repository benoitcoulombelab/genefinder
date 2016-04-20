package ca.qc.ircm.genefinder.annotation;

import org.apache.commons.lang3.ObjectUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Writes NCBI gene info files.
 */
public class GeneInfoWriter implements Closeable {
  private static final String HEADER =
      "#Format: tax_id GeneID Symbol LocusTag Synonyms dbXrefs chromosome map_location "
          + "description type_of_gene Symbol_from_nomenclature_authority "
          + "Full_name_from_nomenclature_authority Nomenclature_status Other_designations "
          + "Modification_date (tab is used as a separator, pound sign - start of a comment)";
  private static final String NULL_PATTERN = "-";
  private static final String FIELD_SEPARATOR = "\t";
  private static final String COLLECTION_SEPARATOR = "|";
  private static final String DATE_FORMAT_PATTERN = "yyyyMMdd";
  private final Writer writer;

  public GeneInfoWriter(Writer writer) {
    this.writer = writer;
  }

  /**
   * Writes file header.
   *
   * @throws IOException
   *           could not write header
   */
  public void writeHeader() throws IOException {
    writer.write(HEADER);
    writer.write("\n");
  }

  /**
   * Writes gene info.
   *
   * @param geneInfo
   *          gene info
   * @throws IOException
   *           could not write gene info
   */
  public void writeGeneInfo(GeneInfo geneInfo) throws IOException {
    writer.write(toString(geneInfo.getOrganismId()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getId()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getSymbol()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getLocusTag()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getSynonyms()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getDbXrefs()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getChromosome()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getMapLocation()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getDescription()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getTypeOfGene()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getSymbolFromNomenclatureAuthority()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getFullNameFromNomenclatureAuthority()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getNomenclatureStatus()));
    writer.write(FIELD_SEPARATOR);
    writer.write(toString(geneInfo.getOtherDesignations()));
    writer.write(FIELD_SEPARATOR);
    writer.write(format(geneInfo.getModificationDate()));
    writer.write("\n");
  }

  private String format(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
    return dateFormat.format(date);
  }

  private String toString(Object object) {
    if (object instanceof Collection) {
      Collection<?> collection = (Collection<?>) object;
      if (collection.isEmpty()) {
        return NULL_PATTERN;
      } else {
        return String.join(COLLECTION_SEPARATOR,
            collection.stream().map(o -> toString(o)).collect(Collectors.toList()));
      }
    }
    return ObjectUtils.toString(object, NULL_PATTERN);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }
}
