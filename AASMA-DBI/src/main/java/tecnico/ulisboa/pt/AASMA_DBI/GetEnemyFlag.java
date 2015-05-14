package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;

public class GetEnemyFlag extends Goal {


	public GetEnemyFlag(BDIBot bot) {
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
					bot.getLog().info("Failed cannot see the flag");
					setFailed(true);
					return;
				} else {
					bot.getLog().info("goTo enemyEnemyFlag");
					}

				bot.goTo(target);
				if(bot.getInfo().getId().equals(bot.getEnemyFlag().getHolder())) {
					setFinished(true);
				}
			}

		} else {
			bot.getLog().info("goTo enemyFlagBase null");
			bot.goTo(bot.getEnemyFlagBase());
		}
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
	public String toString() {
		return "GOAL ------> Get Enemy Flag";
	}
}
