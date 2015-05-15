package tecnico.ulisboa.pt.AASMA_Hibrido;

import java.util.ArrayList;
import java.util.List;

import tecnico.ulisboa.pt.AASMA_Hibrido.Comunication.BotLocation;
import tecnico.ulisboa.pt.AASMA_Hibrido.Comunication.FirstSpawn;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.agent.module.comm.PogamutJVMComm;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;




@AgentScoped
public class HibridBot extends UT2004BotModuleController<UT2004Bot> {



	/**
	 * Max number of players that need to enter the game so that bot's logic can
	 * start working
	 */
	public int nPlayers = 4;

	/**
	 * List that contains bots id's.
	 * Used to check if all bots are ready to play 
	 */
	public List<UnrealId> botsReady;

	/**
	 * Channel for communicating that bot is ready
	 */
	public int worldChannel = 2;

	/**
	 * how many enemies the bot killed (i.e., bot has fragged them /
	 * got point for killing somebody)
	 */
	@JProp
	public int frags = 0;

	/** how many times the bot died */
	@JProp
	public int deaths = 0;



	protected GameInfo gameInfo;

	@JProp
	protected Location pathTarget;


	/**
	 * Returns parameters of the bot.
	 * @return
	 */
	public HibridBotParams getParams() {
		if (!(bot.getParams() instanceof HibridBotParams)) return null;
		return (HibridBotParams)bot.getParams();
	}

	public Location getPathTarget() {
		return pathTarget;
	}


	/**
	 * {@link PlayerKilled} listener that provides "frag" counting + is switches
	 * the state of the bot.
	 * 
	 * @param event
	 */
	@EventListener(eventClass = PlayerKilled.class)
	public void playerKilled(PlayerKilled event) {
		if (event.getKiller().equals(info.getId()))	++frags;
		if (enemy == null) return;
		if (enemy.getId().equals(event.getId())) {
			enemy = null;
		}
	}

	/**
	 * Listener that broadcasts bot id to see if all players are ready
	 * @param event
	 */
	@EventListener(eventClass = FirstSpawn.class)
	public void broadcastId(FirstSpawn event) {
		if(botsReady.size() + 1 != nPlayers) { // used + 1 with size because bot doesnt send the message to himself
			if(!botsReady.contains(event.getId())){
				botsReady.add(event.getId());
				if(event.getTeam() == info.getTeam())
					bdiArchitecture.addLocation(event.getLocation(), event.getId());
			}
			PogamutJVMComm.getInstance().broadcastToOthers(new FirstSpawn(info.getLocation(),info.getId(),info.getTeam()), bot);
		}
	}


	@EventListener(eventClass = BotLocation.class)
	public void botLocation(BotLocation event) {
		bdiArchitecture.addLocation(event.getLocation(), event.getId());
	}



	/**
	 * Used internally to maintain the information about the bot we're currently
	 * hunting, i.e., should be firing at.
	 */
	protected Player enemy = null;

	/**
	 * Taboo list of items that are forbidden for some time.
	 */
	protected TabooSet<Item> tabooItems = null;

	protected GetItems getItemsGoal;

	protected boolean firstLogic = true;

	private UT2004PathAutoFixer autoFixer;

