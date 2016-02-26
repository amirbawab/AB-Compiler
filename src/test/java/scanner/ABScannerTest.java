package scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ABScannerTest {
	
	// Machine
	private static ABScanner abScanner;
	
	@BeforeClass
	public static void initMachine() {
		abScanner = new ABScanner("/scanner/machine.dfa");
	}
	
	@Test
	public void testProcessText_example1() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example1.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example1_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example1_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			List<ABToken> nonError = abScanner.getNonErrorTokens();
			List<ABToken> error = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToString(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToString(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_example2() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example2.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			List<ABToken> nonError = abScanner.getNonErrorTokens();
			List<ABToken> error = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToString(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToString(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_example3() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example3.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			List<ABToken> nonError = abScanner.getNonErrorTokens();
			List<ABToken> error = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToString(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToString(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_example4() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example4.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			List<ABToken> nonError = abScanner.getNonErrorTokens();
			List<ABToken> error = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToString(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToString(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_example5() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example5.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			List<ABToken> nonError = abScanner.getNonErrorTokens();
			List<ABToken> error = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToString(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToString(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	/**
	 * Get tokens keys arrays from an array of token
	 * @param tokens
	 * @return tokens keys array
	 */
	public String[] ABTokenToString(List<ABToken> tokens) {
		String tokensKeys[] = new String[tokens.size()];
		for(int i=0; i<tokens.size(); i++)
			tokensKeys[i] = String.format("%s %s %d %d", tokens.get(i).getToken(), tokens.get(i).getValue().replace("\n", ""), tokens.get(i).getRow(), tokens.get(i).getCol());
		return tokensKeys;
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
