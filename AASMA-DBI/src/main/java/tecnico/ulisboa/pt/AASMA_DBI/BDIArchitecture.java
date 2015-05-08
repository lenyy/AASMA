package tecnico.ulisboa.pt.AASMA_DBI;

import java.util.HashMap;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;

public class BDIArchitecture {
	protected final HashMap<String,Goal> goals = new HashMap<String,Goal>();
	protected Goal currentGoal = null;
	protected UT2004Bot bot;
	protected DBIBot dbiBot;

	public BDIArchitecture(UT2004Bot bot, DBIBot dbiBot) {
		this.bot = bot;
		this.dbiBot = dbiBot;
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

		if(dbiBot.getEnemyFlag() != null) {
			UnrealId holderId = dbiBot.getEnemyFlag().getHolder();

			if (dbiBot.getInfo().getId().equals(holderId))
			{
				result = "GO HOME";
				dbiBot.getLog().info("GO HOME");
			}
			else
			{
				if( !dbiBot.getOurFlag().getState().equalsIgnoreCase("home")) {
					result = "GET OUR FLAG";
					dbiBot.getLog().info("GET OUR FLAG");
				}
				else {
					if(dbiBot.getCTF().isEnemyFlagHome() && dbiBot.getCTF().isOurFlagHome())
					{
						result = "GET ENEMY FLAG";
						dbiBot.getLog().info("GET ENEMY FLAG");
					}
					else
					{
						if(dbiBot.getInfo().getHealth() < 20 && dbiBot.getItems().getAllItems(Category.HEALTH).size() > 0 ) 
						{
							result = "GET HEALTH";
							dbiBot.getLog().info("GET HEALTH");
						}
						else
						{
							result = "GET ITEMS";
							dbiBot.getLog().info("GET ITEMS");
						}
					}
				}
			}
		}
		else
		{
			result = "GET ENEMY FLAG";
			dbiBot.getLog().info("GET ENEMY FLAG 2");
		}


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
				dbiBot.getLog().info("FAILEDDDDDDDDD");
				currentGoal.setFailed(false);
				currentGoal = null;
			}
			else {
				if(currentGoal.hasFinished()) {
					dbiBot.getLog().info("FINISHEDDDDDD");
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




	}

	public Goal getCurrentGoal() {
		return currentGoal;
	}


}
