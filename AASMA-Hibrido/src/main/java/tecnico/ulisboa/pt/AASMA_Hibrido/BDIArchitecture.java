package tecnico.ulisboa.pt.AASMA_Hibrido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;

public class BDIArchitecture {
	protected final HashMap<String,Goal> goals;
	protected Goal currentGoal = null;
	protected HibridBot hBot;
	protected String flagInfoTeam;
	protected String flagInfoEnemy;
	protected String roll;
	protected Map<UnrealId,Location> teamMatesLocation;
	protected int maxPlayersAtacking;
	protected LogCategory log;
	protected int numTeammates;
	protected int maxPlayersDefending;
	protected boolean newRoll;
	protected boolean rollChanged;

	public BDIArchitecture(HibridBot hBot,int numTeammates) {
		this.hBot = hBot;
		this.flagInfoEnemy = "home";
		this.flagInfoTeam = "home";
		this.roll = "";
		this.teamMatesLocation = new HashMap<UnrealId,Location>();
		this.goals = new HashMap<String,Goal>();
		if(numTeammates == 1){
			this.maxPlayersDefending = 1;
			this.maxPlayersAtacking = 1;
		}
		else{
			this.maxPlayersAtacking = numTeammates/2 + 1; // always leave less players defending at the beginning
			this.maxPlayersDefending = numTeammates/2;
		}
		this.log = hBot.getLog();
		this.numTeammates = numTeammates;
		this.newRoll = false;
		this.rollChanged = false;

	}


	public boolean addGoal(Goal goal, String description) {
		if (!goals.containsKey(description)) {
			goals.put(description, goal);
			return true;
		} else {
			return false;
		}
	}

	public void addLocation(Location loc,UnrealId id){
		if(!teamMatesLocation.containsKey(id)) {
			teamMatesLocation.put(id,loc);
		}
	}


	public String Options() 
	{
		String result = "";

		if(hBot.getEnemyFlag() != null) {
			UnrealId holderId = hBot.getEnemyFlag().getHolder();

			if (hBot.getInfo().getId().equals(holderId) && roll.equals("attack"))
			{
				result = "GO HOME";
			}			
			else
			{
				if(!hBot.getCTF().isOurFlagHome() && roll.equals("defend")){
					result = "GET OUR FLAG";				
				}
				else {
					if(roll.equals("defend")) {
						result = "DEFEND";
					}
					else
					{
						if(hBot.getCTF().isEnemyFlagHeld() && roll.equals("attack")) {
							result = "SUPPORT TEAM MATE WITH FLAG";	
						}
						else 
						{
							if(hBot.getCTF().isEnemyFlagHome() && roll.equals("attack"))
							{
								result = "GET ENEMY FLAG";
							}
							else
							{
								if(hBot.getInfo().getHealth() < 20 && hBot.getItems().getAllItems(Category.HEALTH).size() > 0 ) 
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

		log.info(result);

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

		if(roll.equals("")) {
			createRoll();
			return;
		}

		if(currentGoal != null) {
			if(currentGoal.hasFailed()) {
				log.info("FAILEDDDDDDDDD");
				currentGoal.setFailed(false);
				currentGoal = null;
			}
			else {
				if(currentGoal.hasFinished()) {
					log.info("FINISHEDDDDDD");
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

		if(newRoll) {
			createRoll();
			log.info("ROLLLLLLLLLLLLLLLLLLLLLLL CHANGINGGGGGGGG");
		}

		log.info(currentGoal.toString());
		log.info(roll);

		if(shouldReviewIntentions())
		{
			log.info("REVIEWING INTENTIONSS");
			option = Options();
			currentGoal = intention(option);
		}

	}

	public Goal getCurrentGoal() {
		return currentGoal;
	}


	public void resetRoll() {
		this.teamMatesLocation.clear();
		this.newRoll = true;
	}



	public boolean shouldReviewIntentions() {
		boolean result = false;

		if(hBot.getPlayers().getNearestVisibleEnemy() != null) {
			result = true;
		}
		else 
		{
			if(!this.flagInfoTeam.equalsIgnoreCase(hBot.getOurFlag().getState()))
			{
				result = true;
				this.flagInfoTeam = hBot.getOurFlag().getState();
				resetRoll();
			}
			else
			{
				if(!this.flagInfoEnemy.equalsIgnoreCase(hBot.getEnemyFlag().getState())) {
					result = true;
					this.flagInfoEnemy = hBot.getEnemyFlag().getState();
				}
				else
				{
					if(rollChanged) {
						this.rollChanged=false;
						result = true;
					}
				}
			}
		}


		return result;
	}

	public void createRoll() 
	{
		List<Double> distances;
		Location myLocation = hBot.getInfo().getLocation();
		Location flagBase = hBot.getEnemyFlagBase().getLocation();
		//TODO
		if(teamMatesLocation.size()  != numTeammates) {			
			hBot.sendLocation();
			log.info("Team Mates Locations = " + this.teamMatesLocation.size());
			return;
		}
		distances = computeClosestBot(flagBase,myLocation);
		double myDistance = flagBase.getDistance(myLocation);
		for(int i=0; i < distances.size(); i++) {
			if(Double.compare(distances.get(i),myDistance) == 0) {
				if(i + 1 <= maxPlayersAtacking)
					roll="attack";
				else
					roll="defend";
			}
		}
		this.newRoll = false;
		this.rollChanged = true;
		log.info("ROLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		log.info(roll);

	}


	public List<Double> computeClosestBot(Location flagBase, Location myLocation) {
		List<Double> distances = new ArrayList<Double>();
		distances.add(flagBase.getDistance(myLocation));
		for(Location loc : teamMatesLocation.values()){
			distances.add(flagBase.getDistance(loc));
		}
		Collections.sort(distances);

		return distances;
	}

	public int getNumberTeamMates() {
		return this.numTeammates;
	}


}
