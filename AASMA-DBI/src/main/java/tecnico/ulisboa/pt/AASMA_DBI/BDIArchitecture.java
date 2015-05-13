package tecnico.ulisboa.pt.AASMA_DBI;

import java.util.HashMap;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;

public class BDIArchitecture {
	protected final HashMap<String,Goal> goals = new HashMap<String,Goal>();
	protected Goal currentGoal = null;
	protected UT2004Bot bot;
	protected BDIBot bdiBot;
	protected String flagInfoTeam;
	protected String flagInfoEnemy;
	protected boolean sawEnemy;

	public BDIArchitecture(UT2004Bot bot, BDIBot bdiBot) {
		this.bot = bot;
		this.bdiBot = bdiBot;
		this.flagInfoEnemy = "home";
		this.flagInfoTeam = "home";
	}


	public boolean addGoal(Goal goal, String description) {
		if (!goals.containsKey(description)) {
			goals.put(description, goal);
			return true;
		} else {
			return false;
		}
	}


	public String Options() 
	{
		String result = "";

		if(bdiBot.getEnemyFlag() != null) {
			UnrealId holderId = bdiBot.getEnemyFlag().getHolder();

			if(bdiBot.getPlayers().getNearestVisibleEnemy() != null 
					&& bdiBot.getInfo().getCurrentAmmo() != 0 
					&& bdiBot.getInfo().getCurrentSecondaryAmmo() != 0) 
			{
				result = "SHOOT";
			}
			else
			{
				if (bdiBot.getInfo().getId().equals(holderId))
				{
					result = "GO HOME";
				}			
				else
				{
					if(!bdiBot.getCTF().isOurFlagHome()){
						result = "GET OUR FLAG";				
					}
					else {
						if(bdiBot.getCTF().isEnemyFlagHeld()) {
							result = "SUPPORT TEAM MATE WITH FLAG";	
						}
						else {



							if(bdiBot.getCTF().isEnemyFlagHome())
							{
								result = "GET ENEMY FLAG";
							}
							else
							{
								if(bdiBot.getInfo().getHealth() < 20 && bdiBot.getItems().getAllItems(Category.HEALTH).size() > 0 ) 
								{
									result = "GET HEALTH";

								}
								else
								{
									result = "GET ITEMS";

								}
							}
						}
					}
				}
			}
		}
		else
		{
			result = "GET ENEMY FLAG";
		}

		bdiBot.getLog().info(result);

		return result;
	}



	public Goal intention(String Option) 
	{
		Goal result = null;

		if (goals.containsKey(Option)) 
		{
			result = goals.get(Option);
		}

		return result;
	}


	public void BDIPlanner() {
		if(currentGoal != null) {
			if(currentGoal.hasFailed()) {
				bdiBot.getLog().info("FAILEDDDDDDDDD");
				currentGoal.setFailed(false);
				currentGoal = null;
			}
			else {
				if(currentGoal.hasFinished()) {
					bdiBot.getLog().info("FINISHEDDDDDD");
					currentGoal.setFinished(false);
					currentGoal = null;
				}
			}
		}
		String option = "";
		if(currentGoal == null) {
			option = Options();
			currentGoal = intention(option);
			currentGoal.perform();
		}
		else 
		{

			currentGoal.perform();
		}

		bdiBot.getLog().info(currentGoal.toString());

		if(shouldReviewIntentions())
		{
			bdiBot.getLog().info("REVIEWING INTENTIONSS");
			option = Options();
			currentGoal = intention(option);
		}

	}

	public Goal getCurrentGoal() {
		return currentGoal;
	}



	public boolean shouldReviewIntentions() {
		boolean result = false;

		if(bdiBot.getPlayers().getNearestVisibleEnemy() != null) {
			result = true;
		}
		else 
		{
			if(!this.flagInfoTeam.equalsIgnoreCase(bdiBot.getOurFlag().getState()))
			{
				result = true;
				this.flagInfoTeam = bdiBot.getOurFlag().getState();
			}
			else
			{
				if(!this.flagInfoEnemy.equalsIgnoreCase(bdiBot.getEnemyFlag().getState())) {
					result = true;
					this.flagInfoEnemy = bdiBot.getEnemyFlag().getState();
				}
			}
		}


		return result;
	}


}
