package com.gempukku.swccgo.logic.modifiers;

import com.gempukku.swccgo.common.Filterable;
import com.gempukku.swccgo.game.PhysicalCard;
import com.gempukku.swccgo.logic.conditions.Condition;

/**
 * A modifier for not being able to have forfeit increased beyond printed value.
 */
public class MayNotHaveForfeitIncreasedAbovePrintedModifier extends AbstractModifier {


    /**
     * Creates a modifier for not being able to have forfeit increased beyond printed value.
     * @param source the source of the modifier
     * @param affectFilter the filter for cards that may not have forfeit increased above printed
     */
    public MayNotHaveForfeitIncreasedAbovePrintedModifier(PhysicalCard source, Filterable affectFilter) {
        this(source, affectFilter, null);
    }

    /**
     * Creates a modifier for not being able to have forfeit increased beyond printed value.
     * @param source the source of the modifier
     * @param affectFilter the filter for cards that may not have forfeit increased above printed
     * @param condition the condition that must be fulfilled for the modifier to be in effect
     */
    public MayNotHaveForfeitIncreasedAbovePrintedModifier(PhysicalCard source, Filterable affectFilter, Condition condition) {
        super(source, "May not have forfeit increased above printed value", affectFilter, condition, ModifierType.MAY_NOT_HAVE_FORFEIT_VALUE_INCREASED_ABOVE_PRINTED, true);
    }
}
