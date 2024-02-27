package src;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class RemotePlayerObject extends UnicastRemoteObject implements RemotePlayerInterface {

    public RemotePlayerObject() throws Exception {
        super();
    }

    public synchronized void setLeader() throws RemoteException {
        System.out.println("Node is set as leader");
        Game.isLeader = true;
    }

    public boolean isLeader() throws RemoteException {
        return Game.isLeader;
    }

    public synchronized void setBackupLeader() throws RemoteException {
        System.out.println("Node is set as backup leader");
        Game.isBackupLeader = true;
    }

    public boolean isBackupLeader() throws RemoteException {
        return Game.isBackupLeader;
    }

    public synchronized GameState makeAMove(String move, String playerId) throws RemoteException {
        if (Game.isLeader) {
            switch (move) {
                case "1":
                    Game.gameState.move(MOVE.WEST, playerId);
                    break;
                case "2":
                    Game.gameState.move(MOVE.SOUTH, playerId);
                    break;
                case "3":
                    Game.gameState.move(MOVE.EAST, playerId);
                    break;
                case "4":
                    Game.gameState.move(MOVE.NORTH, playerId);
                    break;
                case "9":
                    Game.gameState.removePlayerFromMaze(playerId);
                    break;
                default:
                    break;
            }

            for (Player player : Game.activePlayerList) {
                try {
                    if (player.remotePlayerObject.isBackupLeader()) {
                        player.remotePlayerObject.updateGameState(Game.gameState);
                        break;
                    }
                } catch (Exception innerException) {
                    innerException.printStackTrace();
                }
            }
            return Game.gameState;
        }
        return null;
    }

    public synchronized void updateGameState(GameState g) {
        Game.gameState = g;
    }

    public synchronized GameState registerPlayer(Player requestingPlayer) throws RemoteException {
        if (!requestingPlayer.getPlayerId().equals(Game.player.getPlayerId())) {
            // add the player to the active players' list and return the updated game state
            System.out.println("Registering player " + requestingPlayer.getPlayerId() + " to the game!");
            Game.activePlayerList.add(requestingPlayer);
            Game.gameState.addPlayerToMaze(requestingPlayer.getPlayerId());
            return Game.gameState;
        }

        return null;
    }

    public boolean isNodeAlive(Player player) throws RemoteException {
        try {
            if (!player.IsPlayerAlive())
                return false;
            boolean response = player.remotePlayerObject.ping();
            return response == true;
        } catch (RemoteException e) {
            // Handle exceptions (e.g., node not reachable)
            return false;
        }
    }

    public void checkHeartBeat() throws RemoteException {
        try {
            List<Player> respondingPlayers = new ArrayList<>();
            if (Game.isLeader) {
                Player backupLeader = null;

                // Ping each of the players for response. Dead players are removed from the list
                for (Player player : Game.activePlayerList) {
                    if (isNodeAlive(player)) {
                        if (player.remotePlayerObject.isBackupLeader()) {
                            backupLeader = player;
                        }
                        respondingPlayers.add(player);
                    } else {
                        Game.gameState.removePlayerFromMaze(player.getPlayerId());
                    }
                }

                // update the active player list of the backup leader and tracker
                Game.activePlayerList = respondingPlayers;
                if (backupLeader != null) {
                    backupLeader.remotePlayerObject.updateActivePlayerList(Game.activePlayerList);
                    backupLeader.remotePlayerObject.updateGameState(Game.gameState);
                } else if (Game.activePlayerList.size() > 1) {
                    for (Player player : Game.activePlayerList) {
                        if (isNodeAlive(player) && !player.getPlayerId().equals(Game.player.getPlayerId())) {
                            player.remotePlayerObject.setBackupLeader();
                            player.remotePlayerObject.updateActivePlayerList(Game.activePlayerList);
                            player.remotePlayerObject.updateGameState(Game.gameState);
                            break;
                        }
                    }
                }

                // update tracker with the latest active player list
                Game.trackerRemoteObject.updateActivePlayerList(respondingPlayers);

            } else if (Game.isBackupLeader) {
                if (!isNodeAlive(Game.activePlayerList.get(0))) {
                    System.out.println("leader is dead");
                    System.out.println("Node is set as leader");
                    Game.isBackupLeader = false;
                    Game.isLeader = true;
                    Player backupLeader = null;
                    Game.gameState.removePlayerFromMaze(Game.activePlayerList.get(0).getPlayerId()); // remove leader

                    // ping each player in the list
                    // set the first non self active player as the backup leader
                    for (Player player : Game.activePlayerList) {
                        if (isNodeAlive(player)) {
                            respondingPlayers.add(player);
                            if (backupLeader == null && !player.getPlayerId().equals(Game.player.getPlayerId())) {
                                backupLeader = player;
                            }
                        } else {
                            System.out.println("Removing player " + player.getPlayerId() + " from the game!");
                            Game.gameState.removePlayerFromMaze(player.getPlayerId());
                        }
                    }
                    Game.activePlayerList = respondingPlayers;
                    Game.trackerRemoteObject.updateActivePlayerList(Game.activePlayerList);
                    if (backupLeader != null) {
                        backupLeader.remotePlayerObject.updateActivePlayerList(Game.activePlayerList);
                        backupLeader.remotePlayerObject.updateGameState(Game.gameState);
                        backupLeader.remotePlayerObject.setBackupLeader();
                    }

                } else {
                    // System.out.println("leader is alive");
                }
            }

        } catch (Exception e) {
            System.out.println("Exception in checkHeartBeat");
            e.printStackTrace();
        }
    }

    public boolean ping() throws RemoteException {
        return true;
    }

    public void updateActivePlayerList(List<Player> activePlayerList) throws RemoteException {
        Game.activePlayerList = activePlayerList;
    }
}
