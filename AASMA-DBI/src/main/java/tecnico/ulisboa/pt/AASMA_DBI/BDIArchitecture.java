package tecnico.ulisboa.pt.AASMA_DBI;

import java.util.LinkedList;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;

public class BDIArchitecture {
	protected final LinkedList<IGoal> goals = new LinkedList<IGoal>();
	protected IGoal currentGoal = null;
	protected DBIBot bot;

	public BDIArchitecture(DBIBot bot) {
		this.bot = bot;
	}


	public String Options() 
	{
		String result = "";

		if (bot.getEnemyFlag().getHolder().equals(bot.getInfo().getId()))
		{
			result = "GO TO BASE";
		}
		else
		{
			if(bot.getEnemyFlag() != null && bot.getOurFlag().getHolder() == null && bot.getEnemyFlag().getHolder() == null)
			{
				result = "GET ENEMY FLAG";
			}
			else
			{
				if(bot.getInfo().getHealth() < 20 && bot.getItems().getAllItems(Category.HEALTH).size() > 0 ) 
				{
					result = "GET HEALTH";
				}
				else
				{
					result = "GET ITEMS";
				}
			}
		}


		return result;
	}
	
	public Goal Intention(String option) {
		Goal result = null;
		
		if(option.equals("GET ENEMY FLAG"))
		{
			result = new GetEnemyFlag(this.bot);
		}
		if(option.equals("GET HEALTH")) 
		{
			result = new GetHealth(this.bot);	
		}
		if(option.equals("GET ITEMS"))
		{
			result = new GetItems(this.bot);
		}
		
		return result;
	}

	public IGoal BDIPlanner() {
		
		

		return currentGoal;
	}

	public IGoal getCurrentGoal() {
		return currentGoal;
	}

	public void abandonAllGoals() {
		for (IGoal goal : goals) {
			goal.abandon();
		}
	}
}
