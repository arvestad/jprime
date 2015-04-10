package se.cbb.jprime.math;

/**
 * Class for Runge-Kutta numerical solution of a system of ordinary differential
 * equations (ODEs) of the first order, y' = f(x,y) with initial value y_0 = y(x_0).
 * Uses coefficients due to Dormand-Prince of order 4 for y_1 and order 5 for error
 * estimator ŷ_1 (where order refers to number of coinciding terms in Taylor series
 * for estimate and exact solution). Uses automatic step size control.
 * <p/>
 * Note: This class has been ported from Fortran-to-C++-to-Java, meaning that some
 * idiosyncracies may remain.
 * <p/>
 * The outline of this solver stems from "Solving Ordinary Differential Equations I" by
 * Hairer, Nörsett and Wanner, and the code is an adopted version of the author's old
 * Fortran code DOPRI5(). As such, it pretty much uses the same short variable names and
 * outline, but a slightly different invokation style.
 * <p/>
 * The user creates an evaluation function f(x,y) and passes that as input to the instance of this class.
 * Scalar tolerances are specified in the constructor. One may change various
 * default settings if required. Actual integration is made by calling <code>dopri5()</code>.
 * If the initial step size is set to 0, the solver will make a qualified start guess.
 * <p/>
 * Additionally, one has the ability to input an external callback function ("external solution
 * provider") </code>solout</code> which will be invoked after every accepted solver iteration.
 * This function can, if desired, alter the solution before next step. It may also call the
 * convenience function <code>contd5()</code> to get an interpolated estimate of the solution
 * between the old and current x-values, e.g. for fine-coarsed output.
 * However, such estimates are only available if a flag has been set to make
 * interpolation coefficients computed.
 * <p/>
 * In essence, use like this:
 * <ol>
 * <li>Create the evaluation function by implementing <code>ODEFunction</code>.</li>
 * <li>If required, create an external solution provider by implementing <code>ODEExternalSolutionProvider</code>.</li>
 * <li>Create an instance of this class.</li>
 * <li>If required, change default settings.
 * <li>Solve one or multiple ODEs by calling <code>dopri5()</code>.
 * </ol>
 * 
 * @author Hairer, Nörsett, Wanner.
 * @author Joel Sjöstrand.
 */
public class ODESolver {
	
	/** Return values from solver function dopri5(). */
	public enum SolverResult {
		/** Normal exit. */												SUCCESSFUL,  
		/** External solution provider requested interrupt. */			SUCCESSFUL_INTERRUPTED,  
		/** Aborted. Invalid input to solver. */						INCONSISTENT_INPUT,   
		/** Aborted. Specified max no. of steps were insufficient. */ 	INSUFFICIENT_NMAX,
		/** Aborted, Step size became to small. */						TOO_SMALL_GEN_STEP_SIZE,
		/** Aborted. Evaluation function was deemed stiff. */			PROBABLY_STIFF;
	}
	
	/** Fifth-order Runge-Kutta method. */
	public static final int IORD = 5;
	
	/** Dormand-Prince parameters. */
	private static final double C2 =       1.0 /      5.0;
	private static final double C3 =       3.0 /     10.0;
	private static final double C4 =       4.0 /      5.0;
	private static final double C5 =       8.0 /      9.0;
	private static final double A21 =      1.0 /      5.0;
	private static final double A31 =      3.0 /     40.0;
	private static final double A32 =      9.0 /     40.0;
	private static final double A41 =     44.0 /     45.0;
	private static final double A42 =    -56.0 /     15.0;
	private static final double A43 =     32.0 /      9.0;
	private static final double A51 =  19372.0 /   6561.0;
    private static final double A52 = -25360.0 /   2187.0;
    private static final double A53 =  64448.0 /   6561.0;
    private static final double A54 =   -212.0 /    729.0;
    private static final double A61 =   9017.0 /   3168.0;
    private static final double A62 =   -355.0 /     33.0;
    private static final double A63 =  46732.0 /   5247.0;
    private static final double A64 =     49.0 /    176.0;
    private static final double A65 =  -5103.0 /  18656.0;
    private static final double A71 =     35.0 /    384.0;
    private static final double A73 =    500.0 /   1113.0;
    private static final double A74 =    125.0 /    192.0;
    private static final double A75 =  -2187.0 /   6784.0;
    private static final double A76 =     11.0 /     84.0;
    private static final double E1 =      71.0 /  57600.0;
    private static final double E3 =     -71.0 /  16695.0;
    private static final double E4 =      71.0 /   1920.0;
    private static final double E5 =  -17253.0 / 339200.0;
    private static final double E6 =      22.0 /    525.0;
    private static final double E7 =      -1.0 /     40.0;

