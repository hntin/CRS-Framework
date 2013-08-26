package uit.tkorg.crs.isolatedauthor.feature;

/**
 *
 * @author TinHuynh
 */
public class FeatureVectorObject {
    public static final int NUMBER_OF_FEATURE = 4;
    public static final String CONTENT_SIM = "contentSim";
    float contentSimValue;
    public static final String ORGANIZATION_RSS = "orgRSS";
    float orgRSSValue;    
    public static final String IMPORTANT_RATE = "importantRate";
    float importantRateValue;  
    public static final String ACTIVE_SCORE = "activeScore";
    float activeScoreValue;
    
    public static final String LABEL_CLASS = "label";
    public String labelValue;

    public float getContentSimValue() {
        return contentSimValue;
    }

    public void setContentSimValue(float contentSimValue) {
        this.contentSimValue = contentSimValue;
    }

    public float getOrgRSSValue() {
        return orgRSSValue;
    }

    public void setOrgRSSValue(float orgRSSValue) {
        this.orgRSSValue = orgRSSValue;
    }

    public float getImportantRateValue() {
        return importantRateValue;
    }

    public void setImportantRateValue(float importantRateValue) {
        this.importantRateValue = importantRateValue;
    }

    public float getActiveScoreValue() {
        return activeScoreValue;
    }

    public void setActiveScoreValue(float activeScoreValue) {
        this.activeScoreValue = activeScoreValue;
    }

    public String getLabelValue() {
        return labelValue;
    }

    public void setLabelValue(String labelValue) {
        this.labelValue = labelValue;
    }

}
