package gui;

import gui.center.CenterPanel;
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

	// Components
	private CenterPanel centerPanel;
	private ToolBarPanel toolBarPanel;
	private MainMenu mainMenu;
	
	public MainFrame(String title) {
		
		// Set application name
		setTitle(title);
		
		// Set default layout to border layout
		setLayout(new BorderLayout());
		
		// Init components
		this.centerPanel = new CenterPanel();
		this.toolBarPanel = new ToolBarPanel();
		this.mainMenu = new MainMenu(this);
		
		// Set listener
		this.toolBarPanel.setClickListener(new ClickListener() {
			
			@Override
			public void onClickListener(Button type) {
				switch(type) {
				case NEW_FILE:
					centerPanel.addNewFile();
					break;
					
				case RUN:
					break;
				}
			}
		});
		
		// Add menu
		setJMenuBar(mainMenu);
		
		// Add components
		add(this.centerPanel, BorderLayout.CENTER);
		add(this.toolBarPanel, BorderLayout.NORTH);
		
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
}
