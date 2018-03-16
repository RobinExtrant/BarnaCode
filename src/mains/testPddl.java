package mains;

import java.io.File;

import pddl.Server;
import pddl.pddlLoader;
import pddl.testServer;

public class testPddl {
	public static void main(String[] args) {
		File domain = new File("/home/vincent/BarnaCode/barnaplan/domain.pddl");
		File problem = new File("/home/vincent/BarnaCode/barnaplan/test9case.pddl");
		pddlLoader pddl = new pddlLoader();
		pddl.generatePlan(domain, problem);
		//pddl.display();
		testServer ts = new testServer();
		Server s = new Server(ts);
		s.start();
		ts.displayList();
	}
}
