package littlelui.krosswords;

/* todo next steps:
 * 
 *  - toolbar ausblenden
 *  
 *  - zwischenstand speichern und laden
 *  - kreuzworträtsel automatisch von derstandard.at laden
 *  - lösungen auch laden und prüfen wenn fertig (oder via menüpunkt)
 *  
 */
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import littlelui.krosswords.model.Panel;
import littlelui.krosswords.model.Word;

import com.amazon.kindle.kindlet.AbstractKindlet;
import com.amazon.kindle.kindlet.KindletContext;

public class Main extends AbstractKindlet {
        
        private KindletContext ctx;
        
        private Panel model = createModel();

        public void create(KindletContext context) {
                this.ctx = context;
                
                //this crashed the thing.. damnit
//                ((StandardKindletContext)ctx).getToolbar().setToolbarStyle(ToolbarStyle.TOOLBAR_TRANSIENT);
        }

        private static Panel createModel() {
        	Panel p = new Panel(13, 13);
        	
        	p.add(new Word(0, 1, 4, Word.DIRECTION_HORIZONTAL, 7, "Er macht aus purer Schlamperei zu Noël den Weihnachts- zum Elternabend"));
        	p.add(new Word(5, 1, 8, Word.DIRECTION_HORIZONTAL, 8, "Bei Wels das Viertel zu erleben, heißt: Durchs Gebäude geht ein Beben"));
        	p.add(new Word(0, 3, 9, Word.DIRECTION_HORIZONTAL, 9, "Bei der Subtraktion macht die Ziffer den Unterschied"));
        	p.add(new Word(10, 3, 3, Word.DIRECTION_HORIZONTAL, 10, "Wenn du den Sketch siehst, bitte undsoweitersagen"));
        	p.add(new Word(0, 5, 6, Word.DIRECTION_HORIZONTAL, 12, "Womit der Hosenträger an der Verkleidung des Pultes hängen blieb"));
        	p.add(new Word(7, 5, 6, Word.DIRECTION_HORIZONTAL, 13, "Korrekte Fehldiagnose der Zahnärztin"));
        	
        	p.add(new Word(1, 0, 8, Word.DIRECTION_VERTICAL, 1, "Immer wieder donnerstags: Danach sind die Straßen blitzsauber"));
        	p.add(new Word(3, 0, 9, Word.DIRECTION_VERTICAL, 2, "Der Gutschein ist nicht mehr einlösbar, weshalb wir in Schwermut stürzen"));
        	p.add(new Word(5, 0, 6, Word.DIRECTION_VERTICAL, 3, "\"In einer Nebenrolle soll ich Los legen?\" - \"Ach, reg dich nicht auf!\""));

        	/* Kreuzworträtsel Nr. 6951
        	 * Waagrecht:


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

                        HintsPanel hp = new HintsPanel(model);
                        c.add(hp, BorderLayout.CENTER);

                        //TODO: tight coupling is ugly
                        hp.setCrosswordPanel(cp);
                        cp.setHintsPanel(hp);

                        
                } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t.getMessage(), t);
                }
        }
		

}