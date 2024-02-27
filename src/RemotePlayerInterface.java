package src;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemotePlayerInterface extends Remote {

    /* set the node as leader */
    public void setLeader() throws RemoteException;

    /* check if the node is a leader */
    public boolean isLeader() throws RemoteException;

    /* set the node as backup leader */
    public void setBackupLeader() throws RemoteException;

    /* check if the node is a backup leader */
    public boolean isBackupLeader() throws RemoteException;

    /* ping the node to check communication */
    public boolean ping() throws RemoteException;

    /*
     * send the local game state and receive the updated game state from the server
     */
    public GameState makeAMove(String move, String playerId) throws RemoteException;

    public void updateGameState(GameState g) throws RemoteException;

    /* register a player. The latest game state is returned */
    public GameState registerPlayer(Player player) throws RemoteException;

    /* update the active player list of a node */
    public void updateActivePlayerList(List<Player> activePlayerList) throws RemoteException;

    /* leader checks the status of other nodes */
    public void checkHeartBeat() throws RemoteException;

}