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

import net.imglib2.type.numeric.ComplexType;

/**
 * A complex number that stores values in BigDecimal (arbitrary) precision.
 * Besides providing precise numeric operations this class is useful for
 * supporting DataType translations with minimal data loss. Some methods may
 * round values to 50 decimal places of precision.
 * 
 * @author Barry DeZonia
 */
public class BigComplex implements ComplexType<BigComplex> {

	// TODO - use FloatingType rather than ComplexType but not yet merged to
	// Imglib. Once merged then implement the exponential and trig methods to a
	// fixed number of decimal places.

	// TODO - make decimal place accuracy a setting. This is easily possible. It
	// can have an upper limit determined by the number of leading zeroes present
	// in the last entry of the ANGLES table. Also limit cannot exceed our
	// representation of E and PI. The lower limit can be 1.

	// -- constants --

	private static final int DIGITS = 50;
	private static final BigDecimal TWO = new BigDecimal(2);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(DIGITS);
	// NB - E & PI limited to 50 decimal places of precision so narrowing possible
	private static final BigDecimal PI = new BigDecimal(
		"3.14159265358979323846264338327950288419716939937510");
	private static final BigDecimal E = new BigDecimal(
		"2.71828182845904523536028747135266249775724709369995");

	// -- static variables and initialization --

	private static final BigDecimal[] ANGLES;
	private static final BigDecimal[] POWERS_OF_TWO;

	static {
		ANGLES = angles();
		POWERS_OF_TWO = powersOfTwo(ANGLES.length);
	}

	// -- fields --

	private BigDecimal r, i;

	// -- constructors --

	/**
	 * Default constructor: value = (0,0).
	 */
	public BigComplex() {
		setZero();
	}

