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

import org.helm.chemtoolkit.AbstractChemistryManipulator;
import org.helm.chemtoolkit.CTKException;
import org.helm.notation2.Attachment;
import org.helm.notation2.Chemistry;
import org.helm.notation2.DeepCopy;
import org.helm.notation2.Monomer;
import org.helm.notation2.MonomerFactory;
import org.helm.notation2.MonomerStore;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.MonomerLoadingException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.parser.notation.polymer.MonomerNotation;
import org.helm.notation2.parser.notation.polymer.MonomerNotationGroup;
import org.helm.notation2.parser.notation.polymer.MonomerNotationList;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MethodsMonomerUtils
 *
 * @author hecht
 */
public final class MethodsMonomerUtils {

  private static final Logger LOG =
      LoggerFactory.getLogger(MethodsMonomerUtils.class);

  /**
   * Default constructor.
   */
  private MethodsMonomerUtils() {

  }

  /**
   * method to get all HELM1 valid MonomerNotations Only on these monomers
   * required HELM1 functions are performed
   *
   * @param monomerNotations List of MonomerNotation
   * @return List of Monomer
   * @throws HELM2HandledException if the HELM2 features were there
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   * @throws NotationException
   */
  public static List<Monomer> getListOfHandledMonomers(List<MonomerNotation> monomerNotations)
      throws HELM2HandledException, ChemistryException{
    List<Monomer> items = new ArrayList<Monomer>();
    for (int i = 0; i < monomerNotations.size(); i++) {
      MonomerNotation monomerNotation = monomerNotations.get(i);

      /* group element */
      if (monomerNotation instanceof MonomerNotationGroup || monomerNotation instanceof MonomerNotationList) {
        throw new HELM2HandledException("Functions can't be called for HELM2 objects");
      } else {
        try {
          int count = Integer.parseInt(monomerNotation.getCount());
          if (count == 0 || count > 1) {
            throw new HELM2HandledException("Functions can't be called for HELM2 objects");
          }

          // for (int j = 0; j < count; j++) {
          items.addAll(Validation.getAllMonomers(monomerNotation, i));
          // }
        } catch (NumberFormatException | JDOMException | MonomerException | IOException | NotationException | CTKException e) {
          e.printStackTrace();
          throw new HELM2HandledException("Functions can't be called for HELM2 objects");
        }

      }
    }
    return items;
  }

