package ijx.measure;
import ijx.gui.dialog.GenericDialog;
import ijx.macro.Interpreter;
import ijx.macro.Tokenizer;
import ijx.macro.Program;
import ijx.IJ;


/** Curve fitting class based on the Simplex method described
 *  in the article "Fitting Curves to Data" in the May 1984
 *  issue of Byte magazine, pages 340-362.
 *
 *	2001/02/14: Modified to handle a gamma variate curve.
 *  Uses altered Simplex method based on method in "Numerical Recipes in C".
 *  This method tends to converge closer in less iterations.
 *  Has the option to restart the simplex at the initial best solution in
 *  case it is "stuck" in a local minimum (by default, restarted twice).  Also includes
 *  settings dialog option for user control over simplex parameters and functions to
 *  evaluate the goodness-of-fit.  The results can be easily reported with the
 *  getResultString() method.
 *  Kieran Holland (holki659 at student.otago.ac.nz)
 *
 *	2008/01/21: Modified to do Gaussian fitting by Stefan Woerz (s.woerz at dkfz.de).
 *
 */
public class CurveFitter {    
	public static final int STRAIGHT_LINE=0,POLY2=1,POLY3=2,POLY4=3,
	EXPONENTIAL=4,POWER=5,LOG=6,RODBARD=7,GAMMA_VARIATE=8, LOG2=9,
	RODBARD2=10, EXP_WITH_OFFSET=11, GAUSSIAN=12, EXP_RECOVERY=13;
	
	private static final int CUSTOM = 20;
	
	public static final int IterFactor = 500;
	
	public static final String[] fitList = {"Straight Line","2nd Degree Polynomial",
	"3rd Degree Polynomial", "4th Degree Polynomial","Exponential","Power",
	"Log","Rodbard", "Gamma Variate", "y = a+b*ln(x-c)","Rodbard (NIH Image)",
	"Exponential with Offset","Gaussian", "Exponential Recovery"}; // fList and doFit() must also be updated
	
	public static final String[] fList = {"y = a+bx","y = a+bx+cx^2",
	"y = a+bx+cx^2+dx^3", "y = a+bx+cx^2+dx^3+ex^4","y = a*exp(bx)","y = ax^b",
	"y = a*ln(bx)", "y = d+(a-d)/(1+(x/c)^b)", "y = a*(x-b)^c*exp(-(x-b)/d)",
	"y = a+b*ln(x-c)", "y = d+(a-d)/(1+(x/c)^b)", "y = a*exp(-bx) + c", 
	"y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))", "y=a*(1-exp(-b*x)) + c"}; 
	   
	private static final double alpha = -1.0;	  // reflection coefficient
	private static final double beta = 0.5;	  // contraction coefficient
	private static final double gamma = 2.0;	  // expansion coefficient
	private static final double root2 = 1.414214; // square root of 2
	
	private int fit;                // Number of curve type to fit
	private double[] xData, yData;  // x,y data to fit
	private int numPoints;          // number of data points
	private int numParams;          // number of parametres
	private int numVertices;        // numParams+1 (includes sumLocalResiduaalsSqrd)
	private int worst;			// worst current parametre estimates
	private int nextWorst;		// 2nd worst current parametre estimates
	private int best;			// best current parametre estimates
	private double[][] simp; 		// the simplex (the last element of the array at each vertice is the sum of the square of the residuals)
	private double[] next;		// new vertex to be tested
	private int numIter;		// number of iterations so far
	private int maxIter; 	// maximum number of iterations per restart
	private int restarts; 	// number of times to restart simplex after first soln.
	private static int defaultRestarts = 2;  // default number of restarts
	private int nRestarts;  // the number of restarts that occurred
	private static double maxError = 1e-10;    // maximum error tolerance
	private double[] initialParams;  // user specified initial parameters
	private long time;  //elapsed time in ms
	private String customFormula;
	private int customParamCount;
	private Interpreter macro;
	private double[] initialValues;
	
    /** Construct a new CurveFitter. */
    public CurveFitter (double[] xData, double[] yData) {
        this.xData = xData;
        this.yData = yData;
        numPoints = xData.length;
    }
    
    /**  Perform curve fitting with the simplex method
     *          doFit(fitType) just does the fit
     *          doFit(fitType, true) pops up a dialog allowing control over simplex parameters
     *  	alpha is reflection coefficient  (-1)
     *  	beta is contraction coefficient (0.5)
     *  	gamma is expansion coefficient (2)
     */
    public void doFit(int fitType) {
        doFit(fitType, false);
    }
    
