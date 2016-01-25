package gui.listener;

public interface ABIDEListener {
	public void analyze(String string);
	public Object[][] scanner_output();
	public Object[][] scanner_error();
}
