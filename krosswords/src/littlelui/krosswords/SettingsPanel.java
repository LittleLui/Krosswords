package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import littlelui.krosswords.fetch.DownloadManager;
import littlelui.krosswords.model.Settings;

/** The settings view root UI. 
 * 
 * @author LittleLui
 *
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
public class SettingsPanel extends JPanel {
	private JCheckBox cbAutoDownload = new JCheckBox("Download puzzles automatically when connected");
	private JButton bSave = new JButton("Save");
	private JButton bCancel = new JButton("Cancel");
	
	private Map/*<String, JCheckBox>*/ fetcherCBs = new HashMap();
	
	private Settings settings;
	
	
	public SettingsPanel(Settings se, final DownloadManager dm) {
		super();
		this.settings = se;

		Font titleFont = new Font(Font.SANS_SERIF, Font.PLAIN, se.getSettingsPanelTitleFontSize());

		setLayout(new BorderLayout(0, 15));
//		setLayout(new GridLayout(0, 1));
		
		JLabel jlTitle = new JLabel("Krosswords Settings");
		jlTitle.setFont(titleFont);
		add(jlTitle, BorderLayout.NORTH);
		
		
		JPanel pMain = new JPanel();
		pMain.setLayout(new GridBagLayout());
		add(pMain, BorderLayout.CENTER);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx=0;
		gbc.gridy=GridBagConstraints.RELATIVE;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.WEST;

		cbAutoDownload.setSelected(settings.isAutoDownload());
		pMain.add(cbAutoDownload, gbc);
		
		
		pMain.add(new JLabel(" "), gbc);
		pMain.add(new JLabel("Enabled Puzzle Sources"), gbc);

		JPanel pSetters = new JPanel();
		pSetters.setLayout(new BoxLayout(pSetters, BoxLayout.Y_AXIS));

		Iterator iFetchers = DownloadManager.fetchers.keySet().iterator();
		while (iFetchers.hasNext()) {
			String s = (String) iFetchers.next();
			JCheckBox cb = new JCheckBox(s);
			fetcherCBs.put(s,  cb);
			cb.setSelected(settings.getEnabledFetchers().contains(s));
			pSetters.add(cb);
		}
		
		gbc.insets.left = 15;
		pMain.add(pSetters, gbc);
		JPanel jpButtons = new JPanel();
		jpButtons.add(bSave);
		jpButtons.add(bCancel);

		
		gbc.insets.left = 0;
		pMain.add(jpButtons, gbc);
		
		bSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.setAutoDownload(cbAutoDownload.isSelected());
				
				Iterator iFetcherIds = fetcherCBs.keySet().iterator();
				Set enabledFetchers = new HashSet();
				while (iFetcherIds.hasNext()) {
					String s = (String) iFetcherIds.next();
					JCheckBox cb = (JCheckBox)fetcherCBs.get(s);
					if (cb.isSelected())
						enabledFetchers.add(s);
				}
				
				settings.setEnabledFetchers(enabledFetchers);
				
				settings.save();
				dm.configure(settings);
				
				Main.getInstance().navigateToCatalog();
			}
		});
		
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Main.getInstance().navigateToCatalog();
			}
		});
	}
	
	 
  	

	public static void main (String[] p) {
		Settings s = new Settings();
		SettingsPanel sp = new SettingsPanel(s, new DownloadManager(null, null, null, s));
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.getContentPane().add(sp);
		f.pack();
		f.setVisible(true);
		
		
	}

}
