package net.runelite.client.plugins.crowdsourcing.rooftop;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RooftopData
{
	private final String name;
	private final int objectId;
    private final int level;
	private final boolean success;
}