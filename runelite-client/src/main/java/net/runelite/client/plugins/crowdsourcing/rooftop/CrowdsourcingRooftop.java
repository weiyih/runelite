package net.runelite.client.plugins.crowdsourcing.rooftop;

import com.google.common.collect.ImmutableSet;
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
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingRooftop
{
	private String eventType;
	private int eventObject;
	private int eventLevel;

	@Getter(AccessLevel.PACKAGE)

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private static Set<Integer> ROOFTOP_FALL_ANIMS = ImmutableSet.of(AnimationID.FALL_TIGHTROPE, AnimationID.FALL_GAP, AnimationID.FALL_KNEEL, AnimationID.FALL_FLAIL);

	private static Set<Integer> ROOFTOP_OBSTACLES = ImmutableSet.of(
			ObjectID.TIGHTROPE, ObjectID.NARROW_WALL,
			ObjectID.TIGHTROPE_14398, ObjectID.ZIP_LINE_14403,
			ObjectID.CLOTHES_LINE, ObjectID.WALL_14832,
			ObjectID.GAP_14848, ObjectID.GAP_14847,
			ObjectID.HAND_HOLDS_14901, ObjectID.TIGHTROPE_14905,
			ObjectID.GAP_14928, ObjectID.TIGHTROPE_14932,
			ObjectID.MARKET_STALL_14936, ObjectID.TIGHTROPE_14987,
			ObjectID.TIGHTROPE_14987, ObjectID.GAP_14990,
			ObjectID.GAP_15610, ObjectID.GAP_15612
	);

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
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (ROOFTOP_OBSTACLES.contains(event.getId()))
		{
			int level = client.getBoostedSkillLevel(Skill.AGILITY);
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
				case ObjectID.GAP_15610:
				case ObjectID.GAP_15612:
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
			RooftopData data = new RooftopData(eventType, eventObject, eventLevel, true);
			log.debug("Success: {}", data);
			manager.storeEvent(data);
			resetEvent();
		}
	}


	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		final int animId = actor.getAnimation();
		if (ROOFTOP_FALL_ANIMS.contains(animId) && eventType != null)
		{
			RooftopData data = new RooftopData(eventType, eventObject, eventLevel, false);
			manager.storeEvent(data);
			log.debug("Failed: {}", data);
			resetEvent();
		}
	}
}