    public void doFit(int fitType, boolean showSettings) {
        if (fitType<STRAIGHT_LINE || (fitType>EXP_RECOVERY&&fitType!=CUSTOM))
            throw new IllegalArgumentException("Invalid fit type");
        int saveFitType = fitType;
        if (fitType==RODBARD2) {
			double[] temp;
			temp = xData;
			xData = yData;
			yData = temp;
        	fitType = RODBARD;
        }
        fit = fitType;
        initialize();
 		if (initialParams!=null) {
			for (int i=0; i<numParams; i++)
				simp[0][i] = initialParams[i];
			initialParams = null;
		}
        if (showSettings) settingsDialog();
 		long startTime = System.currentTimeMillis();
        restart(0);
        
        numIter = 0;
        boolean done = false;
        double[] center = new double[numParams];  // mean of simplex vertices
        while (!done) {
            numIter++;
            for (int i = 0; i < numParams; i++) center[i] = 0.0;
            // get mean "center" of vertices, excluding worst
            for (int i = 0; i < numVertices; i++)
                if (i != worst)
                    for (int j = 0; j < numParams; j++)
                        center[j] += simp[i][j];
            // Reflect worst vertex through centre
            for (int i = 0; i < numParams; i++) {
                center[i] /= numParams;
                next[i] = center[i] + alpha*(simp[worst][i] - center[i]);
            }
            sumResiduals(next);
            // if it's better than the best...
            if (next[numParams] <= simp[best][numParams]) {
                newVertex();
                // try expanding it
                for (int i = 0; i < numParams; i++)
                    next[i] = center[i] + gamma * (simp[worst][i] - center[i]);
                sumResiduals(next);
                // if this is even better, keep it
                if (next[numParams] <= simp[worst][numParams])
                    newVertex();
            }
            // else if better than the 2nd worst keep it...
            else if (next[numParams] <= simp[nextWorst][numParams]) {
                newVertex();
            }
            // else try to make positive contraction of the worst
            else {
                for (int i = 0; i < numParams; i++)
                    next[i] = center[i] + beta*(simp[worst][i] - center[i]);
                sumResiduals(next);
                // if this is better than the second worst, keep it.
                if (next[numParams] <= simp[nextWorst][numParams]) {
                    newVertex();
                }
                // if all else fails, contract simplex in on best
                else {
                    for (int i = 0; i < numVertices; i++) {
                        if (i != best) {
                            for (int j = 0; j < numVertices; j++)
                                simp[i][j] = beta*(simp[i][j]+simp[best][j]);
                            sumResiduals(simp[i]);
                        }
                    }
                }
            }
            order();
            
            double rtol = 2 * Math.abs(simp[best][numParams] - simp[worst][numParams]) /
            (Math.abs(simp[best][numParams]) + Math.abs(simp[worst][numParams]) + 0.0000000001);
            
            if (numIter >= maxIter)
            	done = true;
            else if (rtol < maxError) {
                restarts--;
                if (restarts < 0)
                    done = true;
                else
                    restart(best);
             }
        }
        fit = saveFitType;
		time = System.currentTimeMillis()-startTime;
    }
        
	public int doCustomFit(String equation, double[] initialValues, boolean showSettings) {
		customFormula = null;
		customParamCount = 0;
		Program pgm = (new Tokenizer()).tokenize(equation);
		if (!pgm.hasWord("y")) return 0;
		if (!pgm.hasWord("x")) return 0;
		String[] params = {"a","b","c","d","e","f"};
		for (int i=0; i<params.length; i++) {
			if (pgm.hasWord(params[i]))
				customParamCount++;
		}
		if (customParamCount==0)
			return 0;
		customFormula = equation;
		String code =
			"var x, a, b, c, d, e, f;\n"+
			"function dummy() {}\n"+
			equation+";\n"; // starts at program counter location 21
		macro = new Interpreter();
		macro.run(code, null);
		if (macro.wasError())
			return 0;
		this.initialValues = initialValues;
		doFit(CUSTOM, showSettings);
		return customParamCount;
	}

