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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.Monomer;
import org.helm.notation2.Nucleotide;
import org.helm.notation2.NucleotideFactory;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.FastaFormatException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.NucleotideLoadingException;
import org.helm.notation2.exception.RNAUtilsException;
import org.helm.notation2.parser.exceptionparser.NotationException;
import org.helm.notation2.parser.notation.connection.ConnectionNotation;
import org.helm.notation2.parser.notation.polymer.MonomerNotation;
import org.helm.notation2.parser.notation.polymer.MonomerNotationGroup;
import org.helm.notation2.parser.notation.polymer.MonomerNotationGroupElement;
import org.helm.notation2.parser.notation.polymer.MonomerNotationList;
import org.helm.notation2.parser.notation.polymer.MonomerNotationUnitRNA;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;
import org.helm.notation2.parser.notation.polymer.RNAEntity;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RNAUtils, class to provide methods for rna polymer
 *
 * @author hecht
 */
public class RNAUtils {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(RNAUtils.class);

  private static Map<String, String> complementMap = null;

  public static final int MINUMUM_MATCH_FRAGMENT_LENGTH = 2;

  /**
   * Default constructor.
   */
  private RNAUtils() {

  }

  /**
   * initialize Map to get the complement of each nucleotide
   */
  private static void initComplementMap() {
    complementMap = new HashMap<String, String>();
    complementMap.put("A", "U");
    complementMap.put("G", "C");
    complementMap.put("C", "G");
    complementMap.put("U", "A");
    complementMap.put("T", "A");
    complementMap.put("X", "X");
  }

  /**
   * method to generate the reverse rna/dna sequence of a given polymer
   *
   * @param polymer PolymerNotation
   * @return sequence reverse rna/dna sequence
   * @throws RNAUtilsException if the polymer is not a RNA/DNA
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws org.helm.notation2.exception.NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static String getReverseSequence(PolymerNotation polymer) throws RNAUtilsException, HELM2HandledException, org.helm.notation2.exception.NotationException, ChemistryException {
    StringBuilder sb = new StringBuilder(getNaturalAnalogSequence(polymer));
    return sb.reverse().toString();
  }

  /**
   * method to generate the natural analogue sequence of a rna/dna of a given
   * polymer
   *
   * @param polymer PolymerNotation
   * @return sequence natural analogue sequence
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws RNAUtilsException if the polymer is not RNA or DNA
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static String getNaturalAnalogSequence(PolymerNotation polymer) throws HELM2HandledException,
      RNAUtilsException, ChemistryException {
    checkRNA(polymer);
    return FastaFormat.generateFastaFromRNA(MethodsMonomerUtils.getListOfHandledMonomers(polymer.getListMonomers()));
  }

  /**
   * method to check if two given polymers are complement to each other
   *
   * @param polymerOne PolymerNotation of the first polymer
   * @param polymerTwo PolymerNotation of the second polymer
   * @return true, if they are opposite to each other, false otherwise
   * @throws RNAUtilsException if the polymers are not rna/dna or the
   *           antiparallel strand can not be built from polymerOne
   * @throws HELM2HandledException if the polymers contain HELM2 features
   * @throws JDOMException
   * @throws IOException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws org.helm.notation2.exception.NotationException
   */
  public static boolean areAntiparallel(PolymerNotation polymerOne, PolymerNotation polymerTwo) throws RNAUtilsException, HELM2HandledException, IOException, JDOMException, ChemistryException {
    checkRNA(polymerOne);
    checkRNA(polymerTwo);
    PolymerNotation antiparallel = getAntiparallel(polymerOne);
    String sequenceOne =
        FastaFormat.generateFastaFromRNA(MethodsMonomerUtils.getListOfHandledMonomers(antiparallel.getListMonomers()));
    String sequenceTwo =
        FastaFormat.generateFastaFromRNA(MethodsMonomerUtils.getListOfHandledMonomers(polymerTwo.getListMonomers()));
    return sequenceOne.equals(sequenceTwo);
  }

  /**
   * method to get the largest matched fragment between two sequences, replace T
   * with U before Match
   *
   * @param seq1 single letter, all upper case nucleotide sequence
   * @param seq2 single letter, all upper case nucleotide sequence
   * @return
   * @throws NotationException
   */
  public static String getMaxMatchFragment(String seq1, String seq2) throws NotationException {
    return getMaxMatchFragment(seq1, seq2, MINUMUM_MATCH_FRAGMENT_LENGTH);
  }

