package mains;

import java.io.File;

import fr.uga.pddl4j.util.SequentialPlan;
import pddl.Server;
import pddl.SshConnector;
import pddl.pddlLoader;
import pddl.testServer;

public class testPddl {
	public static void main(String[] args) {
		File domain = new File("barnaplan/domain.pddl");
		File problem = new File("barnaplan/test9case.pddl");
		pddlLoader pddl = new pddlLoader();
		pddl.generatePlan(domain, problem);
		SequentialPlan sp = pddl.getPlan();
		pddl.display();
		/*System.out.println(sp.actions().get(29).getName());
		System.out.println(sp.actions().get(29).getValueOfParameter(0));
		System.out.println(sp.actions().get(29).getValueOfParameter(1));
		testServer ts = new testServer();
		Server s = new Server(ts);
		s.start();*/
		SshConnector ssh = new SshConnector();
		ssh.copyFile();
		System.out.println("File copied");
	}
}
