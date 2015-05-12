package tecnico.ulisboa.pt.AASMA_DBI.Comunication;

import cz.cuni.amis.pogamut.base.agent.module.comm.CommEvent;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;

public class GotFlag extends CommEvent {

	private UnrealId my_id;

	public GotFlag(UnrealId botId) {
		this.my_id=botId;
	}

	public UnrealId getBotId() {
		return this.my_id;
	}

}
