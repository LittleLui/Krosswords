package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  - charmanteres layout
 *  - hints anzeigen (scrollend?)
 *  - bei tap auf key die hints des/der dort beginnenden worte anzeigen (popup? hinscrollen?)
 *  - solution eingeben lassen (und dabei l�nge und konformit�t mit kreuzenden w�rtern pr�fen)
 *  - zwischenstand speichern und laden
 *  - kreuzwortr�tsel automatisch von derstandard.at laden
 *  - l�sungen auch laden und pr�fen wenn fertig (oder via men�punkt)
 *  
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;

import littlelui.krosswords.model.Panel;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;

public class Main extends AbstractKindlet {
        
        private KindletContext ctx;
        
        private Panel model = createModel();

        public void create(KindletContext context) {
                this.ctx = context;
        }

        private static Panel createModel() {
        	Panel p = new Panel(13, 13);
        	
        	p.add(new Word(0, 1, 4, Word.DIRECTION_HORIZONTAL, 7, "Er macht aus purer Schlamperei zu No�l den Weihnachts- zum Elternabend"));
        	p.add(new Word(5, 1, 8, Word.DIRECTION_HORIZONTAL, 8, "Bei Wels das Viertel zu erleben, hei�t: Durchs Geb�ude geht ein Beben"));
        	p.add(new Word(0, 3, 9, Word.DIRECTION_HORIZONTAL, 9, "Bei der Subtraktion macht die Ziffer den Unterschied"));
        	p.add(new Word(10, 3, 3, Word.DIRECTION_HORIZONTAL, 10, "Wenn du den Sketch siehst, bitte undsoweitersagen"));
        	
        	p.add(new Word(1, 0, 8, Word.DIRECTION_VERTICAL, 1, "Immer wieder donnerstags: Danach sind die Stra�en blitzsauber"));
        	p.add(new Word(3, 0, 9, Word.DIRECTION_VERTICAL, 2, "Der Gutschein ist nicht mehr einl�sbar, weshalb wir in Schwermut st�rzen"));
        	p.add(new Word(5, 0, 6, Word.DIRECTION_VERTICAL, 3, "\"In einer Nebenrolle soll ich Los legen?\" - \"Ach, reg dich nicht auf!\""));

        	/*
        	 * Waagrecht:


12 Womit der Hosentr�ger an der Verkleidung des Pultes h�ngen blieb
13 Korrekte Fehldiagnose der Zahn�rztin
15 Aus n�mlichen Gr�nden Moserte Annamirl �ber Erwin
17 Gib ach, dort wird die Donau zur Innhaberin (irgendwie 1-2 W�rter)
18 Sprich aos, denn seiner ist des Strudels Kern
20 In die erste Reihe gehen? Kann geschehen!
22 Angeschlagen, weil mir die Radteile um die Ohren geflogen sind
23 Planette Aussichten: Die Tr�ne quillt, st�rzt sie denn ins(!) Verderben


Senkrecht:

4 Sollts am Tauern l�nger dauern, stehst du dort H�hlenqualen aus
5 K�nstliche Altphilologie: Er kreist wie erkur in der ilchstra�e?
6 Was gibst du auf Gefahren? Neun danke, weniger!
11 In der Mensa beim Mittagessen sa�en wir alle auf einem Haufen
14 Ein Jammer, vor Gericht solche Stimmen zu h�ren!
16 Dort wollen wir die Eoxistenz des H�ttenwesens preeisen
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
                		c.setLayout(new FlowLayout());
                		JTextField tf = new JTextField("foobar");
                		CrosswordPanel cp = new CrosswordPanel(model, ctx, tf);
                        c.add(cp);
                        
                        c.setLayout(null);
                        Dimension d = cp.getPreferredSize();
                        cp.setBounds(0,0, d.width, d.height);
                        
                        c.add(tf);
                        tf.requestFocus();
                        tf.requestFocusInWindow();
                        ctx.getOnscreenKeyboardManager().setVisible(true);
                        
                } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t.getMessage(), t);
                }
        }
        
        
        private class MyComponent extends Component {
        	private String text;

        	
        	
			public MyComponent(String text) {
				super();
				this.text = text;
			}

			

			public void paint(Graphics g) {
				super.paint(g);

				g.setColor(Color.black);

				Rectangle2D rView = g.getClipBounds();
				FontMetrics fm = g.getFontMetrics();
				if (fm != null) {
					int width = fm.stringWidth(text);
					int height = fm.getHeight();
					
					int x2 = (int)((rView.getWidth() - width)/2 + rView.getMinX());
					int y2 = (int)((rView.getHeight() - height)/2 + rView.getMinY());
					
					
					g.drawString(text, x2, y2);
				}				
				
				g.drawLine(0, 0, 100,100);
				g.drawLine(0, 100, 100, 0);
			}
        	
        }
}