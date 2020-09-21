
package com.gempukku.swccgo.communication;

import com.gempukku.swccgo.game.state.GameState;

public interface InGameStatisticsListener {
	public void writePileCounts(final GameState gameState,boolean gameComplete);
	public void writeActivationTotals(final GameState gameState);
}