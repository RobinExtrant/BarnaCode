package controler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lejos.hardware.Button;
import lejos.robotics.Calibrate;
import lejos.robotics.Color;
import motors.Graber;
import motors.Propulsion;
import sensors.ColorSensor;
import sensors.PressionSensor;
import sensors.VisionSensor;
import ui.InputHandler;
import ui.Screen;

public class Controler {
	private Graber graber;
	private Propulsion propulsion;
	private ColorSensor color;
	private PressionSensor pression;
	private VisionSensor vision;
	private InputHandler input;
	private Screen screen;

	public Controler() {
		this.propulsion = new Propulsion();
		this.graber     = new Graber();
		this.color      = new ColorSensor();
		this.pression   = new PressionSensor();
		this.vision     = new VisionSensor();
		this.screen     = new Screen();
		this.input      = new InputHandler(screen);
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
		propulsion.run(true);
		while(propulsion.isRunning()){
			if(pression.isPressed()){
				propulsion.stopMoving();
				graber.close();
			}
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
			screen.drawText("Calibration", 
						"Appuyer sur Entree", "pour commencer la",
						"calibration de l'ouverture", "et sur Entree quand les", "pinces sont ouvertes");
			input.waitAny();
			graber.startCalibrate(true);
			input.waitAny();
			graber.stopCalibrate(true);

		} else {
			this.graber.stopCalibrate(false);
		}
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
}
