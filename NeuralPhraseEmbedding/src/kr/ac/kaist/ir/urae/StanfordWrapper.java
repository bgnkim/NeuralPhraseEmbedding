/**
 * Wrapper of Stanford SU-RNN CVG parser.
 */
package kr.ac.kaist.ir.urae;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.dvparser.DVModel;
import edu.stanford.nlp.parser.dvparser.DVParser;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;

/**
 * Wrapper of Stanford SU-RNN CVG parser &amp; URAE phrase embedding. This
 * singleton wrapper generates score vector for each input sentence.
 *
 * @author Bugeun Kim
 *
 */
public class StanfordWrapper {
	/**
	 * For multi-threading only.
	 *
	 * @return another instance of Embedding Wrapper.
	 */
	public static StanfordWrapper getAnotherInstance() {
		return new StanfordWrapper(StanfordWrapper.instance);
	}

	/**
	 * Generates singleton instance and pass it to caller.
	 *
	 * @return Singleton instance of Embedding Wrapper.
	 */
	public static StanfordWrapper getInstance() {
		if (StanfordWrapper.instance == null) {
			StanfordWrapper.instance = new StanfordWrapper();
		}

		return StanfordWrapper.instance;
	}

	/**
	 * Command-line main function.
	 *
	 * @param args
	 *            contains Command-line Arguments
	 */
	public static void main(String[] args) {
		final StanfordWrapper instance = StanfordWrapper.getInstance();

		final Scanner scan = new Scanner(System.in);
		String line;
		do {
			System.out.print("Phrase? ");
			line = scan.nextLine();
			final Tree tree = instance.parseTree(line);
			final SimpleMatrix matrix = instance.getPhraseVectorOf(tree);
			System.out.println(matrix.toString());
			System.out.println(tree.toString());
		} while (line.length() > 0);
		scan.close();
	}

	/** Singleton instance **/
	private static volatile StanfordWrapper instance;
	/** Stanford Parser **/
	private final LexicalizedParser parser;

	/** Model of SU-RNN Parser **/
	private final DVModel model;
	/** Tokenizer in Stanford Parser Package **/
	private final TokenizerFactory<CoreLabel> tokenizerFactory;
	/** Parameters for URAE encoder **/
	public Parameters param;

