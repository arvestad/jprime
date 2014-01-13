package se.cbb.jprime.apps.vmcmc.libs;

/**
 * 
 */
abstract public class MCMCMath {
	/* **************************************************************************** *
	 * 							CLASS PRIVATE FUNCTIONS								*
	 * **************************************************************************** */
	private static int calculateS(int numPoints){
		if(numPoints-1 < 2000)
			return numPoints;
		else
			return 2000;
	}
	
	private static double normalCDF(double x) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		double 			cdf;
		double 			q;
		double 			a;
		double 			a1;
		double 			a2;
		double 			a3;
		double 			a4;
		double 			a5;
		double 			a6;
		double 			a7;
		double 			b0;
		double 			b1;
		double 			b2;
		double 			b3;
		double 			b4;
		double 			b5;
		double 			b6;
		double 			b7;
		double 			b8;
		double 			b9;
		double 			b10;
		double 			b11;
		double 			y;
		double 			b;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		a 				= java.lang.Math.abs(x);
		a1				= 0.398942280444;
		a2 				= 0.399903438504;
		a3				= 5.75885480458;
		a4				= 29.8213557808;
		a5				= 2.62433121679;
		a6				= 48.6959930692;
		a7				= 5.92885724438;
		b0				= 0.398942280385;
		b1 				= 3.8052E-08;
		b2 				= 1.00000615302;
		b3 				= 3.98064794E-04;
		b4 				= 1.98615381364;
		b5 				= 0.151679116635;
		b6 				= 5.29330324926;
		b7 				= 4.8385912808;
		b8 				= 15.1508972451;
		b9 				= 0.742380924027;
		b10				= 30.789933034;
		b11				= 3.99019417011;
		
		/* ******************** FUNCTION BODY ************************************* */
		if(x <= 1.28 && x >= -1.28) {
			y 							= 0.5 * x * x;
			q 							= 0.5 - a * ( a1 - a2 * y / ( y + a3 - a4 / ( y + a5 + a6 / ( y + a7 ) ) ) );
		} else if(x <= 12.7 && x >= -12.7) {
			y		 					= 0.5 * x * x;
			b 							= java.lang.Math.exp(-y);
			q 							= b*b0/(a-b1+b2/(a+b3+b4/(a-b5+b6/(a+b7-b8/(a+b9+b10/(a+b11))))));
		} else
			q 							= 0.0;
		
		if ( x < 0.0)
			cdf 						= q;
		else
			cdf 						= 1.0 - q;
		return cdf;
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	/* **************************************************************************** *
	 * 							CLASS PUBLIC FUNCTIONS								*
	 * **************************************************************************** */	
	/**
	 * Will calculate and return the Effective Sample Size using the 
	 * series value and the difference between points in the series. 
	 * @return Effective Sample Size where convergence is met
	 */
	public static int calculateESS(final Object[] serie) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 				numPoints;
		int 				s;
		int 				k;
		int 				ess;
		float 				sum;
		float 				values1;
		float 				sum2;
		float 				mean;
		float 				gammaInit;
		float 				gammaInit1;
		float 				gammaInit2;
		float 				gammaold;
		float 				modVariance;
		float 				act;
		
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		numPoints	 		= serie.length;
		s					= calculateS(numPoints);
		sum					= 0;
		values1				= 0;
		sum2				= 0;
		final Double[] data = new Double[numPoints];
		
		/* ******************** FUNCTION BODY ************************************* */
		System.arraycopy(serie, 0, data, 0, serie.length);
		
