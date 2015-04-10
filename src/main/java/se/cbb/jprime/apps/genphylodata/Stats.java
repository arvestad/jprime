/**
 * 
 */
package se.cbb.jprime.apps.genphylodata;

/**
 * This data structure keeps track of all the statistics during gene evolution process
 * 
 * @author Ikram Ullah
 *
 */
public class Stats {

    /* duplications events */
    int duplications;

    /* LGT events  */
    int transfers;
    
    /* loss events */
    int losses;
    
    /* speciation events */
    int speciations;
    
    /* total unpruned gene tree time */
    double totalTime;
    
    public Stats() {
        this.duplications = 0;
        this.losses = 0;
        this.transfers = 0;
        this.speciations = 0;
        this.totalTime = 0.0; 
    }
    
    public Stats(int d, int l, int t, int s, double tTime) {
        this.duplications = d;
        this.losses = l;
        this.transfers = t;
        this.speciations = s;
        this.totalTime = tTime; 
    }
    
    public int getDuplications() {
        return duplications;
    }

    public void setDuplications(int duplications) {
        this.duplications = duplications;
    }

    public int getTransfers() {
        return transfers;
    }

    public void setTransfers(int transfers) {
        this.transfers = transfers;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getSpeciations() {
        return speciations;
    }

    public void setSpeciations(int speciations) {
        this.speciations = speciations;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
}