    /** Pop up a dialog allowing control over simplex starting parameters */
    private void settingsDialog() {
        GenericDialog gd = new GenericDialog("Simplex Fitting Options");
        gd.addMessage("Function name: " + getName() + "\n" +
        "Formula: " + getFormula());
        char pChar = 'a';
        for (int i = 0; i < numParams; i++) {
            gd.addNumericField("Initial "+(new Character(pChar)).toString()+":", simp[0][i], 2);
            pChar++;
        }
        gd.addNumericField("Maximum iterations:", maxIter, 0);
        gd.addNumericField("Number of restarts:", defaultRestarts, 0);
        gd.addNumericField("Error tolerance [1*10^(-x)]:", -(Math.log(maxError)/Math.log(10)), 0);
        gd.showDialog();
        if (gd.wasCanceled() || gd.invalidNumber()) {
            IJ.error("Parameter setting canceled.\nUsing default parameters.");
        }
        // Parametres:
        for (int i = 0; i < numParams; i++) {
            simp[0][i] = gd.getNextNumber();
        }
        maxIter = (int) gd.getNextNumber();
        defaultRestarts = restarts = (int) gd.getNextNumber();
        maxError = Math.pow(10.0, -gd.getNextNumber());
    }

    /** Initialise the simplex */
    void initialize() {
        // Calculate some things that might be useful for predicting parametres
        numParams = getNumParams();
        numVertices = numParams + 1;      // need 1 more vertice than parametres,
        simp = new double[numVertices][numVertices];
        next = new double[numVertices];
        
        double firstx = xData[0];
        double firsty = yData[0];
        double lastx = xData[numPoints-1];
        double lasty = yData[numPoints-1];
        double xmean = (firstx+lastx)/2.0;
        double ymean = (firsty+lasty)/2.0;
        double miny=firsty, maxy=firsty;
        if (fit==GAUSSIAN) {
            for (int i=1; i<numPoints; i++) {
              if (yData[i]>maxy) maxy = yData[i];
              if (yData[i]<miny) miny = yData[i];
            }
        }
        double slope;
        if ((lastx - firstx) != 0.0)
            slope = (lasty - firsty)/(lastx - firstx);
        else
            slope = 1.0;
        double yintercept = firsty - slope * firstx;
        maxIter = IterFactor * numParams * numParams;  // Where does this estimate come from?
        restarts = defaultRestarts;
        nRestarts = 0;
        switch (fit) {
            case STRAIGHT_LINE:
                simp[0][0] = yintercept;
                simp[0][1] = slope;
                break;
            case POLY2:
                simp[0][0] = yintercept;
                simp[0][1] = slope;
                simp[0][2] = 0.0;
                break;
            case POLY3:
                simp[0][0] = yintercept;
                simp[0][1] = slope;
                simp[0][2] = 0.0;
                simp[0][3] = 0.0;
                break;
            case POLY4:
                simp[0][0] = yintercept;
                simp[0][1] = slope;
                simp[0][2] = 0.0;
                simp[0][3] = 0.0;
                simp[0][4] = 0.0;
                break;
            case EXPONENTIAL:
                simp[0][0] = 0.1;
                simp[0][1] = 0.01;
                break;
            case EXP_WITH_OFFSET:
                simp[0][0] = 0.1;
                simp[0][1] = 0.01;
                simp[0][2] = 0.1;
                break;            
            case EXP_RECOVERY:
                simp[0][0] = 0.1;
                simp[0][1] = 0.01;
                simp[0][2] = 0.1;
                break;            
            case GAUSSIAN:
                simp[0][0] = miny;   // a0
                simp[0][1] = maxy;   // a1
                simp[0][2] = xmean;  // x0
                simp[0][3] = 3.0;    // sigma
                break;            
            case POWER:
                simp[0][0] = 0.0;
                simp[0][1] = 1.0;
                break;
            case LOG:
                simp[0][0] = 1.0;
                simp[0][1] = 1.0;
                break;
            case RODBARD: case RODBARD2:
                simp[0][0] = firsty;
                simp[0][1] = 1.0;
                simp[0][2] = xmean;
                simp[0][3] = lasty;
                break;
            case GAMMA_VARIATE:
                //  First guesses based on following observations:
                //  t0 [b] = time of first rise in gamma curve - so use the user specified first limit
                //  tm = t0 + a*B [c*d] where tm is the time of the peak of the curve
                //  therefore an estimate for a and B is sqrt(tm-t0)
                //  K [a] can now be calculated from these estimates
                simp[0][0] = firstx;
                double ab = xData[getMax(yData)] - firstx;
                simp[0][2] = Math.sqrt(ab);
                simp[0][3] = Math.sqrt(ab);
                simp[0][1] = yData[getMax(yData)] / (Math.pow(ab, simp[0][2]) * Math.exp(-ab/simp[0][3]));
                break;
            case LOG2:
                simp[0][0] = 0.5;
                simp[0][1] = 0.05;
                simp[0][2] = 0.0;
                break;
           case CUSTOM:
                if (macro==null)
                	throw new IllegalArgumentException("No custom formula!");
                if (initialValues!=null && initialValues.length>=numParams) {
                	for (int i=0; i<numParams; i++)
                		simp[0][i] = initialValues[i];
                } else {
                	for (int i=0; i<numParams; i++)
                		simp[0][i] = 1.0;
                }
                break;
        }
    }
        
