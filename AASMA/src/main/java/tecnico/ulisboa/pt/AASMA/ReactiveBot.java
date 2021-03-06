package tecnico.ulisboa.pt.AASMA;

import javax.vecmath.Vector3d;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.FlagInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.Random;

/**
 * Example of Simple Pogamut bot, that randomly walks around the map. Bot is
 * incapable of handling movers so far. 
 * 
 * <p><p> 
 * The crucial method to read
 * through is {@link RaycastingBot#botInitialized(GameInfo, ConfigChange, InitedMessage)},
 * it will show you how to set up ray-casting.
 * 
 * <p><p>
 * We recommend you to try this bot on DM-TrainingDay or DM-Albatross or DM-Flux2.
 * 
 * <p><p>
 * Note that this is a bit deprecated way to do raycasting as we have more advanced approach via "geometry-at-client", see {@link LevelGeometryModule} 
 * and checkout svn://artemis.ms.mff.cuni.cz/pogamut/trunk/project/Main/PogamutUT2004Examples/35-ManualBot that contains hints how to do raycasting client-side.
 * 
 *
 * @author Ondrej Burkert
 * @author Rudolf Kadlec aka ik
 * @author Jakub Gemrot aka Jimmy
 */
@AgentScoped
public class ReactiveBot extends UT2004BotModuleController {

	// Constants for rays' ids. It is allways better to store such values
	// in constants instead of using directly strings on multiple places of your
	// source code
	protected static final String FRONT = "frontRay";
	protected static final String LEFT45 = "left45Ray";
	protected static final String LEFT90 = "left90Ray";
	protected static final String RIGHT45 = "right45Ray";
	protected static final String RIGHT90 = "right90Ray";
        
        protected static final String FRONT_FLOOR = "frontFloorRay";
        protected static final String LEFT_FLOOR45 = "leftFloor45Ray";
        protected static final String RIGHT_FLOOR45 = "rightFloor45Ray";
        protected static final String LEFT_FLOOR90 = "leftFloor90Ray";
        protected static final String RIGHT_FLOOR90 = "rightFloor90Ray";

	public static int instanceCount;

	private AutoTraceRay left, front, right, bigRight, bigLeft, floorLeft45, floorRight45, floorLeft90,
                floorRight90 ,floorFront;

	/**
	 * Flag indicating that the bot has been just executed.
	 */
	private boolean first = true;
	private boolean raysInitialized = false;
	/**
	 * Whether the left45 sensor signalizes the collision. (Computed in the
	 * doLogic()) <p><p> Using {@link RaycastingBot#LEFT45} as the key for the
	 * ray.
	 */
	@JProp
	private boolean sensorLeft45 = false;
	/**
	 * Whether the right45 sensor signalizes the collision. (Computed in the
	 * doLogic()) <p><p> Using {@link RaycastingBot#RIGHT45} as the key for the
	 * ray.
	 */
	@JProp
	private boolean sensorRight45 = false;
	/**
	 * Whether the front sensor signalizes the collision. (Computed in the
	 * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
	 * ray.
	 */
	@JProp
	private boolean sensorFront = false;
	/**
	 * Whether the right 90º signalizes the collision. (Computed in the
	 * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
	 * ray.
	 */
	@JProp
	private boolean sensorRight90 = false;

	/**
	 * Whether the front sensor signalizes the collision. (Computed in the
	 * doLogic()) <p><p> Using {@link RaycastingBot#FRONT} as the key for the
	 * ray.
	 */
	@JProp
	private boolean sensorLeft90 = false;
        
        @JProp
        private boolean sensorFloorLeft45 = false;

        @JProp
        private boolean sensorFloorRight45 = false;
        
        @JProp
        private boolean sensorFloorLeft90 = false;

        @JProp
        private boolean sensorFloorRight90 = false;

        @JProp
        private boolean sensorFloorFront = false;

	/**
	 * Whether the bot is moving. (Computed in the doLogic())
	 */
	@JProp
	private boolean moving = false;
	/**
	 * Whether any of the sensor signalize the collision. (Computed in the
	 * doLogic())
	 */
	@JProp
	private boolean sensor = false;
	/**
	 * How much time should we wait for the rotation to finish (milliseconds).
	 */
        