	/**
	 * Bot's preparation - called before the bot is connected to GB2004 and
	 * launched into UT2004.
	 */
	@Override
	public void prepareBot(UT2004Bot bot) {
		tabooItems = new TabooSet<Item>(bot);

		autoFixer = new UT2004PathAutoFixer(bot, navigation.getPathExecutor(), fwMap, aStar, navBuilder); // auto-removes wrong navigation links between navpoints

		botsReady = new ArrayList<UnrealId>();

		// listeners
		navigation.getPathExecutor().getState().addListener(
				new FlagListener<IPathExecutorState>() {
					@Override
					public void flagChanged(IPathExecutorState changedValue) {
						switch (changedValue.getState()) {
						case STUCK:
							Item item = getItemsGoal.getItem();
							if (item != null && pathTarget != null
									&& item.getLocation()
									.equals(pathTarget, 10d)) {
								tabooItems.add(item, 10);
							}
							reset();
							break;

						case TARGET_REACHED:
							reset();
							break;
						}
					}
				});


		// DEFINE WEAPON PREFERENCES
		weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
		weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, false);
		weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, false);
		weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);


		// AND THEN RANGED
		weaponPrefs.newPrefsRange(80)
		.add(UT2004ItemType.SHIELD_GUN, true);

		weaponPrefs.newPrefsRange(1000)
		.add(UT2004ItemType.FLAK_CANNON, true)
		.add(UT2004ItemType.MINIGUN, true)
		.add(UT2004ItemType.LINK_GUN, false)
		.add(UT2004ItemType.ASSAULT_RIFLE, true);        

		weaponPrefs.newPrefsRange(4000)
		.add(UT2004ItemType.SHOCK_RIFLE, true)
		.add(UT2004ItemType.MINIGUN, false);

		weaponPrefs.newPrefsRange(100000)
		.add(UT2004ItemType.LIGHTNING_GUN, true)
		.add(UT2004ItemType.SHOCK_RIFLE, true);  


		reaArchitecture = new ReactiveArchitecture(this);
		bdiArchitecture = new BDIArchitecture(this,nPlayers/2 - 1);

		bdiArchitecture.addGoal(new GetEnemyFlag(this),"GET ENEMY FLAG");
		bdiArchitecture.addGoal(new SupportTeamMateWithFlag(this),"SUPPORT TEAM MATE WITH FLAG");
		bdiArchitecture.addGoal(new GetHealth(this),"GET HEALTH");
		bdiArchitecture.addGoal(new GetOurFlag(this),"GET OUR FLAG");
		bdiArchitecture.addGoal(getItemsGoal = new GetItems(this),"GET ITEMS");
		bdiArchitecture.addGoal(new GoToOurBase(this), "GO HOME");
		bdiArchitecture.addGoal(new Defend(this), "DEFEND");


	}

	/**
	 * Here we can modify initialization-command for our bot.
	 * @return
	 */
	@Override
	public Initialize getInitializeCommand() {
		if (getParams() == null) {
			return new Initialize();
		} else {
			return new Initialize().setDesiredSkill(getParams().getSkillLevel()).setSkin(getParams().getBotSkin()).setTeam(getParams().getTeam());
		}
	}

	/**
	 * Handshake with GameBots2004 is over - bot has information about the map
	 * in its world view. Many agent modules are usable since this method is
	 * called.
	 *
	 * @param gameInfo informaton about the game type
	 * @param config information about configuration
	 * @param init information about configuration
	 */
	@Override
	public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
		// By uncommenting line below, you will see all messages that goes trough GB2004 parser (GB2004 -> BOT communication)
		//bot.getLogger().getCategory("Parser").setLevel(Level.ALL);
	}



	@Override
	public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
		PogamutJVMComm.getInstance().registerAgent(bot, self.getTeam());
		PogamutJVMComm.getInstance().broadcastToOthers(new FirstSpawn(info.getLocation(),info.getId(),info.getTeam()), bot);
	}


	@Override
	public void botShutdown() {
		PogamutJVMComm.getInstance().unregisterAgent(bot);
	}


	protected BDIArchitecture bdiArchitecture = null;

	protected ReactiveArchitecture reaArchitecture = null;


	public boolean goTo(ILocated target) {
		if (target == null) {
			log.info("goTo: null");
			return false;
		}
		log.info(String.format(
				"goTo: %s %s",
				target.toString(),
				info.getLocation()));

		pathTarget = target.getLocation();
		navigation.navigate(target);

		return true;
	}
	public boolean goTo(Location target) {

		if (target == null) {
			log.info("goTo: null");
			return false;
		}
		log.info(String.format(
				"goTo: %s %s",
				target.toString(),
				info.getLocation()));

		pathTarget = target.getLocation();
		navigation.navigate(target);

		return true;
	}


	public void shoot() {
		enemy = this.getPlayers().getNearestVisibleEnemy();

		if (enemy != null)
			shoot.shoot(weaponPrefs, enemy);
		else {
			shoot.stopShooting();
		}
	}


	/**
	 * This method is called only once, right before actual logic() method is
	 * called for the first time.
	 * 
	 * Similar to {@link HibridBot#botFirstSpawn(cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage, cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self)}.
	 */
	@Override
	public void beforeFirstLogic() {
	}


	/**
	 * Called each time the bot dies. Good for reseting all bot's state
	 * dependent variables.
	 *
	 * @param event
	 */
	@Override
	public void botKilled(BotKilled event) {
		// Uncomment this line to have the bot comment on its death.
		//sayGlobal("I was KILLED!");
	}

	@EventListener(eventClass=BotDamaged.class)
	public void botDamaged(BotDamaged event) {
		// Uncomment this line to gain information about damage the bot receives
		//sayGlobal("GOT DAMAGE: " + event.getDamage() + ", HEALTH: " + info.getHealth());
		// Notice that "HEALTH" does not fully match the damage received, because "HEALTH" is updated periodically
		// but BotDamaged message comes as event, therefore the "HEALTH" number lags behind a bit (250ms at max)
	}



	public NavPoint getOurFlagBase() {
		return ctf.getOurBase();
	}

	public NavPoint getEnemyFlagBase() {
		return ctf.getEnemyBase();
	}

	public FlagInfo getOurFlag() {
		return ctf.getOurFlag();
	}

	public FlagInfo getEnemyFlag() {
		return ctf.getEnemyFlag();
	}

	public Player getEnemy() {
		return enemy;
	}


	public TabooSet<Item> getTaboo() {
		return tabooItems;
	}


	public void sendLocation() {
		PogamutJVMComm.getInstance().sendToOthers(new BotLocation(info.getLocation(),info.getId()), info.getTeam(), bot);
	}



	/**
	 * Resets the state of the bot.
	 */
	protected void reset() {
		notMoving = 0;
		enemy = null;
		navigation.stopNavigation();
	}

	/**
	 * Global anti-stuck mechanism. When this counter reaches a certain
	 * constant, the bot's mind gets a {@link BDIBot#reset()}.
	 */
	protected int notMoving = 0;


	public boolean reactiveArchitecture() {
		boolean result = false;
		if(players.getNearestVisibleEnemy() != null) {
			result = true;
		}
		else
		{
			if(items.getNearestItem(ItemType.Category.HEALTH) != null) {
				if(items.getNearestItem(ItemType.Category.HEALTH).isVisible()) {
					result = true;
				}
			}
			else
			{
				if(items.getNearestItem(ItemType.Category.WEAPON) != null) {
					if(items.getNearestVisibleItem(ItemType.Category.WEAPON).isVisible() 
							&& !weaponry.hasWeapon(items.getNearestVisibleItem(ItemType.Category.WEAPON).getType()))
					{
						result = true;
					}
				}
				else
				{
					if(items.getNearestVisibleItem(ItemType.Category.AMMO) != null) {
						if(items.getNearestVisibleItem(ItemType.Category.AMMO).isVisible()) {
							result = true;
						}
					}
					else
					{
						if(items.getNearestVisibleItem(ItemType.Category.ARMOR) != null){
							if(items.getNearestVisibleItem(ItemType.Category.ARMOR).isVisible()) {
								result = true;
							}
						}
					}
				}
			}
			shoot.stopShooting();
		}

		return result;
	}



	/**
	 * Main method that controls the bot - makes decisions what to do next. It
	 * is called iteratively by Pogamut engine every time a synchronous batch
	 * from the environment is received. This is usually 4 times per second - it
	 * is affected by visionTime variable, that can be adjusted in ini file
	 * inside UT2004/System/GameBots2004.ini
	 *
	 * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
	 */
	@Override
	public void logic() throws PogamutException {

		if(botsReady.size() + 1 == nPlayers){

			if(reactiveArchitecture())
				reaArchitecture.execute();
			else
				bdiArchitecture.BDIPlanner();	

		}
	}

	/**
	 * This method is called when the bot is started either from IDE or from
	 * command line.
	 *
	 * @param args
	 */
	public static void main(String args[]) throws PogamutException {


		new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(HibridBot.class, "Hibrid Bot").setMain(true)
		.startAgents(
				new HibridBotParams().setBotSkin("HumanMaleA.MercMaleC")       .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 1"))
				,new HibridBotParams().setBotSkin("HumanFemaleA.MercFemaleA").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 1"))
				,new HibridBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 2"))				
				,new HibridBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 2"))
				/*,new HibridBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 3"))				
				,new HibridBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 3"))
				,new HibridBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 4"))				
				,new HibridBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 4"))
				,new HibridBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 5"))				
				,new HibridBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 5"))*/
				);
	}
}
