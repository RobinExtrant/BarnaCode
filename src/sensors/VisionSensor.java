package sensors;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import utils.BarnaConstants;

public class VisionSensor {
	private EV3UltrasonicSensor sonar   = null;
	private Port                port    = null;

	public VisionSensor(){
		port  = LocalEV3.get().getPort(BarnaConstants.IR_SENSOR);
		sonar = new EV3UltrasonicSensor(port);
	}

	/**
	 * 
	 * @return la distance lue par le capteur ultrason
	 */
	public float[] getRaw() {
		float[] sample = new float[1];
		sonar.fetchSample(sample, 0);
		return sample;
	}
}
