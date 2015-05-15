package tecnico.ulisboa.pt.AASMA_Hibrido;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class SupportTeamMateWithFlag extends Goal {

	
	public SupportTeamMateWithFlag(HibridBot bot) {
		super(bot);
	}

	@Override
	public void perform() {
		
		Player friend = null;
	
		if(bot.getCTF().isEnemyFlagHeld())
			friend = bot.getPlayers().getPlayer(bot.getEnemyFlag().getHolder());
		else 
		{
			setFinished(true);
			return;
		}
		if (friend != null) {

			bot.getLog().info("Getting Close to Friend " + friend.getName());	
			bot.goTo(friend.getLocation());
		}
		else {
			
			bot.getLog().info("Suporting Team Mate, Cannot see Team Mate. Going to base to wait for is arrival");
			bot.goTo(bot.getOurFlagBase());
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
		return "GOAL ------> Support Team MAte With Flag";
	}
	

}
