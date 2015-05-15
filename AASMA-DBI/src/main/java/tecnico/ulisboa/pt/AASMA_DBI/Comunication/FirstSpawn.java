package tecnico.ulisboa.pt.AASMA_DBI.Comunication;

import cz.cuni.amis.pogamut.base.agent.module.comm.CommEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;

public class FirstSpawn extends CommEvent {

	private UnrealId myId;
	
	private Location myLocation;
	
	private int team;

	public FirstSpawn(Location loc,UnrealId botId,int team) {
		this.myId = botId;
		this.myLocation = loc;
		this.team=team;
	}

	public UnrealId getId() {
		return this.myId;
	}
	
	public Location getLocation() {
		return this.myLocation;
	}
	
	public int getTeam() {
		return this.team;
	}

}