    /** Dense-output parameters of Shampine (1986). */
    private static final double D1 =  -12715105075.0 /  11282082432.0;
    private static final double D3 =   87487479700.0 /  32700410799.0;
    private static final double D4 =  -10690763975.0 /   1880347072.0;
    private static final double D5 =  701980252875.0 / 199316789632.0;
    private static final double D6 =   -1453857185.0 /    822651844.0;
    private static final double D7 =      69997945.0 /     29380423.0;
	
    /** The evaluation function y' = f(x,y). */
    protected ODEFunction fcn;
    
    /** External solution provider. Null if not used. */
    protected ODEExternalSolutionProvider m_solout;
    
    /** Flag indicating if to compute dense output. Only applicable if m_solout != null. */
    protected boolean m_doDense;
    
    /** Scalar relative tolerance. */
    protected double m_rtol;
    
    /** Scalar absolute tolerance. */
    protected double m_atol;
    
    /** Maximum number of allowed steps. */
    protected int m_nmax;     
    
    /** Stiffness detection every i-th step. */
    protected int m_nstiff;     
    
    /** Rounding unit, smallest number satisfying 1 + m_uround > 1. */
    protected double m_uround; 
    
    /** Safety factor. */
    protected double m_safe;    
    
    /** Step size selection parameter. */
    protected double m_fac1;   
    
    /** Step size selection parameter. */
    protected double m_fac2;  
    
    /** Step size stabilization factor. */
    protected double m_beta; 
    
    /** Maximum step size. Always positive. */
    protected double m_hmax;            
	
    /** Number of function evaluations of last run. */
    protected int m_nfcn;	
    
    /** Number of computed steps of last run. */
    protected int m_nstep;	
    
    /** Number of accepted steps of last run. */
    protected int m_naccpt;
    
    /** Number of rejected steps of last run (rejections in first step excluded). */
    protected int m_nrejct;
    
	/** Storage for dense output, concat. for all 5 coefficients. */
    protected double[] m_cont;
    
    /** Work var.: System size (no of comps.). */
    protected int m_n;
    
    /** Work var.: x-value of last iteration. */
    private double m_xold;
    
    /** Work var.: Step size of last iteration. */
    private double m_hout;
    
    /** Work var.: Main storage for integration. */
    private double[] m_y1;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k1;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k2;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k3;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k4;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k5;
    
    /** Work var.: Main storage for integration. */
    private double[] m_k6;
    
    /** Work var.: Main storage for integration. */
    private double[] m_ysti;
    
    /**
	 * Constructor for when interpolated values or external solution provider is not required.
	 * @param fcn the ODE evaluation function.
	 * @param rtol the relative tolerance.
	 * @param atol the absolute tolerance.
	 */
	public ODESolver(ODEFunction fcn, double rtol, double atol) {
		this(fcn, null, false, rtol, atol);
	}
    
