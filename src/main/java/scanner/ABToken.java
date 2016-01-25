package scanner;

public class ABToken {
	private String token;
	private String value;
	private int row, col;
	
	/**
	 * Create ABToken
	 * @param token
	 * @param value
	 * @param row
	 * @param col
	 */
	public ABToken(String token, String value, int row, int col) {
		this.token = token;
		this.value = value;
		this.row = row;
		this.col = col;
	}

	/**
	 * Get token
	 * @return token
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Set token
	 * @param token
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Get value
	 * @return value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Set value
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Get row
	 * @return row
	 */
	public int getRow() {
		return row;
	}
	
	/**
	 * Set row
	 * @param row
	 */
	public void setRow(int row) {
		this.row = row;
	}
	
	/**
	 * Get col
	 * @return col
	 */
	public int getCol() {
		return col;
	}
	
	/**
	 * Set col
	 * @param col
	 */
	public void setCol(int col) {
		this.col = col;
	}
}