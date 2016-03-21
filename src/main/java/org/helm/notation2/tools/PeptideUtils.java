/**
 * *****************************************************************************
 * Copyright C 2015, The Pistoia Alliance
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *****************************************************************************
 */
package org.helm.notation2.tools;

import java.util.List;

import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.Monomer;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.PeptideUtilsException;
import org.helm.notation2.parser.notation.polymer.PeptideEntity;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;

/**
 * PeptideUtils, class to provide peptide utils
 *
 * @author hecht
 */
public final class PeptideUtils {

  /**
   * Default constructor.
   */
  private PeptideUtils() {

  }

  /**
   * method to produce for a peptide PolymerNotation the natural analogue
   * sequence
   *
   * @param polymer PolymerNotation
   * @return natural analogue sequence
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws PeptideUtilsException if the polymer is not a peptide
   * @throws ChemistryException if the Chemistry Engine is not initialized
 * @throws CTKException 
   */
  public static String getNaturalAnalogueSequence(PolymerNotation polymer) throws HELM2HandledException, PeptideUtilsException, ChemistryException {
    checkPeptidePolymer(polymer);
    return FastaFormat.generateFastaFromPeptide(MethodsMonomerUtils.getListOfHandledMonomers(polymer.getListMonomers()));
  }

  /**
   * method to produce for a peptide PolymerNotation the sequence
   *
   * @param polymer PolymerNotation
   * @return sequence
   * @throws HELM2HandledException if the polmyer contains HELM2 features
   * @throws PeptideUtilsException is not a peptide
   * @throws ChemistryException if the Chemistry Engine is not initialized
   */
  public static String getSequence(PolymerNotation polymer) throws HELM2HandledException, PeptideUtilsException, ChemistryException{
    checkPeptidePolymer(polymer);
    StringBuilder sb = new StringBuilder();
    List<Monomer> monomers = MethodsMonomerUtils.getListOfHandledMonomers(polymer.getListMonomers());

    for (Monomer monomer : monomers) {
      String id = monomer.getAlternateId();
      if (id.length() > 1) {
        id = "[" + id + "]";
      }
      sb.append(id);
    }
    return sb.toString();
  }

  /**
   * method to check if the polmyer is a peptide
   *
   * @param polymer PolymerNotation
   * @throws PeptideUtilsException if the polymer is not a peptide
   */
  private static void checkPeptidePolymer(PolymerNotation polymer) throws PeptideUtilsException {
    if (!(polymer.getPolymerID() instanceof PeptideEntity)) {
      throw new PeptideUtilsException("Polymer is not a peptide");
    }
  }
}
