package parser;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.ABParser.ABParserSnapshot;
import scanner.ABScanner;
import scanner.ABToken;

public class ABParserTest {
	
	// Components
	private static ABScanner abScanner;
	private static ABParser abParser;
	
	@BeforeClass
	public static void initMachine() {
		abScanner = new ABScanner("/scanner/machine.dfa");
		abParser = new ABParser("/parser/grammar.bnf");
	}
	
	@Test
	public void testParse_example1() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example1.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example1_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example1_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, true);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();

		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example2() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example2.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example2_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example2_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, true);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example3() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example3.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example3_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example3_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, false);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example4() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example4.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example4_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example4_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, false);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example5() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example5.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example5_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example5_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, false);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example6() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example6.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example6_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example6_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, false);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	@Test
	public void testParse_example7() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/parser/input/example7.txt"));
		String expected_non_error = IOUtils.toString(getClass().getResource("/parser/output/example7_non_error.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/parser/output/example7_error.txt"));
		
		// Run machine
		abScanner.processText(input);
		List<ABToken> nonError = abScanner.getNonErrorTokens();
		List<ABToken> error = abScanner.getErrorTokens();

		// No scanner error
		assertEquals(error.size(), 0);
		
		// Parse
		boolean parse = abParser.parse(nonError);
		
		// Run parser
		assertEquals(parse, false);
		
		// Get parse snapshots
		List<ABParserSnapshot> snapshots = abParser.getAllSnapshots();
		List<ABParserSnapshot> errorSnapshots = abParser.getFilteredErrorSnapshots();
		
		// Check
		assertArrayEquals(stringToArray(expected_non_error), snapshotsToString(snapshots));
		assertArrayEquals(stringToArray(expected_error), errorSnapshotsToString(errorSnapshots));
	}
	
	/**
	 * Convert snapshot to string
	 * @param snapshot
	 * @return string
	 */
	public String snapshotToString(ABParserSnapshot snapshot) {
		return String.format("%d\t%s\t%s\t%s\t%s", snapshot.getId(), snapshot.getStack(), snapshot.getInput(), snapshot.getProduction(), snapshot.getDerivation());
	}
	
	/**
	 * Convert snapshot to string
	 * @param snapshot
	 * @return string
	 */
	public String errorSnapshotToString(ABParserSnapshot snapshot) {
		return String.format("%d\t%s\t%s\t%s", snapshot.getId(), snapshot.getStack(), snapshot.getInput(), snapshot.getDerivation());
	}
	
	/**
	 * Convert snapshots to string
	 * @param snapshots
	 * @return string
	 */
	public String[] snapshotsToString(List<ABParserSnapshot> snapshots) {
		String[] output = new String[snapshots.size()];
		for(int i=0; i<output.length; i++)
			output[i] = snapshotToString(snapshots.get(i));
		return output;
	}
	
	/**
	 * Convert error snapshots to string
	 * @param snapshots
	 * @return string
	 */
	public String[] errorSnapshotsToString(List<ABParserSnapshot> snapshots) {
		String[] output = new String[snapshots.size()];
		for(int i=0; i<output.length; i++)
			output[i] = errorSnapshotToString(snapshots.get(i));
		return output;
	}
	
	/**
	 * Convert string to string array
	 * @param string
	 * @return string array
	 */
	public String[] stringToArray(String string) {
		Scanner scanner = new Scanner(string);
		List<String> str = new ArrayList<>();
		while(scanner.hasNextLine())
			str.add(scanner.nextLine());
		String strArray[] = new String[str.size()];
		str.toArray(strArray);
		scanner.close();
		return strArray;
	}
}