	/**
	 * Constructor for when external solution provider is indeed provided. This must be the case for e.g. obtaining interpolated values.
	 * @param fcn the ODE evaluation function.
	 * @param solout the external solution provider.
 	 * @param doDense set to true if solout() calls contd5() to get interpolated values.
	 * @param rtol the relative tolerance.
	 * @param atol the absolute tolerance.
	 */
	public ODESolver(ODEFunction fcn, ODEExternalSolutionProvider solout, boolean doDense, double rtol, double atol) {
		if (fcn == null) {
			throw new IllegalArgumentException("Missing ODE evaluation function.");
		}
		this.fcn = fcn;
		m_solout = solout;
		m_doDense = doDense && (solout != null);
		m_cont = null;
		m_rtol = rtol;
		m_atol = atol;
		m_nmax = 100000;
		m_nstiff = 1000;
		m_uround = 2.3e-16;
		m_safe = 0.9;
		m_fac1 = 0.2;
		m_fac2 = 10;
		m_beta = 0.04;
		m_hmax = 0.0;
		m_nfcn = 0;
		m_nstep = 0;
		m_naccpt = 0;
		m_nrejct = 0;
		m_n = 0;
		m_xold = 0.0;
		m_hout = 0.0;
		m_y1 = null;
		m_k1 = null;
		m_k2 = null;
		m_k3 = null;
		m_k4 = null;
		m_k5 = null;
		m_k6 = null;
		m_ysti = null;
	}
	
	/**
	 * Called by dopri5() before solving. Performs trivial initialization tasks.
	 */
	protected void initialize() {
		// Init. working vectors.
		m_y1 = new double[m_n];
		m_k1 = new double[m_n];
		m_k2 = new double[m_n];
		m_k3 = new double[m_n];
		m_k4 = new double[m_n];
		m_k5 = new double[m_n];
		m_k6 = new double[m_n];
		m_ysti = new double[m_n];
		m_cont = new double[5 * m_n];  // All 5 coefficient rows concatenated.

		// Reset statistics counters.
		m_nfcn = 0;
		m_nstep = 0;
		m_naccpt = 0;
		m_nrejct = 0;
	}
		
		
	/**
	 * Returns various counters from last call to dopri5() thus:
	 * [0] number of function evaluations.
	 * [1] number of solver iterations (accepted or rejected steps).
	 * [2] number of accepted steps.
	 * [3] number of rejected steps (rejections in first step excluded).
	 */
	public int[] getStatistics() {
		return new int[] { m_nfcn, m_nstep, m_naccpt, m_nrejct };
	}
	
	/**
	 * Gets the relative tolerance.
	 * @return the relative tolerance. 
	 */
	public double getRelativeTolerance() {
		return m_rtol;
	}

	/**
	 * Gets the absolute tolerance.
	 * @return the absolute tolerance. 
	 */
	public double getAbsoluteTolerance() {
		return m_atol;
	}
	
	/**
	 * Sets the relative and absolute tolerances.
	 * @param rtol the relative tolerance.
	 * @param atol the absolute tolerance.
	 */
	public void setTolerance(double rtol, double atol) {
		m_rtol = rtol;
		m_atol = atol;
	}		
	
	/**
	 * Gets the external solution provider.
	 * @return the external solution provider.
	 */
	public ODEExternalSolutionProvider getSolout() {
		return m_solout;
	}
	
	/**
	 * Sets external solution provider.
	 * @param provider provider.
	 * @boolean doDense set to true if solout() calls contd5() to get interpolated values.
	 */
	public void setSolout(ODEExternalSolutionProvider solout, boolean doDense) {
		m_solout = solout;
		m_doDense = doDense && (solout != null);
	}
	
	/**
	 * Gets the dense output flag.
	 * @return true if calculating coefficients for dense output. 
	 */
	public boolean getDenseOuput() {
		return m_doDense;
	}
	
	/**
	 * Sets the dense output flag. Setting to true only has an effect if
	 * there is an external solution provider.
	 * @param doDense true to make solver compute dense output
	 *        coefficients.
	 */
	public void setDenseOutput(boolean doDense) {
		m_doDense = doDense && (m_solout != null);
	}

	/**
	 * Returns the maximum number of step the solver will make before aborting.
	 * @return the number of steps. Defaults to 100000.
	 */
	public int getMaxNoOfSteps() {
		return m_nmax;
	}
	
