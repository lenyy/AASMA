package tecnico.ulisboa.pt.AASMA_DBI.Comunication;

import cz.cuni.amis.pogamut.base.agent.module.comm.CommEvent;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;

/*
 * Class Used for broadcasting Bot's ID's
 * 
 */

public class BotReady extends CommEvent {

	private UnrealId myId;
	
	public BotReady(UnrealId botId) {
		this.myId = botId;
	}
	
	public UnrealId getId() {
		return this.myId;
	}

}