  /**
   * This method returns the largest matched fragment between two sequences,
   * replace T with U before match
   *
   * @param seq1 single letter, all upper case nucleotide sequence
   * @param seq2 single letter, all upper case nucleotide sequence
   * @param minLength - minimum fragment length
   * @return largest match fragment
   */
  public static String getMaxMatchFragment(String seq1, String seq2,
      int minLength) throws NotationException {
    if (null == seq1 || null == seq2) {
      throw new NotationException("Both sequences must not be null ");
    }

    if (!seq1.equals(seq1.toUpperCase())
        || !seq2.equals(seq2.toUpperCase())) {
      throw new NotationException(
          "Both sequences must be natural nucleotide sequence in upper case ");
    }

    String longSeq, shortSeq;

    if (seq1.length() > seq2.length()) {
      longSeq = seq1;
      shortSeq = seq2;
    } else {
      longSeq = seq2;
      shortSeq = seq1;
    }
    // replace T with U
    longSeq = longSeq.replaceAll("T", "U");
    shortSeq = shortSeq.replaceAll("T", "U");

    int min = MINUMUM_MATCH_FRAGMENT_LENGTH;
    if (minLength > min) {
      min = minLength;
    }

    for (int len = shortSeq.length(); len > min; len--) {
      for (int i = 0; i <= shortSeq.length() - len; i++) {
        String tmp = shortSeq.substring(i, i + len);

        if (longSeq.contains(tmp)) {
          return tmp;
        }
      }
    }

    return "";
  }

  /**
   * method to remove the phosphate of the last nucleotide
   *
   * @param polymer PolymerNotation
   * @throws RNAUtilsException if the PolmyerNotation is not a rna or dna
   * @throws IOException
   * @throws NotationException if the changed notation object can not be
   *           generated
   * @throws HELM2HandledException if HELM2 features are involved
   */
  public static void removeLastP(PolymerNotation polymer) throws RNAUtilsException, NotationException, IOException, HELM2HandledException {
    checkRNA(polymer);
    /* Get last monomerNotation */
    MonomerNotation lastObject = polymer.getPolymerElements().getListOfElements().get(polymer.getPolymerElements().getListOfElements().size() - 1);

    /* What happens to HELM2 features */
    if (lastObject instanceof MonomerNotationGroup || lastObject instanceof MonomerNotationList) {
      throw new HELM2HandledException("HELM2 features are involved");
    }

    if (hasPhosphat((MonomerNotationUnitRNA) lastObject)) {
      MonomerNotation lastObjectwithoutPhosphat = new MonomerNotationUnitRNA(lastObject.getUnit().substring(0, lastObject.getUnit().length() - 1), "RNA");
      ChangeObjects.changeMonomerNotation(polymer.getPolymerElements().getListOfElements().size() - 1, polymer, lastObjectwithoutPhosphat);
      LOG.info("Last phosphate was removed from the last nucleotide");
    }
  }

  /**
   * method to check if the MonomerNotationUnitRNA has a phosphate
   *
   * @param monomerNotationUnitRNA MonomerNotationUnitRNA
   * @return true, if it ends with "P", false otherwise
   */
  /* To Do check for other modified phosphates? */
  private static boolean hasPhosphat(MonomerNotationUnitRNA monomerNotationUnitRNA) {
    if (monomerNotationUnitRNA.getContents().get(monomerNotationUnitRNA.getContents().size() - 1).getUnit().endsWith("P")) {
      LOG.info("MonomerNotationUnitRNA " + monomerNotationUnitRNA.getUnit() + " has a phosphate");
      return true;
    }
    LOG.info("MonomerNotationUnitRNA " + monomerNotationUnitRNA.getUnit() + " has no phosphate");
    return false;
  }

