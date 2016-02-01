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
	public void testProcessText_no_error_correct_token() {
		try {
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example1.txt"));
			String expected = IOUtils.toString(getClass().getResource("/scanner/output/example1_1.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected), ABTokenToTokenKey(nonError));
			
			// Check if no errors are found
			assertEquals(abScanner.getErrorTokens().length, 0);
			
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_no_error_correct_value() {
		try {
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example1.txt"));
			String expected = IOUtils.toString(getClass().getResource("/scanner/output/example1_2.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected), ABTokenToTokenValue(nonError));
									
			// Check if no errors are found
			assertEquals(abScanner.getErrorTokens().length, 0);
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_no_error_correct_row_col() {
		try {
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example1.txt"));
			String expected = IOUtils.toString(getClass().getResource("/scanner/output/example1_3.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected), ABTokenToPosition(nonError));
			
			// Check if no errors are found
			assertEquals(abScanner.getErrorTokens().length, 0);
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_1_correct_token() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example2.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_1_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_1_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenKey(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenKey(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_1_correct_value() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example2.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_2_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_2_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenValue(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenValue(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_1_correct_row_col() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example2.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_3_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example2_3_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToPosition(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToPosition(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_2_correct_token() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example3.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_1_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_1_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenKey(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenKey(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_2_correct_value() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example3.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_2_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_2_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenValue(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenValue(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_2_correct_row_col() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example3.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_3_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example3_3_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToPosition(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToPosition(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_3_correct_token() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example4.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_1_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_1_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenKey(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenKey(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_3_correct_value() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example4.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_2_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_2_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenValue(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenValue(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_3_correct_row_col() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example4.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_3_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example4_3_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToPosition(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToPosition(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_4_correct_token() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example5.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_1_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_1_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenKey(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenKey(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_4_correct_value() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example5.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_2_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_2_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToTokenValue(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToTokenValue(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	@Test
	public void testProcessText_mixed_4_correct_row_col() {
		try {
			
			String input = IOUtils.toString(getClass().getResource("/scanner/input/example5.txt"));
			String expected_non_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_3_non_error.txt"));
			String expected_error = IOUtils.toString(getClass().getResource("/scanner/output/example5_3_error.txt"));
			
			// Run machine
			abScanner.processText(input);
			ABToken nonError[] = abScanner.getNonErrorTokens();
			ABToken error[] = abScanner.getErrorTokens();
			
			// Verify output
			assertArrayEquals(stringToArray(expected_non_error), ABTokenToPosition(nonError));
			assertArrayEquals(stringToArray(expected_error), ABTokenToPosition(error));
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
	
	/**
	 * Get tokens keys arrays from an array of token
	 * @param tokens
	 * @return tokens keys array
	 */
	public String[] ABTokenToTokenKey(ABToken tokens[]) {
		String tokensKeys[] = new String[tokens.length];
		for(int i=0; i<tokens.length; i++)
			tokensKeys[i] = tokens[i].getToken();
		return tokensKeys;
	}
	
	/**
	 * Get tokens values arrays from an array of token
	 * @param tokens
	 * @return tokens values array
	 */
	public String[] ABTokenToTokenValue(ABToken tokens[]) {
		String tokensValues[] = new String[tokens.length];
		for(int i=0; i<tokens.length; i++)
			tokensValues[i] = tokens[i].getValue().replace("\n", "");
		return tokensValues;
	}
	
	/**
	 * Get tokens position arrays from an array of token
	 * @param tokens
	 * @return tokens positions array
	 */
	public String[] ABTokenToPosition(ABToken tokens[]) {
		String tokensPositions[] = new String[tokens.length];
		for(int i=0; i<tokens.length; i++)
			tokensPositions[i] = tokens[i].getRow() + " " + tokens[i].getCol();
		return tokensPositions;
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
