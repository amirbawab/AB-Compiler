package semantic;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.ABParser;
import parser.ABParser.ABParserSnapshot;
import scanner.ABScanner;
import scanner.ABToken;

public class ABSemanticTest {
	
	// Components
	private static ABScanner abScanner;
	private static ABParser abParser;
	
	@BeforeClass
	public static void initMachine() {
		abScanner = new ABScanner("/scanner/machine.dfa");
		abParser = new ABParser("/parser/grammar.bnf");
	}
	
	@Test
	public void testSemantic_example1() throws IOException {
		
		String input = IOUtils.toString(getClass().getResource("/semantic/input/example1.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example1_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example1_error.txt"));
		
		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example2() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example2.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example2_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example2_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example3() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example3.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example3_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example3_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example4() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example4.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example4_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example4_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example5() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example5.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example5_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example5_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example6() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example6.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example6_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example6_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example7() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example7.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example7_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example7_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example8() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example8.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example8_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example8_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	@Test
	public void testSemantic_example9() throws IOException {

		String input = IOUtils.toString(getClass().getResource("/semantic/input/example9.txt"));
		String expected_symbol_table = IOUtils.toString(getClass().getResource("/semantic/output/example9_symbol_table.txt"));
		String expected_error = IOUtils.toString(getClass().getResource("/semantic/output/example9_error.txt"));

		// Run machine
		abScanner.processText(input);

		// Parse
		abParser.parse(abScanner.getNonErrorTokens());

		// Semantic
		List<ABSymbolTable> tables = abParser.getSymbolTables();
		List<ABSemantic.ABSemanticError> errors = abParser.getSemantic().getErrors();

		// Test
		assertArrayEquals(stringToArray(expected_error), errorMessagesToString(errors));
		assertArrayEquals(stringToArray(expected_symbol_table), tablesToString(tables));
	}

	public String[] errorMessagesToString(List<ABSemantic.ABSemanticError> errors) {
		String output[] = new String[errors.size()];
		for(int i=0; i < output.length; i++) {
			ABSemantic.ABSemanticError error = errors.get(i);
			output[i] = String.format("%s\t%s\t%s\t%s", error.getToken().getValue(), error.getToken().getRow(), error.getToken().getCol(), error.getMessage());
		}
		return output;
	}

	public String[] tablesToString(List<ABSymbolTable> tables) {
		String result = "";
		for(int i=0; i < tables.size(); i++)
			if(i == 0) {
				result = tables.get(0).toString();
			} else {
				result += tables.get(i).toString();
			}
		return stringToArray(result);
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
