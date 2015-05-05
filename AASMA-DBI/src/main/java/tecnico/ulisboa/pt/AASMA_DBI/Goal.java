package tecnico.ulisboa.pt.AASMA_DBI;

public abstract class Goal implements IGoal {

	protected DBIBot bot;
	
	protected boolean finished;
	
	protected boolean failed;

	public Goal(DBIBot bot) {
		this.bot = bot;
		this.finished = false;
		this.failed = false;
	}

	/**
	 * Reverse ordering, greater numbers first, lesser later
	 */
	@Override
	public int compareTo(IGoal arg0) {
		if (getPriority() == ((IGoal) arg0).getPriority())
			return 0;
		else if ((getPriority()) > ((IGoal) arg0).getPriority())
			return -1;
		else
			return 1;
	}
}
