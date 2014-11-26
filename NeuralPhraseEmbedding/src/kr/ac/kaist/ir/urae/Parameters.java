/**
 *
 */
package kr.ac.kaist.ir.urae;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.ejml.simple.SimpleMatrix;

/**
 * @author 김부근
 *
 */
public class Parameters {
	/**
	 * <p>
	 * Get instance of Parameters. Automatically load parameters on singleton
	 * construction.
	 * </p>
	 *
	 * @return singleton instance of Parameters class.
	 */
	public static Parameters getInstance() {
		if (Parameters.instance == null) {
			Parameters.instance = new Parameters();
		}

		return Parameters.instance;
	}

	/** Constant denotes UNKNOWN word **/
	private static final String UNKNOWN = "*UNKNOWN*";
	/** Singleton instance **/
	private static volatile Parameters instance;

	/** Logger **/
	private final Logger logger = Logger.getAnonymousLogger();
	/** Neural Network Matrix Parameters in (Socher et al., NIPS 2011). **/
	private SimpleMatrix W1, W2, b1;
	/** Cache for mapping : word \to embedding **/
	private HashMap<String, SimpleMatrix> wordCache;
	/** Stored resource for whole word embedding (50002 items). **/
	private JsonObject words;

	/**
	 * <p>
	 * This is private, to prohibit unnecessary multiple load behavior for the
	 * parameters.
	 * </p>
	 *
	 */
	private Parameters() {
		this.logger.info("LOADING RNN PARAMETERS...");
		this.loadParams();
		this.logger.info("LOADING WORD EMBEDDINGS...");
		this.loadWordMap();
		this.logger.info("DONE.");
	}

	/**
	 * <p>
	 * Construct a matrix from two dimensional JSON array.
	 * </p>
	 * <p>
	 * JSON array formed as array of columns. That is, [[1,2][3,4]] is a matrix
	 * with 1st row (1 2) and 2nd row (3 4).
	 * </p>
	 *
	 * @param array
	 *            to be converted to SimpleMatrix
	 * @return converted SimpleMatrix.
	 */
	private SimpleMatrix constructMatrix(JsonArray array) {
		final int rows = array.size();
		final int cols = array.getJsonArray(0).size();
		final double[][] data = new double[rows][cols];

		// read rows in the array, and store it in to matrix.
		for (int r = 0; r < rows; r++) {
			final JsonArray row = array.getJsonArray(r);
			for (int c = 0; c < cols; c++) {
				data[r][c] = row.getJsonNumber(c).doubleValue();
			}
		}

		return new SimpleMatrix(data);
	}

	/**
	 * <p>
	 * Construct a column vector from one dimensional JSON array.
	 * </p>
	 * <p>
	 * JSON array formed as array of rows. That is, [1, 2, 3, 4] is a matrix
	 * with a column (1 2 3 4).
	 * </p>
	 *
	 * @param array
	 *            to be converted to SimpleMatrix
	 * @return converted SimpleMatrix.
	 */
	private SimpleMatrix constructVector(JsonArray array) {
		final int rows = array.size();
		final double[][] data = new double[rows][1];

		// read rows in the array, store it into the matrix.
		for (int r = 0; r < rows; r++) {
			data[r][0] = array.getJsonNumber(r).doubleValue();
		}

		return new SimpleMatrix(data);
	}

	/**
	 * Pass Trained b1 (bias) matrix of (Socher et al., NIPS 2011)
	 *
	 * @return SimpleMatrix of b1
	 **/
	public SimpleMatrix getb1() {
		return this.b1;
	}

	/**
	 * Pass Trained W1 (parameter of word1) matrix of (Socher et al., NIPS 2011)
	 *
	 * @return SimpleMatrix of W1
	 **/
	public SimpleMatrix getW1() {
		return this.W1;
	}

	/**
	 * Pass Trained W2 (parameter of word2) matrix of (Socher et al., NIPS 2011)
	 *
	 * @return SimpleMatrix of W2
	 **/
	public SimpleMatrix getW2() {
		return this.W2;
	}

	/**
	 * Returns a word embedding vector of given word. If word does not exists in
	 * the mapping, returns "*UNKNOWN*" word embedding (by default).
	 *
	 * @param word
	 *            to be found in word embedding map
	 * @return SimpleMatrix representing word (100-dimension <b>column
	 *         vector</b>)
	 **/
	public SimpleMatrix getWordVectorOf(String word) {
		word = word.toLowerCase();
		if (this.wordCache.containsKey(word)) {
			// if it is found in the cache, return.
			return this.wordCache.get(word);
		} else if (this.words.containsKey(word)) {
			// if it is found not in cache but in entire embedding set, convert,
			// store and return.
			this.wordCache.put(word,
					this.constructVector(this.words.getJsonArray(word)));
			return this.wordCache.get(word);
		} else {
			// otherwise return UNKNOWN.
			return this.wordCache.get(Parameters.UNKNOWN);
		}
	}

	/**
	 * Load Trained Parameters of RAE in (Socher et al, NIPS 2011).
	 *
	 */
	private void loadParams() {
		final JsonReader reader = Json
				.createReader(ClassLoader
						.getSystemResourceAsStream("kr/ac/kaist/ir/resource/params.json"));
		final JsonObject obj = reader.readObject();

		this.W1 = this.constructMatrix(obj.getJsonArray("W1"));
		this.W2 = this.constructMatrix(obj.getJsonArray("W2"));
		this.b1 = this.constructMatrix(obj.getJsonArray("b1"));
		reader.close();
	}

	/**
	 * Load normalized Word embedding of (Collobert & Weston, 2008) in (Socher
	 * et al, NIPS 2011).
	 *
	 */
	private void loadWordMap() {
		final JsonReader reader = Json
				.createReader(ClassLoader
						.getSystemResourceAsStream("kr/ac/kaist/ir/resource/words.json"));
		// Store the words as JSON object. We convert it on demand.
		this.words = reader.readObject();
		this.wordCache = new HashMap<String, SimpleMatrix>();

		// Store the UNKNOWN word for the convenience.
		this.wordCache.put(Parameters.UNKNOWN, this.constructVector(this.words
				.getJsonArray(Parameters.UNKNOWN)));
		reader.close();
	}
}
