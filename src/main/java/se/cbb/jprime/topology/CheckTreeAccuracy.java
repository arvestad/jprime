package se.cbb.jprime.topology;

import org.forester.evoinference.matrix.distance.BasicSymmetricalDistanceMatrix;
import org.forester.evoinference.matrix.distance.DistanceMatrix;

/**
 * Resolves the biojava dependency conflict between forester package V. 0.995 and V. 1.005.
 * Used in TreeConstructor class in the same package. 
 * 
 * @author Joel Sjöstrand & Raja Hashim Ali
 */
public class CheckTreeAccuracy {

    public static DistanceMatrix copyMatrix(DistanceMatrix matrix) {

        DistanceMatrix distanceMatrix = new BasicSymmetricalDistanceMatrix(matrix.getSize());
        for (int i = 0; i < matrix.getSize(); i++) {
            distanceMatrix.setIdentifier(i, matrix.getIdentifier(i));
        }

        for (int col = 0; col < matrix.getSize(); col++) {
            for (int row = 0; row < matrix.getSize(); row++) {
                distanceMatrix.setValue(col, row, matrix.getValue(col, row));
            }
        }
        return distanceMatrix;
    }
}
