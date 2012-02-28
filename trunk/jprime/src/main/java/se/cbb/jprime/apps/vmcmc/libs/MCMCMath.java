package se.cbb.jprime.apps.vmcmc.libs;

/**
 * 
 */
public class MCMCMath {
	/*
	 * calculateESS: Will calculate and return the Estimated Sample Size using the 
	 * series value and the difference between points in the series. 
	 */
	public int calculateESS(final Object[] serie)
	{
		int s, numPoints = serie.length, k;	
		float sum, values1, sum2, result1, gammaInit, gammaInit1, gammaInit2, gammaold, modVariance;
		final Double[] data1 = new Double[serie.length];
		
		System.arraycopy(serie, 0, data1, 0, serie.length);
		s = calculateS(numPoints);
		sum = 0;
		values1 = 0;
		sum2 = 0;
		
		for(int i = 0; i < data1.length; i++)
			values1+= (Double)data1[i];

		result1 = values1/data1.length;
		
		for (int j = 0; j < s; j++)
			sum = (float) (sum + ((data1[j] - result1) * (data1[j] - result1))); 
		
		gammaInit = (sum/numPoints);
		gammaInit1 = gammaInit;
		
		for (int j = 1; j < (s-1); j++)
			sum2 = (float) (sum2 + ((data1[j] - result1) * (data1[j+1] - result1))); 
		
		gammaInit2 = (sum2/(numPoints - 1));
		int i = 2;
		gammaold = gammaInit;
		modVariance = 0;
				
		while((gammaInit2 + gammaInit) > 0)
		{
			modVariance = modVariance + gammaold; 
			sum = 0;
			sum2 = 0;
			k = i-1;
			
			for (int j = k; j < (s-k); j++)
				sum = (float) (sum + ((data1[j] - result1) * (data1[j+k] - result1))); 
			
			for (int j = i; j < (s-i); j++)
				sum2 = (float) (sum2 + ((data1[j] - result1) * (data1[j+i] - result1))); 
			
			gammaInit = (sum/(numPoints - k));
			gammaInit2 = (sum2/(numPoints - i));
			i = i + 1;
			gammaold = gammaInit;
		}
		modVariance = gammaInit1 + (2 * modVariance);
		float act = modVariance / gammaInit1;
		int ess = (int) (numPoints / act);
		return ess;
	}

	private int calculateS(int numPoints){
		if(numPoints-1 < 2000)
			return numPoints;
		else
			return 2000;
	}
	
	public int calculateGeweke(final Object[] serie)
	{
		int numPoints = serie.length, sizeA, sizeB, geweke = 0;
		boolean convergence = false;
		final Double[] data = new Double[serie.length];
		double p = 0.01;
		
		System.arraycopy(serie, 0, data, 0, serie.length);
		
		sizeA = numPoints/20;
		sizeB = numPoints/2;
		
		while (geweke < sizeB && convergence == false)
		{
			int sampleSize = (int) numPoints - geweke;
			int startWindow1 = geweke;
			int endWindow1 = (int) ((sampleSize * 0.05) + geweke);
			
			double m = 0;
			for (int i=startWindow1; i<endWindow1; i++) 
			{
				m += data[i];
			}
			double meanWindow1 = m/(endWindow1-startWindow1);
			
			int samples = endWindow1 - startWindow1;
			int maxLag = (samples - 1 < 1000 ? samples - 1 : 1000);
			
		    double[] gammaStat = new double[maxLag];
		    // setting values to 0
		    for (int i=0; i<maxLag; i++) {
		        gammaStat[i] = 0;
		    }
		    double varStat = 0.0;
		    
		    for (int lag = 0; lag < maxLag; lag++) {
		        for (int j = 0; j < samples - lag; j++) {
		            double del1 = data[startWindow1 + j] - meanWindow1;
		            double del2 = data[startWindow1 + j + lag] - meanWindow1;
		            gammaStat[lag] += (del1 * del2);
		        }
		        
		        gammaStat[lag] /= ((double) (samples - lag));
		        
		        if (lag == 0) {
		            varStat = gammaStat[0];
		        } else if (lag % 2 == 0) {
		            // fancy stopping criterion :)
		            if (gammaStat[lag - 1] + gammaStat[lag] > 0) {
		                varStat += 2.0 * (gammaStat[lag - 1] + gammaStat[lag]);
		            }
		            // stop
		            else
		                maxLag = lag;
		        }
		    }
		    
		    // standard error of mean
		    double semWindow1 = java.lang.Math.sqrt(varStat / samples);
			double varWindow1 = semWindow1 * semWindow1;
			
		    int startWindow2    = (int)(numPoints - sampleSize * 0.5);
		    int endWindow2      = numPoints;
		    
			m = 0;
			for (int i=startWindow2; i<endWindow2; i++) 
			{
				m += data[i];
			}
			double meanWindow2 = m/(endWindow2-startWindow2);
			
			int samples1 = endWindow2 - startWindow2;
			int maxLag1 = (samples1 - 1 < 1000 ? samples1 - 1 : 1000);
			
		    double[] gammaStat1 = new double[maxLag1];
		    for (int i=0; i<maxLag1; i++) {
		        gammaStat1[i] = 0;
		    }
		    double varStat1 = 0.0;
		    
		    for (int lag = 0; lag < maxLag1; lag++) {
		        for (int j = 0; j < samples1 - lag; j++) {
		            double del1 = data[startWindow2 + j] - meanWindow2;
		            double del2 = data[startWindow2 + j + lag] - meanWindow2;
		            gammaStat1[lag] += (del1 * del2);
		        }
		        
		        gammaStat1[lag] /= ((double) (samples1 - lag));
		        
		        if (lag == 0) {
		            varStat1 = gammaStat1[0];
		        } else if (lag % 2 == 0) {
		            // fancy stopping criterion :)
		            if (gammaStat1[lag - 1] + gammaStat1[lag] > 0) {
		                varStat1 += 2.0 * (gammaStat1[lag - 1] + gammaStat1[lag]);
		            }
		            // stop
		            else
		                maxLag1 = lag;
		        }
		    }
		    
		    // standard error of mean
		    double semWindow2 = java.lang.Math.sqrt(varStat / samples);
			double varWindow2 = semWindow2 * semWindow2;
		    
		    double z            = (meanWindow1 - meanWindow2)/java.lang.Math.sqrt(varWindow1 + varWindow2);
		    double cdf          = normalCDF(z);
		    
		    convergence = cdf > p/2.0 && cdf < (1.0 - p/2.0);
			
			geweke = geweke + sizeA;
			
		}
		
		if (convergence == true)
			return geweke;
		else
			return -1;
	}
	
