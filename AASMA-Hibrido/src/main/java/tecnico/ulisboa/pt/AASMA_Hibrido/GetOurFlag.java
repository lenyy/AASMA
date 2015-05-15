package tecnico.ulisboa.pt.AASMA_Hibrido;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class GetOurFlag extends Goal {


	protected Player enemy = null;
	Location flagLocation;

	public GetOurFlag(HibridBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		UnrealId holderId = null;

		if (bot.getOurFlag() != null) {

			holderId = bot.getOurFlag().getHolder();
					
			flagLocation = bot.getOurFlag().getLocation();
			
			if(bot.getOurFlagBase().getLocation().equals(flagLocation, 2))
				flagLocation = null;

			if (flagLocation != null) {
				
				enemy = bot.getPlayers().getPlayer(holderId);

				bot.getLog().info(
						String.format("FlagLocation: %s %s %.2f", flagLocation,
								bot.getInfo().getLocation(),
								bot.getInfo().getDistance(flagLocation)));
				if (enemy != null) {
					bot.getLog().info("Getting Closer to Enemy carring my flag");
					bot.goTo(enemy);
					if(bot.getInfo().atLocation(enemy, 3.0)) 
						setFinished(true);
					
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
		return "GOAL ------> Get Our Flag";
	}
}
