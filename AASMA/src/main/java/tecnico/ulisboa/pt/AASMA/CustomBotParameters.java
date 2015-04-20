package tecnico.ulisboa.pt.AASMA;

import cz.cuni.amis.pogamut.ut2004.bot.params.UT2004BotParameters;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;

/**
 * This class defines additional parameters you want your bot to have. Params such
 * "custom name", "custom skin", "aggressiveness", "weapon preferences", "bot skill level", "team", etc. that you might need
 * to customize for your bot.
 * <p><p>
 * There are three things you should do when defining params.
 * <ol>
 * <li>define private fields that will hold your parameters' values</li>
 * <li>define getters for your params</li>
 * <li>define setters for your params</li>
 * </ol>
 * 
 * @author Jimmy
 *
 */
public class CustomBotParameters extends UT2004BotParameters {

	/**
	 * This will represent bot skin to be used during initialization, i.e., {@link Initialize#setSkin(String)}.
	 */
	private String botSkin;
	
	/**
	 * This will represent bot's name that is to be used during initialization, i.e., {@link Initialize#setName(String)}.
	 */
	private String name;
	
	/**
	 * This will represent bot skill level that is to be used during initialization, i.e., {@link Initialize#setDesiredSkill(Integer)}.
	 * <p><p>
	 * Notice that by setting some default values, you may provide "defaults" for your custom params.
	 */
	private int skillLevel = 4;
	
	/**
	 * This will tell the bot whether it should be jumping.
	 * <p><p>
	 * Notice that by setting some default values, you may provide "defaults" for your custom params.
	 */
	private boolean jumping = false;
	
	/**
	 * This will tell the bot whether it should be rotating.
	 * <p><p>
	 * Notice that by setting some default values, you may provide "defaults" for your custom params.
	 */
	private boolean rotating = false;

	/**
	 * This returns the skin of the bot to be used.
	 * @return
	 */
	public String getBotSkin() {
		return botSkin;
	}

	/**
	 * Sets the skin to be used for the bot. Notice the return value - we will return the very same object so you will be able to chain your setters,
	 * see {@link BotWithParams#main(String[])}.
	 * @param botSkin
	 * @return
	 */
	public CustomBotParameters setBotSkin(String botSkin) {
		this.botSkin = botSkin;
		return this;
	}

	/**
	 * This returns the name of the bot to be used.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name for the bot. Notice the return value - we will return the very same object so you will be able to chain your setters,
	 * see {@link BotWithParams#main(String[])}.
	 * @param name
	 * @return
	 */
	public CustomBotParameters setName(String name) {
		this.name = name;
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
	 * Sets desired skill level of the bot. Notice the return value - we will return the very same object so you will be able to chain your setters,
	 * see {@link BotWithParams#main(String[])}.
	 * @param skillLevel
	 * @return
	 */
	public CustomBotParameters setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
		return this;
	}
	
	
	/**
	 * Sets the Bot team. Notice the return value - we will return the very same object so you will be able to chain your setters,
	 * see {@link BotWithParams#main(String[])}.
	 * @param rotating
	 * @return
	 */
	public CustomBotParameters setTeam(int team) 
	{
		super.setTeam(team);
		return this;
	}
	
}
