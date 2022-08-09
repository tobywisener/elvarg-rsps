package com.elvarg.game.entity.impl.player.persistence;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.util.PasswordUtil;

public abstract class PlayerPersistence {

    public abstract PlayerSave load(String username);

    public abstract void save(Player player);

    public abstract boolean exists(String username);

    public String encryptPassword(String plainPassword) {
        return PasswordUtil.generatePasswordHashWithSalt(plainPassword);
    }

    public boolean checkPassword(String plainPassword, PlayerSave playerSave) {
        String passwordHashWithSalt = playerSave.getPasswordHashWithSalt();
        return PasswordUtil.passwordsMatch(plainPassword, passwordHashWithSalt);
    }

}
