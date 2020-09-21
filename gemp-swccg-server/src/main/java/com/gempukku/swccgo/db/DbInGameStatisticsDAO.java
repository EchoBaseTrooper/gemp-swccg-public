package com.gempukku.swccgo.db;

import com.gempukku.swccgo.db.vo.GameHistoryEntry;
import com.gempukku.swccgo.game.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DbInGameStatisticsDAO implements InGameStatisticsDAO {
    private DbAccess _dbAccess;

    public DbInGameStatisticsDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    public void addPileCounts(int gameId, int sequence, int turnNumber, String side, int darkHand, int darkReserveDeck, int darkForcePile, int darkUsedPile, int darkLostPile, int darkOutOfPlay, int lightHand, int lightReserveDeck, int lightForcePile, int lightUsedPile, int lightLostPile, int lightOutOfPlay) {
        try {
            Connection connection = _dbAccess.getDataSource().getConnection();
            try {
            	//delete rows if necessary to handle reverts that cross over turns
            	PreparedStatement idsToDeleteFromRevertStatement = connection.prepareStatement("select id from pile_count_by_turn where gameId = ? and sequence >= ? and activeGame = 1");
            	PreparedStatement deleteStatement = connection.prepareStatement("delete from pile_count_by_turn where id = ? and gameId = ? and sequence >= ? and activeGame = 1");
            	try {
            		idsToDeleteFromRevertStatement.setInt(1, gameId);
            		idsToDeleteFromRevertStatement.setInt(2, sequence);
            		
            		ResultSet rs = idsToDeleteFromRevertStatement.executeQuery();
            		while(rs.next()) {
            			int idToDelete = rs.getInt("id");
            			deleteStatement.setInt(1, idToDelete);
                    	deleteStatement.setInt(2, gameId);
                    	deleteStatement.setInt(3, sequence);
                    	
                    	deleteStatement.execute();

            		}
            	} finally {
            		idsToDeleteFromRevertStatement.close();
            		deleteStatement.close();
            	}
            	
            	PreparedStatement statement = connection.prepareStatement("insert into pile_count_by_turn (gameId, activeGame, sequence, turnNumber, side, darkHand, darkReserveDeck, darkForcePile, darkUsedPile, darkLostPile, darkOutOfPlay, lightHand, lightReserveDeck, lightForcePile, lightUsedPile, lightLostPile, lightOutOfPlay) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                try {
                	
                	statement.setInt(1,gameId); 
                	statement.setInt(2,1);
                	statement.setInt(3,sequence);
                	statement.setInt(4,turnNumber);
                	statement.setString(5,side);
                	statement.setInt(6,darkHand); 
                	statement.setInt(7,darkReserveDeck);
                	statement.setInt(8,darkForcePile); 
                	statement.setInt(9,darkUsedPile);
                	statement.setInt(10,darkLostPile);
                	statement.setInt(11,darkOutOfPlay);
                	statement.setInt(12,lightHand); 
                	statement.setInt(13,lightReserveDeck); 
                	statement.setInt(14,lightForcePile);
                	statement.setInt(15,lightUsedPile);
                	statement.setInt(16,lightLostPile);
                	statement.setInt(17,lightOutOfPlay);
  

                    statement.execute();
                } finally {
                	deleteStatement.close();
                    statement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to write pile counts to database", exp);
        }
    }

    public void updatePileCountsEndOfGame(int gameId, int updatedGameID) {
    	try {
    		Connection connection = _dbAccess.getDataSource().getConnection();
    		try {
    			//get a ResultSet with the entries from this game to update them
    			PreparedStatement entriesToUpdate = connection.prepareStatement("select id from pile_count_by_turn where gameId = ? and activeGame = 1");
    			PreparedStatement updateGameIdStatement = connection.prepareStatement("update pile_count_by_turn set gameId = ?,activeGame=0 where id = ? and gameId = ? and activeGame = 1");

    			entriesToUpdate.setInt(1, gameId);
    			
    			ResultSet rs = entriesToUpdate.executeQuery();
    			try {
    				while(rs.next()) {
    					int entryId = rs.getInt("id");
    					
    					updateGameIdStatement.setInt(1, updatedGameID);
    					updateGameIdStatement.setInt(2, entryId);
    					updateGameIdStatement.setInt(3, gameId);

    					updateGameIdStatement.execute();
    	    				
    				}
    			} finally {
    				rs.close();
    				entriesToUpdate.close();
    				updateGameIdStatement.close();
    			}
    		} finally {
    			connection.close();
    		}
    	} catch (SQLException exp) {
    		throw new RuntimeException("Unable to update pile counts in database at end of game", exp);
    	}
    }


	@Override
	public int findGameIDinGameHistory(String winner, String dark, String light) {
		int result = -1;
	   	try {
    		Connection connection = _dbAccess.getDataSource().getConnection();
    		try {
    			//TODO make sure this is always after game_history is updated
    			PreparedStatement checkHistoryWithWinner = connection.prepareStatement("select id from game_history where winner = ? and loser = ? order by end_date desc limit 1");
    			PreparedStatement checkHistoryWithUnknownWinner = connection.prepareStatement("select id from game_history where ? in (winner,loser) and ? in (winner,loser) order by end_date desc limit 1");

    			ResultSet rs = null;
    			try {
    				if(winner==null) {
    					checkHistoryWithUnknownWinner.setString(1, dark);
    					checkHistoryWithUnknownWinner.setString(2, light);

    					rs = checkHistoryWithUnknownWinner.executeQuery();
    				} else {
    					String loser = winner.equals(dark)?light:dark;

    					checkHistoryWithWinner.setString(1, winner);
    					checkHistoryWithWinner.setString(2, loser);

    					rs = checkHistoryWithWinner.executeQuery();
    				}

    				//get the id for this game in game_history
    				while(rs.next()) {
    					result = rs.getInt("id");
    				}

    			} finally {
    				checkHistoryWithWinner.close();
    				checkHistoryWithUnknownWinner.close();
    				if(rs!=null)
    					rs.close();
    			}

    		} finally {
    			connection.close();
    		}
	   	} catch (SQLException exp) {
	   		throw new RuntimeException("Unable to find id from database for pile count update", exp);
	   	}	
	   	return result;
	}

    
    public List<GameHistoryEntry> getPileCountByTurn() {
        try {
            Connection connection = _dbAccess.getDataSource().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("select winner, loser, win_reason, lose_reason, win_recording_id, lose_recording_id, format_name, tournament, winner_deck_name, loser_deck_name, start_date, end_date from game_history where winner=? or loser=? order by end_date desc limit ?, ?");
//                try {
//                    statement.setString(1, player.getName());
//                    statement.setString(2, player.getName());
//                    statement.setInt(3, start);
//                    statement.setInt(4, count);
//                    ResultSet rs = statement.executeQuery();
//                    try {
//                        List<GameHistoryEntry> result = new LinkedList<GameHistoryEntry>();
//                        while (rs.next()) {
//                            String winner = rs.getString(1);
//                            String loser = rs.getString(2);
//                            String winReason = rs.getString(3);
//                            String loseReason = rs.getString(4);
//                            String winRecordingId = rs.getString(5);
//                            String loseRecordingId = rs.getString(6);
//                            String formatName = rs.getString(7);
//                            String tournament = rs.getString(8);
//                            String winnerDeckName = rs.getString(9);
//                            String loserDeckName = rs.getString(10);
//                            Date startDate = new Date(rs.getLong(11));
//                            Date endDate = new Date(rs.getLong(12));
//
//                            GameHistoryEntry entry = new GameHistoryEntry(winner, winReason, winRecordingId, loser, loseReason, loseRecordingId, formatName, tournament, winnerDeckName, loserDeckName, startDate, endDate);
//                            result.add(entry);
//                        }
//                        return result;
//                    } finally {
//                        rs.close();
//                    }
//                } finally {
//                    statement.close();
//                }
            } finally {
                connection.close();
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get pile counts from database", exp);
        }
        return null;
    }

	@Override
	public void updateActivationCounts(int gameId, int sequence, int darkActivation, int lightActivation) {
		try {
			Connection connection = _dbAccess.getDataSource().getConnection();
			try {
				PreparedStatement getIdStatement = connection.prepareStatement("select id from pile_count_by_turn where gameId = ? and sequence = ? and activeGame = 1 and side in ('Dark','Light')");
				PreparedStatement updateActivationStatement = connection.prepareStatement("update pile_count_by_turn set darkActivation = ?, lightActivation = ? where id = ? and gameId = ? and sequence = ? and activeGame = 1");
				try { 
					getIdStatement.setInt(1, gameId);
					getIdStatement.setInt(2, sequence);

					ResultSet rs = getIdStatement.executeQuery();

					while(rs.next()) {
						int idToUpdate = rs.getInt("id");

						updateActivationStatement.setInt(1, darkActivation);
						updateActivationStatement.setInt(2, lightActivation);
						updateActivationStatement.setInt(3, idToUpdate);
						updateActivationStatement.setInt(4, gameId);
						updateActivationStatement.setInt(5, sequence);

						updateActivationStatement.execute();
					}

				} finally {
					updateActivationStatement.close();
				}
			} finally {
				connection.close();
			} 
		} catch (SQLException exp) {
			throw new RuntimeException("Unable to write activation totals to database", exp);
		}
	}

}