        @JProp
        private boolean sensorFloor = false;
        
	@JProp
	private int turnSleep = 250;
	/**
	 * How fast should we move? Interval <0, 1>.
	 */
	private float moveSpeed = 0.6f;
	/**
	 * Small rotation (degrees).
	 */
	@JProp
	private int smallTurn = 45;
	/**
	 * Big rotation (degrees).
	 */
	@JProp
	private int bigTurn = 90;

	/**
	 * how many bot the hunter killed other bots (i.e., bot has fragged them /
	 * got point for killing somebody)
	 */
	@JProp
	public int frags = 0;

	/**
	 * Used internally to maintain the information about the bot we're currently
	 * hunting, i.e., should be firing at.
	 */
	protected Player enemy = null;

	/**
	 * {@link PlayerKilled} listener that provides "frag" counting + is switches
	 * the state of the hunter.
	 *
	 * @param event
	 */
	@EventListener(eventClass = PlayerKilled.class)
	public void playerKilled(PlayerKilled event) {
		if (event.getKiller().equals(info.getId())) {
			++frags;
			log.info("I'm the KILLER ");
		}
		if (enemy == null) {
			return;
		}
		if (enemy.getId().equals(event.getId())) {
			enemy = null;
		}
	}


	/**
	 * The bot is initialized in the environment - a physical representation of
	 * the bot is present in the game.
	 *
	 * @param config information about configuration
	 * @param init information about configuration
	 */
	@Override
	public void botInitialized(GameInfo info, ConfigChange currentConfig, InitedMessage init) {   	
		// initialize rays for raycasting
		final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 3);
                final int rayLengthFloor = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 10);
		// settings for the rays
		boolean fastTrace = true;        // perform only fast trace == we just need true/false information
		boolean floorCorrection = false; // provide floor-angle correction for the ray (when the bot is running on the skewed floor, the ray gets rotated to match the skew)
		boolean traceActor = false;      // whether the ray should collid with other actors == bots/players as well

		// 1. remove all previous rays, each bot starts by default with three
		// rays, for educational purposes we will set them manually
		getAct().act(new RemoveRay("All"));

		// 2. create new rays
		raycasting.createRay(LEFT45,  new Vector3d(1, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
		raycasting.createRay(FRONT,   new Vector3d(1, 0, 0), rayLength, fastTrace, floorCorrection, traceActor);
		raycasting.createRay(RIGHT45, new Vector3d(1, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
		// note that we will use only three of them, so feel free to experiment with LEFT90 and RIGHT90 for yourself
		raycasting.createRay(LEFT90,  new Vector3d(0, -1, 0), rayLength, fastTrace, floorCorrection, traceActor);
		raycasting.createRay(RIGHT90, new Vector3d(0, 1, 0), rayLength, fastTrace, floorCorrection, traceActor);
                
                raycasting.createRay(LEFT_FLOOR45,  new Vector3d(1, -1, -0.6), rayLengthFloor, fastTrace, floorCorrection, traceActor);
                raycasting.createRay(FRONT_FLOOR,   new Vector3d(1, 0, -0.6), rayLengthFloor, fastTrace, floorCorrection, traceActor);
                raycasting.createRay(RIGHT_FLOOR45, new Vector3d(1, 1, -0.6), rayLengthFloor, fastTrace, floorCorrection, traceActor);
                raycasting.createRay(LEFT_FLOOR90,  new Vector3d(0, -1, -0.6), rayLengthFloor, fastTrace, floorCorrection, traceActor);
		raycasting.createRay(RIGHT_FLOOR90, new Vector3d(0, 1, -0.6), rayLengthFloor, fastTrace, floorCorrection, traceActor);


		// register listener called when all rays are set up in the UT engine
		raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {

			public void flagChanged(Boolean changedValue) {
				// once all rays were initialized store the AutoTraceRay objects
				// that will come in response in local variables, it is just
				// for convenience
				left = raycasting.getRay(LEFT45);
				front = raycasting.getRay(FRONT);
				right = raycasting.getRay(RIGHT45);
				bigRight = raycasting.getRay(RIGHT90);
				bigLeft = raycasting.getRay(LEFT90);
                                
                                floorLeft45 = raycasting.getRay(LEFT_FLOOR45);
                                floorFront = raycasting.getRay(FRONT_FLOOR);
                                floorRight45 = raycasting.getRay(RIGHT_FLOOR45);
                                floorLeft90 = raycasting.getRay(LEFT_FLOOR90);
                                floorRight90 = raycasting.getRay(RIGHT_FLOOR90);
			}
		});
		// have you noticed the FlagListener interface? The Pogamut is often using {@link Flag} objects that
		// wraps some iteresting values that user might respond to, i.e., whenever the flag value is changed,
		// all its listeners are informed

		// 3. declare that we are not going to setup any other rays, so the 'raycasting' object may know what "all" is        
		raycasting.endRayInitSequence();

		// change bot's default speed
		config.setSpeedMultiplier(moveSpeed);

		// IMPORTANT:
		// The most important thing is this line that ENABLES AUTO TRACE functionality,
		// without ".setAutoTrace(true)" the AddRay command would be useless as the bot won't get
		// trace-lines feature activated
		getAct().act(new Configuration().setDrawTraceLines(true).setAutoTrace(true));

		// FINAL NOTE: the ray initialization must be done inside botInitialized method or later on inside
		//             botSpawned method or anytime during doLogic method

		// DEFINE WEAPON PREFERENCES
		weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);                
		weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
		weaponPrefs.addGeneralPref(UT2004ItemType.FLAK_CANNON, true);        
		weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, true);
		weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);        
		weaponPrefs.addGeneralPref(UT2004ItemType.BIO_RIFLE, true);
	}


	/**
	 * Returns parameters of the bot.
	 * @return
	 */
	public CustomBotParameters getParams() {
		if (!(bot.getParams() instanceof CustomBotParameters)) return null;
		return (CustomBotParameters)bot.getParams();
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
			return new Initialize().setDesiredSkill(getParams().getSkillLevel()).setSkin(getParams().getBotSkin()).setTeam(getParams().getTeam()).setName(getParams().getName());
		}
	}



	public FlagInfo getEnemyFlag() {
		return ctf.getEnemyFlag();
	}



	/**
	 * Main method that controls the bot.
	 *
	 * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
	 */
	@Override
	public void logic() throws PogamutException {
		// mark that another logic iteration has began
		log.info("--- Logic iteration ---");

	
		
		if (carryingFlag() && !players.canSeeEnemies()) {
			this.move();
			return;
		}
		if (players.canSeeEnemies() && info.hasWeapon() && weaponry.getCurrentPrimaryAmmo() != 0){
			this.stateEngage();
			return;
		}
		else {
			// 2) are you shooting? 	-> stop shooting, you've lost your target
			if (info.isShooting() || info.isSecondaryShooting()) {
				getAct().act(new StopShooting());
			}
			this.move();
			return;
		}
	}

	protected boolean carryingFlag(){
		boolean result = false;
		if (this.getEnemyFlag() != null) {
			UnrealId holderId = getEnemyFlag().getHolder();

			if (getInfo().getId().equals(holderId)) 
				result = true;
		}
		return result;
	}


	protected void stateEngage(){
		log.info("Decision is: ENGAGE");

		// pick new enemy
		enemy = players.getNearestVisibleEnemy();
		if (enemy == null) 
		{
			log.info("Can't see any enemies... ???");
			return;
		}
		else 
		{
			// 2) or shoot on enemy if it is visible
			if (shoot.shoot(weaponPrefs, enemy) != null) 
				log.info("Shooting at enemy!!!");
		}
		enemy = null;

	}


	protected void move() {

		Random rnd = new Random();


		// if the rays are not initialized yet, do nothing and wait for their initialization 
		if (!raycasting.getAllRaysInitialized().getFlag()) {
			log.info("Aqui");

			return;
		}

		// once the rays are up and running, move according to them

		sensorFront = front.isResult();
		sensorLeft45 = left.isResult();
		sensorRight45 = right.isResult();
		sensorRight90 = bigRight.isResult();
		sensorLeft90 = bigLeft.isResult();
                
                sensorFloorFront = floorFront.isResult();
                sensorFloorLeft45 = floorLeft45.isResult();
                sensorFloorRight45 = floorRight45.isResult();
                sensorFloorLeft90 = floorLeft90.isResult();
                sensorFloorRight90 = floorRight90.isResult();

		// is any of the sensor signalig?
		sensor = sensorFront || sensorLeft45 || sensorRight45 || sensorLeft90 || sensorRight90;
                sensorFloor = sensorFloorFront && sensorFloorLeft45 && sensorFloorRight45 && sensorFloorLeft90 && sensorFloorRight90;

		if (!sensor && sensorFloor) {
			// no sensor are signalizes - just proceed with forward movement
			goForward();
			return;
		}

		// some sensor/s is/are signaling

		// if we're moving
		if (moving) {
			// stop it, we have to turn probably
			move.stopMovement();
			moving = false;
		}

		// according to the signals, take action...
		// 32 cases that might happen follows.
		if (sensorFront || !sensorFloorFront) {
			if (sensorLeft45 || !sensorFloorLeft45) 
			{
				if (sensorRight45 || !sensorFloorRight45) 
				{
					if(sensorRight90 || !sensorFloorRight90)
					{
						if(sensorLeft90 || !sensorFloorLeft90) 
						{
							// LEFT45, LEFT90, RIGHT45, RIGHT90 and FRONT are signaling
							log.info("180 degrees to Right");
							move.turnHorizontal(bigTurn*2);
						} 
						else {
							log.info("90 Degrees to Left");
							// LEFT45, FRONT45, FRONT and RIGHT90 are signaling
							move.turnHorizontal(-bigTurn);
						}
					}
					else
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, FRONT45, FRONT and LEFT90 are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);		
						}
						else 
						{
							if (rnd.nextInt(1)==1) 
							{
								// LEFT45, FRONT45, FRONT are signaling
								log.info("90 Degrees to Right 2");
								move.turnHorizontal(bigTurn);
							}
							else 
							{
								// LEFT45, FRONT45, FRONT are signaling
								log.info("90 Degrees to Left 2");
								move.turnHorizontal(-bigTurn);
							}
						}
					}
				}
				else 
				{
					if(sensorRight90 || !sensorFloorRight90)
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, LEFT90, RIGHT90, FRONT are signaling
							log.info("45 Degrees to Right");
							move.turnHorizontal(smallTurn);
						}
						else
						{
							// Two rays still available, random solves the problem on choosing one
							if(rnd.nextInt(1)==1)
							{
								// LEFT45, RIGHT90, FRONT are signaling
								log.info("90 Degrees to Left 3");
								move.turnHorizontal(-bigTurn);
							}
							else
							{
								// LEFT45, RIGHT90, FRONT are signaling
								log.info("45 Degrees to Right 2");
								move.turnHorizontal(smallTurn);
							}
						}

					}
					else
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, LEFT90, FRONT are signaling
							log.info("45 Degrees to Right");
							move.turnHorizontal(smallTurn);
						}
						else
						{
							// LEFT45, FRONT are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);
						}
					}

				}
			}
			else
			{
				if(sensorRight45 || !sensorFloorRight45)
				{
					if(sensorLeft90 || !sensorFloorLeft90)
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT45, LEFT90, RIGHT90, FRONT are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// LEFT90, RIGHT45, FRONT are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);
						}
					}
					else
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT45, RIGHT90, FRONT are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// RIGHT45, FRONT are signaling
							log.info("90 Degrees to Left");
							move.turnHorizontal(-bigTurn);
						}
					}
				}
				else
				{
					if(sensorLeft90 || !sensorFloorLeft90)
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// LEFT90, RIGHT90, FRONT are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// LEFT90, FRONT are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);
						}
					}
					else
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT90, FRONT are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// FRONT are signaling
							log.info("90 Degrees to Left");
							move.turnHorizontal(-bigTurn);
						}
					}
				}
			}
		} 
		else 
		{
			if (sensorLeft45 || !sensorFloorLeft45) 
			{
				if (sensorRight45 || !sensorFloorRight45) 
				{
					if(sensorRight90 || !sensorFloorRight90)
					{
						if(sensorLeft90 || !sensorFloorLeft90) 
						{
							// LEFT45, LEFT90, RIGHT45, RIGHT90 are signaling
							log.info("Forward");
							goForward();						
						} 
						else {
							// LEFT45, RIGHT45, RIGHT90 are signaling
							log.info("90 Degrees to Left 2");
							move.turnHorizontal(-bigTurn);
						}
					}
					else
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, RIGHT45 and LEFT90 are signaling
							log.info("Forward");
							goForward();		
						}
						else 
						{
							if (rnd.nextInt(1)==1) 
							{
								// LEFT45, RIGHT45 are signaling
								log.info("90 Degrees to Right 2");
								move.turnHorizontal(bigTurn);
							}
							else 
							{
								// LEFT45, RIGHT45 are signaling
								log.info("90 Degrees to Left 2");
								move.turnHorizontal(-bigTurn);
							}
						}
					}
				}
				else 
				{
					if(sensorRight90 || !sensorFloorRight90)
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, LEFT90, RIGHT90 are signaling
							log.info("45 Degrees to Right");
							move.turnHorizontal(smallTurn);
						}
						else
						{
							// Two rays still available, random solves the problem on choosing one
							if(rnd.nextInt(1)==1)
							{
								// LEFT45, RIGHT90 are signaling
								log.info("90 Degrees to Left 3");
								move.turnHorizontal(-bigTurn);
							}
							else
							{
								// LEFT45, RIGHT90 are signaling
								log.info("45 Degrees to Right 2");
								move.turnHorizontal(smallTurn);
							}
						}

					}
					else
					{
						if(sensorLeft90 || !sensorFloorLeft90)
						{
							// LEFT45, LEFT90 are signaling
							log.info("Forward");
							goForward();
						}
						else
						{
							// LEFT45, FRONT are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);
						}
					}

				}
			}
			else
			{
				if(sensorRight45 || !sensorFloorRight45)
				{
					if(sensorLeft90 || !sensorFloorLeft90)
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT45, LEFT90, RIGHT90 are signaling
							log.info("Forward");
							goForward();
						}
						else
						{
							// LEFT90, RIGHT45 are signaling
							log.info("90 Degrees to Right");
							move.turnHorizontal(bigTurn);
						}
					}
					else
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT45, RIGHT90 are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// Four rays still available, random solves the problem on choosing one
							if(rnd.nextInt(1)==1)
							{
								// RIGHT45 is signaling
								log.info("Forward");
								goForward();
							}
							else
							{
								//RIGHT45 is signaling
								log.info("45 Degrees to Left 2");
								move.turnHorizontal(-smallTurn);
							}
						}
					}
				}
				else
				{
					if(sensorLeft90 || !sensorFloorLeft90)
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// LEFT90, RIGHT90 are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// LEFT90 are signaling
							log.info("Forward");
							goForward();
						}
					}
					else
					{
						if(sensorRight90 || !sensorFloorRight90)
						{
							// RIGHT90 are signaling
							log.info("45 Degrees to Left");
							move.turnHorizontal(-smallTurn);
						}
						else
						{
							// No signaling
							log.info("Forward");
							goForward();
						}
					}
				}
			}
		} 
	}

	/**
	 * Simple method that starts continuous movement forward + marking the
	 * situation (i.e., setting {@link RaycastingBot#moving} to true, which
	 * might be utilized later by the logic).
	 */
	protected void goForward() {
		move.moveContinuos();
		moving = true;
	}



	///////////////////////////////////
	public static void main(String args[]) throws PogamutException {



		// starts 10 Hunters at once
		// note that this is the most easy way to get a bunch of (the same) bots running at the same time
		// Bots divided into 2 teams
		new UT2004BotRunner(ReactiveBot.class, "Reactive").setMain(true).startAgents
		(new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount)))/**,

				new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),

				new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),

				new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
				new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
				new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
				new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),

				new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),

				new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
				new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount)))*/);
	}
}
