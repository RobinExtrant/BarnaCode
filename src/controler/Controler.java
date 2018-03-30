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
	
	public enum RelativeOrientation {WEST, MIDDLE, EAST}
	//private ArrayList<Item> listItem;
	
	public Controler() {
		this.propulsion = new Propulsion();
		this.graber     = new Graber();
		this.color      = new ColorSensor();
		this.pression   = new PressionSensor();
		this.vision     = new VisionSensor();
		this.screen     = new Screen();
		this.input      = new InputHandler(screen);
		this.initOrientation = BarnaConstants.NORTH;
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
		//base_test();
		//spin_test();
		goToIntersection(1);
		propulsion.runFor(300, false);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		propulsion.orientateSouth(true);
		goToIntersection(2);
		/*while (true) {
			propulsion.run(true);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			//screen.drawText("Bleu : " + Color.BLUE, "Noir : " + Color.BLACK, "Vert : " + Color.GREEN);
			screen.drawText("Bleu : " + Color.BLUE, "Noir : " + Color.BLACK, "Vert : " + Color.GREEN," color : " + String.valueOf(color.getCurrentColor()));
		}*/
	}
	
	private void goToIntersection(int choix) {
		int lineColor;
		RelativeOrientation relativeOrientation;
		
		if (choix == 1) {
			lineColor = Color.GREEN;
			relativeOrientation = RelativeOrientation.MIDDLE;
		} else {
			lineColor = Color.BLUE;
			relativeOrientation = RelativeOrientation.MIDDLE;
		}
		/*int lineColor = Color.GREEN;
		RelativeOrientation relativeOrientation = RelativeOrientation.WEST;*/
		
		propulsion.run(true);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
			if(color.getCurrentColor() == lineColor){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				propulsion.stopMoving();
			}
		}
		
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
		
		propulsion.rotate((float)orientation, false, false);
		while(propulsion.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		
		propulsion.run(true);
		while(propulsion.isRunning()){
			if(pression.isPressed()){
				propulsion.stopMoving();
				graber.close();
				//Si on est au milieu, on met à jour westBlack car nous allons passé
				//de l'autre côté en allant dans le camps adverse
			}
		}
		
		goEnemyCamp();
		
		switch (relativeOrientation) {
		case WEST:
			westBlack = true;
			break;
		case MIDDLE:
			westBlack = !westBlack;		
			break;
		case EAST:		
			westBlack = false;	
			break;
		default:
			orientation = BarnaConstants.WEST;
		}
	}
	
 	private void forward_test() {
		/*Test palet droit puis le ramener en reculant*/
		propulsion.run(true);
		while(propulsion.isRunning()){
			if(pression.isPressed()){
				propulsion.stopMoving();
				graber.close();
			}
		}
		while(graber.isRunning()) {
			graber.checkState();
		}
		propulsion.run(false);
		while(propulsion.isRunning()){
			propulsion.checkState();
			if(input.escapePressed())
				return;
			if(color.getCurrentColor() == Color.WHITE){
				propulsion.stopMoving();
			}
		}
	}
	
	private void spin_test() {
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
				goEnemyCamp();
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
	
	private void goEnemyCamp() {
		double rotation = propulsion.getRotateToNorth();
		/*if (rotation < 0) {
			rotation = rotation+BarnaConstants.SOUTH;
		} else {
			rotation = rotation-BarnaConstants.SOUTH;
		}*/
		
		/*propulsion.rotate((float)rotation, false, false);
		while(propulsion.isRunning() || graber.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.graber, this.propulsion)));
		}*/
		
		int angleToShift;
		if (westBlack) angleToShift = 45;
		else angleToShift = -45;
		
		rotation = rotation + angleToShift;
		/*
		propulsion.rotate(45, false, false);
		while(propulsion.isRunning()){
			checkLeftRightBlack();
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		propulsion.runFor(1200, true);
		while(propulsion.isRunning()){
			checkLeftRightBlack();
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}
		propulsion.rotate(45, true, false);
		while(propulsion.isRunning()){
			checkLeftRightBlack();
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.propulsion)));
		}*/
		
		propulsion.rotate((float)rotation, false, false);
		while(propulsion.isRunning() || graber.isRunning()){
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.graber, this.propulsion)));
		}
		propulsion.runFor(1200, true);
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
				graber.open();
			}
		}
		while(graber.isRunning()) {
			checkMotor(new ArrayList<TimedMotor>(Arrays.asList(this.graber)));
		}
	}

	private void checkMotor(ArrayList<TimedMotor> toCheck) {
		if(input.escapePressed())
			return;
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
