/**
 *
 */
package kr.ac.kaist.ir.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import kr.ac.kaist.ir.urae.Result;
import kr.ac.kaist.ir.urae.StanfordWrapper;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.trees.Tree;

/**
 * Server implementation for parser.
 *
 * @author 김부근
 *
 */
public class ParserServer extends Thread {

	/**
	 * Main method for execute server threads.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			@SuppressWarnings("resource")
			final ServerSocket socket = new ServerSocket(ParserServer.PORT);
			ParserServer.instance = StanfordWrapper.getInstance();
			ParserServer.logger = Logger.getAnonymousLogger();
			ParserServer.logger.info("Server Started with PORT "
					+ ParserServer.PORT);

			while (true) {
				try {
					final Socket accept = socket.accept();
					new ParserServer(accept).start();

					System.out.print("+");
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e) {
			ParserServer.logger.log(Level.SEVERE, "Socket Exception", e);
		}
	}

	/** logger **/
	private static Logger logger;
	/** Socket per Thread **/
	private final Socket accept;

	/**
	 * Service port
	 */
	public static final int PORT = 59800;

	/** StanfordWrapper instance **/
	private static StanfordWrapper instance;

	/**
	 * Generate Parser Server with given socket
	 *
	 * @param accept
	 *            is connection Socket.
	 */
	public ParserServer(Socket accept) {
		this.accept = accept;
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
	 * Run parser thread.
	 *
	 * @see java.lang.Thread#run()
	 **/
	@Override
	public void run() {
		try {
			final Scanner in = new Scanner(this.accept.getInputStream());
			final ObjectOutputStream out = new ObjectOutputStream(
					this.accept.getOutputStream());

			while (in.hasNextLine()) {
				final String line = in.nextLine();
				if (line.length() > 0) {
					try {
						final Tree tree = ParserServer.instance.parseTree(line);
						LinkedList<Result> result = new LinkedList<Result>();
						if (tree.numChildren() > 0) {
							result = this.findNPNStructure(tree, result,
									ParserServer.instance
									.getPhraseVectorOf(tree));
						} else {
							ParserServer.logger
									.info("Weird sentence : " + line);
						}
						out.writeObject(result);
						out.flush();
					} catch (final Exception e) {
						ParserServer.logger.log(Level.WARNING,
								"ERROR with sentence : " + line, e);
					}
				} else {
					break;
				}
			}

			in.close();
			out.close();
			System.out.print("-");
			this.accept.close();
		} catch (final Exception e) {
			ParserServer.logger.log(Level.WARNING, "ERROR in Thread", e);
		}
	}
}
