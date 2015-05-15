package tecnico.ulisboa.pt.AASMA_DBI.Comunication;

import cz.cuni.amis.pogamut.base.agent.module.comm.CommEvent;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;



public class BotLocation extends CommEvent {

	private UnrealId myId;
	
	private Location myLocation;
	
	public BotLocation(Location loc,UnrealId botId) {
		this.myId = botId;
		this.myLocation = loc;
	}
	
	public UnrealId getId() {
		return this.myId;
	}
	
	public Location getLocation() {
		return this.myLocation;
	}

}