    /** Restart the simplex at the nth vertex */
    void restart(int n) {
        // Copy nth vertice of simplex to first vertice
        for (int i = 0; i < numParams; i++) {
            simp[0][i] = simp[n][i];
        }
        sumResiduals(simp[0]);          // Get sum of residuals^2 for first vertex
        double[] step = new double[numParams];
        for (int i = 0; i < numParams; i++) {
            step[i] = simp[0][i] / 2.0;     // Step half the parametre value
            if (step[i] == 0.0)             // We can't have them all the same or we're going nowhere
                step[i] = 0.01;
        }
        // Some kind of factor for generating new vertices
        double[] p = new double[numParams];
        double[] q = new double[numParams];
        for (int i = 0; i < numParams; i++) {
            p[i] = step[i] * (Math.sqrt(numVertices) + numParams - 1.0)/(numParams * root2);
            q[i] = step[i] * (Math.sqrt(numVertices) - 1.0)/(numParams * root2);
        }
        // Create the other simplex vertices by modifing previous one.
        for (int i = 1; i < numVertices; i++) {
            for (int j = 0; j < numParams; j++) {
                simp[i][j] = simp[i-1][j] + q[j];
            }
            simp[i][i-1] = simp[i][i-1] + p[i-1];
            sumResiduals(simp[i]);
        }
        // Initialise current lowest/highest parametre estimates to simplex 1
        best = 0;
        worst = 0;
        nextWorst = 0;
        order();
        nRestarts++;
    }
        
    // Display simplex [Iteration: s0(p1, p2....), s1(),....] in Log window
    void showSimplex(int iter) {
        ijx.IJ.log("" + iter);
        for (int i = 0; i < numVertices; i++) {
            String s = "";
            for (int j=0; j < numVertices; j++)
                s += "  "+ ijx.IJ.d2s(simp[i][j], 6);
            ijx.IJ.log(s);
        }
    }
        
    /** Get number of parameters for current fit formula */
    public int getNumParams() {
        switch (fit) {
            case STRAIGHT_LINE: return 2;
            case POLY2: return 3;
            case POLY3: return 4;
            case POLY4: return 5;
            case EXPONENTIAL: return 2;
            case POWER: return 2;
            case LOG: return 2;
            case RODBARD: case RODBARD2: return 4;
            case GAMMA_VARIATE: return 4;
            case LOG2: return 3;
            case EXP_WITH_OFFSET: return 3;
            case GAUSSIAN: return 4;
            case EXP_RECOVERY: return 3;
            case CUSTOM: return customParamCount;
        }
        return 0;
    }
        
	/** Returns formula value for parameters 'p' at 'x' */
	public double f(double[] p, double x) {
		if (fit==CUSTOM) {
			macro.setVariable("x", x);
			macro.setVariable("a", p[0]);
			if (customParamCount>1) macro.setVariable("b", p[1]);
			if (customParamCount>2) macro.setVariable("c", p[2]);
			if (customParamCount>3) macro.setVariable("d", p[3]);
			if (customParamCount>4) macro.setVariable("e", p[4]);
			if (customParamCount>5) macro.setVariable("f", p[5]);
			macro.run(21);
			return macro.getVariable("y");
		} else
			return f(fit, p, x);
	}

