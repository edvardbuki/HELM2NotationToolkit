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

import org.helm.chemtoolkit.CTKException;
import org.helm.chemtoolkit.CTKSmilesException;
import org.helm.notation2.Chemistry;
import org.helm.notation2.exception.BuilderMoleculeException;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.HELM2HandledException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.MonomerLoadingException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.exception.ParserException;
import org.helm.notation2.parser.ParserHELM2;
import org.helm.notation2.parser.exceptionparser.ExceptionState;
import org.helm.notation2.parser.notation.polymer.PolymerNotation;
import org.helm.notation2.tools.SMILES;
import org.jdom2.JDOMException;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * SMILESTest
 *
 * @author hecht
 */
public class SMILESTest {
  ParserHELM2 parser;

  @Test
  public PolymerNotation getSimpleRNANotation() throws ParserException, JDOMException {
    String notation = "RNA1{P.R(A)[sP].RP.R(G)P.[LR]([5meC])}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  public PolymerNotation getInlineSmilesModAdenine() throws ParserException, JDOMException {
    String notation = "RNA1{R(C)P.R([C[N]1=CN=C(N)C2=C1N([*])C=N2 |$;;;;;;;;;_R1;;$,c:6,11,t:1,3|])[sP].RP.R(G)P.[LR]([5meC])P}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  public PolymerNotation getSimplePeptideNotation() throws ParserException, JDOMException {
    String notation = "PEPTIDE1{G.G.K.A.A.[seC]}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  public PolymerNotation getInlineSmilesPeptideNotation() throws ParserException, JDOMException {
    String notation = "PEPTIDE1{G.G.K.A.[C[C@H](N[*])C([*])=O |$;;;_R1;;_R2;$|].[seC]}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  public PolymerNotation getSimpleChemNotation() throws ParserException, JDOMException {
    String notation = "CHEM1{PEG2}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);

  }

  public PolymerNotation getSmilesNotation() throws ParserException, JDOMException {
    String notation = "CHEM1{[*]OCCOCCOCCO[*] |$_R1;;;;;;;;;;;_R3$|}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);

  }

  public PolymerNotation getRNANotationWithInline() throws ParserException, JDOMException {
    String notation =
        "RNA1{[C[C@@]1([*])O[C@H](CO[*])[C@@H](O[*])[C@H]1O |$;;_R3;;;;;_R1;;;_R2;;$|([Cc1nc2c(N)ncnc2n1[*] |$;;;;;;;;;;;_R1$|])[O[26P]([*])([*])=O |$;;_R1;_R2;$|]].R(C)P.R(T)P.R(G)}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  public PolymerNotation getRNANotationWithSalt() throws ParserException, JDOMException {
    String notation =
        "RNA1{P.R(A)[sP].R(A)[[Na+].[O-]P([*])([*])=O |$;;;_R1;_R2;$|].[LR]([5meC])}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);

  }

  public PolymerNotation getChemNotationWithSalt() throws ParserException, JDOMException {
    String notation =
        "CHEM1{[[Na+].[O-]C1C=CC(=O)N1CC1CCC(CC1)C([*])=O |$;;;;;;;;;;;;;;;;_R1;$,c:2|]}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);

  }

  public PolymerNotation getPeptideNotationWithSalt() throws ParserException, JDOMException {
    String notation =
        "PEPTIDE1{G.G.K.[[Na+].C[C@H](N[*])C([O-])[*] |$;;;;_R1;;;_R2$|].A.[seC]}$$$$";
    return HELM2NotationUtils.readNotation(notation).getListOfPolymers().get(0);
  }

  @Test
  public void testSMILES() throws BuilderMoleculeException, CTKException, ChemistryException, ParserException, JDOMException {
    String test =
        "PEPTIDE1{D}|PEPTIDE2{E}|PEPTIDE3{L.R}|CHEM1{[MCC]}$PEPTIDE1,PEPTIDE2,1:R2-1:R3|PEPTIDE3,PEPTIDE1,2:R2-1:R3$$$V2.0";

    String smile = SMILES.getSMILESForAll(HELM2NotationUtils.readNotation(test));
    String expectedResult = "OC(=O)C1CCC(CN2C(=O)C=CC2=O)CC1.[H]N[C@@H](CCC(=O)C(=O)[C@@H](N[H])CC(=O)C(=O)[C@H](CCCNC(N)=N)NC(=O)[C@@H](N[H])CC(C)C)C(O)=O";
    if (Chemistry.getInstance().getChemistry().equals("org.helm.chemtoolkit.chemaxon.ChemaxonManipulator")) {
      Assert.assertEquals(smile, expectedResult);
    }

  }

  @Test
  public void testSMILESCanonical() throws CTKSmilesException, BuilderMoleculeException, CTKException, NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{D}|PEPTIDE2{E}|PEPTIDE3{L.R}|CHEM1{[MCC]}$PEPTIDE1,PEPTIDE2,1:R2-1:R3|PEPTIDE3,PEPTIDE1,2:R2-1:R3$$$V2.0";

