/**
 *
 */
package kr.ac.kaist.ir.test;

import java.util.LinkedList;
import java.util.Scanner;

import kr.ac.kaist.ir.server.ParserClient;
import kr.ac.kaist.ir.server.Result;

/**
 * Test Client for ParserServer-Client connection.
 *
 * @author 김부근
 *
 */
public class ParserClientTester {
	/**
	 * STATIC +main: method for run.
	 **/
	public static void main(String[] args) {
		try {
			final ParserClient client = new ParserClient("irbig2.kaist.ac.kr");
			final Scanner scan = new Scanner(System.in);
			System.out.println("Phrase> ");

			while (scan.hasNextLine()) {
				final LinkedList<Result> result = client.getParsedResultOf(scan
						.nextLine());
				System.out.println("COUNT = " + result.size());
				for (final Result r : result) {
					System.out.println(r.getPhraseString());
					System.out.println(r.getPPString());
				}
				System.out.println("Phrase> ");
			}
			scan.close();
			client.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
