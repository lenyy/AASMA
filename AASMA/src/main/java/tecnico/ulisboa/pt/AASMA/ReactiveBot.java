package tecnico.ulisboa.pt.AASMA;

import javax.vecmath.Vector3d;

import cz.cuni.amis.introspection.java.JProp;
import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.LevelGeometryModule;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.RemoveRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.StopShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;

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
    
    public static int instanceCount;
    
    private AutoTraceRay left, front, right;
    
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
    private int turnSleep = 250;
    /**
     * How fast should we move? Interval <0, 1>.
     */
    private float moveSpeed = 0.6f;
    /**
     * Small rotation (degrees).
     */
    @JProp
    private int smallTurn = 30;
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
        final int rayLength = (int) (UnrealUtils.CHARACTER_COLLISION_RADIUS * 10);
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


        // register listener called when all rays are set up in the UT engine
        raycasting.getAllRaysInitialized().addListener(new FlagListener<Boolean>() {

            public void flagChanged(Boolean changedValue) {
                // once all rays were initialized store the AutoTraceRay objects
                // that will come in response in local variables, it is just
                // for convenience
                left = raycasting.getRay(LEFT45);
                front = raycasting.getRay(FRONT);
                right = raycasting.getRay(RIGHT45);
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
    
    

    /**
     * Main method that controls the bot.
     *
     * @throws cz.cuni.amis.pogamut.base.exceptions.PogamutException
     */
    @Override
    public void logic() throws PogamutException {
        // mark that another logic iteration has began
        log.info("--- Logic iteration ---");
        
        if (players.canSeeEnemies()){
        	this.stateEngage();
        	return;
        }
        else {
        	this.move();
        	return;
        }
    }
    
    
    protected void stateEngage(){
        log.info("Decision is: ENGAGE");
        
     // 1) pick new enemy if the old one has been lost
        if (enemy == null || !enemy.isVisible()) {
            // pick new enemy
            enemy = players.getNearestVisiblePlayer(players.getVisibleEnemies().values());
            if (enemy == null) {
                log.info("Can't see any enemies... ???");
                return;
            }
        }

        // 2) stop shooting if enemy is not visible
        if (!enemy.isVisible()) {
	        if (info.isShooting() || info.isSecondaryShooting()) {
                // stop shooting
                getAct().act(new StopShooting());
            }
        } else {
        	// 2) or shoot on enemy if it is visible
	        if (shoot.shoot(weaponPrefs, enemy) != null) 
	            log.info("Shooting at enemy!!!");
	        
        }

    }
    
    
    protected void move() {

        // if the rays are not initialized yet, do nothing and wait for their initialization 
        if (!raycasting.getAllRaysInitialized().getFlag()) {
            log.info("Aqui");

        	return;
        }

        // once the rays are up and running, move according to them

        sensorFront = front.isResult();
        sensorLeft45 = left.isResult();
        sensorRight45 = right.isResult();

        // is any of the sensor signalig?
        sensor = sensorFront || sensorLeft45 || sensorRight45;

        if (!sensor) {
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
        // 8 cases that might happen follows
        if (sensorFront) {
            if (sensorLeft45) {
                if (sensorRight45) {
                    // LEFT45, RIGHT45, FRONT are signaling
                    move.turnHorizontal(bigTurn);
                } else {
                    // LEFT45, FRONT45 are signaling
                    move.turnHorizontal(smallTurn);
                }
            } else {
                if (sensorRight45) {
                    // RIGHT45, FRONT are signaling
                    move.turnHorizontal(-smallTurn);
                } else {
                    // FRONT is signaling
                    move.turnHorizontal(smallTurn);
                }
            }
        } else {
            if (sensorLeft45) {
                if (sensorRight45) {
                    // LEFT45, RIGHT45 are signaling
                    goForward();
                } else {
                    // LEFT45 is signaling
                    move.turnHorizontal(smallTurn);
                }
            } else {
                if (sensorRight45) {
                    // RIGHT45 is signaling
                    move.turnHorizontal(-smallTurn);
                } else {
                    // no sensor is signaling
                    goForward();
                }
            }
        }

        // HOMEWORK FOR YOU GUYS:
        // Try to utilize LEFT90 and RIGHT90 sensors and implement wall-following behavior!

    }

    /**
     * Simple method that starts continuous movement forward + marking the
     * situation (i.e., setting {@link RaycastingBot#moving} to true, which
     * might be utilized later by the logic).
     */
    protected void goForward() {
    	log.info("AQUI");
        move.moveContinuos();
        moving = true;
    }

    

    ///////////////////////////////////
    public static void main(String args[]) throws PogamutException {
        // starts 10 Hunters at once
        // note that this is the most easy way to get a bunch of (the same) bots running at the same time
    	// Bots divided into 2 teams
    	new UT2004BotRunner(ReactiveBot.class, "Reactive").setMain(true).startAgents
    	(new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	
    	new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),

    	new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	
      	new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
      	new CustomBotParameters().setTeam(0).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	
    	new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	
    	new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))),
    	new CustomBotParameters().setTeam(1).setSkillLevel(5).setAgentId(new AgentId ("Reactive - " + (++instanceCount))));
    }
}
