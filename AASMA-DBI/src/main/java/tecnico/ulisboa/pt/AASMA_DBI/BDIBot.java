package tecnico.ulisboa.pt.AASMA_DBI;



import java.util.ArrayList;
import java.util.List;

import tecnico.ulisboa.pt.AASMA_DBI.Comunication.BotReady;
import tecnico.ulisboa.pt.AASMA_DBI.Comunication.DroppedEnemyFlag;
import tecnico.ulisboa.pt.AASMA_DBI.Comunication.GotFlag;
import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.agent.module.comm.PogamutJVMComm;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathExecutorState;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.event.WorldObjectAppearedEvent;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.UT2004PathAutoFixer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.TeamScore.TeamScoreUpdate;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.Heatup;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;


@AgentScoped
public class BDIBot extends UT2004BotModuleController<UT2004Bot> {

	/**
	 * Max number of players that need to enter the game so that bot's logic can
	 * start working
	 */
	public int players = 4;
	
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


	protected UnrealId friendWithFlag = null;

	/**
	 * Returns parameters of the bot.
	 * @return
	 */
	public BDIBotParams getParams() {
		if (!(bot.getParams() instanceof BDIBotParams)) return null;
		return (BDIBotParams)bot.getParams();
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
	@EventListener(eventClass = BotReady.class)
	public void broadcastId(BotReady event) {
		if(botsReady.size() + 1 != players) { // used + 1 with size because bot doesnt send the message to himself
			if(!botsReady.contains(event.getId())){
				botsReady.add(event.getId());
			}
			PogamutJVMComm.getInstance().sendToOthers(new BotReady(info.getId()), worldChannel, bot);
		}
	}

	@EventListener(eventClass = GotFlag.class)
	public void gotEnemyFlag(GotFlag event) {
		this.friendWithFlag = event.getBotId();
		log.info("My Friend " + this.getPlayers().getPlayer(friendWithFlag).getName() + " has the enemy flag");
	}

	@EventListener(eventClass = DroppedEnemyFlag.class)
	public void DroppedEnemyFlag(DroppedEnemyFlag event) {
		this.friendWithFlag = null;
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

		bdiArchitecture = new BDIArchitecture(bot,this);

		bdiArchitecture.addGoal(new GetEnemyFlag(this),"GET ENEMY FLAG");
		bdiArchitecture.addGoal(new SupportTeamMateWithFlag(this),"SUPPORT TEAM MATE WITH FLAG");
		bdiArchitecture.addGoal(new GetHealth(this),"GET HEALTH");
		bdiArchitecture.addGoal(new GetOurFlag(this),"GET OUR FLAG");
		bdiArchitecture.addGoal(getItemsGoal = new GetItems(this),"GET ITEMS");
		bdiArchitecture.addGoal(new GoToOurBase(this), "GO HOME");
		bdiArchitecture.addGoal(new Shoot(this,weaponPrefs,shoot), "SHOOT");


	}

	@Override
	public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
		this.gameInfo = gameInfo;
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

	@Override
	public void botFirstSpawn(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init, Self self) {
		PogamutJVMComm.getInstance().registerAgent(bot, self.getTeam());
		PogamutJVMComm.getInstance().registerAgent(bot, worldChannel);
		PogamutJVMComm.getInstance().sendToOthers(new BotReady(info.getId()), worldChannel, bot);
	}

	@Override
	public void botShutdown() {
		PogamutJVMComm.getInstance().unregisterAgent(bot);
	}

	protected BDIArchitecture bdiArchitecture = null;

	protected final Heatup targetHU = new Heatup(5000);

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


	/**
	 * Resets the state of the bot.
	 */
	protected void reset() {
		notMoving = 0;
		enemy = null;
		navigation.stopNavigation();
		friendWithFlag = null;
	}

	/**
	 * Global anti-stuck mechanism. When this counter reaches a certain
	 * constant, the bot's mind gets a {@link BDIBot#reset()}.
	 */
	protected int notMoving = 0;

	/*
	 * protected IWorldEventListener<GameInfoMessage> gameInfoListener = new
	 * IWorldEventListener<GameInfoMessage>() {
	 * 
	 * @Override public void notify(GameInfoMessage event) {
	 * log.info(String.format("Glorious! %s", event.toString())); switch
	 * (getInfo().getTeam()) { case 0: ourBase = event.getRedBaseLocation();
	 * enemyBase = event.getBlueBaseLocation(); break; case 1: ourBase =
	 * event.getBlueBaseLocation(); enemyBase = event.getRedBaseLocation();
	 * break; } } };
	 */

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

	public UnrealId getFriendWithFlag() {
		return this.friendWithFlag;
	}



	/**
	 * Main method that controls the bot - makes decisions what to do next. It
	 * is called iteratively by Pogamut engine every time a synchronous batch
	 * from the environment is received. This is usually 4 times per second - it
	 * is affected by visionTime variable, that can be adjusted in GameBots ini
	 * file in UT2004/System folder.
	 * 
	 * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
	 */
	@Override
	public void logic() {

		if(botsReady.size() + 1 == players)
			bdiArchitecture.BDIPlanner();	


	}

	public TabooSet<Item> getTaboo() {
		return tabooItems;
	}


	public void gotEnemyFlag() {
		log.info("GOTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT THEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE FLAAAAAAAAAAAAG");
		PogamutJVMComm.getInstance().sendToOthers(new GotFlag(info.getId()), bot.getSelf().getTeam(), bot);
	}


	// //////////
	// //////////////
	// BOT KILLED //
	// //////////////
	// //////////

	@Override
	public void botKilled(BotKilled event) {
		if(bot.getSelf().getId().equals(getEnemyFlag().getHolder())) {			
			PogamutJVMComm.getInstance().sendToOthers(new DroppedEnemyFlag(), bot.getSelf().getTeam(), bot);
		}
		reset();		
	}


	public void teamScore(TeamScoreUpdate event) {
		this.friendWithFlag = null;
	}

	// //////////////////////////////////////////
	// //////////////////////////////////////////
	// //////////////////////////////////////////

	public static void main(String args[]) throws PogamutException {

		// starts 2 or 4 CTFBots at once
		// note that this is the most easy way to get a bunch of bots running at the same time

		new UT2004BotRunner<UT2004Bot, UT2004BotParameters>(BDIBot.class, "DBIBot").setMain(true)
		.startAgents(
				new BDIBotParams().setBotSkin("HumanMaleA.MercMaleC")       .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 1"))
				,new BDIBotParams().setBotSkin("HumanFemaleA.MercFemaleA").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 1"))
				,new BDIBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 2"))				
				,new BDIBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 2"))
				/*,new DBIBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 3"))				
				,new DBIBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 3"))
				,new DBIBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 4"))				
				,new DBIBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 4"))
				,new DBIBotParams().setBotSkin("HumanMaleA.MercMaleA")    .setSkillLevel(5).setTeam(0).setAgentId(new AgentId("Team RED - Bot 5"))				
				,new DBIBotParams().setBotSkin("HumanFemaleA.MercFemaleB").setSkillLevel(5).setTeam(1).setAgentId(new AgentId("Team BLUE - Bot 5"))*/
				);

	}

}