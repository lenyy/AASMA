package tecnico.ulisboa.pt.AASMA_Hibrido;

public abstract class Goal{

	protected HibridBot bot;
	
	protected boolean finished;
	
	protected boolean failed;

	public Goal(HibridBot bot) {
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
