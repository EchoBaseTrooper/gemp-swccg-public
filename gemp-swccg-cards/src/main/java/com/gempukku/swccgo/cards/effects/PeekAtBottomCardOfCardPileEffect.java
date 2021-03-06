package com.gempukku.swccgo.cards.effects;

import com.gempukku.swccgo.common.Zone;
import com.gempukku.swccgo.logic.timing.Action;

/**
 * An effect for peeking at the bottom card of a specified card pile.
 */
public class PeekAtBottomCardOfCardPileEffect extends PeekAtBottomCardsOfCardPileEffect {

    /**
     * Creates an effect for peeking at the bottom card of a specified card pile.
     *
     * @param action        the action performing this effect
     * @param playerId      the player to peek at card
     * @param cardPileOwner the owner of the card pile
     * @param cardPile      the card pile
     */
    public PeekAtBottomCardOfCardPileEffect(Action action, String playerId, String cardPileOwner, Zone cardPile) {
        super(action, playerId, cardPileOwner, cardPile, 1);
    }
}