  /**
   * method to get all HELM1 valid MonomerNotations Only on these monomers
   * required HELM1 functions are performed
   *
   * @param monomerNotations List of MonomerNotation
   * @return List of Monomer
   * @throws HELM2HandledException if HELM2 features are there
   * @throws NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static List<Monomer> getListOfHandledMonomersOnlyBase(List<MonomerNotation> monomerNotations)
      throws HELM2HandledException, NotationException, ChemistryException{
    LOG.debug("Get all bases of the rna");
    List<Monomer> items = new ArrayList<Monomer>();

    for (MonomerNotation monomerNotation : monomerNotations) {
      /* group element */
      if (monomerNotation instanceof MonomerNotationGroup) {
        throw new HELM2HandledException("Functions can't be called for HELM2 objects");
      } else {
        try {
          int count = Integer.parseInt(monomerNotation.getCount());
          if (count != 1) {
            throw new HELM2HandledException("Functions can't be called for HELM2 objects");
          }

          // for (int j = 0; j < count; j++) {
          items.addAll(Validation.getAllMonomersOnlyBase(monomerNotation));
          // }
        } catch (NumberFormatException | JDOMException | MonomerException | IOException | CTKException e) {
          e.printStackTrace();
          throw new HELM2HandledException("Functions can't be called for HELM2 objects");
        }

      }
    }
    return items;
  }

  /**
   * method to get all MonomerNotations for all given polymers
   *
   * @param polymers List of PolymerNotation
   * @return List of MonomerNotation
   */
  public static List<MonomerNotation> getListOfMonomerNotation(List<PolymerNotation> polymers) {
    List<MonomerNotation> items = new ArrayList<MonomerNotation>();
    for (PolymerNotation polymer : polymers) {
      items.addAll(polymer.getListMonomers());
    }

    return items;

  }

  /**
   * method to get all monomers for all MonomerNotations
   *
   * @param monomerNotations List of MonomerNotation
   * @return List of Monomer
   * @throws MonomerException if the Monomer is not valid
   * @throws IOException
   * @throws JDOMException
   * @throws HELM2HandledException if HELM2 features are there
   * @throws CTKException
   * @throws NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static List<Monomer> getListOfMonomer(List<MonomerNotation> monomerNotations) throws MonomerException,
      IOException, JDOMException, HELM2HandledException, CTKException, NotationException, ChemistryException {
    List<Monomer> items = new ArrayList<Monomer>();
    for (int i = 0; i < monomerNotations.size(); i++) {
      items.addAll(Validation.getAllMonomers(monomerNotations.get(i), i));
    }
    return items;

  }

  /**
   * method to get the monomer from the database!
   *
   * @param type Type of the Monomer
   * @param id Id of the Monomer
   * @return Monomer
   * @throws MonomerException if the desired monomer is not in the database
   * @throws NotationException
   * @throws ChemistryException if the Chemistry Engine can not be initialized 
   */
  public static Monomer getMonomer(String type, String id, String info) throws MonomerException, NotationException, ChemistryException{
    try {
      if (id.startsWith("[") && id.endsWith("]")) {
        id = id.substring(1, id.length() - 1);
      }
      MonomerFactory monomerFactory = MonomerFactory.getInstance();
      MonomerStore monomerStore = monomerFactory.getMonomerStore();
      Monomer monomer;
      /* Monomer was saved to the database */
      monomer = monomerStore.getMonomer(type, id);
      if (monomer == null) {
        /*
         * smiles check! Maybe the smiles is already included in the data base
         */
        if (monomerFactory.getSmilesMonomerDB().get(id) != null) {
          monomer = monomerFactory.getSmilesMonomerDB().get(id);
          return monomer;

        } else {
          /* This has to be done */
          monomer = monomerFactory.getSmilesMonomerDB().get(id);
          if (monomer == null) {
            /* Rgroups information are not given -> only smiles information */
            AbstractChemistryManipulator manipulator = Chemistry.getInstance().getManipulator();
            if (manipulator.validateSMILES(id)) {
              if (type.equals(Monomer.CHEMICAL_POLYMER_TYPE)) {
                monomer = generateTemporaryMonomer(id, type, "X");

              } else if (type.equals(Monomer.PEPTIDE_POLYMER_TYPE)) {
                monomer = generateTemporaryMonomer(id, type, "X");
              } else if (type.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
                monomer = generateTemporaryMonomer(id, type, info);
              }
            } else {
              throw new MonomerException("Defined Monomer is not in the database and also not valid SMILES " + id);
            }

            /* Add new monomer to the database */
            MonomerFactory.getInstance().getMonomerStore().addNewMonomer(monomer);
            MonomerFactory.getInstance().getSmilesMonomerDB().put(monomer.getCanSMILES(), monomer);
            // save monomer db to local file after successful update //
            MonomerFactory.getInstance().saveMonomerCache();
            LOG.info("Monomer was added to the database");
          }
        }
      }
      try{
      List<Attachment> idList = monomer.getAttachmentList();
      for (Attachment att : idList) {
			if (att.getCapGroupSMILES() == null) {
				MonomerParser.fillAttachmentInfo(att);
			}
		}
      } catch(CTKException |JDOMException ex){
    	  throw new MonomerException("Attachments could not be filled with default attachments");
      }
      return monomer;
    } catch (IOException e) {
      e.printStackTrace();
      /*
       * monomer is not in the database and also not a valid SMILES -> throw
       * exception
       */
      throw new MonomerException("Defined Monomer is not in the database and also not a valid SMILES " + id);
    }
  }

  public static Monomer generateTemporaryMonomer(String id, String polymerType, String naturalAnalog) throws NotationException, MonomerLoadingException, ChemistryException {
    String uniqueSmiles = id;

    String alternateId = generateNextAdHocMonomerID(polymerType);
    Map<String, Attachment> ids = MonomerFactory.getInstance().getAttachmentDB();
    Attachment R1HAtt = ids.get("R1-H");

    Monomer m = null;
    if (polymerType.equals(Monomer.CHEMICAL_POLYMER_TYPE)) {
      m = new Monomer(polymerType, Monomer.UNDEFINED_MOMONER_TYPE,
          naturalAnalog, alternateId);
    } else if (polymerType.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
      if (naturalAnalog.equals("P") || naturalAnalog.equals("R")) {
        m = new Monomer(polymerType, Monomer.BACKBONE_MOMONER_TYPE,
            naturalAnalog, alternateId);
      } else
        m = new Monomer(polymerType, Monomer.BRANCH_MOMONER_TYPE,
            naturalAnalog, alternateId);

    }
    // Peptide
    else {
      m = new Monomer(polymerType, Monomer.BACKBONE_MOMONER_TYPE,
          naturalAnalog, alternateId);
    }
    m.setAdHocMonomer(true);
    m.setCanSMILES(uniqueSmiles);
    m.setName("Dynamic");

    List<Attachment> al = new ArrayList<Attachment>();
    int start = 0;
    int pos = uniqueSmiles.indexOf("R", start);
    String number = "";
    while (pos >= 0) {
      pos++;
      String letter = uniqueSmiles.substring(pos, pos + 1);
      while (letter.matches("\\d")) {
        number = number + letter;
        pos++;
        letter = uniqueSmiles.substring(pos, pos + 1);
      }

      try {
        Attachment tmpAtt = DeepCopy.copy(R1HAtt);
        tmpAtt.setLabel("R" + number);
        tmpAtt.setAlternateId("R" + number + "-H");
        String oldSmi = tmpAtt.getCapGroupSMILES();
        String newSmi = oldSmi.replace("R1", "R" + number);
        tmpAtt.setCapGroupSMILES(newSmi);
        al.add(tmpAtt);
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new NotationException(
            "Unable to create attachment by copying from attachment database",
            ex);
      }

      start = pos;
      pos = uniqueSmiles.indexOf("R", start);
      number = "";
    }

    m.setAttachmentList(al);
    try {
      MonomerFactory.getInstance().getMonomerStore().addNewMonomer(m);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new NotationException(
          "Unable to add adhoc new monomer into monomer databse",
          ex);
    }

    return m;

  }

  private static Map<String, Integer> seedMap = new HashMap<String, Integer>();

  private static String getAdHocMonomerIDPrefix(String polymerType) {
    if (polymerType.equals(Monomer.CHEMICAL_POLYMER_TYPE)) {
      return "CM#";
    } else if (polymerType.equals(Monomer.PEPTIDE_POLYMER_TYPE)) {
      return "PM#";
    } else if (polymerType.equals(Monomer.NUCLIEC_ACID_POLYMER_TYPE)) {
      return "NM#";
    } else {
      return "AM#";
    }
  }

  private static String generateNextAdHocMonomerID(String polymerType) throws MonomerLoadingException, ChemistryException {
    Map<String, Monomer> internalMonomers = null;
    internalMonomers = MonomerFactory.getInstance().getMonomerDB().get(polymerType);

    Map<String, Monomer> monomers = MonomerFactory.getInstance().getMonomerStore().getMonomers(polymerType);

    Integer seed = seedMap.get(polymerType);
    if (seed == null) {
      seed = 0;
    }
    seed++;
    seedMap.put(polymerType, seed);

    String result = getAdHocMonomerIDPrefix(polymerType) + seed;

    if ((monomers != null && monomers.containsKey(result))
        || (internalMonomers != null && internalMonomers.containsKey(result))) {
      return generateNextAdHocMonomerID(polymerType);
    } else {
      return result;
    }
  }

}
