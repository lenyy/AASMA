package tecnico.ulisboa.pt.AASMA_DBI;

public abstract class Goal{

	protected BDIBot bot;
	
	protected boolean finished;
	
	protected boolean failed;

	public Goal(BDIBot bot) {
		this.bot = bot;
		this.finished = false;
		this.failed = false;
	}

	void setFinished(boolean finished) {
		this.finished = finished;
	}
	
	void setFailed(boolean failed) {
		this.failed = failed;
	}
	
	abstract void perform();

	abstract boolean hasFailed();

	abstract boolean hasFinished();


	
}
