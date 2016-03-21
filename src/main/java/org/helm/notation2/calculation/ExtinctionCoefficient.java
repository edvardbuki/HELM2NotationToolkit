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
package org.helm.notation2.calculation;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.Monomer;
import org.helm.notation2.exception.CalculationException;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.ExtinctionCoefficientException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.parser.notation.HELM2Notation;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;
import org.helm.notation2.tools.MethodsMonomerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExtinctionCoefficient class to calculate the extinction coefficient
 *
 * @author hecht
 */
public final class ExtinctionCoefficient {

  /**
   *
   */
  private static final int UNIT = 1000;

  private static final Logger LOG =
      LoggerFactory.getLogger(ExtinctionCoefficient.class);

  public static final int RNA_UNIT_TYPE = 1;

  public static final int PEPTIDE_UNIT_TYPE = 2;

  private static Map<String, Float> monoNucleotideMap = new HashMap<String, Float>();

  private static Map<String, Float> diNucleotideMap = new HashMap<String, Float>();

  private static Map<String, Float> aminoAcidMap = new HashMap<String, Float>();

  private static String rnaPropertyFile = "/org/helm/notation2/resources/RNAExtinctionCoefficient.properties";

  private static String peptidePropertyFile = "/org/helm/notation2/resources/PEPTIDEExtinctionCoefficient.properties";

  private static ExtinctionCoefficient instance;

  private ExtinctionCoefficient() {

  }

  public static ExtinctionCoefficient getInstance() throws ExtinctionCoefficientException {
    if (null == instance) {
      instance = new ExtinctionCoefficient();
      try {
        initMaps();
      } catch (IOException ex) {
        throw new ExtinctionCoefficientException("Unable to initalize extinction coefficient property files");
      }
    }
    return instance;
  }

  private static void initMaps() throws IOException {
    InputStream ris = ExtinctionCoefficient.class.getResourceAsStream(rnaPropertyFile);
    Properties rp = new Properties();
    rp.load(ris);

    Enumeration<?> re = rp.propertyNames();
    while (re.hasMoreElements()) {
      String key = (String) re.nextElement();
      String value = rp.getProperty(key);
      Float f = new Float(value);
      int len = key.length();
      if (len == 1) {
        monoNucleotideMap.put(key, f);
      } else if (len == 2) {
        diNucleotideMap.put(key, f);
      }
    }
    ris.close();

    InputStream pis = ExtinctionCoefficient.class.getResourceAsStream(peptidePropertyFile);
    Properties pp = new Properties();
    pp.load(pis);

    Enumeration<?> pe = pp.propertyNames();
    while (pe.hasMoreElements()) {
      String key = (String) pe.nextElement();
      String value = pp.getProperty(key);

      Float f = new Float(value);
      aminoAcidMap.put(key, f);
    }
    pis.close();
  }

  public String getUnit(int unitType) {
    switch (unitType) {
    case RNA_UNIT_TYPE:
      return getRnaUnit();
    case PEPTIDE_UNIT_TYPE:
      return getPeptideUnit();
    default:
      return null;
    }
  }

  public String getRnaUnit() {
    return "mM-1cm-1";
  }

  public String getPeptideUnit() {
    return "M-1cm-1";
  }

  public String getDefaultUnit() {
    return getRnaUnit();
  }

  public int getDefaultUnitType() {
    return RNA_UNIT_TYPE;
  }

