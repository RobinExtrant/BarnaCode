package controler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.SequentialPlan;
import lejos.hardware.Button;
import lejos.robotics.Color;
import motors.*;
import sensors.ColorSensor;
import sensors.PressionSensor;
import sensors.VisionSensor;
import ui.InputHandler;
import ui.Screen;
import utils.BarnaConstants;
import pddl.Item;
import pddl.ServerListener;
import pddl.pddlLoader;

public class Controler {
	private Graber graber;
	private Propulsion propulsion;
	private ColorSensor color;
	private PressionSensor pression;
	private VisionSensor vision;
	private InputHandler input;
	private Screen screen;
	private boolean westBlack;
	private int initOrientation;
	private ArrayList<Intersection> intersections;
	
	public enum RelativeOrientation {WEST, MIDDLE, EAST}
	//private ArrayList<Item> listItem;
	private int counterColor;
	
	public Controler() {
		this.counterColor = 0;
		this.propulsion = new Propulsion();
		this.graber     = new Graber();
		this.color      = new ColorSensor();
		this.pression   = new PressionSensor();
		this.vision     = new VisionSensor();
		this.screen     = new Screen();
		this.input      = new InputHandler(screen);
		this.initOrientation = BarnaConstants.NORTH;
		this.intersections = new ArrayList<Intersection>(Arrays.asList(new Intersection("A", Color.GREEN, RelativeOrientation.WEST),
																	new Intersection("B", Color.GREEN, RelativeOrientation.MIDDLE),
																	new Intersection("C", Color.GREEN, RelativeOrientation.EAST),
																	new Intersection("D", Color.BLACK, RelativeOrientation.WEST),
																	new Intersection("E", Color.BLACK, RelativeOrientation.MIDDLE),
																	new Intersection("F", Color.BLACK, RelativeOrientation.EAST),
																	new Intersection("G", Color.BLUE, RelativeOrientation.WEST),
																	new Intersection("H", Color.BLUE, RelativeOrientation.MIDDLE),
																	new Intersection("I", Color.BLUE, RelativeOrientation.EAST)));
		//this.listItem = new ArrayList<Item>();
	}
	
	public void start() throws FileNotFoundException, ClassNotFoundException, IOException {
		boolean loaded = loadCalibration();
		//boolean loaded = false;
		boolean nocalibrate = false;
		if (loaded) {
			screen.drawText("Calibration", 
				"Appuyez sur echap ","pour skipper");
			nocalibrate = input.waitButton(Button.ID_ESCAPE);
		}
		if(!nocalibrate){
			calibrate();
			saveCalibration();
		}
		
		mainLoop();
	}
	
	private void mainLoop() {
		screen.drawText("Position", 
				"Appuyez sur echap pour gauche","ou autre pour droite");
		this.westBlack = input.waitButton(Button.ID_ESCAPE);
		screen.drawText("Commencer");
		input.waitAny();
		
		/*File domain = new File("barnaplan/domain.pddl");
		File problem = new File("barnaplan/test9case.pddl");
		pddlLoader pddl = new pddlLoader();
		pddl.generatePlan(domain, problem);
		SequentialPlan sp = pddl.getPlan();
		for (BitOp action : sp.actions()) {
			if (action.getName().equals("moveoutbut")) {
				int indexIntersection = action.getValueOfParameter(1);
				goToIntersection(indexIntersection);
			}
		}*/
		/*for (int index = 0; index <9; index++) {
			goToIntersection(index);
		}*/
		goToIntersection(0);
		goToIntersection(1);
		goToIntersection(2);
		goToIntersection(5);
		goToIntersection(4);
		goToIntersection(3);
		goToIntersection(7);
		goToIntersection(6);
		goToIntersection(8);
		//color_test();
	}
	
	private void goToIntersection(int choix) {
		Intersection intersection = this.intersections.get(choix);
		int lineColor = intersection.getColor();
		RelativeOrientation relativeOrientation = intersection.getRelativeOrientation();
		
		/*Tant qu'on ne croise pas la ligne de couleur sur laquelle se trouve le palet
		On continue d'avancer*/
		propulsion.run(true);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
			int colorCapted = color.getCurrentColor();
			if(colorCapted == lineColor){
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.counterColor ++;
			}else {
				this.counterColor = 0;
			}
			if(this.counterColor >= 2) {
				this.counterColor = 0;
				propulsion.stopMoving();
			}
		}
		
