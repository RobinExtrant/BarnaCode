package pddl;

import java.util.ArrayList;
import java.util.List;

public class testServer implements ServerListener {
	private ArrayList<Item> listItem;
	
	public testServer() {
		this.listItem = new ArrayList<Item>();
	}
	
	@Override
	public void receiveRawPoints(List<Item> lastPointsReceived) {
		this.listItem.addAll(lastPointsReceived);
	}

	@Override
	public void displayList() {
		for (int i = 0; i < listItem.size(); i++) {
			System.out.println("Palet " + i + " : x = " + listItem.get(i).getX() + " y = " + listItem.get(i).getY());
		}
	}

}
