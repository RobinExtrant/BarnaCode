package pddl;

public class Item {
	private int x;
	private int y;

	public Item(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString(){
		return "Item = [" + this.x +"," + this.y + "]";
	}
	
	public void update(final int x, final int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
}
