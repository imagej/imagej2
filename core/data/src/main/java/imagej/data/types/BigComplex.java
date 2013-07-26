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
import java.math.RoundingMode;

import net.imglib2.type.numeric.ComplexType;

/**
 * A complex number that stores values in BigDecimal (arbitrary) precision. This
 * class is useful for supporting DataType translations with minimal data loss.
 * 
 * @author Barry DeZonia
 */
public class BigComplex implements ComplexType<BigComplex> {

	// TODO - if we implement FloatingType interface then this class will be
	// tricky to implement. acos, tanh, sin, atanh, etc.

	private BigDecimal r, i;

	public BigComplex() {
		setZero();
	}

	public BigComplex(BigDecimal r, BigDecimal i) {
		setReal(r);
		setImag(i);
	}

	public BigComplex(double r, double i) {
		setReal(BigDecimal.valueOf(r));
		setImag(BigDecimal.valueOf(i));
	}

	public BigDecimal getReal() {
		return r;
	}

	public BigDecimal getImag() {
		return i;
	}

	public void setReal(BigDecimal r) {
		this.r = r;
	}

	public void setImag(BigDecimal i) {
		this.i = i;
	}

	@Override
	public void add(BigComplex c) {
		r = r.add(c.r);
		i = i.add(c.i);
	}

	@Override
	public void sub(BigComplex c) {
		r = r.subtract(c.r);
		i = i.subtract(c.i);
	}

	@Override
	public void mul(BigComplex c) {
		BigDecimal a = r.multiply(c.r);
		BigDecimal b = i.multiply(c.i);
		BigDecimal sum1 = a.subtract(b);
		a = i.multiply(c.r);
		a = r.multiply(c.i);
		BigDecimal sum2 = a.add(b);
		r = sum1;
		i = sum2;
	}

	@Override
	public void div(BigComplex c) {
		BigDecimal a = c.r.multiply(c.r);
		BigDecimal b = c.r.multiply(c.r);
		BigDecimal denom = a.add(b);
		a = r.multiply(c.r);
		b = i.multiply(c.i);
		BigDecimal sum1 = a.add(b);
		a = i.multiply(c.r);
		b = r.multiply(c.i);
		BigDecimal sum2 = a.subtract(b);
		r = sum1.divide(denom);
		i = sum2.divide(denom);
	}

	@Override
	public void setZero() {
		r = BigDecimal.ZERO;
		i = BigDecimal.ZERO;
	}

	@Override
	public void setOne() {
		r = BigDecimal.ONE;
		i = BigDecimal.ZERO;
	}

