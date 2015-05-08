package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

public class GetEnemyFlag extends Goal {


	public GetEnemyFlag(DBIBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		if (bot.getEnemyFlag() != null) {

			if (bot.getCTF().isEnemyFlagHome()) {
				bot.getLog().info("goTo enemyFlagBase, flag is at enemy base");
				bot.goTo(bot.getEnemyFlagBase());
			} else {
				Location target = bot.getEnemyFlag().getLocation();
				if (target == null) {
					target = bot.getEnemyFlagBase().getLocation();
					bot.getLog().info("goTo enemyFlagBase");
				} else {
					bot.getLog().info("goTo enemyEnemyFlag");
					setFinished(true);
				}

				bot.goTo(target);
			}

		} else {
			bot.getLog().info("goTo enemyFlagBase null");
			bot.goTo(bot.getEnemyFlagBase());
		}
		bot.updateFight();
	}


	@Override
	public boolean hasFailed() {
		return this.failed;
	}

	@Override
	public boolean hasFinished() {
		return this.finished;
	}

	@Override
	public void abandon() {
	}
}
