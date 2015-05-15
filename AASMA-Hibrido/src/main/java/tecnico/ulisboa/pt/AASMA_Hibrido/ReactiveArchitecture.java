package tecnico.ulisboa.pt.AASMA_Hibrido;

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;

public class ReactiveArchitecture {

	private HibridBot hBot;
	private AgentInfo info;
	private Weaponry weapons;
	private Items items;
	private LogCategory log;

	public ReactiveArchitecture(HibridBot hBot) {
		this.hBot = hBot;
		this.info = hBot.getInfo();
		this.weapons = hBot.getWeaponry();
		this.items = hBot.getItems();
		this.log = hBot.getLog();
	}


	public void execute() {

		if(hBot.getPlayers().getNearestVisibleEnemy() != null 
				&& info.getHealth() > 20) 
		{
			hBot.shoot();
			log.info("REACTIVEEEEEEEEEEEEEEEEEEEEEEE");
			log.info("SHOOTINGGGGG");
		}
		else
		{
			if(items.getNearestVisibleItem(ItemType.Category.HEALTH) != null) {
				if(items.getNearestVisibleItem(ItemType.Category.HEALTH).isVisible() 
						&& info.getHealth() < 190)
				{
					hBot.goTo(items.getNearestItem(ItemType.Category.HEALTH).getLocation());
					log.info("REACTIVEEEEEEEEEEEEEEEEEEEEEEE");
					log.info("GOING TO HEALT");

				}
			}
			else
			{
				if(items.getNearestVisibleItem(ItemType.Category.WEAPON) != null) {
					if(items.getNearestVisibleItem(ItemType.Category.WEAPON).isVisible() 
							&& !weapons.hasWeapon(items.getNearestVisibleItem(ItemType.Category.WEAPON).getType()))
					{
						hBot.goTo(items.getNearestItem(ItemType.Category.WEAPON).getLocation());
						log.info("REACTIVEEEEEEEEEEEEEEEEEEEEEEE");
						log.info("GOING TO WEAPON");
					}
				}
				else
				{
					if(items.getNearestVisibleItem(ItemType.Category.AMMO) != null) {
						if(items.getNearestVisibleItem(ItemType.Category.AMMO).isVisible() 
								&& !weapons.hasAmmo(items.getNearestVisibleItem(ItemType.Category.AMMO).getType())) {
							hBot.goTo(items.getNearestItem(ItemType.Category.AMMO).getLocation());
							log.info("REACTIVEEEEEEEEEEEEEEEEEEEEEEE");
							log.info("GOING TO AMMO");
						}
					}
				}
			}
		}
	}
}