		/*Selon la direction cardinale où se trouve le palet sur cette ligne, on récupère la bonne
		orientation*/
		int orientation;
		switch (relativeOrientation) {
			case WEST:
				orientation = BarnaConstants.WEST;				
				break;
			case MIDDLE:
				if (westBlack) orientation = BarnaConstants.EAST;
				else orientation = BarnaConstants.WEST;
				break;
			case EAST:
				orientation = BarnaConstants.EAST;
				break;
			default:
				orientation = BarnaConstants.WEST;
		}
		//IF NOT FIRST INTERSECTION, WE INVERSE ORIENTATION
		if (choix != 0) {
			orientation = -orientation;
		}
		
		/* Rotation de l'angle voulu */
		propulsion.rotate((float)orientation, false, false);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		boolean withGraberClosed = false;
		/* On avance jusqu'au palet */
		propulsion.run(true);
		while(propulsion.isRunning()){
			if(pression.isPressed()){
				propulsion.stopMoving();
				graber.close();
				withGraberClosed = true;
			}
			
			int colorCapted = color.getCurrentColor();
			if(colorCapted == Color.RED || colorCapted == Color.YELLOW){
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.counterColor ++;
			}else {
				this.counterColor = 0;
			}
			if(this.counterColor >= 2) {
				this.counterColor = 0;
				propulsion.stopMoving();
				withGraberClosed = false;
			}
		}
		
		goEnemyCamp(withGraberClosed);
		
