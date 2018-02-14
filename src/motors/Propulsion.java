package motors;
import java.util.Date;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.MoveListener;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.navigation.MoveProvider;
import utils.BarnaConstants;

public class Propulsion extends TimedMotor implements MoveListener{

	//chassis
	private Wheel     left           = null;
	private Wheel     right          = null;
	private Chassis   chassis        = null;
	private MovePilot pilot          = null;
	//Constants
	private float     orientation    = BarnaConstants.NORTH;
	private float     DIAMETER       = BarnaConstants.WHEEL_DIAMETER;
	private int       HALF_TURN      = BarnaConstants.HALF_CIRCLE;
	private int       MID_TURN       = BarnaConstants.QUART_CIRCLE;
	//state
	private long      lastAskRunning = -1;
	private int       time           = -1;
	private float     lastTurnedAngle= 0;
	private long      startTime      = 0;
	private long      stopTime       = 0;
	private long      lastRunTime    = 0;
	private boolean   running        = false;
	
	public Propulsion(){
		// Change this to match your robot
		left      = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(LocalEV3.get().getPort(BarnaConstants.LEFT_WHEEL)), DIAMETER).offset(-1*BarnaConstants.DISTANCE_TO_CENTER);
		right     = WheeledChassis.modelWheel(new EV3LargeRegulatedMotor(LocalEV3.get().getPort(BarnaConstants.RIGHT_WHEEL)), DIAMETER).offset(BarnaConstants.DISTANCE_TO_CENTER);
		chassis   = new WheeledChassis(new Wheel[]{left, right},  WheeledChassis.TYPE_DIFFERENTIAL);
		pilot     = new MovePilot(chassis);
		pilot.addMoveListener(this);
		pilot.setLinearAcceleration(BarnaConstants.LINEAR_ACCELERATION);
		pilot.setAngularSpeed(BarnaConstants.MAX_ROTATION_SPEED);
	}

	@Override
	public boolean isStall() {
		return chassis.isStalled();
	}

	@Override
	public void stopMoving() {
		pilot.stop();
	}

	@Override
	public void run(boolean forward) {
		if(forward){
			pilot.forward();
		}else{
			pilot.backward();
		}
	}
	
	/**
	 * Effectue un demi tour
	 * @param left vrai si à gauche
	 */
	public void volteFace(boolean left){
		rotate(HALF_TURN, left, false);
	}
	
	/**
	 * Effectue un demi tour
	 * @param left vrai si à gauche
	 * @param speed vitesse de rotation
	 */
	public void volteFace(boolean left, double speed){
		pilot.setAngularSpeed(speed);
		volteFace(left);
	}
	
	/**
	 * Effectue un quart de tour
	 * @param left vrai si à gauche
	 * @param speed vitesse de rotation
	 */
	public void halfTurn(boolean left, double speed){
		pilot.setAngularSpeed(speed);
		halfTurn(left);
	}

	/**
	 * Effectue un quart de tour
	 * @param left vrai si à gauche
	 */
	public void halfTurn(boolean left){
		rotate(MID_TURN, left, true);
	}

	/**
	 * Effectue une rotation custom
	 * @param i degrés à tourner
	 * @param left vrai si à gauche
	 * @param speed vitesse de rotation
	 */
	public void rotate(int i, boolean left, double speed) {
		pilot.setAngularSpeed(speed);
		rotate(i, left, true);
	}

	/**
	 * Effectue une rotation à gauche
	 * @param i defrés à tourner
	 * @param left vrai si à gauche
	 * @param correction vrai si on doit appliquer une correction de 2%
	 */
	public void rotate(float i, boolean left, boolean correction) {
		if(correction)
			i = i - (i * BarnaConstants.PR_ANGLE_CORRECTION);
		if(left){
			pilot.rotate(i*-1, true);
		}else{
			pilot.rotate(i, true);	
		}
	}

	/**
	 * oriente le robot au nord
	 */
	public void orientateNorth() {
		double orientate = getRotateToNorth();
		if(orientate < 0)
			pilot.rotate(orientate, false);
		else
			pilot.rotate(orientate, true);
	}
	
	/**
	 * oriente le robot à l'ouest
	 */
	public void orientateWest(){
		double orientate = getRotateToNorth()+BarnaConstants.WEST;
		if(orientate < 0)
			pilot.rotate(orientate, false);
		else
			pilot.rotate(orientate, true);
	}

	public void orientateEast() {
		double orientate = getRotateToNorth()+BarnaConstants.EAST;
		if(orientate < 0)
			pilot.rotate(orientate, false);
		else
			pilot.rotate(orientate, true);
	}

	public void orientateSouth(boolean left) {
		double orientate = left ? getRotateToNorth()-BarnaConstants.SOUTH :
		                          getRotateToNorth()+BarnaConstants.SOUTH;
		if(orientate < 0)
			pilot.rotate(orientate, false);
		else
			pilot.rotate(orientate, true);
	}
	
	/**
	 * 
	 * @return le nombre de degrés pour aller au nord
	 */
	public double getRotateToNorth(){
		if(orientation<-BarnaConstants.HALF_CIRCLE){
			return Math.abs(orientation)-BarnaConstants.FULL_CIRCLE;
		}else if(orientation > BarnaConstants.HALF_CIRCLE){
			return BarnaConstants.FULL_CIRCLE-orientation;
		}else{
			return orientation*-1;
		}
	}
	

	@Override
	public void runFor(int millis, boolean forward) {
		lastAskRunning = new Date().getTime();
		time           = millis;
		run(forward);
	}

	@Override
	public boolean isTimeRunElapsed() {
		boolean timeElapsed = false;
		if(lastAskRunning > -1){
			long attendeeDate = lastAskRunning + time;
			Date d = new Date();
			timeElapsed =  d.getTime() > attendeeDate;
		}
		return timeElapsed;
	}

	/**
	 * @return vrai si le robot est en train de tourner
	 */
	public boolean isRunning() {
		return running;
	}

	@Override
	public void moveStarted(Move event, MoveProvider mp) {
		running   = true;
		startTime = event.getTimeStamp();
	}

	@Override
	public void moveStopped(Move event, MoveProvider mp) {
		running        = false;
		stopTime       = event.getTimeStamp();
		lastRunTime    = stopTime - startTime;
		lastAskRunning = -1;
		pilot.setAngularSpeed(BarnaConstants.MAX_ROTATION_SPEED);

		orientation = orientation + (event.getAngleTurned()+
				(event.getAngleTurned()*BarnaConstants.PR_ANGLE_CORRECTION));
		orientation = orientation % BarnaConstants.FULL_CIRCLE;

		lastTurnedAngle = event.getAngleTurned();
	}
	
	/**
	 * @param percentage le nombre de %
	 * @return vrai si le robot à parcouru pecentage pourcent de son dernier 
	 * voyage
	 */
	public boolean hasRunXPercentOfLastRun(int percentage){
		return System.currentTimeMillis() > startTime + 
				((lastRunTime*(percentage/2))/100);
	}
	
	/**
	 * @return l'orientation par rapport au nord
	 */
	public float getOrientation(){
		return orientation;
	}

	/**
	 * Reinitialise les degrés au nord
	 * @param i
	 */
	public void seDegreeToNorth(int i) {
		orientation = i;
	}
	
	/**
	 * renvoie le dernier angle tourné
	 */
	public double getLastTurnedAngle(){
		return lastTurnedAngle;
	}

}
