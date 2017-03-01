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

package ca.qc.ircm.genefinder.protein;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Services for proteins.
 */
@Component
public class ProteinService {
  private static final Map<Character, Double> AMINO_ACID_WEIGHTS;

  static {
    AMINO_ACID_WEIGHTS = new HashMap<>();
    AMINO_ACID_WEIGHTS.put('A', 71.08);
    AMINO_ACID_WEIGHTS.put('C', 103.14);
    AMINO_ACID_WEIGHTS.put('D', 115.09);
    AMINO_ACID_WEIGHTS.put('E', 129.12);
    AMINO_ACID_WEIGHTS.put('F', 147.18);
    AMINO_ACID_WEIGHTS.put('G', 57.06);
    AMINO_ACID_WEIGHTS.put('H', 137.15);
    AMINO_ACID_WEIGHTS.put('I', 113.17);
    AMINO_ACID_WEIGHTS.put('K', 128.18);
    AMINO_ACID_WEIGHTS.put('L', 113.17);
    AMINO_ACID_WEIGHTS.put('M', 131.21);
    AMINO_ACID_WEIGHTS.put('N', 114.11);
    AMINO_ACID_WEIGHTS.put('P', 97.12);
    AMINO_ACID_WEIGHTS.put('Q', 128.41);
    AMINO_ACID_WEIGHTS.put('R', 156.20);
    AMINO_ACID_WEIGHTS.put('S', 87.08);
    AMINO_ACID_WEIGHTS.put('T', 101.11);
    AMINO_ACID_WEIGHTS.put('V', 99.14);
    AMINO_ACID_WEIGHTS.put('W', 186.21);
    AMINO_ACID_WEIGHTS.put('Y', 163.18);
  }

  public double weight(String sequence) {
    double water = 18.015;
    double weight = sequence.chars().map(aa -> Character.toUpperCase(aa)).mapToObj(aa -> (char) aa)
        .filter(aa -> AMINO_ACID_WEIGHTS.containsKey(aa))
        .mapToDouble(aa -> AMINO_ACID_WEIGHTS.get(aa)).sum();
    if (weight > 0) {
      weight += water;
    }
    return weight / 1000;
  }
}