		/* Selon de quel côté on est allé chercher le palet, on met à jour l'information
		 * Si l'on se trouve à l'ouest ou à l'est de la ligne noire verticale
		 */
		switch (relativeOrientation) {
		case WEST:
			westBlack = true;
			break;
		case MIDDLE:
			/* Si on est au milieu, alors avec notre technique de décalage pour éviter les palets
			alors, on se retrouve du côté inverse */
			westBlack = !westBlack;		
			break;
		case EAST:		
			westBlack = false;	
			break;
		default:
			orientation = BarnaConstants.WEST;
		}
	}
	
	private void color_test() {
		while (true) {
			if(input.escapePressed())
				System.exit(0);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			//screen.drawText("Bleu : " + Color.BLUE, "Noir : " + Color.BLACK, "Vert : " + Color.GREEN);
			screen.drawText("Bleu : " + Color.BLUE, "Noir : " + Color.BLACK, "Vert : " + Color.GREEN," color : " + String.valueOf(color.getCurrentColor()));
		}
	}
	
	private void spin_and_search() {
		/*Test aller chercher premier palet dans sa vision de 20-70cm*/
		propulsion.rotate(BarnaConstants.FULL_CIRCLE, false, false);
		float newDist;
		while(propulsion.isRunning()) {
			newDist = vision.getRaw()[0];
			if(newDist < BarnaConstants.MAX_VISION_RANGE
					   && newDist >= BarnaConstants.MIN_VISION_RANGE){
				propulsion.stopMoving();
				propulsion.run(true);
				while(propulsion.isRunning()){
					checkLeftRightBlack();
					if(pression.isPressed()){
						propulsion.stopMoving();
						graber.close();
					}
				}
				goEnemyCamp(true);
			}
		}
	}
	
	private void checkLeftRightBlack() {
		int currentColor = this.color.getCurrentColor();
		if (currentColor != Color.BLACK && color.getLastColor() == Color.BLACK) {
			if (propulsion.getRotateToNorth() >= BarnaConstants.EAST - BarnaConstants.PADDING_CHECK_ORIENTATION &&
					propulsion.getRotateToNorth() <= BarnaConstants.EAST + BarnaConstants.PADDING_CHECK_ORIENTATION) {
				this.westBlack = true;
				screen.drawText("Droite/Gauche noir", "Gauche", String.valueOf(propulsion.getRotateToNorth()));
			} else if (propulsion.getRotateToNorth() >= BarnaConstants.WEST - BarnaConstants.PADDING_CHECK_ORIENTATION &&
					propulsion.getRotateToNorth() <= BarnaConstants.WEST + BarnaConstants.PADDING_CHECK_ORIENTATION) {
				this.westBlack = false;
				screen.drawText("Droite/Gauche noir", "Droite", String.valueOf(propulsion.getRotateToNorth()));
			}
		}
		color.setLastColor(currentColor);
	}
	
	private void goEnemyCamp(boolean withGraberClosed) {
		double rotation = propulsion.getRotateToNorth();
		
		int angleToShift;
		if (westBlack) angleToShift = 45;
		else angleToShift = -45;
		
		rotation = rotation + angleToShift;
		
		propulsion.rotate((float)rotation, false, false);
		while(propulsion.isRunning() || graber.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.graber, this.propulsion)));
		}
		propulsion.runFor(800, true);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		propulsion.rotate(angleToShift, true, false);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		
		propulsion.run(true);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
			if(color.getCurrentColor() == Color.WHITE){
				propulsion.stopMoving();
				if (withGraberClosed)
				{
					graber.open();
				}
			}
		}
		while(graber.isRunning()) {
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.graber)));
		}
		
		propulsion.runFor(300, false);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		propulsion.orientateSouth(true);
	}

	private void checkMotor(ArrayList<TimedMotor> toCheck) {
		if(input.escapePressed())
			System.exit(0);
		for (TimedMotor motor : toCheck) {
			motor.checkState();
		}
	}

	private void calibrate() {
		calibrationGrabber();
		calibrationCouleur();
	}
	
	private void calibrationGrabber() {
		screen.drawText("Calibration", 
						"Calibration de la fermeture de la pince",
						"Appuyez sur le bouton central ","si les pinces ne sont pas déjà fermées");
		if(input.waitButton(Button.ID_ENTER)){
			screen.drawText("Calibration", 
						"Appuyez sur ok","pour lancer et arrêter", "quand les pinces sont fermées");
			input.waitAny();
			graber.startCalibrate(false);
			input.waitAny();
			graber.stopCalibrate(false);

		} else {
			this.graber.stopCalibrate(false);
		}
		screen.drawText("Calibration", 
				"Appuyer sur Entree", "pour commencer la",
				"calibration de l'ouverture", "et sur Entree quand les", "pinces sont ouvertes");
		input.waitAny();
		graber.startCalibrate(true);
		input.waitAny();
		graber.stopCalibrate(true);
	}
	
	/**
	 * Effectue la calibration de la couleur
	 * @return renvoie vrai si tout c'est bien passé
	 */
	private void calibrationCouleur() {
		screen.drawText("Calibration", 
						"Préparez le robot à la ","calibration des couleurs",
						"Appuyez sur un bouton ","pour continuer");
		input.waitAny();
		color.lightOn();

		//calibration gris
		screen.drawText("Gris", 
				"Placer le robot sur ","la couleur grise");
		input.waitAny();
		color.calibrateColor(Color.GRAY);

		//calibration rouge
		screen.drawText("Rouge", "Placer le robot ","sur la couleur rouge");
		input.waitAny();
		color.calibrateColor(Color.RED);

		//calibration noir
		screen.drawText("Noir", "Placer le robot ","sur la couleur noir");
		input.waitAny();
		color.calibrateColor(Color.BLACK);

		//calibration jaune
		screen.drawText("Jaune", 
				"Placer le robot sur ","la couleur jaune");
		input.waitAny();
		color.calibrateColor(Color.YELLOW);

		//calibration bleue
		screen.drawText("BLeue", 
				"Placer le robot sur ","la couleur bleue");
		input.waitAny();
		color.calibrateColor(Color.BLUE);

		//calibration vert
		screen.drawText("Vert", "Placer le robot ","sur la couleur vert");
		input.waitAny();
		color.calibrateColor(Color.GREEN);

		//calibration blanc
		screen.drawText("Blanc", "Placer le robot ","sur la couleur blanc");
		input.waitAny();
		color.calibrateColor(Color.WHITE);

		color.lightOff();
	}

	/**
	 * Charge la calibration du fichier de configuration si elle existe
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @return true if a file calibration has found and loaded
	 */
	private boolean loadCalibration() throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File("calibration");
		if(file.exists()){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			color.setCalibration((float[][])ois.readObject());
			graber.setOpenTime((long)ois.readObject());
			ois.close();
			return true;
		}
		return false;
	}

	/**
	 * Sauvegarde la calibration
	 * @throws IOException
	 */
	private void saveCalibration() throws IOException {
		screen.drawText("Sauvegarde", 
				"Appuyez sur le bouton central ","pour valider id",
				"Echap pour ne pas sauver");
		if(input.waitButton(Button.ID_ENTER)){
			File file = new File("calibration");
			if(!file.exists()){
				file.createNewFile();
			}else{
				file.delete();
				file.createNewFile();
			}
			ObjectOutputStream str = new ObjectOutputStream(new FileOutputStream(file));
			str.writeObject(color.getCalibration());
			str.writeObject(graber.getOpenTime());
			str.flush();
			str.close();
		}
	}

	/*@Override
	public void receiveRawPoints(List<Item> lastPointsReceived) {
		this.listItem.addAll(lastPointsReceived);
	}

	@Override
	public void displayList() {
		for (int i = 0; i < listItem.size(); i++) {
			System.out.println("Palet " + i + " : x = " + listItem.get(i).getX() + " y = " + listItem.get(i).getY());
		}
	}*/
}
