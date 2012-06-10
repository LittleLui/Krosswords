import java.awt.Container;
import java.io.File;

import javax.swing.JFrame;

import littlelui.krosswords.Main;

import com.amazon.kindle.kindlet.Kindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.input.GlobalGestureHandler;
import com.amazon.kindle.kindlet.input.keyboard.OnscreenKeyboardManager;
import com.amazon.kindle.kindlet.net.Connectivity;
import com.amazon.kindle.kindlet.security.SecureStorage;
import com.amazon.kindle.kindlet.ui.KMenu;
import com.amazon.kindle.kindlet.ui.KindletUIResources;

/** Just a little emulation environment. Doesn't emulate much though, so nearly useless. 
 *	
 * @author LittleLui 
 * 
 * Copyright 2011-2012 Wolfgang Groiss
 * 
 * This file is part of Krosswords.
 * 
 * Krosswords is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
public class KindletRunner implements KindletContext {
	Kindlet main = new Main();
	
	private JFrame jf = new JFrame("Kindlet Runner");

	public KindletRunner() {
		super();
		jf.setSize(600, 800);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void run() {
		main.create(this);
		
		jf.setVisible(true);
		jf.repaint();
		
		main.start();
	}
	
	public static void main(String[] args) {
		KindletRunner kr = new KindletRunner();
		kr.run();
	}

	public Connectivity getConnectivity() {
		// TODO Auto-generated method stub
		return null;
	}

	public GlobalGestureHandler getGlobalGestureHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getHomeDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	public OnscreenKeyboardManager getOnscreenKeyboardManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public Container getRootContainer() {
		return jf.getContentPane();
	}

	public SecureStorage getSecureStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	public KindletUIResources getUIResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMenu(KMenu arg0) {
		// TODO Auto-generated method stub
		
	}

	public void setSubTitle(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public Object getService(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