	/**
	 * Sets the maximum number of step the solver is allowed to make before aborting.
	 * @param maxNoOfSteps the limit.
	 */
	public void setMaxNoOfSteps(int maxNoOfSteps) {
		if (maxNoOfSteps <= 0)
			throw new IllegalArgumentException("Must specify maximum no of steps greater than 0.");
		m_nmax = maxNoOfSteps;
	}
	
	/**
	 * Returns the stiffness detection factor, i.e. every i-th iteration a test is made.
	 * Value 0 means no tests at all. Defaults to 1000.
	 * @return the factor.
	 */
	public int getStiffDetectFactor() {
		return (m_nstiff == Integer.MAX_VALUE ? 0 : m_nstiff);
	}
	
	/**
	 * Sets the stiffness detection factor. A sample test is made every i-th iteration.
	 * Set to 0 for no tests at all.
	 * @param factor the number of iterations between samples.
	 */
	public void setStiffDetectFactor(int factor) {
		// Value 0 means no detection at all.
		m_nstiff = (factor == 0) ? Integer.MAX_VALUE : factor;
	}
	
	/**
	 * Returns the rounding unit: smallest number u so that 1.0 + u > 1.0.
	 * Defaults to 2.3e-16.
	 * @return the unit.
	 */
	public double getRoundingUnit() {
		return m_uround;
	}
	
	/**
	 * Sets the rounding unit. Must be in range (1e-35, 1).
	 * @param the unit.
	 */
	public void setRoundingUnit(double roundingUnit) {
		if (roundingUnit <= 1e-35 || roundingUnit >= 1)
			throw new IllegalArgumentException("Must have rounding unit in range (1e-35, 1).");
		m_uround = roundingUnit;
	}
	
	
	/**
	 * Returns the safety factor for step size prediction. Defaults to 0.9.
	 * @return the factor.
	 */
	public double getSafetyFactor() {
		return m_safe;
	}
	
	/**
	 * Sets the safety factor for step size prediction. Must be in range (1e-4, 1).
	 * @param the factor.
	 */
	public void setSafetyFactor(double factor) {
		if (m_safe <= 1e-4 || m_safe >= 1)
			throw new IllegalArgumentException("Must have safety factor in range (1e-4, 1).");
		m_safe = factor;
	}		
	
	/**
	 * Returns the parameters for step size selection. The new step size is chosen
	 * subject to param1 <= hnew/hold <= param2. Defaults to 0.2 and 10.0.
	 * @param the first size selection parameter.
	 * @param the second size selection parameter.
	 */
	public double[] getStepSizeParams() {
		return new double[] { m_fac1, m_fac2 };
	}

	/**
	 * Sets the parameters for step size selection.
	 * @param the first size selection parameter.
	 * @param the second size selection parameter.
	 */
	public void setStepSizeParams(double param1, double param2) {
		m_fac1 = param1;
		m_fac2 = param2;
	}
	
	/**
	 * Returns the step size control stabilization parameter.
	 * Larger values (<= 0.1) make the step size control more stable.
	 * Defaults to 0.04.
	 * @return the stabilization parameter.
	 */
	public double getStepSizeStabilizationParam() {
		return m_beta;
	}

	/**
	 * Sets the step size control stabilization parameter. Must be in range [0, 0.2].
	 * @param beta the stabilization parameter.
	 */
	public void setStepSizeStabilizationParam(double beta) {
		if (beta < 0 || beta > 0.2)
			throw new IllegalArgumentException("Step size stabilizer must be in range [0, 0.2]");
		m_beta = beta;
	}
	
	/**
	 * Returns the maximum step size. 0 means not specified and leads to xend - x.
	 */
	public double getMaxStepSize() {
		return m_hmax;
	}

	/**
	 * Sets the maximum allowed step size. Sign does not matter. Set to 0 for
	 * unspecified, in which case xend - x is used.
	 */
	public void setMaxStepSize(double maxStepSize) {
		m_hmax = Math.abs(maxStepSize);
	}
	

