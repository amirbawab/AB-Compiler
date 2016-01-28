package scanner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ABScannerTest {
	
	// Machine
	ABScanner abScanner;
	
	@Before
	public void initMachine() {
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
			
			// Scan expected output
			Scanner scan = new Scanner(expected);
			int i = 0;
			while(scan.hasNext())
				assertEquals(scan.next(), nonError[i++].getToken());
			
			// Check the size of both expected output and machine output
			assertEquals(i, nonError.length);
			
			// Check if no errors are found
			assertEquals(abScanner.getErrorTokens().length, 0);
		} catch (IOException e) {
			fail("Failed to load example file");
		}
	}
}
