package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class GetOurFlag extends Goal {


	protected Player enemy = null;
	Location flagLocation;

	public GetOurFlag(DBIBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		UnrealId holderId = null;

		if (bot.getOurFlag() != null) {

			holderId = bot.getOurFlag().getHolder();
					
			flagLocation = bot.getOurFlag().getLocation();

			if (flagLocation != null) {

				enemy = bot.getPlayers().getPlayer(holderId);

				bot.getLog().info(
						String.format("FlagLocation: %s %s %.2f", flagLocation,
								bot.getInfo().getLocation(),
								bot.getInfo().getDistance(flagLocation)));
				if (enemy != null) {
					bot.getLog().info("Getting Closer to Enemy carring my flag");
					bot.goTo(enemy);

					if (enemy.isVisible()) {
						bot.updateFight(enemy);
						return;
					}
				} 
				else 
				{
					bot.getLog().info("Getting Closer to Flag");
					bot.goTo(flagLocation);
					if (bot.getCTF().getOurFlag().getState().equalsIgnoreCase("home"))
					{
						setFinished(true);
					}
				}
			} else {
				bot.getLog().info("Cannot See Enemy Player with my flag, going to Enemy Base");
				bot.goTo(bot.getEnemyFlagBase().getLocation());
			}
		}
		else
		{
			setFailed(true);
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


}
