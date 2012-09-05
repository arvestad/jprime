package se.cbb.jprime.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.biojava3.core.sequence.MultipleSequenceAlignment;
import org.biojava3.core.sequence.template.AbstractSequence;
import org.biojava3.core.sequence.template.Compound;
import org.biojava3.phylo.Comparison;
import org.biojava3.phylo.NJTreeProgressListener;
import org.biojava3.phylo.ResidueProperties;
import org.biojava3.phylo.ScoreMatrix;
import org.biojava3.phylo.TreeConstructionAlgorithm;
import org.biojava3.phylo.TreeType;


import org.forester.io.writers.PhylogenyWriter;
import org.forester.phylogeny.Phylogeny;
import org.forester.evoinference.matrix.distance.BasicSymmetricalDistanceMatrix;
import org.forester.evoinference.matrix.distance.DistanceMatrix;
import org.forester.evoinference.distance.NeighborJoining;

/**
 * Tree constructor uses the forrester tree library to build phylogenetic trees using neighbor joining algorithm. The distance matrix
 * is calculated using code from JalView.
 * 
 * Originally implemented in forrester V 0.995. Causes conflict between V. 0.995 and 1.005. 
 * Updated and added for resolving this conflict.
 *
 * @author Scooter Willis, Joel Sjöstrand and Raja Hashim Ali
 */
public class TreeConstructor<C extends AbstractSequence<D>, D extends Compound> extends Thread {

    TreeType treeType;
    TreeConstructionAlgorithm treeConstructionAlgorithm;
    NJTreeProgressListener treeProgessListener;
    MultipleSequenceAlignment<C, D> multipleSequenceAlignment = new MultipleSequenceAlignment<C, D>();

    public TreeConstructor(MultipleSequenceAlignment<C, D> multipleSequenceAlignment, TreeType _treeType, TreeConstructionAlgorithm _treeConstructionAlgorithm, NJTreeProgressListener _treeProgessListener) {
        treeType = _treeType;
        treeConstructionAlgorithm = _treeConstructionAlgorithm;
        treeProgessListener = _treeProgessListener;
        this.multipleSequenceAlignment = multipleSequenceAlignment;
    }

    private double[][] calculateDistanceMatrix(MultipleSequenceAlignment<C, D> multipleSequenceAlignment, TreeConstructionAlgorithm tca) {
        updateProgress("Determing Distances", 0);
        int numberOfSequences = multipleSequenceAlignment.getSize();
        String[] sequenceString = new String[numberOfSequences];
        for (int i = 0; i < multipleSequenceAlignment.getSize(); i++) {
            sequenceString[i] = multipleSequenceAlignment.getAlignedSequence(i).getSequenceAsString();

        }


        double[][] distance = new double[numberOfSequences][numberOfSequences];

        int totalloopcount = (numberOfSequences / 2) * (numberOfSequences + 1);

        if (tca == TreeConstructionAlgorithm.PID) {
            int loopcount = 0;
            for (int i = 0; i < (numberOfSequences - 1); i++) {
                updateProgress("Determining Distances", (loopcount * 100) / totalloopcount);
                for (int j = i; j < numberOfSequences; j++) {
                    loopcount++;
                    if (j == i) {
                        distance[i][i] = 0;
                    } else {
                        distance[i][j] = 100 - Comparison.PID(sequenceString[i], sequenceString[j]);

                        distance[j][i] = distance[i][j];
                    }
                }
            }
        } else {
            // Pairwise substitution score (with no gap penalties)
            ScoreMatrix pwmatrix = ResidueProperties.getScoreMatrix(treeConstructionAlgorithm.name());
            if (pwmatrix == null) {
                pwmatrix = ResidueProperties.getScoreMatrix(TreeConstructionAlgorithm.BLOSUM62.name());
            }
            int maxscore = 0;
            int end = sequenceString[0].length();
            int loopcount = 0;
            for (int i = 0; i < (numberOfSequences - 1); i++) {
                updateProgress("Determining Distances", (loopcount * 100) / totalloopcount);
                for (int j = i; j < numberOfSequences; j++) {
                    int score = 0;
                    loopcount++;
                    for (int k = 0; k < end; k++) {
                        try {
                            score += pwmatrix.getPairwiseScore(sequenceString[i].charAt(k), sequenceString[j].charAt(k));
                        } catch (Exception ex) {
                            System.err.println("err creating BLOSUM62 tree");
                            ex.printStackTrace();
                        }
                    }

                    distance[i][j] = (float) score;

                    if (score > maxscore) {
                        maxscore = score;
                    }
                }
            }

            for (int i = 0; i < (numberOfSequences - 1); i++) {
                for (int j = i; j < numberOfSequences; j++) {
                    distance[i][j] = (float) maxscore - distance[i][j];
                    distance[j][i] = distance[i][j];
                }
            }

        }
        updateProgress("Determining Distances", 100);

        return distance;
    }

/*    public void cancel() {
        //    if (njtree != null) {
        //        njtree.cancel();
        //    }
    }*/
    boolean verbose = false;
    Phylogeny p = null;
    BasicSymmetricalDistanceMatrix matrix = null;
    DistanceMatrix copyDistanceMatrix = null;

    public void process() throws Exception {


        if (matrix == null) {
            double[][] distances = calculateDistanceMatrix(multipleSequenceAlignment, treeConstructionAlgorithm);
            matrix = new BasicSymmetricalDistanceMatrix(multipleSequenceAlignment.getSize());
            for (int i = 0; i < matrix.getSize(); i++) {
                matrix.setIdentifier(i, multipleSequenceAlignment.getAlignedSequence(i).getAccession().getID());
            }
            for (int col = 0; col < matrix.getSize(); col++) {
                for (int row = 0; row < matrix.getSize(); row++) {
                    matrix.setValue(col, row, distances[col][row]);

                }
            }
            copyDistanceMatrix = CheckTreeAccuracy.copyMatrix(matrix);
        }

        final List<Phylogeny> ps = new ArrayList<Phylogeny>();
        final NeighborJoining nj = NeighborJoining.createInstance();
        //nj.setVerbose(verbose);

        ps.add(nj.execute(matrix));
        p = ps.get(0);

    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String getNewickString(boolean simpleNewick, boolean writeDistanceToParent) throws Exception {
        final PhylogenyWriter w = new PhylogenyWriter();
        StringBuffer newickString = w.toNewHampshire(p, simpleNewick, writeDistanceToParent);
        return newickString.toString();
    }
    Vector<NJTreeProgressListener> progessListenerVector = new Vector<NJTreeProgressListener>();

    public void addProgessListener(NJTreeProgressListener treeProgessListener) {
        if (treeProgessListener != null) {
            progessListenerVector.add(treeProgessListener);
        }
    }

    public void removeProgessListener(NJTreeProgressListener treeProgessListener) {
        if (treeProgessListener != null) {
            progessListenerVector.remove(treeProgessListener);
        }
    }

    public void broadcastComplete() {
        for (NJTreeProgressListener treeProgressListener : progessListenerVector) {
            treeProgressListener.complete(this);
        }
    }

    public void updateProgress(String state, int percentage) {
        for (NJTreeProgressListener treeProgressListener : progessListenerVector) {
            treeProgressListener.progress(this, state, percentage);
        }
    }

    public void updateProgress(String state, int currentCount, int totalCount) {
        for (NJTreeProgressListener treeProgressListener : progessListenerVector) {
            treeProgressListener.progress(this, state, currentCount, totalCount);
        }
    }
}

