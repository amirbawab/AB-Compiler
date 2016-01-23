package gui.menu;

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenu extends JMenuBar{
	
	private static final long serialVersionUID = 856681923844911926L;

	// Components
	private JMenu fileMenu, infoMenu;
	private JMenuItem scannerMenuItem, exitMenuItem;
	private JFrame parent;
	
	public MainMenu(JFrame parent) {
		
		// Set parent
		this.parent = parent;
		
		// Init components
		this.fileMenu = new JMenu("File");
		this.infoMenu = new JMenu("Info");
		this.scannerMenuItem = new JMenuItem("Scanner");
		this.exitMenuItem = new JMenuItem("Exit");
		
		// Add submenu
		this.infoMenu.add(scannerMenuItem);
		this.fileMenu.add(exitMenuItem);
		
		// On exit
		this.exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(MainMenu.this.parent != null)
					MainMenu.this.parent.dispatchEvent(new WindowEvent(MainMenu.this.parent, WindowEvent.WINDOW_CLOSING));
			}
		});
		
		// Add menu
		add(fileMenu);
		add(infoMenu);
	}
}
