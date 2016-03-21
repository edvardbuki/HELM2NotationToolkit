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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.MoleculeProperty;
import org.helm.notation2.Monomer;
import org.helm.notation2.MonomerFactory;
import org.helm.notation2.MonomerStore;
import org.helm.notation2.calculation.ExtinctionCoefficient;
import org.helm.notation2.calculation.MoleculePropertyCalculator;
import org.helm.notation2.exception.BuilderMoleculeException;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.ConnectionNotationException;
import org.helm.notation2.exception.ExtinctionCoefficientException;
import org.helm.notation2.exception.FastaFormatException;
import org.helm.notation2.exception.GroupingNotationException;
import org.helm.notation2.exception.HELM1FormatException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.MonomerLoadingException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.exception.ParserException;
import org.helm.notation2.exception.PeptideUtilsException;
import org.helm.notation2.exception.PolymerIDsException;
import org.helm.notation2.exception.ValidationException;
import org.helm.notation2.parser.notation.HELM2Notation;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * WebService class containing all required methods for the web-service
 *
 * @author hecht
 */
public class WebService {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(WebService.class);

  /**
   * method to get the XHELMRootElement of a document as a string
   *
   * @param resource xhelm input
   * @return XHELMRootElement
   * @throws JDOMException
   * @throws IOException
   */
  private Element getXHELMRootElement(String resource) throws JDOMException,
      IOException {

    ByteArrayInputStream stream = new ByteArrayInputStream(resource.getBytes());
    SAXBuilder builder = new SAXBuilder();
    Document doc = builder.build(stream);

    return doc.getRootElement();
  }

  /**
   * method to combine the new MonomerStore to the existing one, in case of
   * xHELM as input
   *
   * @param monomerStore MonomerStore
   * @throws MonomerLoadingException
   * @throws IOException
   * @throws MonomerException
   * @throws ChemistryException
   */
  private void updateMonomerStore(MonomerStore monomerStore) throws MonomerLoadingException, IOException, MonomerException, ChemistryException {
    for (Monomer monomer : monomerStore.getAllMonomersList()) {
      MonomerFactory.getInstance().getMonomerStore().addNewMonomer(monomer);
      // save monomer db to local file after successful update //
      MonomerFactory.getInstance().saveMonomerCache();
    }
  }

  /**
   * method to read the HELM string, the HELM can be in version 1 or 2, or in
   * Xhelm format
   *
   * @param notation HELM input
   * @return ContainerHELM2
   * @throws ParserException
   * @throws IOException
   * @throws JDOMException
   * @throws MonomerException
   * @throws ChemistryException
   */
  private HELM2Notation readNotation(String notation) throws ParserException, JDOMException, IOException, MonomerException, ChemistryException {
    /* xhelm notation */
    if (notation.contains("<Xhelm>")) {
      LOG.info("xhelm is used as input");
      String xhelm = notation;
      Element xHELMRootElement = getXHELMRootElement(xhelm);

      notation = xHelmNotationParser.getHELMNotationString(xHELMRootElement);
      MonomerStore store = xHelmNotationParser.getMonomerStore(xHELMRootElement);
      updateMonomerStore(store);

    }
    return HELM2NotationUtils.readNotation(notation);
  }

  /**
   * method to validate the HELM-String input
   *
   * @param helm input HELM string
   * @return ContainerHELM2
   * @throws ValidationException if the HELM input is not valid
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws NotationException
   */
  private HELM2Notation validate(String helm) throws ValidationException, ChemistryException {

    try {
      /* Read */
      HELM2Notation helm2notation = readNotation(helm);

      /* Validate */
      LOG.info("Validation of HELM is starting");
      Validation.validateNotationObjects(helm2notation);
      LOG.info("Validation was successful");

      return helm2notation;

    } catch (MonomerException | GroupingNotationException | ConnectionNotationException | PolymerIDsException | ParserException | JDOMException | IOException | NotationException
        | org.helm.notation2.parser.exceptionparser.NotationException e) {
      e.printStackTrace();
      LOG.info("Validation was not successful");
      LOG.error(e.getMessage());
      throw new ValidationException(e.getMessage());
    }
  }

