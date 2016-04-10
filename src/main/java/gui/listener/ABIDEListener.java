package gui.listener;

import javax.swing.*;

public interface ABIDEListener {
	
	// Console
	public void scan(String string);
	public Object[][] getScannerOutput();
	public Object[][] getScannerError();
	public void parse();
	public Object[][] getParserOutput();
	public Object[][] getParserError();
	public long getScannerTime();
	public long getParserTime();
	public boolean doesCompile();
	public Object[][][] getSymbolTables();
	public String getSymbolTableName(int id);
	public Object[][] getSemanticErrors();
	public JPanel getParserTree();
	public String getGeneratedCode();
	
	// Menu
	public Object[][] getStateTable();
	public Object[][] getParsingTable();
	public Object[][] getParsingTableRules();
	public Object[][] getParsingTableErrors();
	public Object[][] getFirstAndFollowSets();
	
}
