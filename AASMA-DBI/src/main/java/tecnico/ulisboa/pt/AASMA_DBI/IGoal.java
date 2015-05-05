package tecnico.ulisboa.pt.AASMA_DBI;

public interface IGoal extends Comparable<IGoal> {
	
	void perform();

	double getPriority();

	boolean hasFailed();

	boolean hasFinished();

	void abandon();
	
}
