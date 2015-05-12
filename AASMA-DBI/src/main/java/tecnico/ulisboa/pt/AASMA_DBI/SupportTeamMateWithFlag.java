package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class SupportTeamMateWithFlag extends Goal {

	protected boolean runningToPlayer = false;

	public SupportTeamMateWithFlag(DBIBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		bot.updateFight();
		
		Player enemy = null;
		
		if(bot.getFriendWithFlag() != null)
			enemy = bot.getPlayers().getPlayer(bot.getFriendWithFlag());
		
		int decentDistance = Math.round(bot.getRandom().nextFloat() * 250) + 200;
		if (enemy != null && bot.getInfo().getDistance(enemy) > decentDistance
				&& !runningToPlayer) {

			bot.goTo(enemy);
			runningToPlayer = true;
		}
		
		bot.updateFight();

	}


	@Override
	public boolean hasFailed() {
		runningToPlayer = false;
		return this.failed;
	}

	@Override
	public boolean hasFinished() {
		runningToPlayer = false;
		return this.finished;
	}

	

}
