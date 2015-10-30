/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import uit.tkorg.crs.model.Sample;

/**
 *
 * @author thucnt
 */
public abstract class FeatureComputation {

    protected Sample positiveSample;
    protected Sample negativeSample;

    /**
     * Get the value of negativeSample
     *
     * @return the value of negativeSample
     */
    public Sample getNegativeSample() {
        return negativeSample;
    }

    /**
     * Set the value of negativeSample
     *
     * @param negativeSample new value of negativeSample
     */
    public void setNegativeSample(Sample negativeSample) {
        this.negativeSample = negativeSample;
    }

    /**
     * Get the value of positiveSample
     *
     * @return the value of positiveSample
     */
    public Sample getPositiveSample() {
        return positiveSample;
    }

    /**
     * Set the value of positiveSample
     *
     * @param positiveSample new value of positiveSample
     */
    public void setPositiveSample(Sample positiveSample) {
        this.positiveSample = positiveSample;
    }

    public abstract void computeFeatureValues(String outputFile);
}
