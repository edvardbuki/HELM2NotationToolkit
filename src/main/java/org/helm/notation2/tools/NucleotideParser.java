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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.Monomer;
import org.helm.notation2.Nucleotide;
import org.helm.notation2.NucleotideFactory;
import org.helm.notation2.SimpleNotationGroupIterator;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.NucleotideLoadingException;
import org.helm.notation2.parser.exceptionparser.NotationException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NucleotideParser
 *
 * @author hecht
 */
public class NucleotideParser {

  public static final char MODIFICATION_START_SYMBOL = '[';

  public static final char MODIFICATION_END_SYMBOL = ']';

  public static final char BRANCH_START_SYMBOL = '(';

  public static final char BRANCH_END_SYMBOL = ')';

  @Deprecated
  public static final String DEFAULT_NOTATION_SOURCE = "HELM Notation";

  public static final int MINUMUM_MATCH_FRAGMENT_LENGTH = 2;

  public static Map<String, String> complementMap = new HashMap<String, String>();

  static {
    complementMap.put("A", "U");
    complementMap.put("G", "C");
    complementMap.put("C", "G");
    complementMap.put("U", "A");
    complementMap.put("T", "A");
    complementMap.put("X", "X");
  }

  public static final String RNA_DESIGN_NONE = "NONE";

  public static final String RNA_DESIGN_TUSCHL_19_PLUS_2 = "TUSCHL_19_PLUS_2";

  // ss 5 1------19--
  // as 3 --19------1
  public static final String RNA_DESIGN_DICER_27_R = "DICER_27_R";

  // ss 5' 1-----------------------25
  // as 3' 123-----------------------27
  public static final String RNA_DESIGN_DICER_27_L = "DICER_27_L";

  // ss 5' 1-------------------------27
  // as 3' 1-----------------------25
  public static final List<String> SUPPORTED_DESIGN_LIST = new ArrayList<String>();

  static {
    SUPPORTED_DESIGN_LIST.add(RNA_DESIGN_NONE);
    SUPPORTED_DESIGN_LIST.add(RNA_DESIGN_TUSCHL_19_PLUS_2);
    SUPPORTED_DESIGN_LIST.add(RNA_DESIGN_DICER_27_L);
    SUPPORTED_DESIGN_LIST.add(RNA_DESIGN_DICER_27_R);
  }

  private static final String NUCLEOTIDE_SYMBOL_ELEMENT = "SYMBOL";

  private static final String NUCLEOTIDE_MONOMER_NOTATION_ELEMENT = "MONOMER_NOTATION";

  private static final String NUCLEOTIDE_ELEMENT = "NUCLEOTIDE";

  private static final String TEMPLATE_ELEMENT = "TEMPLATE";

  private static final String TEMPLATE_NOTATION_SOURCE_ATTRIBUTE = "notationSource";

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(NucleotideParser.class);

  public static Map<String, Map<String, String>> getNucleotideTemplates(
      Element templatesElement) {
    Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

    List templates = templatesElement.getChildren();
    for (Iterator i = templates.iterator(); i.hasNext();) {
      Element templateElement = (Element) i.next();
      String notationSource = templateElement.getAttributeValue(TEMPLATE_NOTATION_SOURCE_ATTRIBUTE);
      Map<String, String> tmpMap = new HashMap<String, String>();
      map.put(notationSource, tmpMap);
      List nucleotides = templateElement.getChildren();
      for (Iterator it = nucleotides.iterator(); it.hasNext();) {
        Element nucleotideElement = (Element) it.next();
        Nucleotide nucleotide = getNucleotide(nucleotideElement);
        tmpMap.put(nucleotide.getSymbol(), nucleotide.getNotation());
      }
    }
    return map;
  }

  public static Nucleotide getNucleotide(Element nucleotideElement) {
    Element symbolE = nucleotideElement.getChild(NUCLEOTIDE_SYMBOL_ELEMENT, nucleotideElement.getNamespace());
    Element notationE = nucleotideElement.getChild(NUCLEOTIDE_MONOMER_NOTATION_ELEMENT, nucleotideElement.getNamespace());
    return new Nucleotide(symbolE.getText(), notationE.getText());
  }

