/**
 *
 */
package kr.ac.kaist.ir.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Client helper for Parser Server-Client communication
 *
 * @author 김부근
 *
 */
public class ParserClient {
	/** Socket of communication **/
	private Socket socket;
	/** Writer for Send Sentences **/
	private BufferedWriter send;
	/** Reader for Retrieving Result Objects **/
	private ObjectInputStream receive;

	/**
	 * <p>
	 * Constructor. Connect to server in given host.
	 * </p>
	 * <p>
	 * <b>When you finish your job, please call {{@link #close()} method for
	 * close connection.</b>
	 * </p>
	 *
	 * @param host
	 *            to connect
	 * @throws UnknownHostException
	 *             when given host is not correct, or unreachable.
	 * @throws IOException
	 *             when failed to open streams.
	 */
	public ParserClient(String host) throws IOException {
		while (this.socket == null) {
			try {
				this.socket = new Socket(host, ParserServer.PORT);
				this.send = new BufferedWriter(new OutputStreamWriter(
						this.socket.getOutputStream()));
				this.receive = new ObjectInputStream(
						this.socket.getInputStream());
				break;
			} catch (final SocketException e) {
				try {
					synchronized (this) {
						Thread.sleep(10);
					}
				} catch (final Exception ie) {
				}
			}
		}
	}

	/**
	 * Close connection.
	 *
	 * @throws IOException
	 *             when failed to send close message.
	 */
	public void close() throws IOException {
		this.send.write("\n");
		this.send.close();
		this.receive.close();
		this.socket.close();
	}

	/**
	 * Get NPN structure result from server, with given sentence.
	 *
	 * @param sentence
	 *            to find NPN structure
	 * @return LinkedList of {@link Result} instances.
	 * @throws IOException
	 *             when write on/retrieve from stream is failed.
	 * @throws ClassNotFoundException
	 *             when casting server's response failed.
	 */
	@SuppressWarnings("unchecked")
	public LinkedList<Result> getParsedResultOf(String sentence)
			throws IOException, ClassNotFoundException {
		if (sentence.length() == 0) {
			return new LinkedList<Result>();
		} else {
			this.send.write(sentence + "\n");
			this.send.flush();
			return ((LinkedList<Result>) this.receive.readObject());
		}
	}
}