	/**
	 * Solver. Numerically seeks the solution of an ODE y' = f(x,y) using Runge-Kutta with
	 * Dormand-Prince parameters given the initial values. Utilizes auto-step size control.
	 * Set the initial step size to zero to let the solver make a suggestion based on an Euler step.
	 * @param x the initial value of x. Updated during solving.
	 * @param xend the value of x where the solution is sought.
	 * @param y the initial value of y. Updated during solving, and containing the solution
	 * after successful return.
	 * @param h the initial step size. Updated during solving. Set to 0 for auto-estimation.
	 * @return a code stating solver's success or failure.
	 */
	public SolverResult dopri5(double x, double xend, double[] y, double h) {
		return dopri5(x, xend, y, h, null, null);
	}
	
	/**
	 * Solver. Numerically seeks the solution of an ODE y' = f(x,y) using Runge-Kutta with
	 * Dormand-Prince parameters given the initial values. Utilizes auto-step size control.
	 * Set the initial step size to zero to let the solver make a suggestion based on an Euler step.
	 * @param x the initial value of x. Updated during solving.
	 * @param xend the value of x where the solution is sought.
	 * @param y the initial value of y. Updated during solving, and containing the solution
	 * after successful return.
	 * @param h the initial step size. Updated during solving. Set to 0 for auto-estimation.
	 * @param rtol per-component relative tolerances. Set to override scalar value.
	 * @param atol per-component absolute tolerances. Set to override scalar value.
	 * @return a code stating solver's success or failure.
	 */
	public SolverResult dopri5(double x, double xend, double[] y, double h, double[] rtol, double[] atol) {
		m_n = y.length;
		initialize();

		if (rtol != null && rtol.length < m_n) { throw new IllegalArgumentException("Too small rel. tol. vector."); }
		if (atol != null && atol.length < m_n) { throw new IllegalArgumentException("Too small abs. tol. vector."); }
		
		// Calculate some more convenient settings values based on members.
		double hmax = (m_hmax == 0.0) ? Math.abs(xend - x) : m_hmax;
		double expo1 = 0.2 - m_beta * 0.75;
		double facc1 = 1.0 / m_fac1;
		double facc2 = 1.0 / m_fac2;
		int posneg = (x <= xend) ? 1 : -1;

		// Err from previous step.
		double facold = 1e-4;

		// Suggested new step size. Recomputed if rejected.
		double hnew;

		// Number of hypothetically stiff samples detected within small proximity.
		int iasti = 0;

		// Number of consecutive samples deemed non-stiff after detecting a stiff one.
		int nonsti = 0;

		// Step size parameter during stiffness detection.
		double hlamb = 0.0;
		
		// Last iteration flag.
		boolean last = false;

		// First evaluation at initial x.       
		fcn.evaluate(x, y, m_k1);
		m_nfcn++;

		// An initial step size set to 0 implies that an auto estimation should be made.
		if (h == 0.0) {
			h = hinit(x, y, posneg, hmax, rtol, atol);
			m_nfcn++;
		}

		// Rejection of step.
		boolean reject = false;
		
		m_xold = x;
		m_hout = h;
		
		// Return-code from external solution provider.
		ODEExternalSolutionProvider.SolutionProviderResult irtrn;

		// Call external solution provider for first time if such exists.
		if (m_solout != null) {
			irtrn = m_solout.solout(m_naccpt + 1, m_xold, x, y);
			if (irtrn == ODEExternalSolutionProvider.SolutionProviderResult.INTERRUPT_SOLVER) {
				return SolverResult.SUCCESSFUL_INTERRUPTED;
			}
		} else {
			irtrn = ODEExternalSolutionProvider.SolutionProviderResult.NOT_INVOKED;
		}

		// BASIC INTEGRATION STEP.
		while (true) {
			// When unable to retrieve answer within max allowed steps.
			if (m_nstep > m_nmax) { return SolverResult.INSUFFICIENT_NMAX; }

			// When step size is too small to continue.
			if (0.1 * Math.abs(h) <= Math.abs(x) * m_uround) { return SolverResult.TOO_SMALL_GEN_STEP_SIZE; }

			// If we will reach xend, we're at the last iteration.
			if ((x + 1.01 * h - xend) * posneg > 0.0) {
				h = xend - x;
				last = true;
			}

			m_nstep++;

			// The first 6 stages.
			if (irtrn == ODEExternalSolutionProvider.SolutionProviderResult.SOLUTION_CHANGED) {
				// Recompute, since solution externally changed.
				fcn.evaluate(x, y, m_k1);
			}
			for (int i = 0; i < m_n; ++i)
			{ m_y1[i] = y[i] + h * A21 * m_k1[i]; }
			fcn.evaluate(x + C2 * h, m_y1, m_k2);
			for (int i = 0; i < m_n; ++i)
			{ m_y1[i] = y[i] + h * (A31 * m_k1[i] + A32 * m_k2[i]); }
			fcn.evaluate(x + C3 * h, m_y1, m_k3);
			for (int i = 0; i < m_n; ++i)
			{ m_y1[i] = y[i] + h * (A41 * m_k1[i] + A42 * m_k2[i] + A43 * m_k3[i]); }
			fcn.evaluate(x + C4 * h, m_y1, m_k4);
			for (int i = 0; i < m_n; ++i)
			{ m_y1[i] = y[i] + h * (A51 * m_k1[i] + A52 * m_k2[i] + A53 * m_k3[i] + A54 * m_k4[i]); }
			fcn.evaluate(x + C5 * h, m_y1, m_k5);
			for (int i = 0; i < m_n; ++i)
			{ m_ysti[i] = y[i] + h * (A61 * m_k1[i] + A62 * m_k2[i] + A63 * m_k3[i] + A64 * m_k4[i] + A65 * m_k5[i]); }
			double xph = x + h;
			fcn.evaluate(xph, m_ysti, m_k6);
			for (int i = 0; i < m_n; ++i)
			{ m_y1[i] = y[i] + h * (A71 * m_k1[i] + A73 * m_k3[i] + A74 * m_k4[i] + A75 * m_k5[i] + A76 * m_k6[i]); }
			fcn.evaluate(xph, m_y1, m_k2);

			// Store dense output coefficients if specified.
			if (m_doDense) {
				for (int i=0; i<m_n; ++i) {
					m_cont[4 * m_n + i] = h * (D1 * m_k1[i] + D3 * m_k3[i] + D4 * m_k4[i]
					       + D5 * m_k5[i] + D6 * m_k6[i] + D7 * m_k2[i]);
				}
			}
			
			for (int i = 0; i < m_n; ++i) {
				m_k4[i] = (E1 * m_k1[i] + E3 * m_k3[i] + E4 * m_k4[i] + E5 * m_k5[i] + E6 * m_k6[i] + E7 * m_k2[i]) * h;
			}
			m_nfcn += 6;

			// Error estimation. 
			double err = 0.0;
			if (rtol == null) {
				// Scalar tolerance.
				for (int i = 0; i < m_n; ++i) {
					double sk = m_atol + m_rtol * Math.max(Math.abs(y[i]), Math.abs(m_y1[i]));
					err += (m_k4[i] / sk) * (m_k4[i] / sk);
				}
			} else {
				// Per-component tolerances.
				for (int i = 0; i < m_n; ++i) {
					double sk = atol[i] + rtol[i] * Math.max(Math.abs(y[i]), Math.abs(m_y1[i]));
					err += (m_k4[i] / sk) * (m_k4[i] / sk);
				}
			}
			err = Math.sqrt(err / m_n);

			// Computation of hnew.
			double fac11 = Math.pow(err, expo1);
			double fac = fac11 / Math.pow(facold, m_beta);      // Lund-stabilization.
			fac = Math.max(facc2, Math.min(facc1, fac / m_safe));  // We require fac1 <= hnew/h <= fac2.
			hnew = h / fac;
			
			if (err <= 1.0) {
				// STEP IS ACCEPTED.
				facold = Math.max(err, 1e-4);
				m_naccpt++;

				// Stiffness detection test. Sample every m_nstiff-th sample, or every time
				// after a sample was positive.
				if (m_naccpt % m_nstiff == 0 || iasti > 0) {
					double stnum = 0.0;
					double stden = 0.0;
					for (int i = 0; i < m_n; ++i) {
						stnum += (m_k2[i] - m_k6[i]) * (m_k2[i] - m_k6[i]);
						stden += (m_y1[i] - m_ysti[i]) * (m_y1[i] - m_ysti[i]); 
					}
					if (stden > 0.0) {
						hlamb = h * Math.sqrt(stnum / stden);
					}
					if (hlamb > 3.25) {
						// Reset conditional non-stiff counter. Increase stiff counter. If too
						// many stiff samples in a row (interspersed with a few non-stiff), abort.
						nonsti = 0;
						iasti++;
						if (iasti == 15) { return SolverResult.PROBABLY_STIFF; }
					} else {
						// Increase conditional non-stiff counter, if enough, reset stiff counter.
						nonsti++;
						if (nonsti == 6) { iasti = 0; }
					}
				}

				// Calculate dense output.
				if (m_doDense) {
					for (int i = 0; i < m_n; ++i) {
						double yd0 = y[i];
						double ydiff = m_y1[i] - yd0;
						double bspl = h * m_k1[i] - ydiff;
						m_cont[i] = y[i];
						m_cont[m_n + i] = ydiff;
						m_cont[2 * m_n + i] = bspl;
						m_cont[3 * m_n + i] = -h * m_k2[i] + ydiff - bspl;
					}
				}
				
				// Move on to next step.
				for (int i = 0; i < m_n; ++i) {
					m_k1[i] = m_k2[i];
					y[i] = m_y1[i];
				}
				m_xold = x;
				m_hout = h;
				x = xph;
				if (m_solout != null) {
					irtrn = m_solout.solout(m_naccpt + 1, m_xold, x, y);
					if (irtrn == ODEExternalSolutionProvider.SolutionProviderResult.INTERRUPT_SOLVER) {
						return SolverResult.SUCCESSFUL_INTERRUPTED;
					}
				}
				if (last) {
					// Normal exit.
					h = hnew;
					return SolverResult.SUCCESSFUL;
				}
				if (Math.abs(hnew) > hmax) { hnew = posneg * hmax; }
				if (reject) { hnew = posneg * Math.min(Math.abs(hnew), Math.abs(h)); }
				reject = false;
			}
			else
			{
				// STEP IS REJECTED. Recompute hnew.
				hnew = h / Math.min(facc1, fac11 / m_safe);
				reject = true;
				if (m_naccpt >= 1) { m_nrejct++; }
				last = false;
			}
			h = hnew;
		}
	}


