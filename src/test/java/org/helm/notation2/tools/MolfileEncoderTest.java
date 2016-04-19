/*******************************************************************************
 * Copyright C 2012, The Pistoia Alliance
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
 ******************************************************************************/
package org.helm.notation2.tools;

import org.helm.notation2.exception.EncoderException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * MolfileEncoderTest
 *
 * @author hecht
 *
 */
public class MolfileEncoderTest {

  @Test
  public void TestMolfileEncoder() throws EncoderException {
    String gzippedBase64 =
        "H4sIAAAAAAAAAKWUu27DMAxFd30FgXatQFJPzk2RKUmRoXvHLh069PsryonlWB4SlaBh60o64EOyATh8/vx+fQNgJilPCMg7mM0YIKcOuPBmIgIfjIimDF6cjZiC6mwxJa9faMsswivcIra9Urx1TKJ7ycYYcJzCl73Z4yClZpSmvZ5HM2KbxdElFif5HxS81sUPUyjl1iMeo5D10bdY4iaF76K0uiwpx/tjQZu8tIwGO10pbqIIMi0op0co6ALNdVmel/PTYz2K86mjEQrV+lOvxlVjTB27Xi2S7wlFCv3aULnrtUVKvZqvKq1U2VpL2KlFIupiUHUjY6l/r1v1AHDev08F0gugb2096czbcWfMczHzB/nXwLMeBQAA";

    String molfile = MolfileEncoder.decode(gzippedBase64);
    String base64 = MolfileEncoder.encode(molfile);
    Assert.assertEquals(molfile, MolfileEncoder.decode(base64));

    molfile = "\n  Marvin  08200815002D          \n\n"
        + " 10  9  0  0  0  0            999 V2000\n"
        + "   -2.7541    2.1476    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -3.4686    0.9102    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -3.4686    1.7352    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -4.1830    2.1477    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -2.0397    1.7351    0.0000 C   0  0  1  0  0  0  0  0  0  0  0  0\n"
        + "   -1.3250    2.1476    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -1.3250    2.9726    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -2.0397    0.9101    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -0.6105    1.7350    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "   -2.7542    0.4976    0.0000 R#  0  0  0  0  0  0  0  0  0  0  0  0\n"
        + "  3  1  1  0  0  0  0\n  5  1  1  1  0  0  0\n  3  2  1  0  0  0  0\n"
        + "  3  4  1  0  0  0  0\n  5  8  1  0  0  0  0\n  5  6  1  0  0  0  0\n"
        + "  6  7  2  0  0  0  0\n  6  9  1  0  0  0  0\n  8 10  1  0  0  0  0\n"
        + "M  RGP  2   9   2  10   1\nM  END\n";

    base64 = MolfileEncoder.encode(molfile);
    System.out.println(MolfileEncoder.decode(base64));
    Assert.assertEquals(molfile, MolfileEncoder.decode(base64));

    molfile =
        "H4sIAAAAAAAAAKWSuw7CMAxF93yFJVhrOc57poipBXVgZ2RhYOD7SYIg6UOiCCtS1Hudo1snAqC73B/XGwBZNuSlZsstfEoIAAdgo1+tUiEEODMRifRlUHsfe6FRSNpz0gijS7CDMWJ5ZYpFJiVfFG3JL1Lkd0qQrmRRf2RJf9xoZG/qLP0vlJjFJQojs6qzHNdTHFrFZS6hogyb1ZRyRxptGM1lNYXz/GV9C1HVb2Paq+ZqlEz2pqqd9+r8BMdqBzAcTpmQjuQ9DVgmZ9+3QmxjiSe9Zxcz4AIAAA==";

    System.out.println(MolfileEncoder.decode(molfile));
  }

}