	@Override
	public void mul(float c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	@Override
	public void mul(double c) {
		mul(new BigComplex(BigDecimal.valueOf(c), BigDecimal.ZERO));
	}

	@Override
	public BigComplex createVariable() {
		return new BigComplex();
	}

	@Override
	public BigComplex copy() {
		return new BigComplex(r, i);
	}

	@Override
	public void set(BigComplex c) {
		this.r = c.r;
		this.i = c.i;
	}

	@Override
	public void complexConjugate() {
		i = i.negate();
	}

	// -- narrowing methods --

	@Override
	public double getRealDouble() {
		return r.doubleValue();
	}

	@Override
	public float getRealFloat() {
		return r.floatValue();
	}

	@Override
	public double getImaginaryDouble() {
		return i.doubleValue();
	}

	@Override
	public float getImaginaryFloat() {
		return i.floatValue();
	}

	@Override
	public void setReal(float f) {
		r = BigDecimal.valueOf(f);
	}

	@Override
	public void setReal(double f) {
		r = BigDecimal.valueOf(f);
	}

	@Override
	public void setImaginary(float f) {
		i = BigDecimal.valueOf(f);
	}

	@Override
	public void setImaginary(double f) {
		i = BigDecimal.valueOf(f);
	}

	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		setImaginary(i);
	}

	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		setImaginary(i);
	}

	@Override
	public float getPowerFloat() {
		return modulus().floatValue();
	}

	@Override
	public double getPowerDouble() {
		return modulus().doubleValue();
	}

	@Override
	public float getPhaseFloat() {
		return phase().floatValue();
	}

	@Override
	public double getPhaseDouble() {
		return phase().doubleValue();
	}

	// -- helpers --

	private BigDecimal modulus() {
		BigDecimal a = r.multiply(r);
		BigDecimal b = i.multiply(i);
		BigDecimal sum = a.add(b);
		return bigSqrt(sum);
	}

	private BigDecimal phase() {
		return atan2(i, r);
	}

	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG
		.intValue());

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti
	 * @url 
	 *      http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 * @return
	 */
	private static BigDecimal bigSqrt(BigDecimal c) {
		return sqrtNewtonRaphson(c, BigDecimal.ONE, BigDecimal.ONE.divide(SQRT_PRE));
	}
	
	/**
	 * Private utility method used to compute the square root of a BigDecimal.
	 * 
	 * @author Luciano Culacciatti 
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 * @param c
	 * @param xn
	 * @param precision
	 * @return
	 */
	private static BigDecimal sqrtNewtonRaphson(BigDecimal c, BigDecimal xn,
		BigDecimal precision)
	{
	    BigDecimal fx = xn.pow(2).add(c.negate());
	    BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 =
			fx.divide(fpx, 2 * SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
	    xn1 = xn.add(xn1.negate());
	    BigDecimal currentSquare = xn1.pow(2);
	    BigDecimal currentPrecision = currentSquare.subtract(c);
	    currentPrecision = currentPrecision.abs();
	    if (currentPrecision.compareTo(precision) <= -1){
	        return xn1;
	    }
	    return sqrtNewtonRaphson(c, xn1, precision);
	}

	private static final BigDecimal TWO = new BigDecimal(2);
	private static final BigDecimal PI = new BigDecimal(
		"3.14159265358979323846264338327950288419716939937510");

	// NB - PI limited to 50 decimal places of precision so narrowing possible

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
		else if (ty.compareTo(BigDecimal.ZERO) < 0)
			angle = TWO.multiply(PI); 

		BigDecimal xNew, yNew;
		
		for (int j = 0; j < MAX_ATAN_ITERS; j++) {
			if (ty.compareTo(BigDecimal.ZERO) < 0) {
				// Rotate counter-clockwise 
				xNew = tx.subtract(ty.divide(TWO.pow(j)));
				yNew = ty.add(tx.divide(TWO.pow(j)));
				angle = angle.subtract(ANGLES[j]);
			}
			else {
				// Rotate clockwise 
				xNew = tx.add(ty.divide(TWO.pow(j)));
				yNew = ty.subtract(tx.divide(TWO.pow(j)));
				angle = angle.add(ANGLES[j]);
			}
			tx = xNew;
			ty = yNew;
		}
		return angle;
	}


	// ATAN helpers

	private static final int MAX_ATAN_ITERS = 40;

	private static final BigDecimal[] ANGLES = new BigDecimal[MAX_ATAN_ITERS];

	static {
		// taken from wolfram alpha: i = [0..MAX_ITERS]: atan(2^(-(i)) to X decimal
		// places
		ANGLES[0] =
			new BigDecimal("0.7853981633974483096156608458198757210492923498437764");
		ANGLES[1] =
			new BigDecimal("0.4636476090008061162142562314612144020285370542861202");
		ANGLES[2] =
			new BigDecimal("0.2449786631268641541720824812112758109141440983811840");
		ANGLES[3] =
			new BigDecimal("0.1243549945467614350313548491638710255731701917698040");
		ANGLES[4] =
			new BigDecimal("0.0624188099959573484739791129855051136062738877974991");
		ANGLES[5] =
			new BigDecimal("0.0312398334302682762537117448924909770324956637254000");
		ANGLES[6] =
			new BigDecimal("0.0156237286204768308028015212565703189111141398009054");
		ANGLES[7] =
			new BigDecimal("0.0078123410601011112964633918421992816212228117250147");
		ANGLES[8] =
			new BigDecimal("0.0039062301319669718276286653114243871403574901152028");
		ANGLES[9] =
			new BigDecimal("0.0019531225164788186851214826250767139316107467772335");
		ANGLES[10] =
			new BigDecimal("0.0009765621895593194304034301997172908516341970158100");
		ANGLES[11] =
			new BigDecimal("0.0004882812111948982754692396256448486661923611331350");
		ANGLES[12] =
			new BigDecimal("0.0002441406201493617640167229432596599862124177909706");
		ANGLES[13] =
			new BigDecimal("0.0001220703118936702042390586461179563009308294090157");
		ANGLES[14] =
			new BigDecimal("0.0000610351561742087750216625691738291537851435368333");
		ANGLES[15] =
			new BigDecimal("0.0000305175781155260968618259534385360197509496751194");
		ANGLES[16] =
			new BigDecimal("0.0000152587890613157621072319358126978851374292381445");
		ANGLES[17] =
			new BigDecimal("0.0000076293945311019702633884823401050905863507439184");
		ANGLES[18] =
			new BigDecimal("0.0000038146972656064962829230756163729937228052573039");
		ANGLES[19] =
			new BigDecimal("0.0000019073486328101870353653693059172441687143421654");
		ANGLES[20] =
			new BigDecimal("0.00000095367431640596087942067068992311239001963412449");
		ANGLES[21] =
			new BigDecimal("0.00000047683715820308885992758382144924707587049404378");
		ANGLES[22] =
			new BigDecimal("0.00000023841857910155798249094797721893269783096898769");
		ANGLES[23] =
			new BigDecimal("0.00000011920928955078068531136849713792211264596758766");
		ANGLES[24] =
			new BigDecimal("0.000000059604644775390554413921062141788874250030195782");
		ANGLES[25] =
			new BigDecimal("0.000000029802322387695303676740132767709503349043907067");
		ANGLES[26] =
			new BigDecimal("0.000000014901161193847655147092516595963247108248930025");
		ANGLES[27] =
			new BigDecimal(
				"0.0000000074505805969238279871365645744953921132066925545");
		ANGLES[28] =
			new BigDecimal(
				"0.0000000037252902984619140452670705718119235836719483287");
		ANGLES[29] =
			new BigDecimal(
				"0.0000000018626451492309570290958838214764904345065282835");
		ANGLES[30] =
			new BigDecimal(
				"0.0000000009313225746154785153557354776845613038929264961");
		ANGLES[31] =
			new BigDecimal(
				"0.0000000004656612873077392577788419347105701629734786389");
		ANGLES[32] =
			new BigDecimal(
				"0.0000000002328306436538696289020427418388212703712742932");
		ANGLES[33] =
			new BigDecimal(
				"0.0000000001164153218269348144525990927298526587963964573");
		ANGLES[34] =
			new BigDecimal(
				"0.00000000005820766091346740722649676159123158234954915625");
		ANGLES[35] =
			new BigDecimal(
				"0.00000000002910383045673370361327303269890394779369363200");
		ANGLES[36] =
			new BigDecimal(
				"0.00000000001455191522836685180663959783736299347421170360");
		ANGLES[37] =
			new BigDecimal(
				"0.000000000007275957614183425903320184104670374184276462938");
		ANGLES[38] =
			new BigDecimal(
				"0.000000000003637978807091712951660140200583796773034557866");
		ANGLES[39] =
			new BigDecimal(
				"0.000000000001818989403545856475830076118822974596629319733");
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
