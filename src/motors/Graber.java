package motors;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import utils.BarnaConstants;

public class Graber extends TimedMotor {
	
	private Port port;
	private EV3LargeRegulatedMotor graber;
	private long startCalibrateOpen;
	private long movementTimeOpen;
	private boolean isOpen;
	private boolean isClose;
	private boolean isRuning;
	private long attendeeDate;

	public Graber(){
		this.port   = LocalEV3.get().getPort(BarnaConstants.PINCH);
		this.graber = new EV3LargeRegulatedMotor(port);
	}
	
	/**
	 * Run calibration
	 * @param open
	 */
	public void startCalibrate(boolean open){
		graber.setSpeed(BarnaConstants.GRAB_CALIBRATE_SPEED);
		if(open){
			this.graber.forward();
			this.startCalibrateOpen = System.currentTimeMillis();
		} else{
			graber.backward();
		}
	}
	
	/**
	 * Stop calibration
	 * @param open
	 */
	public void stopCalibrate(boolean open){
		stopMoving();
		Long stoping = System.currentTimeMillis();
		if(open){
			movementTimeOpen = stoping - startCalibrateOpen;
			isOpen  = true;
			isClose = false;
		}else{
			isOpen  = false;
			isClose = true;
		}
	}
	
	/**
	 * ferme la pince en fonction de la calibration
	 * Le mouvement sera lancé uniquement si aucun mouvement n'est en cours
	 */
	public void close(){
		if(!isRuning){
			graber.setSpeed(BarnaConstants.GRAB_RUNNING_SPEED);
			graber.backward();
			isRuning = true;
			updateAttendeeDate();
		}
	}
	
	/**
	 * ouvre la pince en fonction de la calibration
	 * Le mouvement sera lancé uniquement si aucun mouvement n'est en cours
	 */
	public void open(){
		if(!isRuning){
			graber.setSpeed(BarnaConstants.GRAB_RUNNING_SPEED);
			graber.forward();
			isRuning = true;
			updateAttendeeDate();
		}
	}
	
	@Override
	public void stopMoving() {
		this.attendeeDate = -1;
		graber.stop();
		isRuning = false;
		if(isClose){
			isOpen  = true;
			isClose = false;
		}else{
			isOpen  = false;
			isClose = true;
		}
	}
	
	@Override
	public boolean isStall() {
		return graber.isStalled();
	}

	@Override
	public boolean isTimeRunElapsed() {
		if(this.attendeeDate != -1){
			return System.currentTimeMillis() > this.attendeeDate;
		}
		return false;
	}
	
	private void updateAttendeeDate() {
		long wait = 0;
		wait = movementTimeOpen / (BarnaConstants.GRAB_RUNNING_SPEED / BarnaConstants.GRAB_CALIBRATE_SPEED);

		this.attendeeDate = System.currentTimeMillis() + wait;
	}

	/**
	 * 
	 * @return vrai si la pince est fermée
	 */
	public boolean isClose() {
		return isClose;
	}

	/**
	 * 
	 * @return vrai si la pince est ouverte
	 */
	public boolean isOpen() {
		return isOpen;
	}
	
	/**
	 * 
	 * @return vrai si la pince est en train de bouger
	 */
	public boolean isRunning(){
		return isRuning;
	}

	@Override
	public void run(boolean forward) {
		if(forward){
			open();
		}else{
			close();
		}
	}
	
	public long getOpenTime() {
		return movementTimeOpen;
	}
	
	public void setOpenTime(long mvt){
		this.movementTimeOpen = mvt;
	}
}
