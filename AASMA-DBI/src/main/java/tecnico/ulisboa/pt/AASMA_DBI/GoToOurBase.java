package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;

public class GoToOurBase extends Goal {

	protected GoToOurBase(DBIBot bot) {
		super(bot);
	}

	@Override
	void perform() {
		if (bot.getEnemyFlag() != null) {
			UnrealId holderId = bot.getEnemyFlag().getHolder();

			if (bot.getInfo().getId().equals(holderId)) {
				bot.goTo(bot.getOurFlagBase());
				bot.getLog().info("goTo ourFlagBase");
				setFinished(true);
				
			}
		}
		bot.updateFight();
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
