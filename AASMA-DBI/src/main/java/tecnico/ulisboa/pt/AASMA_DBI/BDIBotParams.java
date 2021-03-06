package tecnico.ulisboa.pt.AASMA_DBI;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;

/**
 * Custom parameters for CTF bots that sets TEAM / SKIN /SKILL LEVEL
 * 
 * @author Jimmy
 */
public class BDIBotParams extends UT2004BotParameters {

	/**
	 * This will represent bot skin to be used during initialization, i.e., {@link Initialize#setSkin(String)}.
	 */
	private String botSkin;
	
	/**
	 * This will represent bot skill level that is to be used during initialization, i.e., {@link Initialize#setDesiredSkill(Integer)}.
	 * <p><p>
	 * Notice that by setting some default values, you may provide "defaults" for your custom params.
	 */
	private int skillLevel = 4;

	/**
	 * This returns the skin of the bot to be used.
	 * @return
	 */
	public String getBotSkin() {
		return botSkin;
	}

	/**
	 * Sets the skin to be used for the bot.
	 * @param botSkin
	 * @return
	 */
	public BDIBotParams setBotSkin(String botSkin) {
		this.botSkin = botSkin;
		return this;
	}

	/**
	 * This returns the desired skill level of the bot to be used.
	 * @return
	 */
	public int getSkillLevel() {
		return skillLevel;
	}

	/**
	 * Sets desired skill level of the bot. 
	 * @param skillLevel
	 * @return
	 */
	public BDIBotParams setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
		return this;
	}
	
	/**
	 * Sets the Bot team. Notice the return value - we will return the very same object so you will be able to chain your setters,
	 * see {@link BotWithParams#main(String[])}.
	 * @param rotating
	 * @return
	 */
	public BDIBotParams setTeam(int team) 
	{
		super.setTeam(team);
		return this;
	}
	
}