   /** Returns 'fit' formula value for parameters "p" at "x" */
    public static double f(int fit, double[] p, double x) {
    	double y;
        switch (fit) {
            case STRAIGHT_LINE:
                return p[0] + p[1]*x;
            case POLY2:
                return p[0] + p[1]*x + p[2]* x*x;
            case POLY3:
                return p[0] + p[1]*x + p[2]*x*x + p[3]*x*x*x;
            case POLY4:
                return p[0] + p[1]*x + p[2]*x*x + p[3]*x*x*x + p[4]*x*x*x*x;
            case EXPONENTIAL:
                return p[0]*Math.exp(p[1]*x);
            case EXP_WITH_OFFSET:
                return p[0]*Math.exp(p[1]*x*-1)+p[2];
            case EXP_RECOVERY:
                return p[0]*(1-Math.exp(-p[1]*x))+p[2];
            case GAUSSIAN:
                return p[0]+(p[1]-p[0])*Math.exp(-(x-p[2])*(x-p[2])/(2.0*p[3]*p[3]));
            case POWER:
                if (x == 0.0)
                    return 0.0;
                else
                    return p[0]*Math.exp(p[1]*Math.log(x)); //y=ax^b
            case LOG:
                if (x == 0.0)
                    x = 0.5;
                return p[0]*Math.log(p[1]*x);
            case RODBARD:
				double ex;
				if (x == 0.0)
					ex = 0.0;
				else
					ex = Math.exp(Math.log(x/p[2])*p[1]);
				y = p[0]-p[3];
				y = y/(1.0+ex);
				return y+p[3];
            case GAMMA_VARIATE:
                if (p[0] >= x) return 0.0;
                if (p[1] <= 0) return -100000.0;
                if (p[2] <= 0) return -100000.0;
                if (p[3] <= 0) return -100000.0;
                
                double pw = Math.pow((x - p[0]), p[2]);
                double e = Math.exp((-(x - p[0]))/p[3]);
                return p[1]*pw*e;
            case LOG2:
            	double tmp = x-p[2];
            	if (tmp<0.001) tmp = 0.001;
				return p[0]+p[1]*Math.log(tmp);
            case RODBARD2:
				if (x<=p[0])
					y = 0.0;
				else {
					y = (p[0]-x)/(x-p[3]);
					y = Math.exp(Math.log(y)*(1.0/p[1]));  //y=y**(1/b)
					y = y*p[2];
				}
				return y;
            default:
                return 0.0;
        }
    }
    
    /** Get the set of parameter values from the best corner of the simplex */
    public double[] getParams() {
        order();
        return simp[best];
    }
    
	/** Returns residuals array ie. differences between data and curve. */
	public double[] getResiduals() {
		int saveFit = fit;
		if (fit==RODBARD2) fit=RODBARD;
		double[] params = getParams();
		double[] residuals = new double[numPoints];
		if (fit==CUSTOM) {
			for (int i=0; i<numPoints; i++)
				residuals[i] = yData[i] - f(params, xData[i]);
		} else {
			for (int i=0; i<numPoints; i++)
				residuals[i] = yData[i] - f(fit, params, xData[i]);
		}
		fit = saveFit;
		return residuals;
	}
    
    /* Last "parametre" at each vertex of simplex is sum of residuals
     * for the curve described by that vertex
     */
    public double getSumResidualsSqr() {
        double sumResidualsSqr = (getParams())[getNumParams()];
        return sumResidualsSqr;
    }
    
    /**  Returns the standard deviation of the residuals. */
    public double getSD() {
    	double[] residuals = getResiduals();
		int n = residuals.length;
		double sum=0.0, sum2=0.0;
		for (int i=0; i<n; i++) {
			sum += residuals[i];
			sum2 += residuals[i]*residuals[i];
		}
		double stdDev = (n*sum2-sum*sum)/n;
		return Math.sqrt(stdDev/(n-1.0));
    }
    
    /** Returns R^2, where 1.0 is best.
    <pre>
     r^2 = 1 - SSE/SSD
     
     where:	 SSE = sum of the squares of the errors
                 SSD = sum of the squares of the deviations about the mean.
    </pre>
    */
    public double getRSquared() {
        double sumY = 0.0;
        for (int i=0; i<numPoints; i++) sumY += yData[i];
        double mean = sumY/numPoints;
        double sumMeanDiffSqr = 0.0;
        for (int i=0; i<numPoints; i++)
            sumMeanDiffSqr += sqr(yData[i]-mean);
        double rSquared = 0.0;
        if (sumMeanDiffSqr>0.0)
            rSquared = 1.0 - getSumResidualsSqr()/sumMeanDiffSqr;
        return rSquared;
    }