  /**
   * method to add a phosphate to the last polymer's nucleotide
   *
   * @param polymer PolymerNotation
   * @throws RNAUtilsException if the polymer is no rna or dna
   * @throws NotationException if the changed notation object can not be
   *           generated
   * @throws IOException
   * @throws HELM2HandledException if HELM2 features are involved
   */
  public static void addLastP(PolymerNotation polymer) throws RNAUtilsException, NotationException, IOException, HELM2HandledException {
    checkRNA(polymer);
    /* Get last monomerNotation */
    MonomerNotation lastObject = polymer.getPolymerElements().getListOfElements().get(polymer.getPolymerElements().getListOfElements().size() - 1);

    /* What happens to HELM2 features */
    if (lastObject instanceof MonomerNotationGroup || lastObject instanceof MonomerNotationList) {
      throw new HELM2HandledException("HELM2 features are involved");
    }

    if (!(hasPhosphat((MonomerNotationUnitRNA) lastObject))) {
      MonomerNotation lastObjectwithPhosphat = new MonomerNotationUnitRNA(lastObject.getUnit() + "P", "RNA");
      ChangeObjects.changeMonomerNotation(polymer.getPolymerElements().getListOfElements().size() - 1, polymer, lastObjectwithPhosphat);
      LOG.info("Phosphate was added to the last nucleotide");
    }

  }

  /**
   * method to get the antiparallel polymer for a rna/dna polymer
   *
   * @param polymer PolymerNotation
   * @return antiparallel polymer
   * @throws RNAUtilsException if the polymer is not rna or dna or the reverse
   *           polymer can not be built
   * @throws JDOMException
   * @throws IOException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static PolymerNotation getAntiparallel(PolymerNotation polymer) throws RNAUtilsException, IOException, JDOMException, ChemistryException {
    checkRNA(polymer);
    PolymerNotation reversePolymer;
    try {
      reversePolymer = SequenceConverter.readRNA(generateAntiparallel(polymer)).getCurrentPolymer();
      reversePolymer =
          new PolymerNotation(reversePolymer.getPolymerID(), reversePolymer.getPolymerElements(), "Antiparallel to "
              + polymer.getPolymerID().getId());
      return reversePolymer;
    } catch (NotationException | FastaFormatException | HELM2HandledException e) {
      e.printStackTrace();
      throw new RNAUtilsException("The reverse polymer can not be built");
    }

  }

  /**
   * method to generate the antiparallel sequence for a rna/dna polymer
   *
   * @param polymer PolymerNotation
   * @return antiparallel sequence
   * @throws HELM2HandledException if th polymer contains HELM2 features
   * @throws RNAUtilsException if the polymer is not RNA or DNA
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  private static String generateAntiparallel(PolymerNotation polymer) throws HELM2HandledException, RNAUtilsException, ChemistryException {
    return generateComplement(polymer).reverse().toString();
  }

  /**
   * method to get the polymer with the inverse sequence of the current polymer
   *
   * @param polymer PolymerNotation
   * @return inverse sequence
   * @throws RNAUtilsException if the polymer is not rna or dna or the inverse
   *           strand can not be built
   * @throws JDOMException
   * @throws IOException
   * @throws org.helm.notation2.exception.NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static PolymerNotation getInverse(PolymerNotation polymer) throws RNAUtilsException, IOException, JDOMException, org.helm.notation2.exception.NotationException, ChemistryException {
    checkRNA(polymer);
    PolymerNotation inverse;
    try {
      inverse = SequenceConverter.readRNA(generateInverse(polymer).toString()).getListOfPolymers().get(0);
      inverse =
          new PolymerNotation(inverse.getPolymerID(), inverse.getPolymerElements(), "Inverse to "
              + polymer.getPolymerID().getId());
      return inverse;
    } catch (NotationException | FastaFormatException | HELM2HandledException e) {
      e.printStackTrace();
      throw new RNAUtilsException("The inverse strand can not be built");
    }

  }

  /**
   * method to get the polymer with the reverse complement sequence of the
   * current polymer
   *
   * @param polymer PolymerNotation
   * @return PolymerNotation with the reverse complement
   * @throws RNAUtilsException if the polymer is not rna or dna or the inverse
   *           strand can not be built
   * @throws JDOMException
   * @throws IOException
   * @throws org.helm.notation2.exception.NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static PolymerNotation getReverseComplement(PolymerNotation polymer) throws RNAUtilsException, IOException, JDOMException, org.helm.notation2.exception.NotationException, ChemistryException{
    checkRNA(polymer);
    PolymerNotation complementReversePolymer;
    try {
      complementReversePolymer = SequenceConverter.readRNA(generateReverseComplement(polymer).toString()).getListOfPolymers().get(0);
      complementReversePolymer =
          new PolymerNotation(complementReversePolymer.getPolymerID(), complementReversePolymer.getPolymerElements(),
              "ReverseComplement to " + polymer.getPolymerID().getId());
      return complementReversePolymer;
    } catch (NotationException | FastaFormatException | HELM2HandledException e) {
      e.printStackTrace();
      throw new RNAUtilsException("Complement polymer can not be built");
    }

  }

  /**
   * @param polymer
   * @return reverse complement sequence
   * @throws org.helm.notation2.exception.NotationException
   * @throws RNAUtilsException
   * @throws HELM2HandledException
   * @throws ChemistryException
   */
  private static StringBuilder generateReverseComplement(PolymerNotation polymer) throws HELM2HandledException, RNAUtilsException, org.helm.notation2.exception.NotationException, ChemistryException {
    initComplementMap();
    String sequence = getNaturalAnalogSequence(polymer);
    StringBuilder sb = new StringBuilder();
    for (char c : sequence.toCharArray()) {
      sb.append(complementMap.get(String.valueOf(c)));
    }
    return sb.reverse();
  }

