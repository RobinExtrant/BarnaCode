package pddl;

import java.util.List;

public interface ServerListener {
	public void receiveRawPoints(List<Item> lastPointsReceived);
	public void displayList();
}
