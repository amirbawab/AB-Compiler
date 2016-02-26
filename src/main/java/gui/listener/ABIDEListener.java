package gui.listener;

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
	
	// Menu
	public Object[][] getStateTable();
	public Object[][] getParsingTable();
	public Object[][] getFirstAndFollowSets();
}