    /**  Get a measure of "goodness of fit" where 1.0 is best. */
    public double getFitGoodness() {
        double sumY = 0.0;
        for (int i = 0; i < numPoints; i++) sumY += yData[i];
        double mean = sumY / numPoints;
        double sumMeanDiffSqr = 0.0;
        int degreesOfFreedom = numPoints - getNumParams();
        double fitGoodness = 0.0;
        for (int i = 0; i < numPoints; i++) {
            sumMeanDiffSqr += sqr(yData[i] - mean);
        }
        if (sumMeanDiffSqr > 0.0 && degreesOfFreedom != 0)
            fitGoodness = 1.0 - (getSumResidualsSqr() / degreesOfFreedom) * ((numPoints) / sumMeanDiffSqr);
        
        return fitGoodness;
    }
    
    /** Get a string description of the curve fitting results
     * for easy output.
     */
    public String getResultString() {
        String results =  "\nFormula: " + getFormula() +
		"\nTime: "+time+"ms" +
        "\nNumber of iterations: " + getIterations() + " (" + getMaxIterations() + ")" +
        "\nNumber of restarts: " + (nRestarts-1) + " (" + defaultRestarts + ")" +
        "\nSum of residuals squared: " + IJ.d2s(getSumResidualsSqr(),4) +
        "\nStandard deviation: " + IJ.d2s(getSD(),4) +
        "\nR^2: " + IJ.d2s(getRSquared(),4) +
        "\nParameters:";
        char pChar = 'a';
        double[] pVal = getParams();
        for (int i = 0; i < numParams; i++) {
            results += ("\n  " + pChar + " = " + IJ.d2s(pVal[i],4));
            pChar++;
        }
        return results;
    }
        
    double sqr(double d) { return d * d; }
    
	/** Adds sum of square of residuals to end of array of parameters */
	void sumResiduals (double[] x) {
		x[numParams] = 0.0;
		if (fit==CUSTOM) {
			for (int i=0; i<numPoints; i++)
			x[numParams] = x[numParams] + sqr(f(x,xData[i])-yData[i]);
		} else {
			for (int i=0; i<numPoints; i++)
			x[numParams] = x[numParams] + sqr(f(fit,x,xData[i])-yData[i]);
		}
	}

    /** Keep the "next" vertex */
    void newVertex() {
        for (int i = 0; i < numVertices; i++)
            simp[worst][i] = next[i];
    }
    
    /** Find the worst, nextWorst and best current set of parameter estimates */
    void order() {
        for (int i = 0; i < numVertices; i++) {
            if (simp[i][numParams] < simp[best][numParams])	best = i;
            if (simp[i][numParams] > simp[worst][numParams]) worst = i;
        }
        nextWorst = best;
        for (int i = 0; i < numVertices; i++) {
            if (i != worst) {
                if (simp[i][numParams] > simp[nextWorst][numParams]) nextWorst = i;
            }
        }
        //        IJ.log("B: " + simp[best][numParams] + " 2ndW: " + simp[nextWorst][numParams] + " W: " + simp[worst][numParams]);
    }

    /** Get number of iterations performed */
    public int getIterations() {
        return numIter;
    }
    
    /** Get maximum number of iterations allowed */
    public int getMaxIterations() {
        return maxIter;
    }
    
    /** Set maximum number of iterations allowed */
    public void setMaxIterations(int x) {
        maxIter = x;
    }
    
    /** Get number of simplex restarts to do */
    public int getRestarts() {
        return defaultRestarts;
    }
    
    /** Set number of simplex restarts to do */
    public void setRestarts(int n) {
        defaultRestarts = n;
    }

	/** Sets the initial parameters, which override the default initial parameters. */
	public void setInitialParameters(double[] params) {
		initialParams = params;
	}

    /**
     * Gets index of highest value in an array.
     * 
     * @param              Double array.
     * @return             Index of highest value.
     */
    public static int getMax(double[] array) {
        double max = array[0];
        int index = 0;
        for(int i = 1; i < array.length; i++) {
            if(max < array[i]) {
            	max = array[i];
            	index = i;
            }
        }
        return index;
    }
    
	public double[] getXPoints() {
		return xData;
	}
	
	public double[] getYPoints() {
		return yData;
	}
	
	public int getFit() {
		return fit;
	}

	public String getName() {
		if (fit==CUSTOM)
			return "User-defined";
		else
			return fitList[fit];
	}

	public String getFormula() {
		if (fit==CUSTOM)
			return customFormula;
		else
			return fList[fit];
	}
	
}
