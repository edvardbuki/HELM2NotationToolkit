/*--
 *
 * @(#) MoleculeInfo.java
 *
 *
 */
package org.helm.notation2;

/**
 * MoleculeInfo
 *
 * @author hecht
 */
public class MoleculeProperty {
  private double molecularWeight;

  private String molecularFormula;

  private double exactMass;

  private float extinctionCoefficient;

  public double getMolecularWeight() {
    return molecularWeight;
  }

  public void setMolecularWeight(double molecularWeight) {
    this.molecularWeight = molecularWeight;
  }

  public String getMolecularFormula() {
    return molecularFormula;
  }

  public void setMolecularFormula(String molecularFormula) {
    this.molecularFormula = molecularFormula;
  }

  public double getExactMass() {
    return exactMass;
  }

  public void setExactMass(double exactMass) {
    this.exactMass = exactMass;
  }

  public void setExtinctionCoefficient(float extinctionCoefficient) {
    this.extinctionCoefficient = extinctionCoefficient;
  }

  public float getExtinctionCoefficient() {
    return extinctionCoefficient;
  }

}
