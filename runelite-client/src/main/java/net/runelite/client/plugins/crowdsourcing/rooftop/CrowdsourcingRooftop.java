package net.runelite.client.plugins.crowdsourcing.rooftop;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import javax.inject.Inject;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingRooftop
{
	private String eventType;
	private int eventObject = -1;
	private int eventLevel = -1;
	private int previousXP = -1;
	private boolean ANIMATING = false;

	@Getter(AccessLevel.PACKAGE)

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private static Set<Integer> ROOFTOP_FALL_ANIMS = ImmutableSet.of(AnimationID.FALL_TIGHTROPE, AnimationID.FALL_GAP, AnimationID.FALL_KNEEL, AnimationID.FALL_FLAIL);

	private static Map<Integer, Integer> ROOFTOP_OBSTACLES = new ImmutableMap.Builder<Integer, Integer>().
		put(ObjectID.TIGHTROPE, 8).
		put(ObjectID.NARROW_WALL, 7). //Tightrope 2 shares same xp

		put(ObjectID.TIGHTROPE_14398, 30).
		put(ObjectID.ZIP_LINE_14403, 40). //shares with cable

		put(ObjectID.CLOTHES_LINE, 21).
		put(ObjectID.WALL_14832, 25).

		put(ObjectID.GAP_14848, 10). //shres with pole-vault
		put(ObjectID.GAP_14847, 11).

		put(ObjectID.HAND_HOLDS_14901, 45).
		put(ObjectID.TIGHTROPE_14905, 45). //shared with hand_hold

		put(ObjectID.GAP_14928, 20).
		put(ObjectID.TIGHTROPE_14932, 20). //shared but should be triggerable since different level

		put(ObjectID.MARKET_STALL_14936, 45).
		put(ObjectID.BANNER_14937, 65).
		put(ObjectID.TIGHTROPE_14987, 40).
		put(ObjectID.GAP_14990, 85).
		put(ObjectID.GAP_15609, 65).
		put(ObjectID.GAP_15610, 21).
		build();


//	private static Map<Integer, Integer> ROOFTOP_OBSTACLE_XP = new ImmutableMap.Builder<Integer, Integer>().
//		put(ObjectID.MARKET_STALL_14936, 45).
//		put(ObjectID.BANNER_14937, 65).
//		build();
//	)

	private void setEvent(String event, int object, int level)
	{
		this.eventType = event;
		this.eventObject = object;
		this.eventLevel = level;
	}

	private void resetEvent()
	{
		eventType = null;
		eventObject = -1;
		eventLevel = -1;
		ANIMATING = false;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (ROOFTOP_OBSTACLES.containsKey(event.getId()) && !ANIMATING)
		log.debug("Client {}", client);
		{
			int level = client.getBoostedSkillLevel(Skill.AGILITY);
			previousXP = client.getSkillExperience(Skill.AGILITY);
			switch (event.getId())
			{
				case ObjectID.TIGHTROPE:
				case ObjectID.NARROW_WALL:
					setEvent("Draynor Village", event.getId(), level);
					break;
				case ObjectID.TIGHTROPE_14398:
				case ObjectID.ZIP_LINE_14403:
					setEvent("Al Kharid", event.getId(), level);
					break;
				case ObjectID.CLOTHES_LINE:
				case ObjectID.WALL_14832:
					setEvent("Varrock", event.getId(), level);
					break;
				case ObjectID.GAP_14848:
				case ObjectID.GAP_14847:
					setEvent("Canifis", event.getId(), level);
					break;
				case ObjectID.HAND_HOLDS_14901:
				case ObjectID.TIGHTROPE_14905:
					setEvent("Falador", event.getId(), level);
					break;
				case ObjectID.GAP_14928:
				case ObjectID.TIGHTROPE_14932:
					setEvent("Seers' Village", event.getId(), level);
					break;
				case ObjectID.MARKET_STALL_14936:
				case ObjectID.BANNER_14937:
					setEvent("Pollnivneach", event.getId(), level);
					break;
				case ObjectID.TIGHTROPE_14987:
				case ObjectID.GAP_14990:
					setEvent("Relleka", event.getId(), level);
					break;
				case ObjectID.GAP_15609:
				case ObjectID.GAP_15610:
					setEvent("Ardougne", event.getId(), level);
					break;
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (statChanged.getSkill() == Skill.AGILITY && eventType != null)
		{
//			Check XP
			int gainedXP = statChanged.getXp() - previousXP;
			if (ROOFTOP_OBSTACLES.get(eventObject) == gainedXP)
			{
				RooftopData data = new RooftopData(eventType, eventObject, eventLevel, true);
				log.debug("Success: {}", data);
				//			manager.storeEvent(data);
				previousXP = statChanged.getXp();
			}
			resetEvent();
		}
	}

	@Subscribe
	private void onChatMessage(ChatMessage message)
	{
		if (message.equals("I can't reach that!"))
		{
			resetEvent();
		}
	}
//I can't reach that! clear event

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		final int animId = actor.getAnimation();

		if (animId == 762)
		{
			ANIMATING = true;
		}

		if (ROOFTOP_FALL_ANIMS.contains(animId) && eventType != null)
		{
			RooftopData data = new RooftopData(eventType, eventObject, eventLevel, false);
//			manager.storeEvent(data);
			log.debug("Failed: {}", data);
			resetEvent();
		}
	}
}
