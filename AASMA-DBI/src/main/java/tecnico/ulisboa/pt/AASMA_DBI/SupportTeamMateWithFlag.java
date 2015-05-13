package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class SupportTeamMateWithFlag extends Goal {

	
	public SupportTeamMateWithFlag(DBIBot bot) {
		super(bot);
	}

	@Override
	public void perform() {

		bot.updateFight();
		
		Player friend = null;
		
		if (!bot.getOurFlag().getState().equalsIgnoreCase("home")){
			setFailed(true);
			return;
		}
		
		if(bot.getCTF().isEnemyFlagHome()) {
			setFailed(true);
			return;
		}
		if(bot.getFriendWithFlag() != null)
			friend = bot.getPlayers().getPlayer(bot.getFriendWithFlag());
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
			
			bot.getLog().info("Suporting Team Mate, Going to base");
			bot.goTo(bot.getOurFlagBase());
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