  public static Nucleotide getNucleotide(String nucleotideXML)
      throws JDOMException, IOException {
    Nucleotide nuc = null;
    if (nucleotideXML != null && nucleotideXML.length() > 0) {
      SAXBuilder builder = new SAXBuilder();
      ByteArrayInputStream bais = new ByteArrayInputStream(
          nucleotideXML.getBytes());
      Document doc = builder.build(bais);
      Element root = doc.getRootElement();
      nuc = getNucleotide(root);
    }
    return nuc;
  }

  public static String getNucleotideTemplatesXML(
      Map<String, Map<String, String>> templates) {
    XMLOutputter outputer = new XMLOutputter(Format.getPrettyFormat());

    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<NUCLEOTIDE_TEMPLATES xsi:schemaLocation=\"lmr NucleotideTemplateSchema.xsd\" xmlns=\"lmr\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

    Set<String> templateSet = templates.keySet();
    for (Iterator i = templateSet.iterator(); i.hasNext();) {
      String template = (String) i.next();
      Element templateElement = new Element(TEMPLATE_ELEMENT);
      Attribute att = new Attribute(TEMPLATE_NOTATION_SOURCE_ATTRIBUTE,
          template);
      templateElement.setAttribute(att);

      Map<String, String> nucMap = templates.get(template);
      Set<String> nucleotideSet = nucMap.keySet();

      for (Iterator it = nucleotideSet.iterator(); it.hasNext();) {
        Element nucleotideElement = new Element(NUCLEOTIDE_ELEMENT);
        templateElement.getChildren().add(nucleotideElement);

        String symbol = (String) it.next();
        Element symbolElement = new Element(NUCLEOTIDE_SYMBOL_ELEMENT);
        symbolElement.setText(symbol);
        nucleotideElement.getChildren().add(symbolElement);

        String notation = nucMap.get(symbol);
        Element notationElement = new Element(
            NUCLEOTIDE_MONOMER_NOTATION_ELEMENT);
        notationElement.setText(notation);
        nucleotideElement.getChildren().add(notationElement);
      }

      String templateString = outputer.outputString(templateElement);
      sb.append(templateString);
    }

    sb.append("\n</NUCLEOTIDE_TEMPLATES>");

    return sb.toString();
  }

  public static List<String> getMonomerIDListFromNucleotide(String element) throws NotationException {
    {
      List<String> ids = new ArrayList<>();

      char[] chars = element.toCharArray();
      char prevLetter = 0;

      for (int i = 0; i < chars.length; i++) {
        char letter = chars[i];
        if (letter == MODIFICATION_START_SYMBOL) {
          int matchingPos = NucleotideParser.getMatchingBracketPosition(chars, i, MODIFICATION_START_SYMBOL, MODIFICATION_END_SYMBOL);
          i++;

          if (matchingPos == -1) {
            throw new NotationException(
                "Invalid Polymer Notation: modified monomer must be enclosed by square brackets");
          } else {
            ids.add(element.substring(i, matchingPos));
          }
          i = matchingPos;

        } else if (letter == BRANCH_START_SYMBOL) {
          if (i == 0) {
            throw new NotationException(
                "Invalid Polymer Notation: branch monomer is not allowed at the beginnig of notation");
          }

          if (prevLetter == BRANCH_END_SYMBOL) {
            throw new NotationException(
                "Invalid Polymer Notation: branch monomers cannot be connected with each other");
          }

          int matchingPos = NucleotideParser.getMatchingBracketPosition(chars, i, BRANCH_START_SYMBOL, BRANCH_END_SYMBOL);
          i++;
          if (matchingPos == -1) {
            throw new NotationException(
                "Invalid Polymer Notation: modified monomer must be enclosed by brackets");
          } else {
            ids.add(element.substring(i, matchingPos));
          }
          i = matchingPos;

        } else {
          ids.add(element.substring(i, i + 1));
        }
        prevLetter = letter;
      }

      return ids;

    }
  }

