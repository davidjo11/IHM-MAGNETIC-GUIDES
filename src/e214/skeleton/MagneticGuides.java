package e214.skeleton ;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import fr.lri.swingstates.canvas.CExtensionalTag;
import fr.lri.swingstates.canvas.CRectangle;
import fr.lri.swingstates.canvas.CSegment;
import fr.lri.swingstates.canvas.CShape;
import fr.lri.swingstates.canvas.CStateMachine;
import fr.lri.swingstates.canvas.Canvas;
import fr.lri.swingstates.canvas.transitions.ClickOnTag;
import fr.lri.swingstates.canvas.transitions.EnterOnTag;
import fr.lri.swingstates.canvas.transitions.LeaveOnTag;
import fr.lri.swingstates.canvas.transitions.PressOnTag;
import fr.lri.swingstates.debug.StateMachineVisualization;
import fr.lri.swingstates.sm.BasicInputStateMachine;
import fr.lri.swingstates.sm.State;
import fr.lri.swingstates.sm.Transition;
import fr.lri.swingstates.sm.transitions.Click;
import fr.lri.swingstates.sm.transitions.Drag;
import fr.lri.swingstates.sm.transitions.Press;
import fr.lri.swingstates.sm.transitions.Release;

/**
 * @author Nicolas Roussel (roussel@lri.fr)
 *
 */
@SuppressWarnings({"unused", "serial"})
public class MagneticGuides extends JFrame {

	private Canvas canvas ;
	private CExtensionalTag oTag ;
	private CExtensionalTag magnet, horizontalMagnet, verticalMagnet;