  /**
   * method to generate the inverse sequence of the given polymer
   *
   * @param polymer PolymerNotation
   * @return inverse sequence of the PolymerNotation
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws RNAUtilsException if the polymer is not rna or dna
   * @throws org.helm.notation2.exception.NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  private static StringBuilder generateInverse(PolymerNotation polymer) throws HELM2HandledException, RNAUtilsException, org.helm.notation2.exception.NotationException, ChemistryException {
    initComplementMap();
    String sequence = getNaturalAnalogSequence(polymer);
    StringBuilder sb = new StringBuilder(sequence);
    return sb.reverse();
  }

  /**
   * method to generate the complement sequence for a rna/dna polymer
   *
   * @param polymer PolymerNotation
   * @return complement sequence saved in StringBuilder
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws RNAUtilsException if the polymer is not RNA or DNA
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  private static StringBuilder generateComplement(PolymerNotation polymer) throws HELM2HandledException, RNAUtilsException, ChemistryException {
    initComplementMap();
    String sequence = getNaturalAnalogSequence(polymer);
    StringBuilder sb = new StringBuilder();
    for (char c : sequence.toCharArray()) {
      sb.append(complementMap.get(String.valueOf(c)));
    }
    return sb;
  }

  /**
   * method to get the normal complement polymer for a given polymer
   *
   * @throws RNAUtilsException if the polymer is not rna or dna or the
   *           complement polymer can not be built
   * @throws JDOMException
   * @throws IOException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   *
   */
  public static PolymerNotation getComplement(PolymerNotation polymer) throws RNAUtilsException, IOException, JDOMException, ChemistryException {
    checkRNA(polymer);
    PolymerNotation complementPolymer;
    try {
      complementPolymer = SequenceConverter.readRNA(generateComplement(polymer).toString()).getListOfPolymers().get(0);
      complementPolymer =
          new PolymerNotation(complementPolymer.getPolymerID(), complementPolymer.getPolymerElements(),
              "NormalComplement to " + polymer.getPolymerID().getId());
      return complementPolymer;
    } catch (NotationException | FastaFormatException | HELM2HandledException e) {
      e.printStackTrace();
      throw new RNAUtilsException("Complement polymer can not be built");
    }

  }

  /**
   * method to check if the given PolymerNotation has a nucleotide Modification
   *
   * @param polymer PolymerNotation
   * @return true if the polymer contains at least one modifcation, false
   *         otherwise
   * @throws NotationException
   */
  public static boolean hasNucleotideModification(PolymerNotation polymer) throws NotationException {
    for (MonomerNotation current : polymer.getPolymerElements().getListOfElements()) {
      if (hasModification(current)) {
        return true;
      }
    }
    return false;
  }

