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


public class KindletRunner implements KindletContext {
	Kindlet main = new Main();
	
	private JFrame jf = new JFrame("Kindlet Runner");

	public KindletRunner() {
		super();
		jf.setSize(600, 800);
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
	
}