	/**
	 * Computes an initial step size guess using
	 * "explicit Euler" according to h = 0.01 * |y_0| / |f_0|.
	 * The increment for explicit Euler is small compared to the solution.
	 * @param x the initial x.
	 * @param y the initial y.
	 * @param posneg integration direction: -1 if x>xend, else 1.
	 * @param hmax the absolute value of the maximum step size.
	 * @param rtol per-component relative tolerances.
	 * @param atol per-component absolute tolerances.
	 * @return the suggested initial step size (with sign).
	 */
	protected double hinit(double x, double[] y, int posneg, double hmax, double[] rtol, double[] atol) {
		double[] f0 = m_k1;
		double[] f1 = m_k2;
		double[] y1 = m_k3;

		double dnf = 0.0;
		double dny = 0.0;

		if (rtol == null) {
			// Scalar tolerance.
			for (int i = 0; i < m_n; ++i)
			{
				double sk = m_atol + m_rtol * Math.abs(y[i]);
				dnf += (f0[i] / sk) * (f0[i] / sk);
				dny += (y[i] / sk) * (y[i] / sk);
			}
		} else {
			// Per-component tolerances.
			for (int i = 0; i < m_n; ++i)
			{
				double sk = atol[i] + rtol[i] * Math.abs(y[i]);
				dnf += (f0[i] / sk) * (f0[i] / sk);
				dny += (y[i] / sk) * (y[i] / sk);
			}
		}

		double h = (dnf <= 1e-10 || dny <= 1e-10) ? 1e-6 : Math.sqrt(dny / dnf) * 0.01;
		h = posneg * Math.min(h, hmax);

		// Perform an exlicit Euler step.
		for (int i = 0; i < m_n; ++i) {
			y1[i] = y[i] + h * f0[i];
		}
		fcn.evaluate(x + h, y1, f1);

		// Estimate the second derivative of the solution.
		// As earlier, use scalar or per-component tolerances.
		double der2 = 0.0;
		if (rtol == null) {
			for (int i = 0; i < m_n; ++i) {
				double sk = m_atol + m_rtol * Math.abs(y[i]);
				der2 += ((f1[i] - f0[i]) / sk) * ((f1[i] - f0[i]) / sk);
			}
		} else {
			for (int i = 0; i < m_n; ++i) {
				double sk = atol[i] + rtol[i] * Math.abs(y[i]);
				der2 += ((f1[i] - f0[i]) / sk) * ((f1[i] - f0[i]) / sk);
			}
		}
		der2 = Math.sqrt(der2) / h;

		// Step size is computed such that 
		// h^5 * max(norm(f0), norm(der2)) == 0.01.
		double der12 = Math.max(Math.abs(der2), Math.sqrt(dnf));
		double h1 = (der12 <= 1e-15) ?
				Math.max(1e-6, Math.abs(h) * 1.0e-3) : Math.pow(0.01 / der12, 1.0 / IORD);
		h = posneg * Math.min(Math.min(100 * Math.abs(h), h1), hmax);
		return h;
	} 


