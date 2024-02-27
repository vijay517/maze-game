package src;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTrackerInterface extends Remote {

    public List<Player> getActivePlayers(String playerName, RemotePlayerInterface remoteTrackerObject)
            throws RemoteException;

    /* Remote object method to update the active player list */
    public void updateActivePlayerList(List<Player> player) throws RemoteException;

    /* Remote object method to get the number of treasures */
    public int getNumberOfTreasures() throws RemoteException;

    /* Remote object method to get the number of players */
    public int getNumberOfPlayers() throws RemoteException;

    /* Remote object method to get the youngest age */
    public int getYoungestAge() throws RemoteException;
}