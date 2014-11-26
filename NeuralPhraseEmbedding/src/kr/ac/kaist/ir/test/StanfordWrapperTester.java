/**
 *
 */
package kr.ac.kaist.ir.test;

import java.io.File;
import java.util.Scanner;

import kr.ac.kaist.ir.urae.StanfordWrapper;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.trees.Tree;

/**
 * <p>
 * Static Class for Test agreement with original MatLab implementation of
 * (Socher et al., NIPS 2011).
 * </p>
 * <p>
 * Because our implementation uses RNN parser of (Socher et al., ACL 2013), if
 * you want to run this code, you must set Stanford Parser with version
 * <b>higher than 3.4</b>, and set serialized model as <b>englishRNN.ser.gz</b>.
 * Otherwise, the result may not be agreed.
 * </p>
 *
 * @author 김부근
 *
 */
public class StanfordWrapperTester {
	/**
	 * Convert line (CSV format) to columnVector.
	 *
	 * @param line
	 *            to be converted
	 * @return SimpleMatrix of columnVector
	 */
	private static SimpleMatrix constructColumnVector(String line) {
		final String[] numbers = line.split(",");
		final double[][] data = new double[numbers.length][1];
		for (int i = 0; i < numbers.length; i++) {
			data[i][0] = Double.parseDouble(numbers[i]);
		}
		return new SimpleMatrix(data);
	}

	/**
	 * Main method for run.
	 *
	 * @param args
	 *            from the command line.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				// Since phrases.txt and outVectors.txt has same number of
				// lines, read together.
				final Scanner scan = new Scanner(new File(args[0]));
				final Scanner vectors = new Scanner(new File(args[1]));
				final StanfordWrapper instance = StanfordWrapper.getInstance();
				String line;
				int lineNo = 0, maxidx = 0, sumidx = 0;
				double max = Double.MAX_VALUE, sum = Double.MAX_VALUE;

				while (scan.hasNextLine()) {
					line = scan.nextLine();
					if (line.trim().length() > 0) {
						lineNo++;
						// Read phrase and parse.
						final Tree tree = instance.parseTree(line);
						// Get Phrase Vector.
						final SimpleMatrix matrix = instance
								.getPhraseVectorOf(tree);
						// Read vector of original MatLab implementation.
						final SimpleMatrix expected = StanfordWrapperTester
								.constructColumnVector(vectors.nextLine());
						// Calculate Difference.
						final SimpleMatrix diff = expected.minus(matrix);

						System.out.println(String.format(
								"%5d line : Max diff %.6f, Sum %.6f", lineNo,
								diff.elementMaxAbs(), diff.normF()));

						// Collect lowest MaxAbs value.
						if (diff.elementMaxAbs() < max) {
							max = diff.elementMaxAbs();
							maxidx = lineNo;
						}

						// Collect lowest F-norm value.
						if (diff.elementSum() < sum) {
							sum = diff.normF();
							sumidx = lineNo;
						}
					}
				}
				vectors.close();
				scan.close();

				System.out.println(String.format(
						"Total : Max diff %.6f(%5d), Sum %.6f(%5d)", max,
						maxidx, sum, sumidx));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out
					.println("USAGE: java -jar [JAR FILE] [PHRASES FILE] [URAE OUTPUT FILE]");
		}
	}
}
