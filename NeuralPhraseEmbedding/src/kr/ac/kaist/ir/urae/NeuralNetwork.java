/**
 *
 */
package kr.ac.kaist.ir.urae;

import org.ejml.simple.SimpleMatrix;

/**
 * @author 김부근
 */
public class NeuralNetwork {
	/**
	 * Apply hyperbolic tangent function to given matrix in element-wise way.
	 *
	 * @param vector
	 *            for tanh applied
	 * @return SimpleMatrix with same dimensionality, which tanh applied.
	 */
	static SimpleMatrix tanh(SimpleMatrix vector) {
		for (int i = vector.getNumElements() - 1; i >= 0; i--) {
			final double value = vector.get(i);
			vector.set(i, Math.tanh(value));
		}

		return vector;
	}
}