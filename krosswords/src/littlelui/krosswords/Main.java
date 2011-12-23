package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  
 *  - bei tap auf hint editing des worts starten
 *  - bei tap auf key die hints des/der dort beginnenden worte anzeigen (popup? hinscrollen?)
 *  - zwischenstand speichern und laden
 *  - kreuzworträtsel automatisch von derstandard.at laden
 *  - lösungen auch laden und prüfen wenn fertig (oder via menüpunkt)
 *  
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Label;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import littlelui.krosswords.model.Panel;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;
import com.amazon.kindle.kindlet.ui.KPages;
import com.amazon.kindle.kindlet.ui.pages.PageProviders;

public class Main extends AbstractKindlet {
        
        private KindletContext ctx;
        
        private Panel model = createModel();

        public void create(KindletContext context) {
                this.ctx = context;
        }

        private static Panel createModel() {
        	Panel p = new Panel(13, 13);
        	
        	p.add(new Word(0, 1, 4, Word.DIRECTION_HORIZONTAL, 7, "Er macht aus purer Schlamperei zu Noël den Weihnachts- zum Elternabend"));
        	p.add(new Word(5, 1, 8, Word.DIRECTION_HORIZONTAL, 8, "Bei Wels das Viertel zu erleben, heißt: Durchs Gebäude geht ein Beben"));
        	p.add(new Word(0, 3, 9, Word.DIRECTION_HORIZONTAL, 9, "Bei der Subtraktion macht die Ziffer den Unterschied"));
        	p.add(new Word(10, 3, 3, Word.DIRECTION_HORIZONTAL, 10, "Wenn du den Sketch siehst, bitte undsoweitersagen"));
        	
        	p.add(new Word(1, 0, 8, Word.DIRECTION_VERTICAL, 1, "Immer wieder donnerstags: Danach sind die Straßen blitzsauber"));
        	p.add(new Word(3, 0, 9, Word.DIRECTION_VERTICAL, 2, "Der Gutschein ist nicht mehr einlösbar, weshalb wir in Schwermut stürzen"));
        	p.add(new Word(5, 0, 6, Word.DIRECTION_VERTICAL, 3, "\"In einer Nebenrolle soll ich Los legen?\" - \"Ach, reg dich nicht auf!\""));

        	/*
        	 * Waagrecht:


12 Womit der Hosenträger an der Verkleidung des Pultes hängen blieb
13 Korrekte Fehldiagnose der Zahnärztin
15 Aus nämlichen Gründen Moserte Annamirl über Erwin
17 Gib ach, dort wird die Donau zur Innhaberin (irgendwie 1-2 Wörter)
18 Sprich aos, denn seiner ist des Strudels Kern
20 In die erste Reihe gehen? Kann geschehen!
22 Angeschlagen, weil mir die Radteile um die Ohren geflogen sind
23 Planette Aussichten: Die Träne quillt, stürzt sie denn ins(!) Verderben


Senkrecht:

4 Sollts am Tauern länger dauern, stehst du dort Höhlenqualen aus
5 Künstliche Altphilologie: Er kreist wie erkur in der ilchstraße?
6 Was gibst du auf Gefahren? Neun danke, weniger!
11 In der Mensa beim Mittagessen saßen wir alle auf einem Haufen
14 Ein Jammer, vor Gericht solche Stimmen zu hören!
16 Dort wollen wir die Eoxistenz des Hüttenwesens preeisen
17 Sendungen, die ich mir zwecks Rationalisierung Spar-
19 All right, ich geb meins, wenns recht ist
21 Bring mir Malven von den Malediven und lass ihn doch stehen!
        	 */
			// TODO Auto-generated method stub
			return p;
		}

		public void start() {
                try {
                		Container c = ctx.getRootContainer();
                        c.setLayout(new BorderLayout());
                        
                        JPanel pTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
                        c.add(pTop, BorderLayout.NORTH); 
                		CrosswordPanel cp = new CrosswordPanel(model, ctx);
                        pTop.add(cp);

                        KPages hc = new KPages(PageProviders.createBoxLayoutProvider(BoxLayout.Y_AXIS));
                        addHints(hc);
                        c.add(hc, BorderLayout.CENTER);

                        
                } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t.getMessage(), t);
                }
        }
		
		private void add(Container cont, Component comp) {
			if (cont instanceof KPages) 
				((KPages)cont).addItem(comp);
			else
				cont.add(comp);
		}

		private void addHints(Container c) {
			addLabel(c, "Horizontal");

            Iterator/*<Word>*/ i = model.getHorizontalWords().iterator();
            while (i.hasNext()) {
            	Word w = (Word)i.next();
            	JComponent jta = createHintText(w);
            	add(c, jta);
            }
            
            i = model.getVerticalWords().iterator();
			
            add(c, new JLabel(" "));
			addLabel(c, "Vertikal");
            while (i.hasNext()) {
            	Word w = (Word)i.next();
            	JComponent jta = createHintText(w);
            	add(c, jta);
            }
		}

		private void addLabel(Container c, String string) {
			JTextArea l = new JTextArea(string);
			l.setEditable(false);
			l.setEnabled(false); //should still allow mouse clicks
			l.setBorder(null);
			l.setDisabledTextColor(Color.BLACK);
			
			add(c, l);
		}

		private JComponent createHintText(Word w) {
			JLabel llNr = new JLabel(w.getKey()+" ");
			llNr.setForeground(Color.DARK_GRAY);
			JPanel lNr = new JPanel();
			lNr.setLayout(new BoxLayout(lNr, BoxLayout.Y_AXIS));
			lNr.add(llNr);
//			llNr.setFont(llNr.getFont().deriveFont(18f));

			JTextArea jta = new JTextArea(w.getHint());
			jta.setLineWrap(true);
			jta.setWrapStyleWord(true);
			jta.setEditable(false);
			jta.setEnabled(false); //should still allow mouse clicks
			jta.setBorder(null);
			jta.setDisabledTextColor(Color.DARK_GRAY);
//			jta.setFont(jta.getFont().deriveFont(18f));
			
			JPanel p = new JPanel(new BorderLayout());
//			BoxLayout bl = new BoxLayout(p, BoxLayout.X_AXIS);
//			p.setLayout(bl);

			p.add(lNr, BorderLayout.WEST);
			p.add(jta, BorderLayout.CENTER);
			
			return p;
		}

}