	/**
	 * Constructor from longs.
	 */
	public BigComplex(long r, long i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Constructor from doubles.
	 */
	public BigComplex(double r, double i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Constructor from BigIntegers.
	 */
	public BigComplex(BigInteger r, BigInteger i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Constructor from BigDecimals.
	 */
	public BigComplex(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Constructor from Strings. The strings must represent numbers that
	 * BigDecimal can parse.
	 */
	public BigComplex(String r, String i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Gets real component of this complex number as a BigDecimal.
	 */
	public BigDecimal getReal() {
		return r;
	}

	/**
	 * Gets imaginary component of this complex number as a BigDecimal.
	 */
	public BigDecimal getImag() {
		return i;
	}

	// -- setters --

	/**
	 * Sets the real and imaginary components of this BigComplex to match those of
	 * another.
	 */
	@Override
	public void set(BigComplex other) {
		this.r = other.r;
		this.i = other.i;
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given long
	 * values.
	 */
	public void set(long r, long i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given double
	 * values.
	 */
	public void set(double r, double i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given
	 * BigInteger values.
	 */
	public void set(BigInteger r, BigInteger i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given
	 * BigDecimal values.
	 */
	public void set(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given String
	 * values. The strings must represent numbers that BigDecimal can parse.
	 */
	public void set(String r, String i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real component of this BigComplex to given long value.
	 */
	public void setReal(long r) {
		this.r = BigDecimal.valueOf(r);
	}

	/**
	 * Sets the real component of this BigComplex to given float value.
	 */
	@Override
	public void setReal(float f) {
		r = BigDecimal.valueOf(f);
	}

	/**
	 * Sets the real component of this BigComplex to given double value.
	 */
	@Override
	public void setReal(double r) {
		this.r = BigDecimal.valueOf(r);
	}

	/**
	 * Sets the real component of this BigComplex to given BigInteger value.
	 */
	public void setReal(BigInteger r) {
		this.r = new BigDecimal(r);
	}

	/**
	 * Sets the real component of this BigComplex to given BigDecimal value.
	 */
	public void setReal(BigDecimal r) {
		this.r = r;
	}

	/**
	 * Sets the real component of this BigComplex to given String value. The
	 * string must represent a number that BigDecimal can parse.
	 */
	public void setReal(String r) {
		this.r = new BigDecimal(r);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given long value.
	 */
	public void setImag(long i) {
		this.i = BigDecimal.valueOf(i);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given double value.
	 */
	public void setImag(double i) {
		this.i = BigDecimal.valueOf(i);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given BigInteger value.
	 */
	public void setImag(BigInteger i) {
		this.i = new BigDecimal(i);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given BigDecimal value.
	 */
	public void setImag(BigDecimal i) {
		this.i = i;
	}

	/**
	 * Sets the imaginary component of this BigComplex to given String value. The
	 * string must represent a number that BigDecimal can parse.
	 */
	public void setImag(String i) {
		this.i = new BigDecimal(i);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given float value.
	 */
	@Override
	public void setImaginary(float f) {
		i = BigDecimal.valueOf(f);
	}

	/**
	 * Sets the imaginary component of this BigComplex to given double value.
	 */
	@Override
	public void setImaginary(double f) {
		i = BigDecimal.valueOf(f);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given float
	 * values.
	 */
	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given double
	 * values.
	 */
	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		setImag(i);
	}

	/**
	 * Sets the real and imaginary components of this BigComplex to given
	 * BigDecimal values.
	 */
	public void setComplexNumber(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	// -- ComplexType methods --

	/**
	 * Sets this BigComplex to the zero value.
	 */
	@Override
	public void setZero() {
		r = BigDecimal.ZERO;
		i = BigDecimal.ZERO;
	}

	/**
	 * Sets this BigComplex to the unity value.
	 */
	@Override
	public void setOne() {
		r = BigDecimal.ONE;
		i = BigDecimal.ZERO;
	}

	/**
	 * Creates a new BigComplex initialized to (0,0).
	 */
	@Override
	public BigComplex createVariable() {
		return new BigComplex();
	}

	/**
	 * Creates a new BigComplex whose values are taken from this BigComplex.
	 */
	@Override
	public BigComplex copy() {
		return new BigComplex(r, i);
	}

	/**
	 * Set self to the result of addition between two BigComplex values.
	 */
	public void add(BigComplex a, BigComplex b) {
		r = a.r.add(b.r);
		i = a.i.add(b.i);
	}

	/**
	 * Adds another BigComplex value to self.
	 */
	@Override
	public void add(BigComplex other) {
		add(this, other);
	}

	/**
	 * Set self to the result of subtraction between two BigComplex values.
	 */
	public void sub(BigComplex a, BigComplex b) {
		r = a.r.subtract(b.r);
		i = a.i.subtract(b.i);
	}

	/**
	 * Subtracts another BigComplex value from self.
	 */
	@Override
	public void sub(BigComplex other) {
		sub(this, other);
	}

	/**
	 * Set self to the result of multiplication between two BigComplex values.
	 */
	public void mul(BigComplex a, BigComplex b) {
		BigDecimal t1 = a.r.multiply(b.r);
		BigDecimal t2 = a.i.multiply(b.i);
		BigDecimal sum1 = t1.subtract(t2);
		t1 = a.i.multiply(b.r);
		t2 = a.r.multiply(b.i);
		BigDecimal sum2 = t1.add(t2);
		r = sum1;
		i = sum2;
	}

	/**
	 * Multiplies another BigComplex value with self.
	 */
	@Override
	public void mul(BigComplex other) {
		mul(this, other);
	}
	
	/**
	 * Set self to the result of division between two BigComplex values. Precision
	 * loss is possible.
	 */
	public void div(BigComplex a, BigComplex b) {
		BigDecimal t1 = b.r.multiply(b.r);
		BigDecimal t2 = b.i.multiply(b.i);
		BigDecimal denom = t1.add(t2);
		t1 = a.r.multiply(b.r);
		t2 = a.i.multiply(b.i);
		BigDecimal sum1 = t1.add(t2);
		t1 = a.i.multiply(b.r);
		t2 = a.r.multiply(b.i);
		BigDecimal sum2 = t1.subtract(t2);
		r = sum1.divide(denom, DIGITS, RoundingMode.HALF_UP);
		i = sum2.divide(denom, DIGITS, RoundingMode.HALF_UP);
	}

	/**
	 * Divides self by another BigComplex value. Precision loss is possible.
	 */
	@Override
	public void div(BigComplex other) {
		div(this, other);
	}

	/**
	 * Multiplies self by a scalar (float) constant.
	 */
	@Override
	public void mul(float c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	/**
	 * Multiplies self by a scalar (double) constant.
	 */
	@Override
	public void mul(double c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	/**
	 * Does complex conjugation on self.
	 */
	@Override
	public void complexConjugate() {
		i = i.negate();
	}

	// -- narrowing methods --

	/**
	 * Gets real component as a double (narrowing possible).
	 */
	@Override
	public double getRealDouble() {
		return r.doubleValue();
	}

	/**
	 * Gets real component as a float (narrowing possible).
	 */
	@Override
	public float getRealFloat() {
		return r.floatValue();
	}

	/**
	 * Gets imaginary component as a double (narrowing possible).
	 */
	@Override
	public double getImaginaryDouble() {
		return i.doubleValue();
	}

	/**
	 * Gets imaginary component as a float (narrowing possible).
	 */
	@Override
	public float getImaginaryFloat() {
		return i.floatValue();
	}

	/**
	 * Gets magnitude as a float (narrowing possible).
	 */
	@Override
	public float getPowerFloat() {
		return modulus().floatValue();
	}

	/**
	 * Gets magnitude as a double (narrowing possible).
	 */
	@Override
	public double getPowerDouble() {
		return modulus().doubleValue();
	}

	/**
	 * Gets magnitude as a BigDecimal.
	 */
	public BigDecimal getPower() {
		return modulus();
	}

	/**
	 * Gets phase as a float (narrowing possible).
	 */
	@Override
	public float getPhaseFloat() {
		return phase().floatValue();
	}

	/**
	 * Gets phase as a double (narrowing possible).
	 */
	@Override
	public double getPhaseDouble() {
		return phase().doubleValue();
	}

	/**
	 * Gets phase as a BigDecimal.
	 */
	public BigDecimal getPhase() {
		return phase();
	}

	/**
	 * Fills self with the representation of pi for the given type.
	 */
	public void PI() {
		setReal(PI);
		setImag(BigDecimal.ZERO);
	}

	/**
	 * Fills self with the representation of e for the given type.
	 */
	public void E() {
		setReal(E);
		setImag(BigDecimal.ZERO);
	}

// TODO - implement these. Can pull code out of OPS since methods already exist.
// There is also a ComplexMath class on the floating-types branch of Imglib that
// shows how to do these.
//
//	/**
//	 * Fills self with result of raising e to the power of the passed as an input.
//	 */
//	public void exp(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the sqrt of the passed in input.
//	 */
//	public void sqrt(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the log of the passed in input.
//	 */
//	public void log(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of raising the passed in input to the given power.
//	 */
//	public void pow(BigComplex input, BigComplex power) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the log (of provided base) of the passed
//	 * in input.
//	 */
//	public void logBase(BigComplex input, BigComplex base) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the sin of the passed in input.
//	 */
//	public void sin(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the cos of the passed in input.
//	 */
//	public void cos(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the tan of the passed in input.
//	 */
//	public void tan(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the asin of the passed in input.
//	 */
//	public void asin(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the acos of the passed in input.
//	 */
//	public void acos(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the atan of the passed in input.
//	 */
//	public void atan(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the sinh of the passed in input.
//	 */
//	public void sinh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the cosh of the passed in input.
//	 */
//	public void cosh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the tanh of the passed in input.
//	 */
//	public void tanh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the asinh of the passed in input.
//	 */
//	public void asinh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the acosh of the passed in input.
//	 */
//	public void acosh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}
//
//	/**
//	 * Fills self with result of taking the atanh of the passed in input.
//	 */
//	public void atanh(BigComplex input) {
//		throw new IllegalArgumentException("TODO");
//	}

	// -- helpers --

	private BigDecimal modulus() {
		BigDecimal a = r.multiply(r);
		BigDecimal b = i.multiply(i);
		BigDecimal sum = a.add(b);
		return bigSqrt(sum);
	}

	// TODO - although javadoc specifies 50 decimal places of accuracy this calc
	// is limited by the accuracy of the numbers in the ANGLES table. As of 8-7-13
	// that is 17 decimal places.

	private BigDecimal phase() {
		return atan2(i, r);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url 
	 *      http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 */
	private static BigDecimal bigSqrt(BigDecimal c) {
		BigDecimal precision =
			BigDecimal.ONE.divide(SQRT_PRE, DIGITS, RoundingMode.HALF_UP);
		return sqrtNewtonRaphson(c, BigDecimal.ONE, precision);
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
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn,
		BigDecimal precision)
	{
		BigDecimal fx = xn.pow(2).add(c.negate());
		BigDecimal fpx = xn.multiply(TWO);
		BigDecimal xn1 = fx.divide(fpx, 2 * DIGITS, RoundingMode.HALF_DOWN);
		xn1 = xn.add(xn1.negate());
		BigDecimal currentSquare = xn1.pow(2);
		BigDecimal currentPrecision = currentSquare.subtract(c);
		currentPrecision = currentPrecision.abs();
		if (currentPrecision.compareTo(precision) <= 0) {
			return xn1;
		}
		return sqrtNewtonRaphson(c, xn1, precision);
	}

	// this code taken from: http://en.wikipedia.org/wiki/Cordic
	// and http://bsvi.ru/uploads/CORDIC--_10EBA/cordic.pdf

	private BigDecimal atan2(BigDecimal y, BigDecimal x) {
		BigDecimal tx = x;
		BigDecimal ty = y;
		BigDecimal angle = BigDecimal.ZERO;
		if (tx.compareTo(BigDecimal.ZERO) < 0) {
			angle = PI;
			tx = tx.negate();
			ty = ty.negate();
		}
		else if (ty.compareTo(BigDecimal.ZERO) < 0) angle = TWO.multiply(PI);

		BigDecimal xNew, yNew;

		for (int j = 0; j < ANGLES.length; j++) {
			BigDecimal twoPowJ = POWERS_OF_TWO[j];
			BigDecimal dx = tx.divide(twoPowJ, DIGITS, RoundingMode.HALF_UP);
			BigDecimal dy = ty.divide(twoPowJ, DIGITS, RoundingMode.HALF_UP);
			if (ty.compareTo(BigDecimal.ZERO) < 0) {
				// Rotate counter-clockwise
				xNew = tx.subtract(dy);
				yNew = ty.add(dx);
				angle = angle.subtract(ANGLES[j]);
			}
			else {
				// Rotate clockwise
				xNew = tx.add(dy);
				yNew = ty.subtract(dx);
				angle = angle.add(ANGLES[j]);
			}
			tx = xNew;
			ty = yNew;
		}
		return angle;
	}

	// ATAN helpers

	// To increase precision: keep adding angles from wolfram alpha. One can see
	// precision by counting leading zeros of last entry in table below. More
	// angles requires more processing time. It takes 3 or 4 angles to increase
	// precision by one place.

	private static BigDecimal[] angles() {
		return new BigDecimal[] {
			// taken from wolfram alpha: entry i = atan(2^(-(i))
			new BigDecimal("0.7853981633974483096156608458198757210492923498437764"),
			new BigDecimal("0.4636476090008061162142562314612144020285370542861202"),
			new BigDecimal("0.2449786631268641541720824812112758109141440983811840"),
			new BigDecimal("0.1243549945467614350313548491638710255731701917698040"),
			new BigDecimal("0.0624188099959573484739791129855051136062738877974991"),
			new BigDecimal("0.0312398334302682762537117448924909770324956637254000"),
			new BigDecimal("0.0156237286204768308028015212565703189111141398009054"),
			new BigDecimal("0.0078123410601011112964633918421992816212228117250147"),
			new BigDecimal("0.0039062301319669718276286653114243871403574901152028"),
			new BigDecimal("0.0019531225164788186851214826250767139316107467772335"),
			new BigDecimal("0.0009765621895593194304034301997172908516341970158100"),
			new BigDecimal("0.0004882812111948982754692396256448486661923611331350"),
			new BigDecimal("0.0002441406201493617640167229432596599862124177909706"),
			new BigDecimal("0.0001220703118936702042390586461179563009308294090157"),
			new BigDecimal("0.0000610351561742087750216625691738291537851435368333"),
			new BigDecimal("0.0000305175781155260968618259534385360197509496751194"),
			new BigDecimal("0.0000152587890613157621072319358126978851374292381445"),
			new BigDecimal("0.0000076293945311019702633884823401050905863507439184"),
			new BigDecimal("0.0000038146972656064962829230756163729937228052573039"),
			new BigDecimal("0.0000019073486328101870353653693059172441687143421654"),
			new BigDecimal("0.00000095367431640596087942067068992311239001963412449"),
			new BigDecimal("0.00000047683715820308885992758382144924707587049404378"),
			new BigDecimal("0.00000023841857910155798249094797721893269783096898769"),
			new BigDecimal("0.00000011920928955078068531136849713792211264596758766"),
			new BigDecimal("0.000000059604644775390554413921062141788874250030195782"),
			new BigDecimal("0.000000029802322387695303676740132767709503349043907067"),
			new BigDecimal("0.000000014901161193847655147092516595963247108248930025"),
			new BigDecimal(
				"0.0000000074505805969238279871365645744953921132066925545"),
			new BigDecimal(
				"0.0000000037252902984619140452670705718119235836719483287"),
			new BigDecimal(
				"0.0000000018626451492309570290958838214764904345065282835"),
			new BigDecimal(
				"0.0000000009313225746154785153557354776845613038929264961"),
			new BigDecimal(
				"0.0000000004656612873077392577788419347105701629734786389"),
			new BigDecimal(
				"0.0000000002328306436538696289020427418388212703712742932"),
			new BigDecimal(
				"0.0000000001164153218269348144525990927298526587963964573"),
			new BigDecimal(
				"0.00000000005820766091346740722649676159123158234954915625"),
			new BigDecimal(
				"0.00000000002910383045673370361327303269890394779369363200"),
			new BigDecimal(
				"0.00000000001455191522836685180663959783736299347421170360"),
			new BigDecimal(
				"0.000000000007275957614183425903320184104670374184276462938"),
			new BigDecimal(
				"0.000000000003637978807091712951660140200583796773034557866"),
			new BigDecimal(
				"0.000000000001818989403545856475830076118822974596629319733"),
			new BigDecimal(
				"0.0000000000009094947017729282379150388117278718245786649666"),
			new BigDecimal(
				"0.0000000000004547473508864641189575194999034839780723331208"),
			new BigDecimal(
				"0.0000000000002273736754432320594787597617066854972590416401"),
			new BigDecimal(
				"0.0000000000001136868377216160297393798823227106871573802050"),
			new BigDecimal(
				"0.00000000000005684341886080801486968994134502633589467252562"),
			new BigDecimal(
				"0.00000000000002842170943040400743484497069547204198683406570"),
			new BigDecimal(
				"0.00000000000001421085471520200371742248535060588024835425821"),
			new BigDecimal(
				"0.000000000000007105427357601001858711242675661672531044282276"),
			new BigDecimal(
				"0.000000000000003552713678800500929355621337875677816380535284"),
			new BigDecimal(
				"0.000000000000001776356839400250464677810668943444102047566910"),
			new BigDecimal(
				"0.0000000000000008881784197001252323389053344724227002559458638"),
			new BigDecimal(
				"0.0000000000000004440892098500626161694526672362989312819932329"),
			new BigDecimal(
				"0.0000000000000002220446049250313080847263336181604132852491541"),
			new BigDecimal(
				"0.0000000000000001110223024625156540423631668090815750981561442"),
			new BigDecimal(
				"0.00000000000000005551115123125782702118158340454095860601951803"),
			new BigDecimal(
				"0.00000000000000002775557561562891351059079170227050068512743975"),
			new BigDecimal(
				"0.00000000000000001387778780781445675529539585113525301532842996"),
			new BigDecimal(
				"0.000000000000000006938893903907228377647697925567626841759803746") };
	}

	private static BigDecimal[] powersOfTwo(int length) {
		BigDecimal[] powers = new BigDecimal[length];
		BigDecimal power = BigDecimal.ONE;
		for (int i = 0; i < length; i++) {
			powers[i] = power;
			power = power.multiply(TWO);
		}
		return powers;
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