  /**
   * method to calculate the extinction coefficient for the whole HELM molecule
   *
   * @param helm2notation input HELM2Notation
   * @return extinction coefficient
   * @throws ExtinctionCoefficientException if the HELM contains HELM2 features
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public float calculate(HELM2Notation helm2notation) throws ExtinctionCoefficientException, ChemistryException {
    return calculate(helm2notation, getDefaultUnitType());
  }

  /**
   * method to calculate the extinction coefficient for the whole HELM molecule
   *
   * @param helm2notation input HELM2Notation
   * @param unitType Unit of the extinction coefficient
   * @return extinction coefficient
   * @throws ExtinctionCoefficientException if the HELM contains HELM2 features
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public float calculate(HELM2Notation helm2notation, int unitType) throws ExtinctionCoefficientException, ChemistryException {
    LOG.debug("ExtinctionCalculation is starting with the unitType: " + unitType);
    float result = 0.0f;
    List<PolymerNotation> polymerNodes = helm2notation.getListOfPolymers();
    for (PolymerNotation polymerNode : polymerNodes) {
      String polymerType = polymerNode.getPolymerID().getType();
      float ext = 0.0f;
      ArrayList<PolymerNotation> not = new ArrayList<PolymerNotation>();
      not.add(polymerNode);
      if (polymerType.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
        try {
          ext = calculateExtinctionFromRNA(MethodsMonomerUtils.getListOfHandledMonomersOnlyBase(polymerNode.getPolymerElements().getListOfElements()));
        } catch (CalculationException | IOException | HELM2HandledException | NotationException e) {
          throw new ExtinctionCoefficientException(e.getMessage());
        }
        if (unitType == PEPTIDE_UNIT_TYPE) {
          ext = ext * UNIT;
        }
      } else if (polymerType.equals(Monomer.PEPTIDE_POLYMER_TYPE)) {
        try {
          ext = calculateExtinctionFromPeptide(MethodsMonomerUtils.getListOfHandledMonomers(polymerNode.getPolymerElements().getListOfElements()));
        } catch (IOException | HELM2HandledException e) {
          throw new ExtinctionCoefficientException(e.getMessage());
        }
        if (unitType == RNA_UNIT_TYPE) {
          ext = ext / UNIT;
        }
      }
      result = result + ext;
    }
    return result;
  }

  /**
   * method to calculate the extinction coefficient for rna
   *
   * @param monomers all Monomers of the RNA/DNA
   * @return extinction coefficient
   * @throws CalculationException if the rna contains not valid nucleotides
   * @throws IOException
   */
  private static float calculateExtinctionFromRNA(List<Monomer> monomers) throws CalculationException, IOException {
    LOG.debug("ExtinctionCalculation of RNA");
    float resultSingle = 0.0f;
    float resultDi = 0.0f;
    String previous = "";
    if (monomers.size() == 0) {
      throw new CalculationException("Input sequence cannot be null");
    } else {
      if (monomers.size() == 1) {
        if (monoNucleotideMap.containsKey(monomers.get(0).getNaturalAnalog())) {
          return monoNucleotideMap.get(monomers.get(0).getNaturalAnalog()).floatValue();

        } else {
          throw new CalculationException("Unknown nucleotide found");
        }
      }
      for (int i = 0; i < monomers.size(); i++) {
        if (i > 0 && i < monomers.size() - 1) {
          if (monoNucleotideMap.containsKey(monomers.get(i).getNaturalAnalog())) {
            Float value = monoNucleotideMap.get(monomers.get(i).getNaturalAnalog()).floatValue();
            resultSingle += (value.floatValue() * 1.0);

          }
        }
        if (previous != "") {
          if (diNucleotideMap.containsKey(previous + monomers.get(i).getNaturalAnalog())) {
            Float value = diNucleotideMap.get(previous + monomers.get(i).getNaturalAnalog()).floatValue();
            resultDi += (value.floatValue() * 1.0);
          }
        }

        previous = monomers.get(i).getNaturalAnalog();
      }

    }
    resultSingle = BigDecimal.valueOf(resultSingle).floatValue();
    resultDi = BigDecimal.valueOf(resultDi).floatValue();
    return 2 * resultDi - resultSingle;
  }

  /**
   * method to calculate the extinction coefficient for peptide
   *
   * @param monomers all monomers of the peptide
   * @return extinction coefficient
   * @throws IOException
   * @throws HELM2HandledException if HELM2 features were there
   */
  private static float calculateExtinctionFromPeptide(List<Monomer> monomers) throws IOException, HELM2HandledException {

    if (null == monomers || monomers.isEmpty()) {
      return 0.0f;
    }

    Map<String, Integer> countMap = new HashMap<String, Integer>();
    for (Monomer monomer : monomers) {
      if (aminoAcidMap.containsKey(monomer.getAlternateId())) {
        int count = 1;
        if (countMap.containsKey(monomer.getAlternateId())) {
          count = count + countMap.get(monomer.getAlternateId());
        }
        countMap.put(monomer.getAlternateId(), count);
      }
    }

    float result = 0.0f;
    Set<String> keys = countMap.keySet();
    for (Iterator<String> it = keys.iterator(); it.hasNext();) {
      String key = it.next();
      int count = countMap.get(key);
      float factor = aminoAcidMap.get(key);
      result = result + factor * count;
    }

    return BigDecimal.valueOf(result).floatValue();

  }

}