  /**
   * method to validate the input HELM-String
   *
   * @param helm input HELM-string
   * @throws ValidationException if the input HELM is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws NotationException
   */
  public void validateHELM(String helm) throws ValidationException, MonomerLoadingException, ChemistryException {
    validate(helm);
    setMonomerFactoryToDefault(helm);
  }

  /**
   * method to convert the input HELM into canonical HELM
   *
   * @param notation HELM input
   * @return canonical HELM
   * @throws HELM1FormatException if HELM input contains HELM2 features
   * @throws NotationException if the notation objects can not be built
   * @throws ValidationException if the HELM input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be loaded
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public String convertStandardHELMToCanonicalHELM(String notation) throws HELM1FormatException, ValidationException, MonomerLoadingException, ChemistryException {
    String result = HELM1Utils.getCanonical(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to convert the input HELM into a standard HELM
   *
   * @param notation HELM input
   * @return standard HELM
   * @throws HELM1FormatException if the HELM input contains HELM2 features
   * @throws NotationException if the notation objects can not be built
   * @throws ValidationException if the HELM input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be loaded
   * @throws CTKException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public String convertIntoStandardHELM(String notation) throws HELM1FormatException, ValidationException, MonomerLoadingException, CTKException, ChemistryException {
    String result = HELM1Utils.getStandard(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to calculate from a non-ambiguous HELM string the extinction
   * coefficient
   *
   * @param notation HELM input
   * @return extinction coefficient from the HELM input
   * @throws ExtinctionCoefficientException if the extinction coefficient can
   *           not be calculated
   * @throws ValidationException if the HELM input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public Float calculateExtinctionCoefficient(String notation) throws ExtinctionCoefficientException, ValidationException, MonomerLoadingException, ChemistryException {
    Float result = ExtinctionCoefficient.getInstance().calculate(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate FASTA-Formats for all rna and peptide sequences from an
   * HELM input
   *
   * @param notation HELM input
   * @return FASTA containing all rna and peptide sequences
   * @throws ValidationException if the HELM input is not valid
   * @throws FastaFormatException if the FASTA-sequences can not be built
   * @throws MonomerLoadingException if the MonomerFactory can not be loaded
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws NotationException
   */
  public String generateFasta(String notation) throws FastaFormatException, ValidationException, MonomerLoadingException, ChemistryException {
    String result = FastaFormat.generateFasta(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate HELM from a FASTA containing rna/dna sequences
   *
   * @param notation FASTA containing rna/dna sequences
   * @return HELM
   * @throws FastaFormatException if the input is not valid
   * @throws JDOMException
   * @throws IOException
   * @throws org.helm.notation2.parser.exceptionparser.NotationException
   * @throws ChemistryException
   */
  public String generateHELMFromFastaNucleotide(String notation) throws FastaFormatException, IOException, JDOMException, org.helm.notation2.parser.exceptionparser.NotationException,
      ChemistryException {
    String result = FastaFormat.generateRNAPolymersFromFastaFormatHELM1(notation).toHELM2();
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate HELM from a FASTA containing peptide sequence(s)
   *
   * @param notation FASTA containing peptide sequence(s)
   * @return HELM
   * @throws FastaFormatException if the FASTA input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be loaded
   * @throws ChemistryException
   */
  public String generateHELMFromFastaPeptide(String notation) throws FastaFormatException, MonomerLoadingException, ChemistryException {
    String result = FastaFormat.generatePeptidePolymersFromFASTAFormatHELM1(notation).toHELM2();
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to calculate from a non-ambiguous HELM input the molecular weight
   *
   * @param notation HELM input
   * @return moleuclar weight from the HELM input
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ValidationException if the HELM input is not valid
   * @throws BuilderMoleculeException if the molecule for the calculation can
   *           not be built
   * @throws CTKException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public Double calculateMolecularWeight(String notation) throws MonomerLoadingException, BuilderMoleculeException, CTKException, ValidationException, ChemistryException {
    Double result = MoleculePropertyCalculator.getMolecularWeight(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to calculate from a non-ambiguous HELM input the molecular formula
   *
   * @param notation HELM input
   * @return molecular formula from the HELM input
   * @throws ValidationException if the HELM input is not valid
   * @throws BuilderMoleculeException if the molecule for the calculation can
   *           not be built
   * @throws CTKException
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public String getMolecularFormula(String notation) throws BuilderMoleculeException, CTKException, ValidationException, MonomerLoadingException, ChemistryException {
    String result = MoleculePropertyCalculator.getMolecularFormular(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to calculate froma non-ambiguous HELM input the molecular
   * properties: molecular formula, molecular weight, exact mass, extinction
   * coefficient
   *
   * @param notation
   * @return
   * @throws BuilderMoleculeException
   * @throws CTKException
   * @throws ExtinctionCoefficientException
   * @throws ValidationException
   * @throws MonomerLoadingException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public List<String> getMolecularProperties(String notation) throws BuilderMoleculeException, CTKException, ExtinctionCoefficientException, ValidationException, MonomerLoadingException,
      ChemistryException {
    MoleculeProperty result = MoleculePropertyCalculator.getMoleculeProperties(validate(notation));
    setMonomerFactoryToDefault(notation);

    return new LinkedList<String>(
        Arrays.asList(result.getMolecularFormula(), Double.toString(result.getMolecularWeight()), Double.toString(result.getExactMass()), Double.toString(result.getExtinctionCoefficient())));

  }

  /**
   * method to read a single peptide sequence and generates HELM
   *
   * @param peptide peptide sequence
   * @return HELM
   * @throws org.helm.notation2.parser.exceptionparser.NotationException if the
   *           notation object can not be built
   * @throws FastaFormatException if the peptide sequence is not in the right
   *           format
   * @throws ChemistryException
   */
  public String readPeptide(String peptide) throws FastaFormatException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException {
    return SequenceConverter.readPeptide(peptide).toHELM2();
  }

  /**
   * method to read a single rna sequence and generates HELM
   *
   * @param rna rna sequence
   * @return HELM
   * @throws org.helm.notation2.parser.exceptionparser.NotationException if the
   *           notation object can not be built
   * @throws FastaFormatException if the rna-sequence is not in the right format
   *           HELM
   * @throws JDOMException
   * @throws IOException
   * @throws ChemistryException
   */
  public String readRNA(String rna) throws org.helm.notation2.parser.exceptionparser.NotationException, FastaFormatException, IOException, JDOMException, ChemistryException {
    return SequenceConverter.readRNA(rna).toHELM2();
  }

  /**
   * method to generate a HELM molecule
   *
   * @param notation HELM string
   * @return generated molecule image in byte[]
   * @throws ValidationException if the HELM string is not valid
   * @throws BuilderMoleculeException if the molecule can't be built
   * @throws CTKException
   * @throws IOException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws NotationException
   * @throws MonomerException if the MonomerFactory can not be loaded
   */
  public byte[] generateImageForHELMMolecule(String notation) throws BuilderMoleculeException, CTKException, IOException, ValidationException, ChemistryException {
    byte[] result = Images.generateImageHELMMolecule(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate an image for a monomer
   *
   * @param monomer Monomer Input
   * @return generated molecule image in byte[]
   * @throws BuilderMoleculeException if the molecule can not be built
   * @throws CTKException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public byte[] generateImageForMonomer(Monomer monomer, boolean showRgroups) throws BuilderMoleculeException, CTKException, ChemistryException {
    return Images.generateImageofMonomer(monomer, showRgroups);
  }

  /**
   * method to generate JSON-Output for the HELM
   *
   * @param helm HELM input
   * @return HELM as JSON-objects
   * @throws ValidationException if the HELM input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws JsonProcessingException
   */
  public String generateJSON(String helm) throws ValidationException, MonomerLoadingException, ChemistryException, JsonProcessingException {
    String result = HELM2NotationUtils.toJSON(validate(helm));
    setMonomerFactoryToDefault(helm);
    return result;
  }

  /**
   * method to set the MonomerFactory to the default one, this is only done in
   * case of xHELM input
   *
   * @param helm input HELM
   * @throws MonomerLoadingException if the MonomerFactory can not be loaded
   * @throws ChemistryException
   */
  private void setMonomerFactoryToDefault(String helm) throws MonomerLoadingException, ChemistryException {
    if (helm.contains("<Xhelm>")) {
      LOG.info("Refresh local Monomer Store in case of Xhelm");
      MonomerFactory.refreshMonomerCache();
    }
  }

  /**
   * method to generate the natural analogue sequence for all peptide-sequences
   * from an HELM input
   *
   * @param notation input HELM
   * @return natural analogue peptide sequences, divided by white spaces
   * @throws org.helm.notation2.parser.exceptionparser.NotationException if the
   *           input complex notation contains non-peptide polymer(s)
   * @throws HELM2HandledException if the HELM input contains HELM2 features
   * @throws ValidationException if the input HELM is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws PeptideUtilsException if the polymer is not a peptide
   * @throws org.helm.notation2.parser.exceptionparser.NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public String generateNaturalAnalogSequencePeptide(String notation) throws HELM2HandledException, ValidationException,
      MonomerLoadingException, PeptideUtilsException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException {
    String result = SequenceConverter.getPeptideNaturalAnalogSequenceFromNotation(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate the natural analogue sequence for all rna-sequences from
   * an HELM input
   *
   * @param notation HELM input
   * @return natural analogue rna sequences, divided by white spaces
   * @throws org.helm.notation2.parser.exceptionparser.NotationException if the
   *           input complex notation contains non-nucleid acid polymer(s)
   * @throws HELM2HandledException if HELM input contains HELM2 features
   * @throws ValidationException if the HELM input is not valid
   * @throws MonomerLoadingException if the MonomerFactory can not be refreshed
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public String generateNaturalAnalogSequenceRNA(String notation) throws org.helm.notation2.parser.exceptionparser.NotationException, HELM2HandledException, ValidationException,
      MonomerLoadingException, ChemistryException {
    String result = SequenceConverter.getNucleotideNaturalAnalogSequenceFromNotation(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate a SMILES representation for a whole HELM2 input
   *
   * @param notation
   * @return
   * @throws BuilderMoleculeException
   * @throws CTKException
   * @throws ChemistryException
   * @throws ValidationException
   * @throws MonomerLoadingException
   */
  public String generateSMILESForHELM2(String notation) throws BuilderMoleculeException, CTKException, ChemistryException, ValidationException, MonomerLoadingException {
    String result = SMILES.getSMILESForAll(validate(notation));
    setMonomerFactoryToDefault(notation);
    return result;
  }

  /**
   * method to generate a SMILES representation for a whole HELM2 input
   *
   * @param notation
   * @return
   * @throws BuilderMoleculeException
   * @throws CTKException
   * @throws ChemistryException
   * @throws ValidationException
   * @throws MonomerLoadingException
   * @throws NotationException
   */
  public String generateCanSMILESForHELM2(String notation) throws BuilderMoleculeException, CTKException, ChemistryException, ValidationException, MonomerLoadingException, NotationException {
    String result = SMILES.getCanonicalSMILESForAll((validate(notation)));
    setMonomerFactoryToDefault(notation);
    return result;
  }

}
