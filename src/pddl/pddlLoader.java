package pddl;
import java.io.File;
import java.io.IOException;
import java.util.List;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.hsp.HSP;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.SequentialPlan;

public class pddlLoader {
	private ProblemFactory pf;
	private HSP hsp;
	private SequentialPlan sp;
	private CodedProblem cp;
	
	public pddlLoader() {
		this.pf = new ProblemFactory();
		this.hsp = new HSP();
		this.hsp.setSaveState(false);
	}
	
	public void generatePlan(File domain, File problem) {
		try {
			pf.parse(domain, problem);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		this.cp = pf.encode();
		this.sp = hsp.search(cp);
	}
	
	public void display() {
		System.out.println(cp.toString(sp));
	}
}