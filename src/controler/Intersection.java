package controler;

public class Intersection {

	private Controler.RelativeOrientation relativeOrientation;
	
	private int color;
	
	private String letter;
	
	public Intersection(String letter, int color, Controler.RelativeOrientation relativeOrientation) {
		this.letter = letter;
		this.color = color;
		this.relativeOrientation = relativeOrientation;
	}

	public Controler.RelativeOrientation getRelativeOrientation() {
		return relativeOrientation;
	}

	public int getColor() {
		return color;
	}

	public String getLetter() {
		return letter;
	}
}
