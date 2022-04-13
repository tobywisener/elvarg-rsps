package com.elvarg.game.content.minigames;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.ZombiesArea;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Zombies {

    public static int CurrentWave = 0;

    public static boolean GameActive = false;

    public static boolean WaveActive = false;

    public static final List<Player> ACTIVE_PLAYERS = new CopyOnWriteArrayList<>();

    public static final List<NPC> CURRENT_ZOMBIES = new CopyOnWriteArrayList<>();

    public static final Location[] ZOMBIE_SPAWN_POSITIONS = {
            new Location(2226, 4857, 0), //NE
            new Location(2185, 4857, 0), //NW
            new Location(2189, 4818, 0), //SW
            new Location(2228, 4820, 0) //SE
    };

    // The number of cycles between waves (milliseconds / cycle_time [600])
    public static final int TIME_BETWEEN_WAVES = 33 /* 33 = 20 seconds */;

    public static final int TIME_BETWEEN_SPAWNS = 4 /* 600 * 4 milliseconds*/;

    public static final int[] ZOMBIES = {
            NpcIdentifiers.ZOMBIE_LVL_13,
            NpcIdentifiers.ZOMBIE_LVL_13_2,
            NpcIdentifiers.ZOMBIE_LVL_13_3,
            NpcIdentifiers.ZOMBIE_LVL_13_4,
            NpcIdentifiers.ZOMBIE_LVL_13_5,
            NpcIdentifiers.ZOMBIE_LVL_13_6,
            NpcIdentifiers.ZOMBIE_LVL_13_7,
            NpcIdentifiers.ZOMBIE_LVL_13_8,
            NpcIdentifiers.ZOMBIE_LVL_13_9,
            NpcIdentifiers.ZOMBIE_LVL_13_10,
            NpcIdentifiers.ZOMBIE_LVL_18_1,
            NpcIdentifiers.ZOMBIE_LVL_18_2,
            NpcIdentifiers.ZOMBIE_LVL_25_1,
            NpcIdentifiers.ZOMBIE_LVL_25_2,
            NpcIdentifiers.ZOMBIE_LVL_25_3,
            NpcIdentifiers.ZOMBIE_LVL_25_4,
            NpcIdentifiers.ZOMBIE_PIRATE,
            NpcIdentifiers.ZOMBIE_PIRATE_2,
            NpcIdentifiers.ZOMBIE_PIRATE_3,
            NpcIdentifiers.ZOMBIE_PIRATE_4,
            NpcIdentifiers.ZOMBIE_PIRATE_5,
            NpcIdentifiers.ZOMBIE_PIRATE_6,
            NpcIdentifiers.ZOMBIE_PIRATE_7,
            NpcIdentifiers.ZOMBIE_PIRATE_8,
            NpcIdentifiers.ZOMBIE_PIRATE_9,
            NpcIdentifiers.ZOMBIE_PIRATE_10,
            NpcIdentifiers.ZOMBIE_PIRATE_11
    };

    public static final int TOKENS = ItemIdentifiers.ECTO_TOKEN;

    // Start a fresh game of The Basement
    public static void startGame(Player starter) {
        GameActive = true;
        WaveActive = false;
        CurrentWave = 1;

        World.sendMessage(""+starter.getUsername()+" has kicked off a game of Zombies! First wave in 20 seconds...");
        TaskManager.submit(new Task(TIME_BETWEEN_WAVES) {
            @Override
            protected void execute() {
                Zombies.startWave();
                stop();
            }
        });
    }


    // Ends the current game
    public static void endGame(Player player) {
        World.sendMessage(""+player+"'s game lasted "+CurrentWave+" waves.");

        List<NPC> actualZombiesLeft = World.getNpcs().stream()
                .filter(map -> map != null && map.getArea() instanceof ZombiesArea)
                .map(map -> map).collect(Collectors.toList());

        // Remove all current zombies from the game
        for (NPC _npc: actualZombiesLeft) {
            World.getRemoveNPCQueue().add(_npc);
            CURRENT_ZOMBIES.remove(_npc);
        }

        GameActive = false;
        WaveActive = false;
    }

    // Start the next wave
    public static void startWave() {
        if(!GameActive) {
            return;
        }

        WaveActive = true;

        // Calculate the number of zombies for the next wave
        int npcs_to_spawn = ACTIVE_PLAYERS.size() * CurrentWave;

        World.sendMessage("Wave of " + npcs_to_spawn + " zombies inbound!");

        int current_spawn_delay = TIME_BETWEEN_SPAWNS;
        for(int i = 0; i < npcs_to_spawn; i++) {
            TaskManager.submit(new Task(current_spawn_delay) {
                @Override
                protected void execute() {
                    if(!Zombies.GameActive || !Zombies.WaveActive) {
                        return;
                    }

                    Zombies.spawnZombie();
                    stop();
                }
            });

            current_spawn_delay += TIME_BETWEEN_SPAWNS;
        }
    }

    // End the current wave
    public static void endWave() {
        WaveActive = false;
        CurrentWave++;

        World.sendMessage("Zombies: Next wave ("+CurrentWave+") starting in 20 seconds!");
        TaskManager.submit(new Task(TIME_BETWEEN_WAVES) {
            @Override
            protected void execute() {
                Zombies.startWave();
                stop();
            }
        });
    }

    public static void spawnZombie() {

        // Choose a random corner for the zombies to come from
        Location spawnLocation = ZOMBIE_SPAWN_POSITIONS[new Random().nextInt(ZOMBIE_SPAWN_POSITIONS.length)];

        int zombie_id = ZOMBIES[new Random().nextInt(ZOMBIES.length)];

        NPC npc = new NPC(zombie_id, spawnLocation);
        World.getAddNPCQueue().add(npc);

        npc.getDefinition().setDoesRetreat(false); // This NPC won't run away
        npc.getDefinition().setRespawn(0); // This NPC won't respawn
        npc.getDefinition().setCombatFollowDistance(100); // This NPC will have a far reaching follow in combat
        npc.setCanUsePathFinding(true); // Allow NPC to use advanced path finding to get around obstacles

        CURRENT_ZOMBIES.add(npc);
    }

    public static void handleKilledNPC(Player killer, NPC npc) {
        CURRENT_ZOMBIES.remove(npc);

        // Check if wave is over (all current zombies dead)
        if(CURRENT_ZOMBIES.size() == 0) {
            endWave();
        }
    }

    public static void handleSeekAndDestroy(Player player) {

        for(NPC zombie: CURRENT_ZOMBIES) {

            // Switch targets randomly if multiple players
            if(CombatFactory.inCombat(zombie) && ACTIVE_PLAYERS.size() > 1) {

                // 2/10 chance of switching targets
                if(Misc.getRandom(10) <= 2) {

                    //Get a random player from the player's localPlayers list.
                    Player randomPlayer = ACTIVE_PLAYERS.get(Misc.getRandom(ACTIVE_PLAYERS.size()));
                    //Attack the new player if they're a valid target.
                    zombie.getCombat().attack(randomPlayer);
                    break; // Skip this NPC
                }
            }

            if(!CombatFactory.inCombat(zombie)){
                zombie.getCombat().attack(player);
            }
        }
    }
}