  /**
   * @param id
   * @param i
   * @return
   * @throws ChemistryException
   * @throws org.helm.notation2.exception.NotationException
   * @throws MonomerException
   * @throws NucleotideLoadingException
   */
  public static Nucleotide convertToNucleotide(String id, boolean last) throws MonomerException, org.helm.notation2.exception.NotationException, ChemistryException, NucleotideLoadingException,
      NotationException {
    Map<String, String> reverseNucMap = NucleotideFactory.getInstance().getReverseNucleotideTemplateMap();
    // last nucleotide will be handled differently
    String tmpNotation = id;
    String symbol = null;
    // if (i == (notations.length - 1) && notation.endsWith(")")) {
    if (last && id.endsWith(")")) {
      tmpNotation = id + "P";
    }

    if (reverseNucMap.containsKey(tmpNotation)) {
      symbol = reverseNucMap.get(tmpNotation);
    } else {
      char[] chars = id.toCharArray();
      String base = null;
      symbol = "X";

      // find base
      for (int j = 0; j < chars.length; j++) {
        char letter = chars[j];
        // skip modifications if not in branch
        if (letter == MODIFICATION_START_SYMBOL) {
          int matchingPos = NucleotideParser.getMatchingBracketPosition(chars, j, MODIFICATION_START_SYMBOL, MODIFICATION_END_SYMBOL);
          j++;

          if (matchingPos == -1) {
            throw new NotationException(
                "Invalid Polymer Notation: Could not find matching bracket");
          }
          j = matchingPos;

        }
        // base is always a branch monomer
        else if (letter == BRANCH_START_SYMBOL) {
          int matchingPos = NucleotideParser.getMatchingBracketPosition(chars, j, BRANCH_START_SYMBOL, BRANCH_END_SYMBOL);
          j++;

          if (matchingPos == -1) {
            throw new NotationException(
                "Invalid Polymer Notation: Could not find matching bracket");
          }

          base = id.substring(j, matchingPos);

          if (base.length() == 1) {
            symbol = base;
          } else {
            Monomer monomer = MethodsMonomerUtils.getMonomer("RNA", base, "");
            if (null == monomer.getNaturalAnalog()) {
              symbol = "X";
            } else {
              symbol = monomer.getNaturalAnalog();
            }
          }

          j = matchingPos;
        }

      }

    }
    Nucleotide nuc = new Nucleotide(symbol, id);
    return nuc;
  }

  /**
   * @param notation
   * @return
   * @throws org.helm.notation2.parser.exceptionparser.NotationException
   */
  public static int getMonomerCountForRNA(String notation) throws org.helm.notation2.parser.exceptionparser.NotationException {
    return getMonomerIDListFromNucleotide(notation).size();
  }

  /**
   * validate RNA simple notation
   *
   * @param polymerNotation
   * @param monomerStore
   * @return true or exception
   * @throws org.helm.notation2.parser.exceptionparser.NotationException
   * @throws java.io.IOException
   * @throws org.helm.notation2.exception.NotationException
   * @throws org.helm.notation2.exception.MonomerException
   * @throws org.helm.notation.StructureException
   * @throws org.jdom.JDOMException
   * @throws org.helm.notation2.parser.exceptionparser.NotationException
   */
  public static boolean validateSimpleNotationForRNA(String polymerNotation) throws org.helm.notation2.parser.exceptionparser.NotationException {
    getMonomerIDListFromNucleotide(polymerNotation);
    return true;
  }

  public static int getMatchingBracketPosition(char[] characters,
      int position, char openingBracket, char closingBracket) {
    if (position < (characters.length - 1)
        && characters[position] == openingBracket) {
      int currentPosition = position;
      int openingBracketCount = 1;

      do {
        char currentCharacter = characters[++currentPosition];
        if (currentCharacter == openingBracket) {
          openingBracketCount++;
        } else if (currentCharacter == closingBracket) {
          openingBracketCount--;
        }
      } while (openingBracketCount > 0
          && currentPosition < (characters.length - 1));

      if (characters[currentPosition] == closingBracket) {
        return currentPosition;
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

}