	/**
	 * Used for e.g. continuous output in connection with an external solution provider.
	 * After a solver iteration has finished and the external provider is invoked,
	 * the latter may invoke this method to get an approximation of a component of the
	 * solution at a specified value in the range [xold,x]. This is only possible if there
	 * is dense output (see hasDense flag).
	 * @param i the index of the component of the solution.
	 * @param xCont the value in the current range [xold,x] for which the solution
	 *        component is sought.
	 * @return an interpolation of the i-th component of the solution at xCont.
	 */
	public double contd5(int i, double xCont) {
		if (!this.m_doDense) {
			throw new IllegalArgumentException("Cannot interpolate ODE solution: the do-dense-flag has not been set.");
		}
		double theta = (xCont - m_xold) / m_hout;
		double theta1 = 1.0 - theta;
		return (m_cont[i] + theta * (m_cont[m_n + i] + theta1 * (m_cont[2 * m_n + i] +
				theta * (m_cont[3 * m_n + i] + theta1 * m_cont[4 * m_n + i]))));
	}


	/**
	 * Works similarly to <code>contd5(int i, double xCont)</code>, but returns
	 * all components.
	 * @param yIpl a vector where the interpolated solution will be stored. Must have sufficient number of elements.
	 * @param xCont the value in the current range [xold,x] for which the solution
	 *        is sought. 
	 */
	public void contd5(double[] yIpl, double xCont) {
		if (!this.m_doDense) {
			throw new IllegalArgumentException("Cannot interpolate ODE solution: the do-dense-flag has not been set.");
		}
		// yIpl must have size m_n (or more).
		double theta = (xCont - m_xold) / m_hout;
		double theta1 = 1.0 - theta;
		for (int i = 0; i < m_n; ++i) {
			yIpl[i] = m_cont[i] + theta * (m_cont[m_n + i] + theta1 * (m_cont[2 * m_n + i] +
					theta * (m_cont[3 * m_n + i] + theta1 * m_cont[4 * m_n + i])));
		}
	}
	
}
