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

package org.helm.notation2;

import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;

import org.helm.notation2.calculation.ExtinctionCoefficient;
import org.helm.notation2.exception.CalculationException;
import org.helm.notation2.exception.ChemistryException;
import org.helm.notation2.exception.ExtinctionCoefficientException;
import org.helm.notation2.exception.FastaFormatException;
import org.helm.notation2.exception.MonomerException;
import org.helm.notation2.exception.NotationException;
import org.helm.notation2.exception.ParserException;
import org.helm.notation2.parser.StateMachineParser;
import org.helm.notation2.parser.notation.HELM2Notation;
import org.helm.notation2.tools.HELM2NotationUtils;
import org.helm.notation2.tools.SequenceConverter;
import org.jdom2.JDOMException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * ExtinctionCalculatorTest
 *
 * @author hecht
 */
public class ExtinctionCalculatorTest {
	StateMachineParser parser;

	@Test
	public void testCalculationOnePeptide()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils
				.readNotation("PEPTIDE1{C}|PEPTIDE2{Y.V.N.L.I}$PEPTIDE2,PEPTIDE1,5:R2-2:R3$$$V2.0");
		Float f = (float) 1.55;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_DOWN).floatValue(), f);
	}

	@Test
	public void testCalculationOneRNA()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("RNA1{P.R(A)P.R([5meC])P.R(G)P.[mR](A)}$$$$V2.0");
		Float f = (float) 46.20;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_DOWN).floatValue(), f);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationRepeatingRNA()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils
				.readNotation("RNA1{P.(R(A)P.R(G)P)'2'.R([5meC])P.R(G)P.[mR](A)}$$$$V2.0");
		Float f = (float) 80.58;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_DOWN).floatValue(), f);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationRepeatingMonomer()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{C'2'}$$$$V2.0");
		Float f = (float) 0.12;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_DOWN).floatValue(), f);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationRepeatingList()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{(F.C.F)'3'}$$$$V2.0");
		Float f = (float) 0.19;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(), f);
	}

	@Test
	public void testCalculationWithCHEMAndBlob()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("CHEM1{[MCC]}|RNA1{R(U)}|BLOB1{?}$$$$V2.0");
		Float f = (float) 10.21;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(), f);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils
				.readNotation("CHEM1{[MCC]}|RNA1{(R(U)+R(A))}|BLOB1{?}$$$$V2.0");
		Float f = (float) 10.21;
		Assert.assertEquals(BigDecimal.valueOf(ExtinctionCoefficient.getInstance().calculate(helm2notation))
				.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue(), f);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException2()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{?}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException3()
			throws ExtinctionCoefficientException, ChemistryException, ParserException, JDOMException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{A.C._}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException4()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{A.C.(_.K)}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException5()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{U}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException6()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("PEPTIDE1{?}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException7()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation("RNA1{R(N)P}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test(expectedExceptions = ExtinctionCoefficientException.class)
	public void testCalculationWithException8()
			throws ParserException, JDOMException, ExtinctionCoefficientException, ChemistryException {
		HELM2Notation helm2notation = HELM2NotationUtils.readNotation(
				"RNA1{[[H]OC[C@H]1O[C@@H]([C@H](O)[C@@H]1OP(O)(=O)OC[C@H]1O[C@@H]([C@H](O)[C@@H]1OP(O)(=O)OC[C@H]1O[C@@H]([C@H](O)[C@@H]1O[H])N1C=CC(=O)NC1=O)N1C=CC(=O)NC1=O)N1C=CC(=O)NC1=O]}$$$$V2.0");
		ExtinctionCoefficient.getInstance().calculate(helm2notation);
	}

	@Test
	public void testCalculateAminoAcidSequence()
			throws CalculationException, org.helm.notation2.parser.exceptionparser.NotationException,
			FastaFormatException, ExtinctionCoefficientException, NotationException, ChemistryException {

		/* amino acid sequence */
		String input = "AGGDDDDDDDDDDDDDDDDDDFFFFFFFFFFFFF";
		Float f = (float) 0.0;
		assertEquals(getExtinctionNewImplementationPEPTIDE(input), f);

		input = "AGGCFFFFFFFFFF";
		f = (float) 62.5;
		assertEquals(getExtinctionNewImplementationPEPTIDE(input), f);

		input = "AGGYEEEEEEEEEEEEEEEEEEE";
		f = (float) 1490.0;
		assertEquals(getExtinctionNewImplementationPEPTIDE(input), f);

		input = "AGGWEEEEEEEEEEEEEEEEEEE";
		f = (float) 5500.0;
		assertEquals(getExtinctionNewImplementationPEPTIDE(input), f);
	}

	@Test
	public void testCalculateFromPeptidePolymerNotation()
			throws NotationException, MonomerException, CalculationException, IOException, JDOMException,
			ExtinctionCoefficientException, ParserException, ChemistryException {
		String notation = "PEPTIDE1{A.G.G.W.E.E.E.E.E.W}$$$$";
		Float f = (float) 11000.0;
		assertEquals(ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(notation),
				ExtinctionCoefficient.PEPTIDE_UNIT_TYPE), f);
	}

	private Float getExtinctionNewImplementationPEPTIDE(String sequence)
			throws org.helm.notation2.parser.exceptionparser.NotationException, FastaFormatException,
			ExtinctionCoefficientException, NotationException, ChemistryException {
		float number = ExtinctionCoefficient.getInstance().calculate(SequenceConverter.readPeptide(sequence),
				ExtinctionCoefficient.PEPTIDE_UNIT_TYPE);
		return number;
	}

	@Test
	public void testCalculateFromComplexNotation() throws NotationException, MonomerException, IOException,
			JDOMException, CalculationException, ExtinctionCoefficientException, ParserException, ChemistryException {
		String input = "PEPTIDE1{A.G.G.W.E.E.E.E.E.W}$$$$";
		float newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input),
				ExtinctionCoefficient.PEPTIDE_UNIT_TYPE);
		Float f = (float) 11000.0;
		assertEquals(newResult, f);

		input = "PEPTIDE1{A.G.G.W.E.E.E.E.E.W}|PEPTIDE2{A.G.G.W.E.Y.E.E.E.E.W}$$$$";
		newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input));
		f = (float) 23.49;
		assertEquals(newResult, f);

		input = "PEPTIDE1{A.G.G.W.E.E.E.E.E.W}|PEPTIDE2{A.G.G.W.E.Y.E.E.E.E.W}$$$$";
		newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input),
				ExtinctionCoefficient.PEPTIDE_UNIT_TYPE);
		f = (float) 23490.0;
		assertEquals(newResult, f);

		input = "RNA1{P.R(A)P.R([5meC])P.R(G)P.[mR](A)}$$$$";
		newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input));
		f = (float) 46.200005;
		assertEquals(newResult, f);

		input = "RNA1{P.R(A)P.R([5meC])P.R(G)P.[mR](A)}$$$$";
		newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input),
				ExtinctionCoefficient.PEPTIDE_UNIT_TYPE);
		f = (float) 46200.004;
		assertEquals(newResult, f);

		input = "RNA1{P.R(A)P.R([5meC])P.R(G)P.[mR](A)}|CHEM1{PEG2}|PEPTIDE1{A.G.G.W.E.E.E.E.E.W}|PEPTIDE2{A.G.G.W.E.Y.E.E.E.E.W}$$$$";
		newResult = ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(input));

		f = (float) 69.69;
		assertEquals(newResult, f);

	}

	@Test
	public void testCalculateFromNucleotideSequence() throws CalculationException,
			org.helm.notation2.parser.exceptionparser.NotationException, FastaFormatException, IOException,
			JDOMException, ExtinctionCoefficientException, NotationException, ChemistryException {
		String input = "ACGTACGT";
		Float f = (float) 81.119995;
		assertEquals(getExtinctionReadRNA(input), f, 0.01);

	}

	@Test
	public void testCalculateFromModifiedNucleotideSequence() throws CalculationException, NotationException,
			IOException, JDOMException, org.helm.notation2.parser.exceptionparser.NotationException,
			FastaFormatException, ExtinctionCoefficientException, ChemistryException {
		String input = "ACGmTACmGT";
		Float f = (float) 81.119995;
		assertEquals(getExtinctionReadRNA(input), f, 0.01);
	}

	private Float getExtinctionReadRNA(String sequence)
			throws org.helm.notation2.parser.exceptionparser.NotationException, FastaFormatException, IOException,
			JDOMException, ExtinctionCoefficientException, NotationException, ChemistryException {
		HELM2Notation containerhelm2 = SequenceConverter.readRNA(sequence);
		float number = ExtinctionCoefficient.getInstance().calculate(containerhelm2);
		return number;
	}

	@Test
	public void testCalculateFromRnaPolymerNotation()
			throws ExtinctionCoefficientException, ChemistryException, ParserException, JDOMException {
		String notation = "RNA1{P.R(A)P.R(C)P.R(G)P.[mR](A)}$$$$";
		Float f = (float) 46.200005;
		assertEquals(ExtinctionCoefficient.getInstance().calculate(HELM2NotationUtils.readNotation(notation)), f);
	}

}