		for(int i = 0; i < data.length; i++)
			values1	+= (Double)data[i];
		mean = values1/data.length;
		for (int j = 0; j < s; j++)
			sum = (float) (sum + ((data[j] - mean) * (data[j] - mean))); 
		gammaInit = (sum/numPoints);
		gammaInit1 = gammaInit;		
		for (int j = 1; j < (s-1); j++)
			sum2 = (float) (sum2 + ((data[j] - mean) * (data[j+1] - mean))); 
		gammaInit2 = sum2/(numPoints - 1);
		int i = 2;
		gammaold = gammaInit;
		modVariance = 0;
		while((gammaInit2 + gammaInit) > 0) {
			modVariance	= modVariance + gammaold; 
			sum = 0;
			sum2 = 0;
			k = i-1;			
			for (int j = k; j < (s-k); j++)
				sum = (float) (sum + ((data[j] - mean) * (data[j+k] - mean))); 
			for (int j = i; j < (s-i); j++)
				sum2 = (float) (sum2 + ((data[j] - mean) * (data[j+i] - mean))); 			
			gammaInit = (sum/(numPoints - k));
			gammaInit2 = (sum2/(numPoints - i));
			i++;
			gammaold = gammaInit;
		}
		modVariance	= gammaInit1 + (2 * modVariance);
		act = modVariance / gammaInit1;
		ess = (int) (numPoints / act);
		return ess;
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	public static int calculateGeweke(final Object[] serie) {
		/* ******************** FUNCTION VARIABLES ******************************** */
		int 					numPoints;
		int 					geweke;
		int 					sizeA;
		int 					sizeB;
		int 					sampleSize;
		int 					startWindow1;
		int						startWindow2;
		int 					endWindow1;
        int 					endWindow2;
		int 					samples;
		int 					samples1;
		int 					maxLag;
		int 					maxLag1;
		boolean 				convergence;
		double 					sum;
		double 					meanWindow1;
		double 					varStat;
		double 					varStat1;
        double 					standardErrorOfMeanWindow1;
        double 					varianceWindow1;
        double 					standardErrorOfMeanWindow2;
        double 					varianceWindow2;
		double 					z;
	    double 					cumulativeDistributionFunction;
        double 					meanWindow2;
        double[] 				gammaStat;
		double[] 				gammaStat1;
        
		/* ******************** VARIABLE INITIALIZERS ***************************** */
		numPoints 				= serie.length;
		geweke 					= 0;
		sizeA 					= numPoints/20;
		sizeB 					= numPoints/2;
		convergence 			= false;
		final Double[] data 	= new Double[numPoints];
		
		/* ******************** FUNCTION BODY ************************************* */
		System.arraycopy(serie, 0, data, 0, serie.length);
		while (geweke < sizeB && convergence == false) {
			sampleSize = (int) numPoints - geweke;
			startWindow1 = geweke;
			endWindow1 = (int) ((sampleSize * 0.05) + geweke);		
			sum	= 0;		
			for (int i=startWindow1; i<endWindow1; i++) 
				sum	+= data[i];	
			meanWindow1	= sum/(endWindow1-startWindow1);
			samples = endWindow1 - startWindow1;
			maxLag = (samples - 1 < 1000 ? samples - 1 : 1000);
			gammaStat = new double[maxLag];
		    // setting values to 0
		    for (int i=0; i<maxLag; i++) 
		        gammaStat[i] = 0;
		    varStat = 0.0;    
		    for (int lag = 0; lag < maxLag; lag++) {
		        for (int j = 0; j < samples - lag; j++)
		            gammaStat[lag] += ((data[startWindow1 + j] - meanWindow1) * (data[startWindow1 + j + lag] - meanWindow1));
		        gammaStat[lag] /= ((double) (samples - lag));
		        
		        if (lag == 0)
		            varStat = gammaStat[0];
		        else if (lag % 2 == 0) { // fancy stopping criterion :)
		            if (gammaStat[lag - 1] + gammaStat[lag] > 0) 
		                varStat	+= 2.0 * (gammaStat[lag - 1] + gammaStat[lag]);
		            else // stop
		            	maxLag 			= lag;
		        }
		    }
		    // standard error of mean sem and variance var
		    standardErrorOfMeanWindow1 = java.lang.Math.sqrt(varStat / samples);
		    varianceWindow1 = standardErrorOfMeanWindow1 * standardErrorOfMeanWindow1;	
		    startWindow2 = (int)(numPoints - sampleSize * 0.5);
		    endWindow2 = numPoints;
			sum = 0;
			for (int i=startWindow2; i<endWindow2; i++) 
				sum	+= data[i];
			samples1 = endWindow2 - startWindow2;
			maxLag1 = (samples1 - 1 < 1000 ? samples1 - 1 : 1000);
			meanWindow2	= sum/(endWindow2-startWindow2);
		    gammaStat1 = new double[maxLag1];
		    varStat1 = 0.0;
		    for (int i=0; i<maxLag1; i++) 
		        gammaStat1[i] = 0;		    
		    for (int lag = 0; lag < maxLag1; lag++) {
		        for (int j = 0; j < samples1 - lag; j++)
		            gammaStat1[lag] += ((data[startWindow2 + j] - meanWindow2) * (data[startWindow2 + j + lag] - meanWindow2));
		        gammaStat1[lag] /= ((double) (samples1 - lag));
		        
		        if (lag == 0) 
		            varStat1 = gammaStat1[0];
		        else if (lag % 2 == 0) { // fancy stopping criterion :)
		            if (gammaStat1[lag - 1] + gammaStat1[lag] > 0) 
		                varStat1 		+= 2.0 * (gammaStat1[lag - 1] + gammaStat1[lag]);
		            else // stop
		                maxLag1 		= lag;
		        }
		    }		    
		    // standard error of mean
		    standardErrorOfMeanWindow2 = java.lang.Math.sqrt(varStat / samples);
		    varianceWindow2 = standardErrorOfMeanWindow2 * standardErrorOfMeanWindow2;
		    z = (meanWindow1 - meanWindow2)/java.lang.Math.sqrt(varianceWindow1 + varianceWindow2);
		    cumulativeDistributionFunction = normalCDF(z);
		    convergence	= cumulativeDistributionFunction > 0.01/2.0 && cumulativeDistributionFunction < (1.0 - 0.01/2.0);
			geweke = geweke + sizeA;
		}
		if (convergence == true)
			return geweke;
		else
			return -1;
		
		/* ******************** END OF FUNCTION *********************************** */
	}
	