	private double normalCDF(double x)
	{
		double cdf;
		double q;
		double a = java.lang.Math.abs(x);
		
		if(x <= 1.28 && x >= -1.28)
		{
			double a1 = 0.398942280444;
			double a2 = 0.399903438504;
			double a3 = 5.75885480458;
			double a4 = 29.8213557808;
			double a5 = 2.62433121679;
			double a6 = 48.6959930692;
			double a7 = 5.92885724438;
			double y = 0.5 * x * x;
			
			q = 0.5 - a * ( a1 - a2 * y / ( y + a3 - a4 / ( y + a5 + a6 / ( y + a7 ) ) ) );
		}
		else if(x <= 12.7 && x >= -12.7)
		{
			double b0 = 0.398942280385;
			double b1 = 3.8052E-08;
			double b2 = 1.00000615302;
			double b3 = 3.98064794E-04;
			double b4 = 1.98615381364;
			double b5 = 0.151679116635;
			double b6 = 5.29330324926;
			double b7 = 4.8385912808;
			double b8 = 15.1508972451;
			double b9 = 0.742380924027;
			double b10 = 30.789933034;
			double b11 = 3.99019417011;
			double y = 0.5 * x * x;
			double b = java.lang.Math.exp(-y);
			
			q = b*b0/(a-b1+b2/(a+b3+b4/(a-b5+b6/(a+b7-b8/(a+b9+b10/(a+b11))))));
		}
		else
			q = 0.0;
		
		if ( x < 0.0)
			cdf = q;
		else
			cdf = 1.0 - q;
		return cdf;
	}
	
	public boolean GelmanRubinTest(final Object[] serie, int burnin) 
	{
	    double  withinBatchVariance             = 0;
	    double  betweenBatchVariance            = 0;
	    int nBatches							= 10;
	    
		int numPoints                           = serie.length;
		final Double[] data                     = new Double[serie.length];
		
		int     totalSampleSize                 = numPoints - burnin;
	    double  batchSize                       = totalSampleSize / (double)nBatches;
		
		System.arraycopy(serie, 0, data, 0, serie.length);
	    
	    double m = 0;
	    for (int i=burnin; i<numPoints; i++) {
	        m += data[i];
	    }
	    
	    double totalMean = m/(numPoints-burnin);
	    
	    // get a mean and standard error for each block
	    double batchMeans[]  = new double[nBatches];
	    for (int i=0; i<nBatches; i++) {
	        m = 0;
	        for (int j=(int)(i*batchSize+burnin); j<(int)((i+1)*batchSize+burnin); j++) {
	            m += data[j];
	        }
	        
	        batchMeans[i] = m/((int)((i+1)*batchSize+burnin)-(int)(i*batchSize+burnin));
	        	        
	        // iterate over all samples from the chains
	        for (int j=(int)(i*batchSize+burnin); j<(int)((i+1)*batchSize+burnin); j++) {
	            withinBatchVariance             += ( (data[j] - batchMeans[i])*(data[j] - batchMeans[i]) );
	            betweenBatchVariance            += ( (data[j] - totalMean)*(data[i] - totalMean) );
	        }
	    }
	    
	    double psrf                             = ((totalSampleSize-nBatches) / (totalSampleSize-1.0)) * (betweenBatchVariance/withinBatchVariance);
	    
	    return psrf < 1.001;
	}
}
