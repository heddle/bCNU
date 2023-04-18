package cnuphys.advisors.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.advisors.log.LogManager;

public class FileMenu extends JMenu implements ActionListener {
	
	//the menu items
	private JMenuItem _quitItem; //quit
	private JMenuItem _logItem;  //log
	
	public FileMenu() {
		super("File");
		MenuManager.getInstance().addMenu(this);
		_logItem = MenuManager.addMenuItem("Log", KeyEvent.VK_L, this, this);
		addSeparator();
		_quitItem = MenuManager.addMenuItem("Quit", KeyEvent.VK_Q, this, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _logItem) {
			handleLog();
		}
		else if (e.getSource() == _quitItem) {
			handleQuit();
		}
	}
	
	//handle quit selection
	private void handleQuit() {
		System.exit(0);
	}
	
	//handle the log selection
	private void handleLog() {
		LogManager.getInstance().setVisible(true);
	}

}