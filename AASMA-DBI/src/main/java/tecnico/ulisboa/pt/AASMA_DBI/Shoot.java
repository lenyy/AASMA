package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

public class Shoot extends Goal {

	private Player enemy;
	
	private WeaponPrefs weapons;
	
	private ImprovedShooting shoot;
	
	public Shoot(BDIBot bot, WeaponPrefs weapons, ImprovedShooting shoot) {
		super(bot);
		this.enemy = null;
		this.weapons = weapons;
		this.shoot = shoot;
	}

	@Override
	void perform() {
			enemy = bot.getPlayers().getNearestVisibleEnemy();

			if (enemy != null)
				shoot.shoot(this.weapons, enemy);
			else {
				shoot.stopShooting();
				setFinished(true);
			}
	}


	@Override
	boolean hasFailed() {
		return this.failed;
	}

	@Override
	boolean hasFinished() {
		return this.finished;
	}
	
	@Override
	public String toString() {
		return "GOAL ------> Shooting Enemy";
	}
	

}
