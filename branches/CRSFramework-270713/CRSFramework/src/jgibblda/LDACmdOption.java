package jgibblda;

import org.kohsuke.args4j.*;

public class LDACmdOption {
	
	@Option(name="-est", usage="Specify whether we want to estimate model from scratch")
	public boolean est = false;
	
	@Option(name="-estc", usage="Specify whether we want to continue the last estimation")
	public boolean estc = false;
	
	@Option(name="-inf", usage="Specify whether we want to do inference")
	public boolean inf = true;
	
	@Option(name="-dir", usage="Specify directory")
	public String dir = "";
	
	@Option(name="-dfile", usage="Specify data file")
	public String dfile = "";
	
	@Option(name="-model", usage="Specify the model name")
	public String modelName = "";
	
	@Option(name="-alpha", usage="Specify alpha")
	public double alpha = -1.0;
	
	@Option(name="-beta", usage="Specify beta")
	public double beta = -1.0;
	
	@Option(name="-ntopics", usage="Specify the number of topics")
	public int K = 100;
	
	@Option(name="-niters", usage="Specify the number of iterations")
	public int niters = 1000;
	
	@Option(name="-savestep", usage="Specify the number of steps to save the model since the last save")
	public int savestep = 100;
	
	@Option(name="-twords", usage="Specify the number of most likely words to be printed for each topic")
	public int twords = 100;
	
	@Option(name="-withrawdata", usage="Specify whether we include raw data in the input")
	public boolean withrawdata = false;
	
	@Option(name="-wordmap", usage="Specify the wordmap file")
	public String wordMapFileName = "wordmap.txt";

    public boolean isEst() {
        return est;
    }

    public void setEst(boolean est) {
        this.est = est;
    }

    public boolean isEstc() {
        return estc;
    }

    public void setEstc(boolean estc) {
        this.estc = estc;
    }

    public boolean isInf() {
        return inf;
    }

    public void setInf(boolean inf) {
        this.inf = inf;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDfile() {
        return dfile;
    }

    public void setDfile(String dfile) {
        this.dfile = dfile;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public int getK() {
        return K;
    }

    public void setK(int K) {
        this.K = K;
    }

    public int getNiters() {
        return niters;
    }

    public void setNiters(int niters) {
        this.niters = niters;
    }

    public int getSavestep() {
        return savestep;
    }

    public void setSavestep(int savestep) {
        this.savestep = savestep;
    }

    public int getTwords() {
        return twords;
    }

    public void setTwords(int twords) {
        this.twords = twords;
    }

    public boolean isWithrawdata() {
        return withrawdata;
    }

    public void setWithrawdata(boolean withrawdata) {
        this.withrawdata = withrawdata;
    }

    public String getWordMapFileName() {
        return wordMapFileName;
    }

    public void setWordMapFileName(String wordMapFileName) {
        this.wordMapFileName = wordMapFileName;
    }
       
}
