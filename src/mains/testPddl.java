package mains;

import java.io.File;

import pddl.pddlLoader;

public class testPddl {
	public static void main(String[] args) {
		File domain = new File("/home/vincent/BarnaCode/barnaplan/domain.pddl");
		File problem = new File("/home/vincent/BarnaCode/barnaplan/test9case.pddl");
		pddlLoader pddl = new pddlLoader();
		pddl.generatePlan(domain, problem);
		pddl.display();
	}
}
