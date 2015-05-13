package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;

public class GoToOurBase extends Goal {

	protected GoToOurBase(BDIBot bot) {
		super(bot);
	}

	@Override
	void perform() {
		if (bot.getEnemyFlag() != null) {
			UnrealId holderId = bot.getEnemyFlag().getHolder();

			if (bot.getInfo().getId().equals(holderId)) {
				bot.goTo(bot.getOurFlagBase());
				bot.getLog().info("goTo ourFlagBase");
				if(bot.getInfo().isAtLocation(bot.getOurFlagBase()) 
						&& bot.getEnemyFlag().getState().equalsIgnoreCase("home"))	
					setFinished(true);

			}
			else
			{
				bot.getLog().info("I dont have the Enemy Flag, Dropping Goal");
				setFailed(true);
			}
		}
		else
		{
			bot.getLog().info("Cannot get Info about Enemy Flag, Dropping Goal");
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

	@Override
	public String toString() {
		return "GOAL ------> Go To Our Base";
	}
	
}