  /**
   * method to check if the MonomerNotation contains a modification
   *
   * @param monomerNotation
   * @return true, if the MonomerNotation contains a modification, false
   *         otherwise
   * @throws NotationException if the MonomerNotation is unknown
   */
  private static boolean hasModification(MonomerNotation monomerNotation) throws NotationException {

    if (monomerNotation instanceof MonomerNotationUnitRNA) {
      if (hasModification((MonomerNotationUnitRNA) monomerNotation)) {
        return true;
      }
    } else if (monomerNotation instanceof MonomerNotationGroup) {
      for (MonomerNotationGroupElement element : ((MonomerNotationGroup) monomerNotation).getListOfElements()) {
        if (hasModification(element.getMonomerNotation())) {
          return true;
        }
      }
    } else if (monomerNotation instanceof MonomerNotationList) {
      for (MonomerNotation element : ((MonomerNotationList) monomerNotation).getListofMonomerUnits()) {
        if (hasModification(element)) {
          return true;
        }
      }
    } else {
      throw new NotationException("Unknown MonomerNotation Type " + monomerNotation.getClass());
    }
    return false;
  }

  /**
   * method to check if the MonomerNotationUnitRNA contains modification
   *
   * @param monomerNotation MonomerNotationUnitRNA
   * @return true, if the MonomerNotationUnitRNA contains modification, false
   *         otherwise
   */
  private static boolean hasModification(MonomerNotationUnitRNA monomerNotation) {
    if (monomerNotation.getUnit().contains("[") || monomerNotation.getUnit().contains("(X)") || monomerNotation.getUnit().endsWith(")")) {
      return true;
    }
    return false;
  }

