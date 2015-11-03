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

    protected Sample _positiveSample;
    protected Sample _negativeSample;

    /**
     * Get the value of _negativeSample
     *
     * @return the value of _negativeSample
     */
    public Sample getNegativeSample() {
        return _negativeSample;
    }

    /**
     * Set the value of _negativeSample
     *
     * @param negativeSample new value of _negativeSample
     */
    public void setNegativeSample(Sample negativeSample) {
        this._negativeSample = negativeSample;
    }

    /**
     * Get the value of _positiveSample
     *
     * @return the value of _positiveSample
     */
    public Sample getPositiveSample() {
        return _positiveSample;
    }

    /**
     * Set the value of _positiveSample
     *
     * @param positiveSample new value of _positiveSample
     */
    public void setPositiveSample(Sample positiveSample) {
        this._positiveSample = positiveSample;
    }
    
    // Calculating value of the specifed feature for both positive samples or negative samples
    public abstract void computeFeatureValues(String outputFile, int typeOfSample);

}
