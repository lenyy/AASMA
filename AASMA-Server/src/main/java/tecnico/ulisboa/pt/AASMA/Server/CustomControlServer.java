package tecnico.ulisboa.pt.AASMA.Server;

import java.util.Collection;

import com.google.inject.Inject;

import cz.cuni.amis.pogamut.base.agent.impl.AgentId;
import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnection;
import cz.cuni.amis.pogamut.base.communication.connection.impl.socket.SocketConnectionAddress;
import cz.cuni.amis.pogamut.base.communication.worldview.event.IWorldEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectEvent;
import cz.cuni.amis.pogamut.base.communication.worldview.object.IWorldObjectListener;
import cz.cuni.amis.pogamut.base.component.bus.IComponentBus;
import cz.cuni.amis.pogamut.base.utils.logging.IAgentLogger;
import cz.cuni.amis.pogamut.ut2004.agent.params.UT2004AgentParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BeginMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.MapList;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.TeamScore;
import cz.cuni.amis.pogamut.ut2004.communication.worldview.UT2004WorldView;
import cz.cuni.amis.pogamut.ut2004.factory.guice.remoteagent.UT2004ServerFactory;
import cz.cuni.amis.pogamut.ut2004.server.IUT2004Server;
import cz.cuni.amis.pogamut.ut2004.server.impl.UT2004Server;
import cz.cuni.amis.utils.exception.PogamutException;

/**
 * Control server connected to UT environment. Through this connection we get global
 * information about the game and can control the game.
 *
 * @author Michal Bida
 */
public class CustomControlServer extends UT2004Server implements IUT2004Server {

    private double currentUTTime;
    
    // MAPAS Disponiveis para o Servidor de Capture the Flag
    
    private String magma = "CTF-Magma";
    
    private String Joust = "CTF-1on1-Joust";
    
    private String absoluteZero = "CTF-AbsoluteZero";
    
    private String avaris = "CTF-Avaris";
    
    private String bridgeOfFate = "CTF-BridgeOfFate";
    
    private String chrome = "CTF-Chrome";
    
    private String citadel = "CTF-Citadel";
    
    private String colossus = "CTF-Colossus";
    
    private String deElecFields = "CTF-DE-ElecFields";
    
    private String december = "CTF-December";
    
    private String doubleDammage = "CTF-DoubleDammage";
    
    private String face = "CTF-Face3";
    
    private String faceClassic = "CTF-FaceClassic";
    
    private String geothermal = "CTF-Geothermal";
    
    private String grassyknoll = "CTF-Grassyknoll";
    
    private String grendelkeep = "CTF-Grendekeep";
    
    private String january = "CTF-January";
    
    private String lostfaith = "CTF-Lostfaith";
    
    private String maul = "CTF-Maul";
    
    private String moonDragon = "CTF-MoonDragon";
    
    private String orbital = "CTF-Orbital2";
    
    private String smote = "CTF-Smote";
    
    private String twinTombs = "CTF-TwinTombs";
    
    
    

    /*
     * BeginMessage listener - we get current server time here.
     */
    IWorldEventListener<BeginMessage> myBeginMessageListener = new IWorldEventListener<BeginMessage>() {
        public void notify(BeginMessage event) {
            currentUTTime = event.getTime();
            System.out.println("Begin: " + event.toString());
        }
    };

    /*
     * Player listener - we simply print out all player messages we receive.
     */
    IWorldObjectListener<Player> myPlayerListener = new IWorldObjectListener<Player>() {
        public void notify(IWorldObjectEvent<Player> event) {
            System.out.println("Player: " + event.getObject().toString());
        }
    };
    
    /*
     * TeamScore listener - we simply print out all TeamScores
     */
    IWorldObjectListener<TeamScore> teamScoreListener = new IWorldObjectListener<TeamScore>() {
        public void notify(IWorldObjectEvent<TeamScore> event) {
            System.out.println("TeamScore: " + event.getObject().toString());
        }
    };

    @Inject
    public CustomControlServer(UT2004AgentParameters params, IAgentLogger agentLogger, IComponentBus bus, SocketConnection connection, UT2004WorldView worldView, IAct act) {
        super(params, agentLogger, bus, connection, worldView, act);
    }


    /**
     * This method is called whenever the server is fully initialized and connected to UT2004.
     * 
     * Listeners attached here are removed within {@link CustomControlServer#reset()}.
     */
    @Override
    protected void init() {
    	super.init();
        getWorldView().addEventListener(BeginMessage.class, myBeginMessageListener);
        getWorldView().addObjectListener(Player.class, myPlayerListener);
        getWorldView().addObjectListener(TeamScore.class, teamScoreListener);
        log.info("ControlConnection initialized.");
    }
    
    /**
     * Clean-up methods.
     */
    @Override
    protected void reset() {
    	getWorldView().removeEventListener(BeginMessage.class, myBeginMessageListener);
        getWorldView().removeObjectListener(Player.class, myPlayerListener);
        getWorldView().removeObjectListener(TeamScore.class, teamScoreListener);
    	super.reset();
    }

    /**
     * This method is called when the server is started either from IDE or from command line.
     * It connects the server to the game.
     * @param args
     */
    public static void main(String args[]) throws PogamutException {
        //creating agent parameters - setting module name and connection adress
        UT2004AgentParameters params = new UT2004AgentParameters();
        params.setAgentId(new AgentId("ControlConnection"));
        params.setWorldAddress(new SocketConnectionAddress("127.0.0.1", 3001));

        //create module that tells guice it should instantiate OUR (this) class
        CustomControlServerModule module = new CustomControlServerModule();
        
        //creating pogamut factory
        UT2004ServerFactory fac = new UT2004ServerFactory(module);
        CustomControlServer cts = (CustomControlServer) fac.newAgent(params);
      
        //starting the connection - connecting to the server
        cts.start();
        
        cts.init();
        
        cts.setGameMap(cts.Joust);
     
        
    }
}