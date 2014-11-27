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
import java.util.logging.Logger;

import kr.ac.kaist.ir.urae.StanfordWrapper;
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
		final ParserServer server = new ParserServer(12);
		server.start();
	}

	/** logger **/
	private Logger logger;
	/** Threads for service **/
	private Thread[] threads;

	/**
	 * Service port
	 */
	public static final int PORT = 59900;

	/** Socket for receive request **/
	private ServerSocket socket;
	/** StanfordWrapper instance **/
	private StanfordWrapper instance;

	/**
	 * Generate Parser Server with given amount of number.
	 *
	 * @param num
	 *            is the number of servers.
	 */
	public ParserServer(int num) {
		try {
			this.socket = new ServerSocket(ParserServer.PORT);
			this.instance = StanfordWrapper.getInstance();
			this.logger = Logger.getAnonymousLogger();

			this.logger.info("Server Started with PORT " + ParserServer.PORT);
			this.threads = new Thread[num];
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Find NP-PP(IN-NP) structure from the given tree, with DFS.
	 *
	 * @param tree
	 *            for find NPN structure.
	 * @return LinkedList of Result instances.
	 */
	private LinkedList<Result> findNPNStructure(Tree tree,
			LinkedList<Result> result) {
		if (tree.isLeaf() || tree.isPreTerminal()) {
			return result;
		} else {
			final int length = tree.numChildren();

			for (int i = 1; i < length; i++) {
				if (tree.getChild(i - 1).value().equalsIgnoreCase("NP")
						&& tree.getChild(i).value().equalsIgnoreCase("PP")) {
					result.add(new Result(tree.getChild(i - 1), tree
							.getChild(i).firstChild(), tree.getChild(i)
							.lastChild()));
				}
			}

			for (int i = 0; i < length; i++) {
				final Tree child = tree.getChild(i);
				if (!child.isLeaf() && !child.isPreTerminal()) {
					this.findNPNStructure(child, result);
				}
			}

			return result;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {

			while (true) {
				final Socket accept = this.socket.accept();
				this.logger.info(accept.getInetAddress()
						+ " RECEIVED connection");
				final Scanner in = new Scanner(accept.getInputStream());
				final ObjectOutputStream out = new ObjectOutputStream(
						accept.getOutputStream());

				while (in.hasNextLine()) {
					final String line = in.nextLine();
					if (line.length() > 0) {
						this.logger.info(accept.getInetAddress()
								+ " READ LINE : " + line);
						final Tree tree = this.instance.parseTree(line);
						this.logger.info(tree.toString());
						final LinkedList<Result> result = this
								.findNPNStructure(tree,
										new LinkedList<Result>());
						out.writeObject(result);
						out.flush();
						this.logger
						.info(accept.getInetAddress()
								+ " PARSE COMPLETE : #ENTRY = "
								+ result.size());
					} else {
						break;
					}
				}

				this.logger.info(accept.getInetAddress() + " CONN closed");
				in.close();
				out.close();
				accept.close();
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Thread#start()
	 */
	@Override
	public synchronized void start() {
		for (int i = 0; i < this.threads.length; i++) {
			this.threads[i] = new Thread(this);
			this.threads[i].start();
		}
	}
}
