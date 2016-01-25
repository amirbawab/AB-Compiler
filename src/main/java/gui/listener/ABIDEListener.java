package gui.listener;

public interface ABIDEListener {
	public void analyze(String string);
	public Object[][] getScannerOutput();
	public Object[][] getScannerError();
	public long getScannerTime();
}
