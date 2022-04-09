package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.*;

public class ManDialogue extends DynamicDialogueBuilder {

    int npcId;

    public ManDialogue(int npcId) {
        this.npcId = npcId;
    }

    @Override
    public void build(Player player) {
        add(new NpcDialogue(0, this.npcId, "Hey, how are you enjoying your time in " + GameConstants.NAME + "?"));
        add(new PlayerDialogue(1, "Okay, thanks."));
    }
}
