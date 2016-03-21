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

import org.helm.chemtoolkit.AbstractMolecule;
import org.helm.chemtoolkit.AttachmentList;

import org.helm.chemtoolkit.CTKException;
import org.helm.chemtoolkit.IAtomBase;
import org.helm.notation2.Attachment;
import org.helm.notation2.Chemistry;
import org.helm.notation2.Monomer;
import org.helm.notation2.RgroupStructure;
import org.helm.notation2.exception.BuilderMoleculeException;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.parser.notation.connection.ConnectionNotation;
import org.helm.notation2.parser.notation.polymer.BlobEntity;
import org.helm.notation2.parser.notation.polymer.ChemEntity;
import org.helm.notation2.parser.notation.polymer.GroupEntity;
import org.helm.notation2.parser.notation.polymer.PeptideEntity;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;
import org.helm.notation2.parser.notation.polymer.RNAEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class to build molecules for the HELMNotation
 *
 * @author hecht
 */

public final class BuilderMolecule {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(BuilderMolecule.class);

  /**
   * Default constructor.
   */
  private BuilderMolecule() {

  }

  /**
   * method to build a molecule for a single polymer
   *
   * @param polymernotation a single polymer
   * @return molecule for the given single polymer
   * @throws BuilderMoleculeException if the polymer type is BLOB or unknown
   * @throws HELM2HandledException if the polymer contains HELM2 features
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static RgroupStructure buildMoleculefromSinglePolymer(final PolymerNotation polymernotation) throws BuilderMoleculeException, HELM2HandledException, ChemistryException {
    LOG.info("Build molecule for single Polymer " + polymernotation.getPolymerID().getId());
    /* Case 1: BLOB -> throw exception */
    if (polymernotation.getPolymerID() instanceof BlobEntity) {
      LOG.error("Molecule can't be build for BLOB");
      throw new BuilderMoleculeException("Molecule can't be build for BLOB");
    } /* Case 2: CHEM */ else if (polymernotation.getPolymerID() instanceof ChemEntity) {
      List<Monomer> validMonomers = MethodsMonomerUtils.getListOfHandledMonomers(polymernotation.getPolymerElements().getListOfElements());
      return buildMoleculefromCHEM(polymernotation.getPolymerID().getId(), validMonomers);
    } /* Case 3: RNA or PEPTIDE */ else if (polymernotation.getPolymerID() instanceof RNAEntity
        || polymernotation.getPolymerID() instanceof PeptideEntity) {
      List<Monomer> validMonomers =
          MethodsMonomerUtils.getListOfHandledMonomers(polymernotation.getPolymerElements().getListOfElements());
      return buildMoleculefromPeptideOrRNA(polymernotation.getPolymerID().getId(), validMonomers);
    } else {
      LOG.error("Molecule can't be build for unknown polymer type");
      throw new BuilderMoleculeException("Molecule can't be build for unknown polymer type");
    }
  }

  /**
   * method to build molecules for the whole HELMNotation
   *
   * @param polymers all polymers of the HELMNotation
   * @param connections all connections of the HELMNotation
   * @return list of built molecules
   * @throws BuilderMoleculeException if HELM2 features were contained
   * @throws ChemistryException if the Chemistry Engine can not be iniialized
   */
  public static List<AbstractMolecule> buildMoleculefromPolymers(final List<PolymerNotation> polymers,
      final List<ConnectionNotation> connections) throws BuilderMoleculeException, ChemistryException {

    LOG.info("Building process for the all polymers is starting");
    Map<String, PolymerNotation> map = new HashMap<String, PolymerNotation>();

    Map<String, RgroupStructure> mapMolecules = new HashMap<String, RgroupStructure>();
    Map<String, String> mapConnections = new HashMap<String, String>();
    Map<String, Map<String, IAtomBase>> mapIAtomBase = new HashMap<String, Map<String, IAtomBase>>();

    List<AbstractMolecule> listMolecules = new ArrayList<AbstractMolecule>();
    RgroupStructure current = new RgroupStructure();

    AbstractMolecule molecule = null;
    /* Build for every single polymer a single molecule */
    LOG.info("Build for each polymer a single molecule");
    for (PolymerNotation polymer : polymers) {
      map.put(polymer.getPolymerID().getId(), polymer);
      try {

        current = buildMoleculefromSinglePolymer(polymer);
        mapMolecules.put(polymer.getPolymerID().getId(), current);
        mapIAtomBase.put(polymer.getPolymerID().getId(), current.getMolecule().getRgroups());
      } catch (HELM2HandledException | CTKException e) {
        throw new BuilderMoleculeException(e.getMessage());
      }
    }

    /* Build interconnections between single molecules */
    LOG.info("Connect the single molecules together");
    for (ConnectionNotation connection : connections) {
      LOG.info("Connection: " + connection.toString());
      /* Group Id -> throw exception */
      if (connection.getSourceId() instanceof GroupEntity || connection.getTargetId() instanceof GroupEntity) {
        LOG.error("Molecule can't be build for group connection");
        throw new BuilderMoleculeException("Molecule can't be build for group connection");
      }
      /* Get the source molecule + target molecule */
      String idFirst = connection.getSourceId().getId();
      String idSecond = connection.getTargetId().getId();
      if (mapMolecules.get(idFirst) == null) {
        idFirst = mapConnections.get(idFirst);
      }
      if (mapMolecules.get(idSecond) == null) {
        idSecond = mapConnections.get(idSecond);
      }

      RgroupStructure one = mapMolecules.get(idFirst);
      RgroupStructure two = mapMolecules.get(idSecond);
      mapMolecules.remove(idFirst);
      mapMolecules.remove(idSecond);

      /*
       * connection details: have to be an integer value + specific
       * MonomerNotationUnit
       */
      int source;
      int target;
      try {
        source = Integer.parseInt(connection.getSourceUnit());
        target = Integer.parseInt(connection.getTargetUnit());
      } catch (NumberFormatException e) {
        throw new BuilderMoleculeException("Connection has to be unambiguous");
      }

      /* if the */
      // if
      // (!((MethodsForContainerHELM2.isMonomerSpecific(map.get(connection.getSourceId().getID()),
      // source)
      // &&
      // MethodsForContainerHELM2.isMonomerSpecific(map.get(connection.getTargetId().getID()),
      // target)))) {
      // throw new BuilderMoleculeException("Connection has to be
      // unambiguous");
      // }

      /* R group of connection is unknown */
      if (connection.getrGroupSource().equals("?") || connection.getrGroupTarget().equals("?")) {
        throw new BuilderMoleculeException("Connection's R groups have to be known");
      }

      String rgroupOne = connection.getrGroupSource();
      String rgroupTwo = connection.getrGroupTarget();
      /* Self cycle */
      if (idFirst.equals(idSecond)) {
        try {
          LOG.debug("Self-cycle connection: " + connection.toString());
          molecule =
              Chemistry.getInstance().getManipulator().merge(one.getMolecule(), one.getRgroupMap().get(connection.getSourceId().getId() + ":" + source + ":"
                  + rgroupOne), one.getMolecule(), one.getRgroupMap().get(connection.getTargetId().getId() + ":"
                      + target + ":"
                      + rgroupTwo));
          one.getRgroupMap().remove(connection.getSourceId().getId() + ":" + source + ":" + rgroupOne);
          one.getRgroupMap().remove(connection.getSourceId().getId() + ":" + source + ":" + rgroupTwo);
          mapMolecules.put(idFirst, one);
        } catch (CTKException e) {
          throw new BuilderMoleculeException(e.getMessage());
        }
      } else {
        try {
          LOG.info("MERGE");
          molecule =
              Chemistry.getInstance().getManipulator().merge(one.getMolecule(), one.getRgroupMap().get(connection.getSourceId().getId() + ":" + source + ":"
                  + rgroupOne), two.getMolecule(), two.getRgroupMap().get(connection.getTargetId().getId() + ":"
                      + target + ":"
                      + rgroupTwo));
          LOG.info("Merge completed");
          RgroupStructure actual = new RgroupStructure();
          actual.setMolecule(molecule);
          Map<String, IAtomBase> rgroupMap = new HashMap<String, IAtomBase>();
          one.getRgroupMap().remove(connection.getSourceId().getId() + ":" + source + ":" + rgroupOne);
          two.getRgroupMap().remove(connection.getTargetId().getId() + ":" + target + ":" + rgroupTwo);
          rgroupMap.putAll(one.getRgroupMap());
          rgroupMap.putAll(two.getRgroupMap());
          actual.setRgroupMap(rgroupMap);
          mapMolecules.put(idFirst + idSecond, actual);
        } catch (CTKException e) {
          throw new BuilderMoleculeException(e.getMessage());
        }

        LOG.info("jkj");
        mapConnections.put(connection.getSourceId().getId(), idFirst + idSecond);
        mapConnections.put(connection.getTargetId().getId(), idFirst + idSecond);

        /* HashMap refresh */
        for (Map.Entry e : mapConnections.entrySet()) {
          if (e.getValue().equals(idFirst) || e.getValue().equals(idSecond)) {
            mapConnections.put((String) e.getKey(), idFirst + idSecond);
          }
        }

      }

    }

    for (Map.Entry<String, RgroupStructure> e : mapMolecules.entrySet()) {
      listMolecules.add(e.getValue().getMolecule());
    }

    return listMolecules;
  }

  /**
   * method to build a molecule from a chemical component
   *
   * @param validMonomers all valid monomers of the chemical component
   * @return Built Molecule
   * @throws BuilderMoleculeException if the polymer contains more than one
   *           monomer or if the molecule can't be built
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  private static RgroupStructure buildMoleculefromCHEM(final String id, final List<Monomer> validMonomers) throws BuilderMoleculeException, ChemistryException {
    LOG.info("Build molecule for chemical component");
    /* a chemical molecule should only contain one monomer */
    if (validMonomers.size() == 1) {
      try {
        Monomer monomer = validMonomers.get(0);
        String input = getInput(monomer);
        if (input != null) {
          /* Build monomer + Rgroup information! */

          List<Attachment> listAttachments = monomer.getAttachmentList();
          AttachmentList list = new AttachmentList();

          for (Attachment attachment : listAttachments) {
            list.add(new org.helm.chemtoolkit.Attachment(attachment.getAlternateId(), attachment.getLabel(), attachment.getCapGroupName(), attachment.getCapGroupSMILES()));
          }
          AbstractMolecule molecule = Chemistry.getInstance().getManipulator().getMolecule(input, list);
          RgroupStructure result = new RgroupStructure();
          result.setMolecule(molecule);
          result.setRgroupMap(generateRgroupMap(id + ":" + "1", molecule));
          return result;

        } else {
          LOG.error("Chemical molecule should have canonical smiles");
          throw new BuilderMoleculeException("Chemical molecule should have canoncial smiles");
        }
      } catch (NullPointerException ex) {
        throw new BuilderMoleculeException("Monomer is not stored in the monomer database");
      } catch (IOException | CTKException e) {
        LOG.error("Molecule can't be built " + e.getMessage());
        throw new BuilderMoleculeException("Molecule can't be built " + e.getMessage());
      }
    } else {
      LOG.error("Chemical molecule should contain exactly one monomer");
      throw new BuilderMoleculeException("Chemical molecule should contain exactly one monomer");
    }
  }

  /**
   * method to generate for a molecule the RgroupMap: Map of unused Rgroups
   *
   * @param detail name of the molecule
   * @param molecule input Molecule
   * @return generated RgroupMap
   * @throws CTKException
   */
  private static Map<String, IAtomBase> generateRgroupMap(final String detail, final AbstractMolecule molecule) throws CTKException {
    Map<String, IAtomBase> rgroupMap = new HashMap<String, IAtomBase>();
    for (Map.Entry<String, IAtomBase> e : molecule.getRgroups().entrySet()) {
      rgroupMap.put(detail + ":" + e.getKey(), e.getValue());
    }

    return rgroupMap;
  }

  /**
   * method to build a molecule from a Peptide or RNA component
   *
   * @param id name of the molecule
   * @param validMonomers all valid monomers of the component
   * @return generated molecule
   * @throws BuilderMoleculeException if the molecule can't be built
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  private static RgroupStructure buildMoleculefromPeptideOrRNA(final String id, final List<Monomer> validMonomers) throws BuilderMoleculeException, ChemistryException {
    try {
      String input = null;
      AbstractMolecule currentMolecule = null;
      input = getInput(validMonomers.get(0));
      AbstractMolecule prevMolecule =
          Chemistry.getInstance().getManipulator().getMolecule(input, generateAttachmentList(validMonomers.get(0).getAttachmentList()));
      AbstractMolecule firstMolecule = null;
      Monomer prevMonomer = null;

      RgroupStructure first = new RgroupStructure();
      RgroupStructure current = new RgroupStructure();

      int prev = 1;

      if (validMonomers.size() == 0 || validMonomers == null) {
        LOG.error("Polymer (Peptide/RNA) has no contents");
        throw new BuilderMoleculeException("Polymer (Peptide/RNA) has no contents");
      }
      int i = 0;
      /* First catch all IAtomBases */
      for (Monomer currentMonomer : validMonomers) {
        LOG.debug("Monomer " + currentMonomer.getAlternateId());
        i++;
        if (prevMonomer != null) {
          input = getInput(currentMonomer);
          currentMolecule = Chemistry.getInstance().getManipulator().getMolecule(input, generateAttachmentList(currentMonomer.getAttachmentList()));

          current.setMolecule(currentMolecule);
          current.setRgroupMap(generateRgroupMap(id + ":" + String.valueOf(i), currentMolecule));
          /* Backbone Connection */
          if (currentMonomer.getMonomerType().equals(Monomer.BACKBONE_MOMONER_TYPE)) {

            prevMolecule = Chemistry.getInstance().getManipulator().merge(first.getMolecule(), first.getRgroupMap().get(id + ":" + prev
                + ":R2"), current.getMolecule(), current.getRgroupMap().get(id
                    + ":" + i
                    + ":R1"));

            first.getRgroupMap().remove(id + ":" + prev + ":R2");
            current.getRgroupMap().remove(id + ":" + i + ":R1");

            first.setMolecule(prevMolecule);
            Map<String, IAtomBase> map = new HashMap<String, IAtomBase>();
            map.putAll(first.getRgroupMap());
            map.putAll(current.getRgroupMap());
            first.setRgroupMap(map);
            prev = i;

          } /* Backbone to Branch Connection */ else if (currentMonomer.getMonomerType().equals(Monomer.BRANCH_MOMONER_TYPE)) {
            prevMolecule = Chemistry.getInstance().getManipulator().merge(first.getMolecule(), first.getRgroupMap().get(id + ":" + prev
                + ":R3"), current.getMolecule(), current.getRgroupMap().get(id
                    + ":"
                    + i + ":R1"));
            first.getRgroupMap().remove(id + ":" + prev + ":R3");
            current.getRgroupMap().remove(id + ":" + i + ":R1");
            first.setMolecule(prevMolecule);
            Map<String, IAtomBase> map = new HashMap<String, IAtomBase>();
            map.putAll(first.getRgroupMap());
            map.putAll(current.getRgroupMap());
            first.setRgroupMap(map);
          } /* Unknown connection */ else {
            LOG.error("Intra connection is unknown");
            throw new BuilderMoleculeException("Intra connection is unknown");
          }
        } /* first Monomer! */ else {
          prevMonomer = currentMonomer;
          input = getInput(prevMonomer);
          prevMolecule =
              Chemistry.getInstance().getManipulator().getMolecule(input, generateAttachmentList(prevMonomer.getAttachmentList()));
          firstMolecule = prevMolecule;
          first.setMolecule(firstMolecule);
          first.setRgroupMap(generateRgroupMap(id + ":" + String.valueOf(i), firstMolecule));
        }

      }
      LOG.debug(first.getRgroupMap().keySet().toString());
      return first;
    } catch (IOException | CTKException e) {
      LOG.error("Polymer(Peptide/RNA) molecule can't be built " + e.getMessage());
      throw new BuilderMoleculeException("Polymer(Peptide/RNA) molecule can't be built " + e.getMessage());
    }
  }

  /**
   * method to generate the AttachmentList given a list of attachments
   *
   * @param listAttachments input list of Attachments
   * @return AttachmentList generated AttachmentList
   */
  private static AttachmentList generateAttachmentList(final List<Attachment> listAttachments) {
    AttachmentList list = new AttachmentList();

    for (Attachment attachment : listAttachments) {
      list.add(new org.helm.chemtoolkit.Attachment(attachment.getAlternateId(), attachment.getLabel(), attachment.getCapGroupName(), attachment.getCapGroupSMILES()));
    }
    return list;
  }

  /**
   * method to merge all unused rgroups into a molecule
   *
   * @param molecule input molecule
   * @return molecule with all merged unused rgroups
   * @throws BuilderMoleculeException if the molecule can't be built
   * @throws ChemistryException if the Chemistry Engine ca not be initialized
   */
  public static AbstractMolecule mergeRgroups(AbstractMolecule molecule) throws BuilderMoleculeException, ChemistryException {
    try {
      boolean flag = true;
      // while (flag) {
      // if (molecule.getAttachments().size() > 0) {
      for (int i = molecule.getAttachments().size() - 1; i > -1; i--) {

        org.helm.chemtoolkit.Attachment attachment = molecule.getAttachments().get(i);
        int groupId = AbstractMolecule.getIdFromLabel(attachment.getLabel());

        AbstractMolecule rMol = Chemistry.getInstance().getManipulator().getMolecule(attachment.getSmiles(), null);
        molecule = Chemistry.getInstance().getManipulator().merge(molecule, molecule.getRGroupAtom(groupId, true), rMol, rMol.getRGroupAtom(groupId, true));
      } // else {
        // flag = false;
      // }
      // }
      return molecule;
    } catch (NullPointerException | IOException | CTKException e) {
      throw new BuilderMoleculeException("Unused rgroups can't be merged into the molecule" + e.getMessage());
    }
  }

  /**
   * method to build a molecule for a given monomer
   *
   * @param monomer input monomer
   * @return generated molecule for the given monomer
   * @throws BuilderMoleculeException if the monomer can't be built
   * @throws ChemistryException if the Chemistry Engine can not be initialized
   */
  public static AbstractMolecule getMoleculeForMonomer(final Monomer monomer) throws BuilderMoleculeException, ChemistryException {
    String input = getInput(monomer);
    if (input != null) {
      List<Attachment> listAttachments = monomer.getAttachmentList();
      AttachmentList list = new AttachmentList();

      for (Attachment attachment : listAttachments) {
        list.add(new org.helm.chemtoolkit.Attachment(attachment.getAlternateId(), attachment.getLabel(), attachment.getCapGroupName(), attachment.getCapGroupSMILES()));
      }
      try {
        return Chemistry.getInstance().getManipulator().getMolecule(input, list);
      } catch (IOException | CTKException e) {
        throw new BuilderMoleculeException("Molecule can't be built for the given monomer");
      }
    }
    return null;
  }

  /**
   * @param smiles
   * @return
   * @throws ChemistryException
   * @throws CTKException
   * @throws IOException
   */
  public static AbstractMolecule getMolecule(String smiles) throws IOException, CTKException, ChemistryException {
    return Chemistry.getInstance().getManipulator().getMolecule(smiles, null);
  }

  private static String getInput(Monomer monomer) {
    String input = null;
    if (monomer.getMolfile() != null) {
      LOG.info("Use molfile for monomer generation");
      input = monomer.getMolfile();
    }
    if (input == null && monomer.getCanSMILES() != null) {
      LOG.info("Use smiles for monomer generation");
      input = monomer.getCanSMILES();
    }
    return input;
  }
}
