import java.util.ArrayList;

class Game {

	static void fullTournament() throws Exception {
		ArrayList<IAgent> al = new ArrayList<IAgent>();
		al.add(new PrescientMoron());
		al.add(new Mixed());
		al.add(new DaiThy());
		al.add(new Blitz());
		al.add(new SittingDuck());
		al.add(new AggressivePack());
		Controller.doTournament(al);
	}

	public static void main(String[] args) throws Exception {
		// Controller.doBattle(new DaiThy(), new Mixed());
		//Controller.doBattle(new Mixed(), new AggressivePack());
		// Controller.doBattle(new DaiThy(), new AggressivePack());
		//Controller.doBattle(new Blitz(), new Mixed());
		//Controller.doBattle(new DaiThy(), new SittingDuck());
		//Controller.doBattle(new Mixed(), new SittingDuck());
		//Controller.doBattle(new DaiThy(), new Blitz());
		//Controller.doBattle(new PrescientMoron(), new SittingDuck());
		//Controller.doBattle(new PrescientMoron(), new DaiThy());
		//Controller.doBattle(new DaiThy(), new PrescientMoron());
		fullTournament();
	}
}
