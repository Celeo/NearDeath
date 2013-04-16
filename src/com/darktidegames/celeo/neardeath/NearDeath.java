package com.darktidegames.celeo.neardeath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <b>NearDeath</b>
 * 
 * @author Celeo
 */
public class NearDeath extends JavaPlugin implements Listener
{

	/** Health level the player has to be at or below to receive potion effects */
	private int threshold = 10;
	/** Duration of effects that are not instant */
	private int duration = 15;
	/** All possible effects we can pull from to add to the player */
	private List<PotionEffectType> effects = new ArrayList<PotionEffectType>();

	@Override
	public void onLoad()
	{
		getDataFolder().mkdirs();
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
	}

	@Override
	public void onEnable()
	{
		load();
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	private void load()
	{
		reloadConfig();
		threshold = getConfig().getInt("threshold", 10);
		duration = getConfig().getInt("duration", 15);
		String p = "";
		// check all potion effects for their status in the config
		for (PotionEffectType effect : PotionEffectType.values())
		{
			if (effect == null)
				continue;
			p = "effects." + effect.getName().toLowerCase();
			// add to the list of possible effects if it is the config and set
			// to true
			if (getConfig().isSet(p) && getConfig().getBoolean(p))
				effects.add(effect);
		}
		getLogger().info("Settings loaded from configuration file");
	}

	@Override
	public void onDisable()
	{
		getLogger().info("Disabled");
	}

	/**
	 * Allow an in-game command to reload the settings from the configuration
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (sender instanceof Player)
		{
			Player player = (Player) sender;
			// server OP or meeting the permission node is suitable
			if (player.isOp() || player.hasPermission("neardeath.reload"))
			{
				load();
				player.sendMessage("§aSettings loaded from configuration file");
			}
			else
				player.sendMessage("§cYou cannot use that command.");
		}
		else
			load();
		return true;
	}

	/**
	 * Checks if the player being hurt has health less than the threshold for
	 * adding effects. If (s)he does, then a random effect from the enabled
	 * effects list is added to the player for the duration setting (if
	 * applicable).
	 * 
	 * @param event
	 *            EntityDamageEvent
	 */
	@EventHandler
	public void onPlayerTakeDamage(EntityDamageEvent event)
	{
		// only looking for players, not other mobs
		if (!(event.getEntity() instanceof Player))
			return;
		Player hurt = (Player) event.getEntity();
		if (hurt.getHealth() <= threshold)
		{
			// get a random effecttype from the list of possibilities
			PotionEffectType add = effects.get(new Random().nextInt(effects.size()));
			// create the potion effect object
			PotionEffect fin = add.createEffect(duration * 20
					* (int) (1 / add.getDurationModifier()), 1);
			// add it to the player
			hurt.addPotionEffect(fin);
			hurt.sendMessage("Potion effect " + add.getName().toLowerCase() + " added");
		}
	}
}