	/**
	 * Hidden constructor. This is hidden to avoid multiple load action of
	 * Stanford parser model.
	 * **/
	private StanfordWrapper() {
		// Load SU-RNN parser
		this.parser = LexicalizedParser
				.loadModel("edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");
		// Extract RNN model from the Parser
		this.model = DVParser.getModelFromLexicalizedParser(this.parser);
		// Initialize tokenizer.
		this.tokenizerFactory = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), "");
		// Load URAE parameters.
		this.param = Parameters.getInstance();
	}

	/**
	 * Copy instance to new one. Copy the models, token factory and so on.
	 *
	 * @param wrapper
	 *            to be copied.
	 */
	private StanfordWrapper(StanfordWrapper wrapper) {
		this.parser = LexicalizedParser.copyLexicalizedParser(wrapper.parser);
		this.model = wrapper.model;
		this.tokenizerFactory = wrapper.tokenizerFactory;
		this.param = wrapper.param;
	}

	/**
	 * Calculate URAE vector from subtree. Since URAE requires computation must
	 * be done by ordered way(lowest-level, next lowest-level, and so on...), we
	 * compute this by BFS. Also for nodes that have more than 2 children, we
	 * followed URAE's association rule which gives highest priority to
	 * right-most one. This is recursive, but not tail-recursive.
	 *
	 * @param subtree
	 *            for compute phrase vector.
	 * @return SimpleMatrix of phrase vector.
	 */
	private SimpleMatrix calculateBFS(Tree subtree) {
		if (!subtree.isPreTerminal() && (subtree.numChildren() > 1)) {
			final Tree[] children = subtree.children();
			SimpleMatrix prev = null;
			for (int i = children.length - 1; i >= 0; i--) {
				final SimpleMatrix curr = this.calculateBFS(children[i]);
				if (prev != null) {
					final SimpleMatrix W1c1 = this.param.getW1().mult(curr);
					final SimpleMatrix W2c2 = this.param.getW2().mult(prev);
					prev = NeuralNetwork.tanh(W1c1.plus(W2c2).plus(
							this.param.getb1()));
				} else {
					prev = curr;
				}
			}

			return prev;
		} else if (subtree.isPreTerminal()) {
			final String word = subtree.getChild(0).value();
			return this.param.getWordVectorOf(word);
		} else if (subtree.numChildren() == 1) {
			return this.calculateBFS(subtree.getChild(0));
		} else {
			return null;
		}
	}

	/**
	 * Find NP-PP(IN-NP) structure from the given tree, with DFS.
	 *
	 * @param sentence
	 *            for top-level sentence.
	 * @return LinkedList of Result instances.
	 */
	public LinkedList<Result> findNPNStructure(String sentence) {
		final Tree tree = this.parseTree(sentence);
		final SimpleMatrix matx = this.getPhraseVectorOf(tree);
		return this.findNPNStructure(tree, new LinkedList<Result>(), matx);
	}

	/**
	 * Find NP-PP(IN-NP) structure from the given tree, with DFS.
	 *
	 * @param tree
	 *            for find NPN structure.
	 * @param result
	 *            LinkedList to be accumulated into.
	 * @param sentence
	 *            for top-level sentence.
	 * @return LinkedList of Result instances.
	 */
	private LinkedList<Result> findNPNStructure(Tree tree,
			LinkedList<Result> result, SimpleMatrix sentence) {
		if (!tree.isLeaf() && !tree.isPreTerminal() && (tree.numChildren() > 0)) {
			final int length = tree.numChildren();

			for (int i = 1; i < length; i++) {
				if (tree.getChild(i - 1).value().equalsIgnoreCase("NP")
						&& tree.getChild(i).value().equalsIgnoreCase("PP")) {
					result.add(new Result(tree.getChild(i - 1), tree
							.getChild(i).firstChild(), tree.getChild(i)
							.lastChild(), sentence));
				}
			}

			for (int i = 0; i < length; i++) {
				final Tree child = tree.getChild(i);
				if (!child.isLeaf() && !child.isPreTerminal()) {
					this.findNPNStructure(child, result, sentence);
				}
			}
		}

		return result;
	}

	/**
	 * Get Phrase vector of given tree.
	 *
	 * @param tree
	 *            to be scored
	 * @return SimpleMatrix of phrae vector
	 **/
	public SimpleMatrix getPhraseVectorOf(Tree tree) {
		return this.calculateBFS(tree);
	}

	/**
	 * From given parse tree, generate score vector, using Stanford SU-RNN
	 * parser. Refer to (Socher et al., ACL 2013)
	 *
	 * @param tree
	 *            to be scored
	 *
	 * @return Scored Vector of tree. A row vector with 25 dimension.
	 */
	public SimpleMatrix getSyntaticScoreVectorOf(Tree tree) {
		return this.model.getScoreWForNode(tree);
	}

	/**
	 * From given parse tree, generate weight matrix, using Stanford SU-RNN
	 * parser.
	 *
	 * @param tree
	 *            to be scored
	 *
	 * @return Weight Matrix of tree. 25 × 26 dimension.
	 */
	public SimpleMatrix getSyntaticWeightMatrixOf(Tree tree) {
		return this.model.getWForNode(tree);
	}

	/**
	 * Get word embedding of given word, from trained embedding. 100 dimensional
	 * re-implementation of (Collobert &amp; Weston, 2008) in (Turian et al.,
	 * 2010) was used.
	 *
	 * @param word
	 *            to find
	 * @return Word Embedding of input word. A column vector with 100 dimension.
	 */
	public SimpleMatrix getWordVectorOf(String word) {
		return this.param.getWordVectorOf(word);
	}

	/**
	 * Get word embedding of given word, from trained embedding. 25 dimensional
	 * re-implementation of (Collobert &amp; Weston, 2008) in (Turian et al.,
	 * 2010) was used.
	 *
	 * @param word
	 *            to find
	 * @return Word Embedding of input word. A column vector with 25 dimension.
	 */
	public SimpleMatrix getWordVectorOf25(String word) {
		return this.model.getWordVector(word);
	}

	/**
	 * Method for parsing.
	 *
	 * @param sentence
	 *            to be parsed
	 * @return Tree instance which contains parse tree. (Not dependency tree)
	 */
	public Tree parseTree(String sentence) {
		final Tokenizer<CoreLabel> tokens = this.tokenizerFactory
				.getTokenizer(new StringReader(sentence));
		final List<CoreLabel> words = tokens.tokenize();
		final Tree tree = this.parser.apply(words);
		return tree;
	}
}
