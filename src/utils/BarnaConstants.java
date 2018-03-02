package utils;
public class BarnaConstants {
	//PORTS
	public static final String LEFT_WHEEL = "B";
	public static final String RIGHT_WHEEL = "C";
	public static final String PINCH = "A";
	public static final String TOUCH_SENSOR = "S2";
	public static final String IR_SENSOR = "S4";
	public static final String COLOR_SENSOR = "S1";
	
	//Calibrage
	public static final int GRAB_CALIBRATE_SPEED = 200;
	public static final int GRAB_RUNNING_SPEED = 2000;
	
	//Propulsion
	public static final int HALF_CIRCLE = 180;
	public static final int QUART_CIRCLE = 90;
	public static final float FULL_CIRCLE = 360;
	public static final int NORTH = 0;
	public static final int WEST = -QUART_CIRCLE;
	public static final int EAST = QUART_CIRCLE;
	public static final int SOUTH = HALF_CIRCLE;
	public static final float WHEEL_DIAMETER = 56;
	public static final float DISTANCE_TO_CENTER = 62.525f;
	public static final float LINEAR_ACCELERATION = 0.2f;
	public static final int MAX_ROTATION_SPEED = 70;
	public static final int   ANGLE_CORRECTION = 2;
	public static final float PR_ANGLE_CORRECTION = ANGLE_CORRECTION/100f;
	
	//Vision
	public static final float MAX_VISION_RANGE = 0.40f;
	public static final float MIN_VISION_RANGE = 0.10f;

}
