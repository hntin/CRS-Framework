package uit.tkorg.crs.isolatedauthor;

import java.util.ArrayList;

/**
 *
 * @author TinHuynh
 */
public class AuthorObj {
    private int _authorID;
    private String _authorName;
    private int _orgID;
    private float _importantRate;
    private float _activeScore;

    public int getAuthorID() {
        return _authorID;
    }

    public void setAuthorID(int _authorID) {
        this._authorID = _authorID;
    }

    public String getAuthorName() {
        return _authorName;
    }

    public void setAuthorName(String _authorName) {
        this._authorName = _authorName;
    }

    public int getOrgID() {
        return _orgID;
    }

    public void setOrgID(int _orgID) {
        this._orgID = _orgID;
    }

    public float getImportantRate() {
        return _importantRate;
    }

    public void setImportantRate(float _importantRate) {
        this._importantRate = _importantRate;
    }

    public float getActiveScore() {
        return _activeScore;
    }

    public void setActiveScore(float _activeScore) {
        this._activeScore = _activeScore;
    }

}