    String canSmile = SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(test));
    if (Chemistry.getInstance().getChemistry().equals("org.helm.chemtoolkit.chemaxon.ChemaxonManipulator")) {
      Assert.assertEquals(canSmile, "OC(=O)C1CCC(CN2C(=O)C=CC2=O)CC1.CC(C)C[C@H](N)C(=O)N[C@@H](CCCNC(N)=N)C(=O)C(=O)C[C@H](N)C(=O)C(=O)CC[C@H](N)C(O)=O");
    }
  }

  public void testHELM1AgainstHELM2(String notation) throws BuilderMoleculeException, CTKException, ChemistryException, ParserException, JDOMException, NotationException {

    String test = notation + "V2.0";
    SMILES.getSMILESForAll(HELM2NotationUtils.readNotation(test));

    SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(test));

  }

  @Test
  public void testGetSmilesPolymer() throws CTKSmilesException, BuilderMoleculeException, HELM2HandledException, CTKException, ParserException, JDOMException, NotationException, ChemistryException {
    SMILES.getCanonicalSMILESForPolymer(getSimpleRNANotation());

    SMILES.getCanonicalSMILESForPolymer(getInlineSmilesModAdenine());

    SMILES.getCanonicalSMILESForPolymer(getSimplePeptideNotation());

    SMILES.getCanonicalSMILESForPolymer(getInlineSmilesPeptideNotation());

    SMILES.getCanonicalSMILESForPolymer(getSimpleChemNotation());

    SMILES.getCanonicalSMILESForPolymer(getSmilesNotation());
    if (Chemistry.getInstance().getChemistry().equals("org.helm.chemtoolkit.chemaxon.ChemaxonManipulator")) {
      SMILES.getCanonicalSMILESForPolymer(getRNANotationWithSalt());
    }

  }

  @Test
  public void testSelfCycle() throws ExceptionState, IOException, JDOMException, BuilderMoleculeException, CTKException, NotationException, MonomerException, ChemistryException,
      ParserException {
    // backbone cyclic peptide
    String notation = "PEPTIDE1{A.A.G.K}$PEPTIDE1,PEPTIDE1,1:R1-4:R2$$$";
    testHELM1AgainstHELM2(notation);

    // backbone cyclic RNA
    notation = "RNA1{R(C)P.RP.R(A)P.RP.R(A)P.R(U)P}$RNA1,RNA1,1:R1-16:R2$$$";
    testHELM1AgainstHELM2(notation);

    // cyclic chem
    notation = "CHEM1{SS3}|CHEM2{SS3}$CHEM1,CHEM2,1:R1-1:R1|CHEM1,CHEM2,1:R2-1:R2$$$";
    testHELM1AgainstHELM2(notation);

    // peptide-chem cycles
    notation = "PEPTIDE1{H.H.E.E.E}|CHEM1{SS3}|CHEM2{EG}$PEPTIDE1,CHEM2,5:R2-1:R2|CHEM2,CHEM1,1:R1-1:R2|PEPTIDE1,CHEM1,1:R1-1:R1$$$";
    testHELM1AgainstHELM2(notation);

    // multiple peptide-chem cycles
    notation =
        "PEPTIDE1{E.E.E.E.E}|PEPTIDE2{E.D.D.I.A.C.D.E}|CHEM1{SS3}|CHEM2{SS3}|CHEM3{SS3}$PEPTIDE2,CHEM2,8:R2-1:R1|PEPTIDE1,CHEM3,5:R2-1:R2|PEPTIDE1,CHEM1,1:R1-1:R1|PEPTIDE2,CHEM3,1:R1-1:R1|CHEM1,CHEM2,1:R2-1:R2$$$";
    testHELM1AgainstHELM2(notation);
  }

  @Test(expectedExceptions = BuilderMoleculeException.class)
  public void testChiralCenter() throws ExceptionState, IOException, JDOMException, BuilderMoleculeException, CTKException, NotationException, MonomerException,
      ChemistryException, ParserException {
    // backbone and branch cyclic RNA
    String notation = "RNA1{R(C)P.RP.R(A)P.RP.R(A)P.R(U)P}$RNA1,RNA1,4:R3-9:R3$$$";
    testHELM1AgainstHELM2(notation);

  }

  @Test
  public void testInlineNotation() throws CTKSmilesException, BuilderMoleculeException, CTKException, NotationException, ParserException, JDOMException, MonomerLoadingException, IOException,
      MonomerException, ChemistryException {

    String notation = "PEPTIDE1{A.G.G.G.C.C.K.K.K.K}|CHEM1{MCC}$PEPTIDE1,CHEM1,10:R3-1:R1$$$";
    String smiles = SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(notation));

    // replaced A with Smiles String
    notation = "PEPTIDE1{[C[C@H](N[*])C([*])=O |$;;;_R1;;_R2;$|].G.G.G.C.C.K.K.K.K}|CHEM1{MCC}$PEPTIDE1,CHEM1,10:R3-1:R1$$$";
    String smilesInline = SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(notation));
    AssertJUnit.assertEquals(smiles, smilesInline);

    // replaced A with slightly modified A
    notation = "PEPTIDE1{[C[C@H](N[*])C(=O)C[*] |$;;;_R1;;;;_R2$|].G.G.G.C.C.K.K.K.K}|CHEM1{MCC}$PEPTIDE1,CHEM1,10:R3-1:R1$$$";

    System.out.println(SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(notation)));

  }

  @Test
  public void testGetGenericStructure() throws HELM2HandledException, ChemistryException, IOException, CTKException, ParserException, JDOMException {
    String notation = "CHEM1{[SS3]}|CHEM2{[SS3]}$CHEM1,CHEM2,1:R1-1:R1|CHEM1,CHEM2,1:R2-1:R2$$$";
    SMILES.containsGenericStructurePolymer(HELM2NotationUtils.readNotation(notation).getListOfPolymers());
  }
  
  @Test
  public void testAttachmentMonomer() throws CTKSmilesException, BuilderMoleculeException, CTKException, NotationException, ChemistryException, ParserException, JDOMException{
	  String notation = "PEPTIDE1{A.A.C.G.[dK].E.C.H.A}$PEPTIDE1,PEPTIDE1,3:R3-7:R3$$$";
	  System.out.println(SMILES.getCanonicalSMILESForAll(HELM2NotationUtils.readNotation(notation)));
  }

}
