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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.helm.notation2.exception.EncoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MolfileEncoder {

  private static final Logger LOG = LoggerFactory.getLogger(MolfileEncoder.class);

  public static String encode(String string) throws EncoderException {
    String result = null;
    if (null != string) {
      result = compress(string);
      return result;
    } else {
      return null;
    }
  }

  public static String decode(String encodedString) throws EncoderException {
    String result = null;
    if (null != encodedString) {
      result = decompress(encodedString);
      return result;
    } else {
      return null;
    }
  }

  /**
   * method to compress the given molfile in a gezipped Base64 string
   *
   * @param str given molfile
 * @throws EncoderException 
   */
  private static String compress(String str) throws EncoderException {
	ByteArrayOutputStream rstBao = null;
	GZIPOutputStream zos = null;
    try {
    	rstBao = new ByteArrayOutputStream();
    	zos = new GZIPOutputStream(rstBao);
    	zos.write(str.getBytes());
    	IOUtils.closeQuietly(zos);
	    
    	byte[] bytes = rstBao.toByteArray();
    	return Base64.encodeToString(bytes,false);
    } catch(Exception e){
    	throw new EncoderException("Molfile could not be compressed. " + str);
   } finally{
	   IOUtils.closeQuietly(zos);
   }
	  
  }

  /**
   * method to decompress the given molfile input
   *
   * @param str the molefile input can be in base64 format or in the gzipped
   *          Base64 format
   * @return molfile
   * @throws EncoderException
   */
  private static String decompress(String str) throws EncoderException {
    /* First base64 decode the string */
    String result = null;
    byte[] bytes = Base64.decode(str);
    GZIPInputStream zi = null;
    try {
      zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
      InputStreamReader reader = new InputStreamReader(zi);
      BufferedReader in = new BufferedReader(reader);
      StringBuilder sb = new StringBuilder();
      String read;
      while ((read = in.readLine()) != null) {
        sb.append(read + "\n");
      }

      String molfile = sb.toString();
      reader.close();
      in.close();
      zi.close();
      return molfile;

    } catch (IOException e) {
      throw new EncoderException("Molfile could not be decompressed. " + str);
    } finally {
      IOUtils.closeQuietly(zi);
    }

  }
}