	public static boolean GelmanRubinTest(final Object[] serie, int burnin) {
	    int 					nBatches;
		int 					numPoints;
		int 					totalSampleSize;
	    double 					batchSize;
	    double 					potentialScaleReductionFactor;
	    double 					withinBatchVariance;
	    double 					betweenBatchVariance;
	    double 					sum;
	    double 					totalMean;
	    double	 				batchMeans[];
	    
	    /* ******************** VARIABLE INITIALIZERS ***************************** */
	    nBatches				= 10;
		numPoints               = serie.length;
		totalSampleSize 	    = numPoints - burnin;
		batchSize  		        = totalSampleSize / (double)nBatches;
	    withinBatchVariance     = 0;
	    betweenBatchVariance	= 0;
	    sum						= 0;
	    batchMeans  			= new double[nBatches];
	    final Double[] data     = new Double[serie.length];
	    
	    /* ******************** FUNCTION BODY ************************************* */
		System.arraycopy(serie, 0, data, 0, serie.length);
	    for (int i=burnin; i<numPoints; i++) 
	        sum 							+= data[i];
	    totalMean		 				= sum/(numPoints-burnin);
	    // get a mean and standard error for each block
	    for (int i=0; i<nBatches; i++) {
	        sum 						= 0;
	        for (int j=(int)(i*batchSize+burnin); j<(int)((i+1)*batchSize+burnin); j++)
	            sum 					+= data[j];        
	        batchMeans[i] 				= sum/((int)((i+1)*batchSize+burnin)-(int)(i*batchSize+burnin));
	        // iterate over all samples from the chains
	        for (int j=(int)(i*batchSize+burnin); j<(int)((i+1)*batchSize+burnin); j++) {
	            withinBatchVariance		+= ( (data[j] - batchMeans[i])*(data[j] - batchMeans[i]) );
	            betweenBatchVariance	+= ( (data[j] - totalMean)*(data[i] - totalMean) );
	        }
	    }
	    potentialScaleReductionFactor	= ((totalSampleSize-nBatches) / (totalSampleSize-1.0)) * (betweenBatchVariance/withinBatchVariance);
	    return potentialScaleReductionFactor < 1.001;
	    
	    /* ******************** END OF FUNCTION *********************************** */
	}
}