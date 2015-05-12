package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class CloseInOnEnemy extends Goal {

	protected boolean runningToPlayer = false;

	public CloseInOnEnemy(DBIBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		bot.updateFight();

		Player enemy = bot.getEnemy();
		int decentDistance = Math.round(bot.getRandom().nextFloat() * 250) + 200;
		if (enemy != null && bot.getInfo().getDistance(enemy) > decentDistance
				&& !runningToPlayer) {

			bot.goTo(enemy);
			runningToPlayer = true;
		}

	}


	@Override
	public boolean hasFailed() {
		return false;
	}

	@Override
	public boolean hasFinished() {
		return this.finished;
	}

	@Override
	public void abandon() {
		bot.getNavigation().stopNavigation();
		runningToPlayer = false;
		return;
	}

}