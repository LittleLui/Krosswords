package littlelui.krosswords;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import littlelui.krosswords.model.Puzzle;

import com.amazon.kindle.kindlet.KindletContext;

public class PuzzlePanel extends JPanel {
	public PuzzlePanel(Puzzle model, KindletContext ctx) {
        setLayout(new BorderLayout());
        
        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        add(pTop, BorderLayout.NORTH); 
		CrosswordPanel cp = new CrosswordPanel(model, ctx);
        pTop.add(cp);

        HintsPanel hp = new HintsPanel(model);
        add(hp, BorderLayout.CENTER);

        //TODO: tight coupling is ugly
        hp.setCrosswordPanel(cp);
        cp.setHintsPanel(hp);
	}

}
