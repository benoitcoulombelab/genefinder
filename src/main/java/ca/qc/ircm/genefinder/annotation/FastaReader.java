package ca.qc.ircm.genefinder.annotation;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Reads FASTA files.
 */
public class FastaReader implements Closeable {
  private final BufferedReader reader;
  private String nextName;

  public FastaReader(BufferedReader reader) {
    this.reader = reader;
  }

  /**
   * Returns next sequence in FASTA file.
   *
   * @return next sequence in FASTA file
   * @throws IOException
   *           could not read FASTA file
   */
  public Sequence nextSequence() throws IOException {
    if (nextName == null) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(">")) {
          nextName = line.substring(1);
          break;
        }
      }
    }
    Sequence sequence = null;
    StringBuilder builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.startsWith(">")) {
        sequence = new Sequence();
        sequence.setName(nextName);
        sequence.setSequence(builder.toString());
        nextName = line.substring(1);
        break;
      } else {
        builder.append(line);
      }
    }
    if (sequence == null && nextName != null) {
      sequence = new Sequence();
      sequence.setName(nextName);
      sequence.setSequence(builder.toString());
      nextName = null;
    }
    return sequence;
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