  /**
   * method to hybridize two PolymerNotations together if they are antiparallel
   *
   * @param one PolymerNotation first
   * @param two PolymerNotation second
   * @return List of ConnectionNotations
   * @throws RNAUtilsException
   * @throws NotationException
   * @throws IOException
   * @throws JDOMException
   * @throws HELM2HandledException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static List<ConnectionNotation> hybridizeAntiparallel(PolymerNotation one, PolymerNotation two) throws RNAUtilsException, NotationException, IOException, JDOMException,
      HELM2HandledException, ChemistryException {
    checkRNA(one);
    checkRNA(two);

    List<ConnectionNotation> connections = new ArrayList<ConnectionNotation>();
    ConnectionNotation connection;
    /* Length of the two rnas have to be the same */
    if (areAntiparallel(one, two)) {
      for (int i = 0; i < PolymerUtils.getTotalMonomerCount(one); i++) {
        int backValue = PolymerUtils.getTotalMonomerCount(one) - i;
        int firstValue = i + 1;

        String details = firstValue + ":pair-" + backValue + ":pair";
        connection = new ConnectionNotation(one.getPolymerID(), two.getPolymerID(), details);
        connections.add(connection);
      }
      return connections;
    } else {
      throw new RNAUtilsException("The given RNAs are not antiparallel to each other");
    }

  }

  /**
   * method to get the rna sequence of the given PolymerNotation
   *
   * @param one PolymerNotation
   * @return sequence
   * @throws RNAUtilsException
   * @throws HELM2HandledException
   * @throws ChemistryException
   */
  public static String getSequence(PolymerNotation one) throws RNAUtilsException, HELM2HandledException, ChemistryException {
    checkRNA(one);

    List<Nucleotide> nucleotideList = getNucleotideList(one);
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < nucleotideList.size(); i++) {
      sb.append(nucleotideList.get(i).getNaturalAnalog());
    }
    return sb.toString();

  }

  /**
   * method to get the modifiedNucleotideSequence of the given PolymerNotation
   *
   * @param polymer PolymerNotation
   * @return modified nucleotide sequence
   * @throws RNAUtilsException
   * @throws HELM2HandledException
   * @throws ChemistryException
   */
  public static String getModifiedNucleotideSequence(PolymerNotation polymer) throws RNAUtilsException, HELM2HandledException, ChemistryException {
    checkRNA(polymer);
    List<Nucleotide> nucleotides = getNucleotideList(polymer);
    StringBuilder sb = new StringBuilder();
    for (Nucleotide nucleotide : nucleotides) {
      sb.append(nucleotide.getSymbol());
    }
    return sb.toString();

  }

  /**
   * method to hybridize two given PolymerNotations together
   *
   * @param one PolymerNotaiton
   * @param two PolymerNotation
   * @return List of ConnectionNotations
   * @throws RNAUtilsException
   * @throws NotationException
   * @throws HELM2HandledException
   * @throws JDOMException
   * @throws IOException
   * @throws org.helm.notation2.exception.NotationException
   * @throws ChemistryException
   */
  public static List<ConnectionNotation> hybridize(PolymerNotation one, PolymerNotation two) throws RNAUtilsException, NotationException, HELM2HandledException, JDOMException, IOException,
      org.helm.notation2.exception.NotationException, ChemistryException {
    initComplementMap();
    checkRNA(one);
    checkRNA(two);

    List<ConnectionNotation> connections = new ArrayList<ConnectionNotation>();
    ConnectionNotation connection;
    String seq1 = RNAUtils.getSequence(one).replaceAll("T", "U");
    String seq2 = RNAUtils.getSequence(two).replaceAll("T", "U");

    char[] chars = seq2.toCharArray();
    StringBuffer sb = new StringBuffer();
    for (int i = chars.length; i > 0; i--) {
      String symbol = String.valueOf(chars[i - 1]);
      String compSymbol = complementMap.get(symbol);
      sb.append(compSymbol);
    }
    String compSeq2 = sb.toString();
    String maxSeqMatch = getMaxMatchFragment(seq1, compSeq2);
    int seqMatchLength = maxSeqMatch.length();
    int seq1NucStart = -1;
    int seq1MonomerStart = 0;
    int seq2NucStart = -1;
    int seq2MonomerStart = 0;

    List<Nucleotide> seq1NucList = RNAUtils.getNucleotideList(one);
    List<Nucleotide> seq2NucList = RNAUtils.getNucleotideList(two);

    if (seqMatchLength > 0) {
      // get the starting monomer position for sequence 1
      seq1NucStart = seq1.indexOf(maxSeqMatch);
      for (int i = 0; i < seq1NucStart; i++) {
        Nucleotide nuc = seq1NucList.get(i);
        int monomerCount = NucleotideParser.getMonomerCountForRNA(nuc.getNotation());
        seq1MonomerStart = seq1MonomerStart + monomerCount;
      }

      // get the starting monomer position for sequence 2
      int compSeq2NucStart = compSeq2.indexOf(maxSeqMatch);
      seq2NucStart = seq2.length() - seqMatchLength - compSeq2NucStart;
      for (int i = 0; i < seq2NucStart; i++) {
        Nucleotide nuc = seq2NucList.get(i);
        int monomerCount = NucleotideParser.getMonomerCountForRNA(nuc.getNotation());
        seq2MonomerStart = seq2MonomerStart + monomerCount;
      }

      // build the matching monomer position
      for (int i = 0; i < seqMatchLength; i++) {

        Nucleotide nuc1 = seq1NucList.get(i + seq1NucStart);
        if (null == nuc1.getBaseMonomer()) {
          throw new NotationException(
              "Nucleotide without base cannot be hybridized with others");
        }
        if (i == 0) {
          seq1MonomerStart = seq1MonomerStart + 2;
        } else {
          seq1MonomerStart = seq1MonomerStart + 3;
        }

        Nucleotide nuc2 = seq2NucList.get(i + seq2NucStart);
        if (null == nuc2.getBaseMonomer()) {
          throw new NotationException(
              "Nucleotide without base cannot be hybridized with others");
        }
        if (i == 0) {
          seq2MonomerStart = seq2MonomerStart + 2;
        } else {
          seq2MonomerStart = seq2MonomerStart + 3;
        }
      }

      // what if there is an X with two monomers in the middle? exception
      // should have been thrown
      // now build the base pair string, offset by 3
      for (int i = seqMatchLength; i > 0; i--) {
        int seq1MonomerPos = seq1MonomerStart - (i - 1) * 3;
        int seq2MonomerPos = seq2MonomerStart - (seqMatchLength - i)
            * 3;

        String details = seq1MonomerPos + ":pair-" + seq2MonomerPos + ":pair";
        connection = new ConnectionNotation(one.getPolymerID(), two.getPolymerID(), details);
        connections.add(connection);
      }
    }

    return connections;
  }

  /**
   * methods of this class are only allowed for RNA/DNA polymers
   *
   * @param polymer PolymerNotation
   * @throws RNAUtilsException if the polymer is not a RNA/DNA type
   */
  private static void checkRNA(PolymerNotation polymer) throws RNAUtilsException {
    if (!(polymer.getPolymerID() instanceof RNAEntity)) {
      throw new RNAUtilsException("Functions can only be called for RNA/DNA");
    }
  }

  /**
   * method to get the nucleotide sequence for the polymer
   *
   * @param polymer PolymerNotation
   * @return nucleotide sequence
   * @throws NotationException
   * @throws RNAUtilsException
   * @throws HELM2HandledException
   * @throws NucleotideLoadingException
   * @throws ChemistryException
   */
  public static String getNucleotideSequence(PolymerNotation polymer) throws NotationException, RNAUtilsException, HELM2HandledException, NucleotideLoadingException, ChemistryException {

    List<Nucleotide> nucleotides = getNucleotideList(polymer);
    StringBuffer sb = new StringBuffer();
    int count = 0;
    Map<String, String> reverseNucMap = NucleotideFactory.getInstance().getReverseNucleotideTemplateMap();
    for (Nucleotide nuc : nucleotides) {
      String nucleotide = nuc.getNotation();
      String nucleoside = nuc.getNucleosideNotation();
      String linker = nuc.getLinkerNotation();

      // it is ok for the first nucleotide not to have a nucleoside
      if (count == 0 && nucleoside.length() == 0) {
        sb.append(nuc.getPhosphateMonomer().getAlternateId());
        count++;
        continue;
      }

      // it is ok for the last nucleotide not to have a linker
      if (count == nucleotides.size() - 1 && linker.length() == 0) {
        nucleotide = nucleotide + Monomer.ID_P;
      }

      if (reverseNucMap.containsKey(nucleotide)) {
        sb.append(reverseNucMap.get(nucleotide));
      } else {
        throw new NotationException("Unknown nucleotide found for "
            + nucleotide + " : missing nucleotide template");
      }

      count++;
    }

    return sb.toString();

  }

  /**
   * method to get all nucleotides for one polymer
   *
   * @param polymer PolymerNotation
   * @return List of nucleotides of the polmyer
   * @throws RNAUtilsException if the polymer is not rna or dna or the
   *           nucleotide can not be read
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws ChemistryException
   *
   */
  public static List<Nucleotide> getNucleotideList(PolymerNotation polymer) throws RNAUtilsException, HELM2HandledException, ChemistryException {
    checkRNA(polymer);
    List<Nucleotide> nucleotides = new ArrayList<Nucleotide>();
    /* check for HELM2Elements */
    List<MonomerNotation> monomerNotations = polymer.getPolymerElements().getListOfElements();
    for (int i = 0; i < monomerNotations.size(); i++) {
      MonomerNotation monomerNotation = monomerNotations.get(i);
      if ((!(monomerNotation instanceof MonomerNotationUnitRNA)) || Integer.parseInt(monomerNotation.getCount()) != 1) {
        LOG.info("MonomerNotation contains HELM2 Elements " + monomerNotation);
        throw new HELM2HandledException("HELM2 Elements are involved");
      }
      try {
        boolean last = false;
        if (i == monomerNotations.size() - 1) {
          last = true;
        }
        nucleotides.add(NucleotideParser.convertToNucleotide(monomerNotation.getUnit(), last));
      } catch (MonomerException | NucleotideLoadingException | NotationException | org.helm.notation2.exception.NotationException e) {
        e.printStackTrace();
        throw new RNAUtilsException("Nucleotide can not be read " + e.getMessage());
      }
    }
    return nucleotides;
  }

  /**
   * method to get the trimmed nucleotide sequence
   *
   * @param polymer
   * @return trimmed nucleotide sequence
   * @throws RNAUtilsException
   * @throws HELM2HandledException
   * @throws ChemistryException
   */
  public static String getTrimmedNucleotideSequence(PolymerNotation polymer) throws RNAUtilsException, HELM2HandledException, ChemistryException {
    checkRNA(polymer);
    List<Nucleotide> list = getNucleotideList(polymer);

    int start = 0;
    Nucleotide na = list.get(start);
    while (null == na.getBaseMonomer()) {
      start++;
      na = list.get(start);
    }

    int end = list.size() - 1;
    na = list.get(end);
    while (null == na.getBaseMonomer()) {
      end--;
      na = list.get(end);
    }

    StringBuffer sb = new StringBuffer();
    for (int i = start; i <= end; i++) {
      sb.append(list.get(i).getNaturalAnalog());
    }
    return sb.toString();
  }

}