	public MagneticGuides(String title, int width, int height) {
		super(title) ;
		canvas = new Canvas(width, height) ;
		canvas.setAntialiased(true) ;
		getContentPane().add(canvas) ;

		oTag = new CExtensionalTag(canvas) {} ;

		magnet =  new MagneticGuide(canvas) {};
		//Ces deux-là sont pour les déplacements (en effet les translate à faire pour les lignes verticales sont différentes de celles de lignes horizontales).
		horizontalMagnet = new MagneticGuide(canvas) {};
		verticalMagnet =  new MagneticGuide(canvas) {};

		CStateMachine sm = new CStateMachine() {

			private Point2D p ;
			private CSegment seg;
			private CShape draggedShape, parent ;

			public State start = new State() {
				//Presser un carré:
				Transition pressOnTag = new PressOnTag(oTag, BUTTON1, ">> oDrag") {
					public void action() {
						p = getPoint() ;
						draggedShape = getShape() ;
					}
				} ;
				//Clic sur un espace vide avec le bouton gauche de la souris crée une guide horizontale
				Transition newHorizontalLine = new Press(BUTTON1, ">> start"){
					public void action(){
						p = getPoint();
						//						System.out.println(p.getX() + "/" + p.getY());
						seg = canvas.newSegment(0,p.getY(),canvas.getWidth(),p.getY());
						seg.addTag(horizontalMagnet);
						seg.addTag(magnet);
						seg.belowAll();
						seg.setOutlinePaint(null);
//						seg.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
						System.out.println("Horizontal segment created!");
						
					}
				};
				//Clic sur un espace vide avec le bouton droit de la souris crée une guide verticale
				Transition newVerticalLine = new Press(BasicInputStateMachine.BUTTON3, ">> start"){
					public void action(){
						p = getPoint();
						seg = canvas.newSegment(p.getX(),0,p.getX(),canvas.getHeight());
						seg.addTag(verticalMagnet);
						seg.addTag(magnet);
						seg.belowAll();
						seg.setOutlinePaint(null);
//						seg.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));
						System.out.println("Vertical segment created!");
					}
				};
				//Presser une ligne horizontale permet ensuite de la trainer
				Transition dragHLine = new PressOnTag(horizontalMagnet, BUTTON1, ">> DragHLine"){
					public void action(){
						p = getPoint();
						draggedShape = getShape();
						System.out.println("Press on tag Hmagnet: " + getShape());
						List<CShape> l = magnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						CShape aux = null;
						while(it.hasNext()){
							CShape s = it.next();
							//On prend uniquement les carrés.
							if(s.hasTag(oTag)){
								System.out.println(s);
								if(s.contains(s.getMinX(), draggedShape.getCenterY()) != null){
									if(s.getParent() != draggedShape){
										double x = s.getCenterX(), y = s.getCenterY();
										draggedShape.addChild(s);
										s.translateTo(x, y);
									}
									System.out.println("the one: " + s + "/" +draggedShape);
								}
							}
						}
					}
				};
				//Appui sur button1 sur une ligne verticale permet de la trainer.
				Transition dragVLine = new PressOnTag(verticalMagnet, BUTTON1, ">> DragVLine"){
					public void action(){
						p = getPoint();
						draggedShape = getShape();
						System.out.println("Press on tag Vmagnet: " + getShape());
						List<CShape> l = magnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						CShape aux = null;
						while(it.hasNext()){
							CShape s = it.next();
							//On prend uniquement les carrés.
							if(s.hasTag(oTag)){
								System.out.println(s);
								if(s.contains(draggedShape.getCenterX(), s.getMinY()) != null){
									if(s.getParent() != draggedShape){
										double x = s.getCenterX(), y = s.getCenterY();
										draggedShape.addChild(s);
										s.translateTo(x, y);
									}
									System.out.println("the one: " + s + "/" +draggedShape);
								}
							}
						}
					}
				};
				//Appui sur les touches button1 & control sur une ligne horizontale permet de la supprimer
				Transition supprHLine = new ClickOnTag(horizontalMagnet, BUTTON1, CONTROL,">> start"){
					public void action(){
						getShape().removeTag(horizontalMagnet);
						getShape().removeTag(magnet);
						getShape().remove();
						System.out.println("HRemoved, I guess?");
					}
				};
				//Appui sur les touches button1 & control sur une ligne verticale permet de la supprimer
				Transition supprVLine = new ClickOnTag(verticalMagnet, BUTTON1, CONTROL, ">> start"){
					public void action(){
						getShape().removeTag(verticalMagnet);
						getShape().removeTag(magnet);
						getShape().remove();
						System.out.println("VRemoved, I guess?");
					}
				};
				//Appui sur les touches button1 et control permet de cacher/afficher les guides (verticales et horizontales).
				Transition hideAll = new Click(BUTTON1, CONTROL,">> start"){
					public void action(){
						List<CShape> l = horizontalMagnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						while(it.hasNext()){
							if(it.next().isDrawable())
								it.next().setDrawable(false);
							else it.next().setDrawable(true);
						}
						l = verticalMagnet.getFilledShapes();
						it = l.iterator();
						while(it.hasNext())
							if(it.next().isDrawable())
								it.next().setDrawable(false);
							else it.next().setDrawable(true);
					}
				};
				Transition enterLine = new EnterOnTag(magnet){
					public void action(){
						if(!getShape().hasTag(oTag)){
							parent = getShape();
							System.out.println("EnterOnShape: "+parent);
							parent.setOutlinePaint(Color.red);
						}
					}
				};
				
				Transition leaveLine = new LeaveOnTag(magnet){
					public void action(){
						if(!getShape().hasTag(oTag)){
							parent.setOutlinePaint(null);
							parent = null;
							System.out.println("LeaveOnShape: "+parent);
						}
					}
				};
			} ;

			public State oDrag = new State() {
				//Déplacer un carré
				Transition drag = new Drag(BUTTON1,">> oDrag") {
					public void action() {
//						System.out.println("DraggedShape dragged.");
						Point2D q = getPoint() ;
						draggedShape.translateBy(q.getX() - p.getX(), q.getY() - p.getY()) ;
						p = q ;
					}
				} ;
				/*
				Transition enterOnHLine = new EnterOnTag(magnet, ">> oDrag"){
					public void action(){
						System.out.println("EnterOnTag: "+getShape());
						getShape().setOutlinePaint(Color.red);
					}
				};
				
				Transition leaveOnHLine = new LeaveOnTag(magnet, ">> oDrag"){
					public void action(){
						getShape().setOutlinePaint(null);
						System.out.println("LeaveOnTag: "+getShape());
					}
				};*/
				
				Transition enterLine = new EnterOnTag(magnet){
					public void action(){
						if(!getShape().hasTag(oTag)){
							parent = getShape();
							System.out.println("EnterOnShape: "+parent);
							parent.setOutlinePaint(Color.red);
						}
					}
				};
				
				Transition leaveLine = new LeaveOnTag(magnet){
					public void action(){
						if(!getShape().hasTag(oTag)){
							parent.setOutlinePaint(null);
							parent = null;
							System.out.println("LeaveOnShape: "+parent);
						}
					}
				};
						
				
				//Relâcher un carré sur une ligne, associe le carré au tag magnet
				Transition release = new Release(BUTTON1, ">> start") {
					public void action(){
						System.out.println("DraggedShape released");
						List<CShape> l = magnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						CShape aux = null;
						//On parcourt l'ensemble des shapes ayant le tag "magnet" (défini un peu plus haut).
						while(it.hasNext()){
							CShape s = it.next();
							//On ne prend que les segments (il peut y avoir des carrés avec le tag magnet si on crée des segments et placés des carrés sur eux).
							if(!s.hasTag(oTag)){
								System.out.println(s);
								//On regarde s'il y a un point d'intersection entre le segment gauche du carré et la ligne horizontale ( respectivement segment haut pour la ligne verticale).
								//if(draggedShape.contains(draggedShape.getMinX(), s.getCenterY()) != null || draggedShape.contains(s.getCenterX(), draggedShape.getMinY()) != null){
								//Autre intersection possible: voir si la ligne passe par le centre du carré
								//draggedShape.contains(draggedShape.getCenterX(), s.getMinY()) != null : pour placer le centre du carré sur la ligne horizontale
								//s.contains(s.getMinX(), draggedShape.getCenterY()) != null : pour placer le centre du carré sur la ligne verticale
								//aux = s;
								//									s.setOutlinePaint(Color.RED);
								//On place le carré en tant qu'enfant du segment qui le coupe...
								//									s.addChild(draggedShape);
								//[...]on ajoute le tag magnet au carré.
								//draggedShape.addTag(magnet);
								//s.addChild(draggedShape);
								//									System.out.println("S X axis: "+s.getReferenceX());
								
								if(draggedShape.contains(draggedShape.getMinX(), s.getCenterY()) != null){
									System.out.println("Parent found: " + s + "/" +draggedShape);
									aux = s;
									draggedShape.addTag(magnet);
									s.addChild(draggedShape);
									draggedShape.translateTo(p.getX(), s.getCenterY());
								}
								else if(draggedShape.contains(s.getCenterX(), draggedShape.getMinY()) != null){
									System.out.println("Parent found: " + s + "/" +draggedShape);	
									aux = s;
									draggedShape.addTag(magnet);
									s.addChild(draggedShape);
									draggedShape.translateTo(s.getCenterX(), p.getY());
								}
							}
						}/*
						if(aux != null){
							l = aux.getChildren();
							it = l.iterator();
							System.out.println("Children:");
							while(it.hasNext()){
								System.out.println(it.next());
							}
						}*/
						//draggedShape == le carré tout juste relâché
						//aux == null quand le carré n'a pas été relâché sur une guide.
						if(aux == null && draggedShape.hasTag(magnet)){
							//							System.out.println(draggedShape.getParent());
							System.out.println("DraggedShape magnet removed.");
							draggedShape.removeTag(magnet);
							draggedShape.translateTo(p.getX(), p.getY());
						}
						if(aux == null && draggedShape.getParent() != null){
							draggedShape.getParent().removeChild(draggedShape);
							System.out.println("Parent removed: " + draggedShape.getParent());
							draggedShape.translateTo(p.getX(), p.getY());
						}
						p = null;
						aux = null;
					}
				} ;
			};

			public State DragHLine = new State(){
				Transition dragLine = new Drag(BUTTON1, ">> DragHLine"){
					public void action(){
						Point2D q = getPoint();
						draggedShape.translateBy(0, q.getY() - p.getY());
						p = q;
					}
				};
				Transition release = new Release(BUTTON1, ">> start") {
					public void action(){
						System.out.println("HLine released.");
						/*List<CShape> l = magnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						CShape aux = null;
						while(it.hasNext()){
							CShape s = it.next();
							if(s.hasTag(oTag) && s.getParent() != null){
								s.getParent().removeChild(s);
							}
						}*/
						p = null;
					}
				} ;
			} ;

			public State DragVLine = new State(){
				Transition dragLine = new Drag(BUTTON1, ">> DragVLine"){
					public void action(){
						Point2D q = getPoint();
						draggedShape.translateBy(q.getX() - p.getX(), 0);
						p = q;
					}
				};
				Transition release = new Release(BUTTON1, ">> start") {
					public void action(){
						System.out.println("VLine released.");
						/*List<CShape> l = magnet.getFilledShapes();
						Iterator<CShape> it = l.iterator();
						CShape aux = null;
						while(it.hasNext()){
							CShape s = it.next();
							if(s.hasTag(oTag) && s.getParent() != null){
								s.getParent().removeChild(s);
							}
						}*/
						p = null;
					}
				} ;
			} ;


		} ;
		sm.attachTo(canvas);

		JFrame jsm = new JFrame();
		//		JFrame jsm2 = new JFrame();

		StateMachineVisualization smv = new StateMachineVisualization(sm);
		//		StateMachineVisualization smv2 = new StateMachineVisualization(sm3);
		jsm.add(smv);
		//		jsm2.add(smv2);
		jsm.getContentPane().add(smv) ;
		//		jsm2.getContentPane().add(smv2);
		jsm.pack() ;
		//		jsm2.pack();
		jsm.setVisible(true) ;

		pack() ;
		setVisible(true) ;
		canvas.requestFocusInWindow() ;
	}

	public void populate() {
		int width = canvas.getWidth() ;
		int height = canvas.getHeight() ;

		double s = (Math.random()/2.0+0.5)*30.0 ;
		double x = s + Math.random()*(width-2*s) ;
		double y = s + Math.random()*(height-2*s) ;


		int red = (int)((0.8+Math.random()*0.2)*255) ;
		int green = (int)((0.8+Math.random()*0.2)*255) ;
		int blue = (int)((0.8+Math.random()*0.2)*255) ;

		CRectangle r = canvas.newRectangle(x,y,s,s) ;
		r.setFillPaint(new Color(red, green, blue)) ;
		r.addTag(oTag) ;
	}

	public static void main(String[] args) {
		MagneticGuides guides = new MagneticGuides("Magnetic guides",600,600) ;
		for (int i=0; i<20; ++i) guides.populate() ;
		guides.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) ;
	}

}
