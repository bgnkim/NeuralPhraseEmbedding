/**
 *
 */
package kr.ac.kaist.ir.urae;

import java.io.Serializable;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.trees.Tree;

/**
 * NPN structure wrapper for ParserServer - Client communication
 *
 * @author 김부근
 *
 */
public class Result implements Serializable {
	/** Serial ID **/
	private static final long serialVersionUID = -7697818199190864663L;
	/** Phrase Embeddings **/
	private final SimpleMatrix np1Matx, ppMatx, np2Matx, sentMatx;
	/** Phrase Strings **/
	private final String np1Str, ppStr, np2Str;

	/**
	 * Constructor. Given three tree structures, generate proper
	 * representations.
	 *
	 * @param np1
	 *            for 1st NP phrase
	 * @param pp
	 *            for intermediate prep phrase
	 * @param np2
	 *            for 2nd NP phrase
	 * @param sent
	 *            for sentence pharse matrix
	 */
	public Result(Tree np1, Tree pp, Tree np2, SimpleMatrix sent) {
		final StanfordWrapper instance = StanfordWrapper.getInstance();

		this.np1Str = this.getStringOf(np1);
		this.np2Str = this.getStringOf(np2);
		this.ppStr = pp.firstChild().value();

		this.np1Matx = instance.getPhraseVectorOf(np1);
		this.np2Matx = instance.getPhraseVectorOf(np2);
		this.ppMatx = instance.getWordVectorOf(this.ppStr);
		this.sentMatx = sent;
	}

	/**
	 * Returns 1st NP phrase's Word Embedding
	 *
	 * @return Phrase Embedding of 1st NP
	 */
	public SimpleMatrix getMatrixOfNP1() {
		return this.np1Matx;
	}

	/**
	 * Returns 2nd NP phrase's Word Embedding
	 *
	 * @return Phrase Embedding of 2nd NP
	 */
	public SimpleMatrix getMatrixOfNP2() {
		return this.np2Matx;
	}

	/**
	 * Returns PP phrase's Word Embedding
	 *
	 * @return Phrase Embedding of PP
	 */
	public SimpleMatrix getMatrixOfPP() {
		return this.ppMatx;
	}

	/**
	 * Returns Sentence's Word Embedding
	 *
	 * @return Phrase Embedding of Sentence
	 */
	public SimpleMatrix getMatrixOfSentence() {
		return this.sentMatx;
	}

	/**
	 * Returns Entire NP1-PP-NP2 String
	 *
	 * @return Phrase String
	 */
	public String getPhraseString() {
		return this.np1Str + " " + this.ppStr + " " + this.np2Str;
	}

	/**
	 * Returns Prep String
	 *
	 * @return String of prep
	 */
	public String getPPString() {
		return this.ppStr;
	}

	/**
	 * Generate String for given Tree
	 *
	 * @param tree
	 *            for string generation
	 * @return generated string
	 */
	private String getStringOf(Tree tree) {
		final List<Tree> leaves = tree.getLeaves();
		final StringBuffer strbuf = new StringBuffer();
		for (int i = 0; i < leaves.size(); i++) {
			strbuf.append(leaves.get(i).value()).append(' ');
		}
		return strbuf.substring(0, strbuf.length() - 1);
	}
}