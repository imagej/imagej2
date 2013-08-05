/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import net.imglib2.type.numeric.RealType;

/**
 * A fixed point floating numeric type. Currently 25 decimal places of accuracy.
 * 
 * @author Barry DeZonia
 */
public class PreciseFixedFloatType implements RealType<PreciseFixedFloatType> {

	// TODO - use FloatingType rather than RealType but not yet merged to Imglib.
	// Once merged then implement the exponential and trig methods to a fixed
	// number of decimal places. Can take example code from BigComplex and then
	// dumb that class down to only be used for castings.

	// -- constants --

	private static final int DECIMAL_PLACES = 25; // TODO - make user configurable

	// -- fields --

	private BigInteger scale;
	private BigInteger amount;

	// -- constructors --

	public PreciseFixedFloatType() {
		scale = BigInteger.TEN.pow(DECIMAL_PLACES);
		amount = BigInteger.ZERO;
	}

	public PreciseFixedFloatType(long v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(double v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(BigInteger v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(BigDecimal v) {
		this();
		set(v);
	}

	public PreciseFixedFloatType(String numStr) {
		this();
		set(new BigDecimal(numStr));
	}

	public PreciseFixedFloatType(PreciseFixedFloatType other) {
		this.scale = other.scale;
		this.amount = other.amount;
	}

	// -- RealType methods --

	public BigDecimal get() {
		BigDecimal numer = new BigDecimal(amount);
		BigDecimal denom = new BigDecimal(scale);
		// NB - since denom power of ten we don't need precision limited division
		return numer.divide(denom);
	}

	@Override
	public double getRealDouble() {
		return get().doubleValue();
	}

	@Override
	public float getRealFloat() {
		return get().floatValue();
	}

	@Override
	public double getImaginaryDouble() {
		return 0;
	}

	@Override
	public float getImaginaryFloat() {
		return 0;
	}

	@Override
	public void setReal(float v) {
		set(v);
	}

	@Override
	public void setReal(double v) {
		set(v);
	}

	@Override
	public void setImaginary(float v) {
		// do nothing
	}

	@Override
	public void setImaginary(double v) {
		// do nothing
	}

	@Override
	public void setComplexNumber(float r, float i) {
		set(r);
	}

	@Override
	public void setComplexNumber(double r, double i) {
		set(r);
	}

	@Override
	public float getPowerFloat() {
		return getRealFloat();
	}

	@Override
	public double getPowerDouble() {
		return getRealDouble();
	}

	@Override
	public float getPhaseFloat() {
		return 0;
	}

	@Override
	public double getPhaseDouble() {
		return 0;
	}

	@Override
	public void complexConjugate() {
		// do nothing
	}

	public void negate() {
		amount = amount.negate();
	}

	public void abs() {
		if (amount.compareTo(BigInteger.ZERO) < 0) negate();
	}

	public void pow(int power) {
		if (power < 0) {
			BigDecimal factor = get();
			BigDecimal total = BigDecimal.ONE;
			for (int i = 1; i < (-power); i++) {
				total = total.divide(factor, DECIMAL_PLACES, RoundingMode.HALF_UP);
			}
			amount = total.multiply(new BigDecimal(scale)).toBigInteger();
		}
		else if (power == 0) {
			amount = scale; // value = ONE
		}
		else { // power > 0
			BigInteger factor = amount;
			// if power == 1 we are done so skip that case in for loop
			for (int p = 1; p < power; p++) {
				amount = amount.multiply(factor);
			}
		}
	}

	@Override
	public void add(PreciseFixedFloatType v) {
		add(this, v);
	}

	public void add(PreciseFixedFloatType a, PreciseFixedFloatType b) {
		amount = a.amount.add(b.amount);
	}

	@Override
	public void sub(PreciseFixedFloatType v) {
		sub(this, v);
	}

	public void sub(PreciseFixedFloatType a, PreciseFixedFloatType b) {
		amount = a.amount.subtract(b.amount);
	}

	@Override
	public void mul(PreciseFixedFloatType v) {
		mul(this, v);
	}

	public void mul(PreciseFixedFloatType a, PreciseFixedFloatType b) {
		amount = a.amount.multiply(b.amount);
	}

	@Override
	public void div(PreciseFixedFloatType v) {
		div(this, v);
	}

	public void div(PreciseFixedFloatType a, PreciseFixedFloatType b) {
		amount = a.amount.divide(b.amount);
	}

	@Override
	public void setZero() {
		amount = BigInteger.ZERO;
	}

	@Override
	public void setOne() {
		this.amount = scale;
	}

	@Override
	public void mul(float v) {
		mul(BigDecimal.valueOf(v));
	}

	@Override
	public void mul(double v) {
		mul(BigDecimal.valueOf(v));
	}

	public void mul(BigInteger v) {
		amount = amount.multiply(v);
	}

	public void mul(BigDecimal v) {
		BigDecimal integer = new BigDecimal(amount);
		BigDecimal number = integer.multiply(v);
		amount = number.toBigInteger();
	}

	@Override
	public PreciseFixedFloatType createVariable() {
		return new PreciseFixedFloatType();
	}

	@Override
	public PreciseFixedFloatType copy() {
		return new PreciseFixedFloatType(this);
	}

	@Override
	public void set(PreciseFixedFloatType other) {
		this.scale = other.scale;
		this.amount = other.amount;
	}

	public void set(double v) {
		BigDecimal d = BigDecimal.valueOf(v);
		BigDecimal factor = new BigDecimal(scale);
		BigDecimal number = d.multiply(factor);
		amount = number.toBigInteger();
	}

	public void set(long v) {
		amount = BigInteger.valueOf(v).multiply(scale);
	}

	public void set(BigInteger v) {
		amount = v.multiply(scale);
	}

	public void set(BigDecimal v) {
		BigDecimal scaled = v.multiply(new BigDecimal(scale));
		amount = scaled.toBigInteger();
	}

	@Override
	public int compareTo(PreciseFixedFloatType other) {
		return amount.compareTo(other.amount);
	}

	@Override
	public void inc() {
		amount = amount.add(scale);
	}

	@Override
	public void dec() {
		amount = amount.subtract(scale);
	}

	@Override
	public double getMaxValue() {
		return Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinValue() {
		return -Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinIncrement() {
		return 1.0 / Math.pow(10, DECIMAL_PLACES); // TODO - prone to precision loss
	}

	@Override
	public int getBitsPerPixel() {
		return 1024; // TODO - a WAG : nothing makes sense here. Use DataType.
	}

	@Override
	public String toString() {
		return get().toString();
	}

	private static final PreciseFixedFloatType ZERO = new PreciseFixedFloatType();
	private static final PreciseFixedFloatType ONE = new PreciseFixedFloatType(1);
	private static final PreciseFixedFloatType TWO = new PreciseFixedFloatType(2);
	// NB - PI limited to 50 decimal places of precision so narrowing possible
	private static final PreciseFixedFloatType PI = new PreciseFixedFloatType(
		"3.14159265358979323846264338327950288419716939937510");
	private static PreciseFixedFloatType[] ANGLES;
	private static PreciseFixedFloatType[] POWERS_OF_TWO;
	private static PreciseFixedFloatType SQRT_PRE;
	static {
		ANGLES = angles(); // must precede powersOfTwo() call
		POWERS_OF_TWO = powersOfTwo();
		SQRT_PRE = sqrtPrecision();
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url 
	 *      http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 */
	public static PreciseFixedFloatType sqrt(PreciseFixedFloatType c) {
		PreciseFixedFloatType precision = new PreciseFixedFloatType();
		precision.div(ONE, SQRT_PRE);
		return sqrtNewtonRaphson(c, ONE, precision);
	}

	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url 
	 *      http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 * @param xn
	 * @param precision
	 */
	private static PreciseFixedFloatType sqrtNewtonRaphson(
		PreciseFixedFloatType c, PreciseFixedFloatType xn,
		PreciseFixedFloatType precision)
	{
		PreciseFixedFloatType negC = c.copy();
		negC.negate();
		PreciseFixedFloatType fx = new PreciseFixedFloatType();
		PreciseFixedFloatType fpx = new PreciseFixedFloatType();
		PreciseFixedFloatType xn1 = new PreciseFixedFloatType();
		PreciseFixedFloatType currentPrecision = new PreciseFixedFloatType();
		PreciseFixedFloatType currentSquare = new PreciseFixedFloatType();
		xn.add(negC);
		fx.set(xn);
		fx.pow(2);
		fx.add(negC);
		fpx.mul(xn, TWO);
		xn1.div(fx, fpx);
		PreciseFixedFloatType negXn1 = new PreciseFixedFloatType(xn1);
		negXn1.negate();
		xn1.add(xn, negXn1);
		currentSquare.set(xn1);
		currentSquare.pow(2);
		currentPrecision.sub(currentSquare, c);
		currentPrecision.abs();
		if (currentPrecision.compareTo(precision) < 0) {
			return xn1;
		}
		return sqrtNewtonRaphson(c, xn1, precision);
	}

	// this code taken from: http://en.wikipedia.org/wiki/Cordic
	// and http://bsvi.ru/uploads/CORDIC--_10EBA/cordic.pdf

	public static PreciseFixedFloatType atan2(PreciseFixedFloatType y,
		PreciseFixedFloatType x)
	{
		PreciseFixedFloatType tx = x.copy();
		PreciseFixedFloatType ty = y.copy();
		PreciseFixedFloatType angle = new PreciseFixedFloatType(0);
		if (tx.compareTo(ZERO) < 0) {
			angle.set(PI);
			tx.negate();
			ty.negate();
		}
		else if (ty.compareTo(ZERO) < 0) angle.mul(TWO, PI);

		PreciseFixedFloatType xNew = new PreciseFixedFloatType();
		PreciseFixedFloatType yNew = new PreciseFixedFloatType();
		PreciseFixedFloatType dx = new PreciseFixedFloatType();
		PreciseFixedFloatType dy = new PreciseFixedFloatType();

		for (int j = 0; j < ANGLES.length; j++) {
			PreciseFixedFloatType twoPowJ = POWERS_OF_TWO[j];
			dy.div(ty, twoPowJ);
			dx.div(tx, twoPowJ);
			if (ty.compareTo(ZERO) < 0) {
				// Rotate counter-clockwise
				xNew.sub(tx, dy);
				yNew.add(ty, dx);
				angle.sub(ANGLES[j]);
			}
			else {
				// Rotate clockwise
				xNew.add(tx, dy);
				yNew.sub(ty, dx);
				angle.add(ANGLES[j]);
			}
			tx.set(xNew);
			ty.set(yNew);
		}
		return angle;
	}

	/*
		for (int j = 0; j < ANGLES.length; j++) {
			BigDecimal twoPowJ = POWERS_OF_TWO[j];
			if (ty.compareTo(BigDecimal.ZERO) < 0) {
				// Rotate counter-clockwise 
				xNew = tx.subtract(ty.divide(twoPowJ));
				yNew = ty.add(tx.divide(twoPowJ));
				angle = angle.subtract(ANGLES[j]);
			}
			else {
				// Rotate clockwise 
				xNew = tx.add(ty.divide(twoPowJ));
				yNew = ty.subtract(tx.divide(twoPowJ));
				angle = angle.add(ANGLES[j]);
			}
			tx = xNew;
			ty = yNew;
		}
	*/
	// ATAN helpers

	// To increase precision: keep adding angles from wolfram alpha. One can see
	// precision by counting leading zeros of last entry in table below. More
	// angles requires more processing time. It takes 3 or 4 angles to increase
	// precision by one place.

	private static PreciseFixedFloatType[] angles() {
		return new PreciseFixedFloatType[] {
			// taken from wolfram alpha: entry i = atan(2^(-(i))
			new PreciseFixedFloatType(
				"0.7853981633974483096156608458198757210492923498437764"),
			new PreciseFixedFloatType(
				"0.4636476090008061162142562314612144020285370542861202"),
			new PreciseFixedFloatType(
				"0.2449786631268641541720824812112758109141440983811840"),
			new PreciseFixedFloatType(
				"0.1243549945467614350313548491638710255731701917698040"),
			new PreciseFixedFloatType(
				"0.0624188099959573484739791129855051136062738877974991"),
			new PreciseFixedFloatType(
				"0.0312398334302682762537117448924909770324956637254000"),
			new PreciseFixedFloatType(
				"0.0156237286204768308028015212565703189111141398009054"),
			new PreciseFixedFloatType(
				"0.0078123410601011112964633918421992816212228117250147"),
			new PreciseFixedFloatType(
				"0.0039062301319669718276286653114243871403574901152028"),
			new PreciseFixedFloatType(
				"0.0019531225164788186851214826250767139316107467772335"),
			new PreciseFixedFloatType(
				"0.0009765621895593194304034301997172908516341970158100"),
			new PreciseFixedFloatType(
				"0.0004882812111948982754692396256448486661923611331350"),
			new PreciseFixedFloatType(
				"0.0002441406201493617640167229432596599862124177909706"),
			new PreciseFixedFloatType(
				"0.0001220703118936702042390586461179563009308294090157"),
			new PreciseFixedFloatType(
				"0.0000610351561742087750216625691738291537851435368333"),
			new PreciseFixedFloatType(
				"0.0000305175781155260968618259534385360197509496751194"),
			new PreciseFixedFloatType(
				"0.0000152587890613157621072319358126978851374292381445"),
			new PreciseFixedFloatType(
				"0.0000076293945311019702633884823401050905863507439184"),
			new PreciseFixedFloatType(
				"0.0000038146972656064962829230756163729937228052573039"),
			new PreciseFixedFloatType(
				"0.0000019073486328101870353653693059172441687143421654"),
			new PreciseFixedFloatType(
				"0.00000095367431640596087942067068992311239001963412449"),
			new PreciseFixedFloatType(
				"0.00000047683715820308885992758382144924707587049404378"),
			new PreciseFixedFloatType(
				"0.00000023841857910155798249094797721893269783096898769"),
			new PreciseFixedFloatType(
				"0.00000011920928955078068531136849713792211264596758766"),
			new PreciseFixedFloatType(
				"0.000000059604644775390554413921062141788874250030195782"),
			new PreciseFixedFloatType(
				"0.000000029802322387695303676740132767709503349043907067"),
			new PreciseFixedFloatType(
				"0.000000014901161193847655147092516595963247108248930025"),
			new PreciseFixedFloatType(
				"0.0000000074505805969238279871365645744953921132066925545"),
			new PreciseFixedFloatType(
				"0.0000000037252902984619140452670705718119235836719483287"),
			new PreciseFixedFloatType(
				"0.0000000018626451492309570290958838214764904345065282835"),
			new PreciseFixedFloatType(
				"0.0000000009313225746154785153557354776845613038929264961"),
			new PreciseFixedFloatType(
				"0.0000000004656612873077392577788419347105701629734786389"),
			new PreciseFixedFloatType(
				"0.0000000002328306436538696289020427418388212703712742932"),
			new PreciseFixedFloatType(
				"0.0000000001164153218269348144525990927298526587963964573"),
			new PreciseFixedFloatType(
				"0.00000000005820766091346740722649676159123158234954915625"),
			new PreciseFixedFloatType(
				"0.00000000002910383045673370361327303269890394779369363200"),
			new PreciseFixedFloatType(
				"0.00000000001455191522836685180663959783736299347421170360"),
			new PreciseFixedFloatType(
				"0.000000000007275957614183425903320184104670374184276462938"),
			new PreciseFixedFloatType(
				"0.000000000003637978807091712951660140200583796773034557866"),
			new PreciseFixedFloatType(
				"0.000000000001818989403545856475830076118822974596629319733"),
			new PreciseFixedFloatType(
				"0.0000000000009094947017729282379150388117278718245786649666"),
			new PreciseFixedFloatType(
				"0.0000000000004547473508864641189575194999034839780723331208"),
			new PreciseFixedFloatType(
				"0.0000000000002273736754432320594787597617066854972590416401"),
			new PreciseFixedFloatType(
				"0.0000000000001136868377216160297393798823227106871573802050"),
			new PreciseFixedFloatType(
				"0.00000000000005684341886080801486968994134502633589467252562") };
	}

	private static PreciseFixedFloatType[] powersOfTwo() {
		PreciseFixedFloatType[] powers = new PreciseFixedFloatType[ANGLES.length];
		PreciseFixedFloatType term = new PreciseFixedFloatType(1);
		for (int i = 0; i < ANGLES.length; i++) {
			powers[i] = term.copy();
			term.mul(TWO);
		}
		return powers;
	}

	private static PreciseFixedFloatType sqrtPrecision() {
		PreciseFixedFloatType prec = new PreciseFixedFloatType(10);
		prec.pow(DECIMAL_PLACES);
		return prec;
	}

	/* useful if we implement sine and cosine
	private static final BigDecimal[] K_VALUES = new BigDecimal[MAX_ATAN_ITERS];
	static {
		// K(0) = (1 / sqrt(1 + (2^(-(2*0)))))
		// K(1) = K(0) * (1 / sqrt(1 + (2^(-(2*1)))))
		// K(2) = K(1) * (1 / sqrt(1 + (2^(-(2*2)))))
		// etc.
		BigDecimal prev = BigDecimal.ONE;
		for (int i = 0; i < MAX_ATAN_ITERS; i++) {
			int power = -2 * i;
			BigDecimal factor = TWO.pow(power);
			BigDecimal sum = BigDecimal.ONE.add(factor);
			BigDecimal root =
				sqrtNewtonRaphson(sum, BigDecimal.ONE, BigDecimal.ONE.divide(SQRT_PRE));
			BigDecimal term = BigDecimal.ONE.divide(root);
			K_VALUES[i] = term.multiply(prev);
			prev = K_VALUES[i];
		}
	}
	 */
}
