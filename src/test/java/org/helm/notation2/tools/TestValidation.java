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

import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.MonomerLoadingException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.exception.ParserException;
import org.helm.notation2.tools.HELM2NotationUtils;
import org.helm.notation2.tools.MethodsMonomerUtils;
import org.helm.notation2.tools.Validation;
import org.jdom2.JDOMException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TestValidation
 *
 * @author hecht
 */
public class TestValidation {

  @Test
  public void testValidationGrouping() throws ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30).T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{R(A)P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G1:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";
    Assert.assertTrue(Validation.validateGrouping(HELM2NotationUtils.readNotation(test)));
  }

  @Test
  public void testValidationGroupingWithException() throws ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30).T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{R(A)P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G1:45,CHEM3:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";
    test += "V2.0";
    Assert.assertFalse(Validation.validateGrouping(HELM2NotationUtils.readNotation(test)));
  }

  @Test
  public void testValidationGroupingFalseGroup() throws ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30).T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{R(A)P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G3:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";

    Assert.assertFalse(Validation.validateGrouping(HELM2NotationUtils.readNotation(test)));
  }

  @Test
  public void testvalidateUniquePolymerIDs() throws ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30).T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{R(A)P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G3:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";
    Assert.assertTrue(Validation.validateUniquePolymerIDs(HELM2NotationUtils.readNotation(test)));
  }

  @Test
  public void testvalidateUniquePolymerIDsWithException() throws ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30).T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{R(A)P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G1(G3:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";
    Assert.assertFalse(Validation.validateUniquePolymerIDs(HELM2NotationUtils.readNotation(test)));
  }

  @Test
  public void testGetMonomerCountsSimple() throws ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.X.G.C.C}|RNA1{R(A)P.(R(N)P).(R(G)P)}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G3:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";

    Assert.assertEquals(HELM2NotationUtils.getTotalMonomerCount(HELM2NotationUtils.readNotation(test)), 16);

  }

  @Test
  public void testGetMonomerCountsExtended() throws ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.C.(A.X.C)'4'}|RNA1{R(A)P.(R(N)P).(RP)}|CHEM1{?}|BLOB1{BEAD}\"Animated Polystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G3:45,CHEM1:55)${\"Name\":\"lipid nanoparticle with RNA payload and peptide ligand\"}$";

    test += "V2.0";

    Assert.assertEquals(HELM2NotationUtils.getTotalMonomerCount(HELM2NotationUtils.readNotation(test)), 27);
  }

  @Test
  public void testConnectionRNA() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "RNA1{[sP].R(C)[sP].R(U)P.R(G)P.R([dabA])P.R(G)P.R(A)P.R(G)P.R(G)P.[dR](G)P.R(U)}|RNA2{R(A)P.R(C)P.R(C)P.R(C)P.R(U)P.R(C)P.R(U)P.R(C)P.R(A)P.R(G)}$RNA1,RNA2,9:pair-23:pair|RNA1,RNA2,6:pair-26:pair|RNA1,RNA2,21:pair-11:pair|RNA1,RNA2,15:pair-17:pair|RNA1,RNA2,12:pair-20:pair|RNA1,RNA2,24:pair-8:pair|RNA1,RNA2,30:pair-2:pair|RNA1,RNA2,18:pair-14:pair|RNA1,RNA2,27:pair-5:pair|RNA1,RNA2,3:pair-29:pair$$$";

    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnection() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{F.L.C'3'}|PEPTIDE2{C.D}$PEPTIDE2,PEPTIDE1,1:R3-4:R3$$$";
    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionMap() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "RNA1{R(U)P.R(T)P.R(G)P.R(C)P.R(A)}$$$$";

    test += "V2.0";
    HELM2NotationUtils.readNotation(test).getListOfPolymers().get(0).initializeMapOfMonomersAndMapOfIntraConnection();

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionFalse() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "RNA1{R(U)P.R(T)P.R(G)P.R(C)P.R(A)}|RNA2{R(U)P.R(G)P.R(C)P.R(A)P.R(A)}$RNA1,RNA2,14:pair-2:pair|RNA1,RNA2,11:pair-5:pair|RNA1,RNA2,2:pair-14:pair|RNA1,RNA2,8:pair-14:pair|RNA1,RNA2,5:pair-11:pair$$$";

    test += "V2.0";

    Assert.assertFalse(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2Simple() throws NotationException, ChemistryException, ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,CHEM1,C:R3-1:R1\"Specific Conjugation\"$$$";

    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2SimpleWithException() throws NotationException, ChemistryException, ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,CHEM1,G:R3-1:R1\"Specific Conjugation\"$$$";

    test += "V2.0";

    Assert.assertFalse(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2SimpleWithExceptio() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,CHEM1,C:R3-1:R1\"Specific Conjugation\"$$$";

    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2Extended() throws NotationException, ChemistryException, ParserException, JDOMException {
    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{C.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,PEPTIDE2,(C,D):R3-1:R3\"Specific Conjugation\"$$$";
    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2ExtendedWithException() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,CHEM1,(C,P):?-1:?\"Specific Conjugation\"$$$";

    test += "V2.0";

    Assert.assertFalse(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testConnectionHELM2Extended2() throws NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.A.C.C.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.D.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E.E}|PEPTIDE2{G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.G.C.S.S.S.S.S.S.S.S.S.P.P.P.P.P.P.P.P.P.K.K.K.K.K.K.K.K.K.K.K.K.K}|CHEM1{[SMPEG2]}$PEPTIDE1,CHEM1,(C,D):?-1:?\"Specific Conjugation\"$$$";

    test += "V2.0";

    Assert.assertTrue(Validation.validateConnections(HELM2NotationUtils.readNotation(test)));

  }

  @Test
  public void testMonomerValidation() throws MonomerLoadingException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,G:30,R:30)'4'\"Group is repeated\".T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{[am6]P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"AnimatedPolystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G1:45,CHEM1:55)${\"Name\":\"lipidnanoparticle with RNA payload and peptide ligand\"}$V2.0";

    Assert.assertTrue(Validation.validateMonomers(MethodsMonomerUtils.getListOfMonomerNotation(HELM2NotationUtils.readNotation(test).getListOfPolymers())));

  }

  @Test
  public void testMonomerValidationCHEM() throws MonomerLoadingException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "CHEM1{[AZE]}$$$$";

    Validation.validateMonomers(MethodsMonomerUtils.getListOfMonomerNotation(HELM2NotationUtils.readNotation(test).getListOfPolymers()));

  }

  @Test
  public void testMonomerValidationWithException() throws MonomerLoadingException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{U.X.G.C.(_,N).(A:10,G:30,R:30)'4'\"Group is repeated\".T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{[am6]P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"AnimatedPolystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G1:45,CHEM1:55)${\"Name\":\"lipidnanoparticle with RNA payload and peptide ligand\"}$V2.0";

    Assert.assertFalse(Validation.validateMonomers(MethodsMonomerUtils.getListOfMonomerNotation(HELM2NotationUtils.readNotation(test).getListOfPolymers())));

  }

  @Test
  public void testMonomerValidationWithException2() throws MonomerLoadingException, org.helm.notation2.parser.exceptionparser.NotationException, ChemistryException, ParserException, JDOMException {

    String test =
        "PEPTIDE1{A.X.G.C.(_,N).(A:10,U:30,R:30)'4'\"Group is repeated\".T.C.F.D.W\"mutation\".(A:?+G:1.5).C}|RNA1{[am6]P.(R(N)P)'4'.(R(G)P)'3-7'\"mutation\"}|CHEM1{?}|BLOB1{BEAD}\"AnimatedPolystyrene\"$PEPTIDE1,BLOB1,X:R3-?:?\"Specific Conjugation\"|PEPTIDE1,CHEM1,(A+T):R3-?:?|PEPTIDE1,PEPTIDE1,(4,8):pair-12:pair$G1(PEPTIDE1:1+RNA1:2.5-2.7+BLOB1)|G2(G1:45,CHEM1:55)${\"Name\":\"lipidnanoparticle with RNA payload and peptide ligand\"}$V2.0";
    

    Assert.assertFalse(Validation.validateMonomers(MethodsMonomerUtils.getListOfMonomerNotation(HELM2NotationUtils.readNotation(test).getListOfPolymers())));

  }

}
