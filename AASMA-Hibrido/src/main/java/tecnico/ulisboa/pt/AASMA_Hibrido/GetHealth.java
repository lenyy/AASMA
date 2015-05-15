package tecnico.ulisboa.pt.AASMA_Hibrido;

import java.util.Set;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;

public class GetHealth extends Goal {

	protected Item health = null;

	public GetHealth(HibridBot bot) {
		super(bot);
	}

	@Override
	public void perform() {


		if (health == null) {
			Set<Item> healths = bot.getTaboo().filter(
					bot.getItems().getSpawnedItems(
							Category.HEALTH).values());

			double min_distance = Double.MAX_VALUE;
			Item winner = null;

			for (Item item : healths) {
				double dist = item.getLocation().getDistance(
						bot.getInfo().getLocation());
				if (dist < min_distance) {
					min_distance = dist;
					winner = item;
				}
			}
			this.health = winner;
		}
		if (health == null)
			return;

		bot.getLog().info(String.format("Found health: %s", health.toString()));

		if (bot.getEnemyFlag() == null
				|| !bot.getInfo().getId()
						.equals(bot.getEnemyFlag().getHolder())) {
			bot.goTo(health);
			setFinished(true);
		}

	}


	@Override
	public boolean hasFailed() {
		return this.failed;
	}

	@Override
	public boolean hasFinished() {
		return this.finished;
	}

	@Override
	public String toString() {
		return "GOAL ------> Get Health";
	}

}
