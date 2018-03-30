package pddl;
import java.io.File;
import java.io.IOException;
import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.parser.Domain;
import fr.uga.pddl4j.parser.NamedTypedList;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.parser.Symbol;
import fr.uga.pddl4j.parser.TypedSymbol;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.hsp.HSP;
import fr.uga.pddl4j.util.SequentialPlan;

public class pddlLoader {
	private ProblemFactory pf;
	private HSP hsp;
	private SequentialPlan sp;
	private CodedProblem cp;
	private Domain domain;
	
	public pddlLoader() {
		this.pf = new ProblemFactory();
		this.hsp = new HSP();
		this.hsp.setSaveState(false);
		//this.domain = createDomain();
	}
	
	/*private Domain createDomain() {
		Domain d = new Domain(new Symbol(Symbol.Kind.DOMAIN, "BARNA"));
		d.addRequirement(RequireKey.STRIPS);
		d.addRequirement(RequireKey.TYPING);
		d.addType(new TypedSymbol(new Symbol(Symbol.Kind.TYPE, "case")));
		d.addType(new TypedSymbol(new Symbol(Symbol.Kind.TYPE, "palet")));
		d.addType(new TypedSymbol(new Symbol(Symbol.Kind.TYPE, "but")));
		NamedTypedList busy = new NamedTypedList(new Symbol(Symbol.Kind.PREDICATE, "busy"));
		busy.add(new TypedSymbol())
		d.addPredicate(busy);
	}*/
	
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
	
	public SequentialPlan getPlan() {
		return this.sp;
	}
}