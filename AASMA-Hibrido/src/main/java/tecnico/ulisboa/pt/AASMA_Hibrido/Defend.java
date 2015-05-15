package tecnico.ulisboa.pt.AASMA_Hibrido;

public class Defend extends Goal {


	public Defend(HibridBot bot) {
		super(bot);

	}

	@Override
	void perform() {
		if(bot.getOurFlag() != null) {
			if(bot.getCTF().isOurFlagHome()) {
				if(!bot.getInfo().isAtLocation(bot.getOurFlag().getLocation(), 4.0)){
					if(bot.getOurFlag().getLocation() != null)
						bot.goTo(bot.getOurFlag().getLocation());
					else
						bot.goTo(bot.getOurFlagBase().getLocation());
					bot.getLog().info("DEFENDDDDDDDD -------------------");
					bot.getLog().info("GOING TO FLAG");
				}
				else
				{
					setFinished(true);
				}
			}
			else{
				setFailed(true);
			}
		}
		else
		{
			setFailed(true);
		}
	}


	@Override
	boolean hasFailed() {

		return this.failed;
	}

	@Override
	boolean hasFinished() {

		return this.finished;
	}

}
