package gui;

import gui.bottom.BottomPanel;
import gui.center.CenterPanel;
import gui.listener.ABIDEListener;
import gui.menu.MainMenu;
import gui.tool.ToolBarPanel;
import gui.tool.ToolBarPanel.Button;
import gui.tool.listeners.ClickListener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = -8026416994513756565L;

	// Listener
	private ABIDEListener abIDElistener;
	
	// Components
	private CenterPanel centerPanel;
	private ToolBarPanel toolBarPanel;
	private MainMenu mainMenu;
	private BottomPanel bottomPanel;
	
	public MainFrame(String title) {
		
		// Set application name
		setTitle(title);
		
		// Set default layout to border layout
		setLayout(new BorderLayout());
		
		// Init components
		this.centerPanel = new CenterPanel();
		this.toolBarPanel = new ToolBarPanel();
		this.mainMenu = new MainMenu(this);
		this.bottomPanel = new BottomPanel();
		
		// Configure bottom panel
		this.bottomPanel.setStyle(BottomPanel.Style.NORMAL);
		
		// Set listener
		this.toolBarPanel.setClickListener(new ClickListener() {
			
			@Override
			public void onClickListener(Button type) {
				switch(type) {
				case NEW_FILE:
					centerPanel.addNewFile();
					break;
					
				case RUN:
					if(abIDElistener != null) {
						
						// Prepare message
						String message = "";
						
						// Analyze input
						abIDElistener.analyze(centerPanel.getFileContent());
						
						// Scanner output
						Object[][] scannerOutputData = abIDElistener.getScannerOutput();
						centerPanel.setTableData(CenterPanel.SCANNER_OUTPUT_TITLE, scannerOutputData);
						
						// Error error
						Object[][] scannerErrorData = abIDElistener.getScannerError();
						centerPanel.setTableData(CenterPanel.SCANNER_ERROR_TITLE, scannerErrorData);
						
						// Compilation time
						long compilationTime = abIDElistener.getScannerTime();
						
						// Update compiler message
						if(scannerErrorData.length > 0) {
							message += String.format("Scanner: %d error(s) found!", scannerErrorData.length);
							bottomPanel.setStyle(BottomPanel.Style.ERROR);
						
						// No scanner error found
						} else {
							
							// Parser output
							Object[][] parserOutputData = abIDElistener.getParserOutput();
							centerPanel.setTableData(CenterPanel.PARSER_OUTPUT_TITLE, parserOutputData);
						}
						
						// Insert time
						message += String.format("Total time: %d ms", compilationTime);
						
						// Set message
						bottomPanel.setCompilerMessageText(message);
					}
					break;
				}
			}
		});
		
		// Add menu
		setJMenuBar(mainMenu);
		
		// Add components
		add(this.centerPanel, BorderLayout.CENTER);
		add(this.toolBarPanel, BorderLayout.NORTH);
		add(this.bottomPanel, BorderLayout.SOUTH);
		
		// Screen dim
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		
		 // Configure the JFrame
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);        // Exit when click on X
        this.setPreferredSize(new Dimension((int) (screenDim.width*0.9), (int) (screenDim.height*0.9)));    // Frame initial size
        this.setMinimumSize(new Dimension(600, 600));        // Minimum window size
        this.setVisible(true);                               // Make the frame visible
        this.pack();                                         // Force setting the size of components
        this.setLocationRelativeTo(null);                    // Load on center of the screen
	}
	
	/**
	 * Set listener
	 * @param abIDElistener
	 */
	public void setABIDEListener(ABIDEListener abIDElistener) {
		this.abIDElistener = abIDElistener;
	}
}
