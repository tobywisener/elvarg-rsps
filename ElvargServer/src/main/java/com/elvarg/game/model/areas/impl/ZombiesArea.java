package com.elvarg.game.model.areas.impl;

import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.content.minigames.Zombies;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;

import java.util.Arrays;
import java.util.Optional;

public class ZombiesArea extends Area {

    public ZombiesArea() {
        super(Arrays.asList(new Boundary(2179, 2236, 4808, 4868)));
    }

    @Override
    public void enter(Mobile character) {
        if(character.isPlayer()) {
            Player player = character.getAsPlayer();
            Zombies.ACTIVE_PLAYERS.add(player);
            if (!Zombies.GameActive) {
                Zombies.startGame(player);
            }
        }
    }

    @Override
    public void leave(Mobile character, boolean logout) {
        if(character.isPlayer()) {
            Player player = character.getAsPlayer();
            Zombies.ACTIVE_PLAYERS.remove(player);
            if (Zombies.ACTIVE_PLAYERS.size() == 0) {
                Zombies.endGame(player);
            }
        }
    }

    @Override
    public void process(Mobile character) {
        if(character.isPlayer()) {
            Zombies.handleSeekAndDestroy(character.getAsPlayer());
        }
    }

    @Override
    public boolean canTeleport(Player player) {
        return false;
    }

    @Override
    public boolean canAttack(Mobile attacker, Mobile target) {
        if (attacker.isPlayer() && target.isPlayer()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean canTrade(Player player, Player target) {
        return true;
    }

    @Override
    public boolean isMulti(Mobile character) {
        return true;
    }

    @Override
    public boolean canEat(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean canDrink(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        return false;
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        return false;
    }

    @Override
    public void onPlayerRightClick(Player player, Player rightClicked, int option) {
    }

    @Override
    public void defeated(Player player, Mobile character) {
        if (character.isNpc()) {
            Zombies.handleKilledNPC(player, character.getAsNpc());
        }
    }

    @Override
    public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
        return true;
    }

    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {
        return false;
    }
}

