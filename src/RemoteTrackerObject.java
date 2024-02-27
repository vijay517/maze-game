package src;

import java.util.List;
import java.util.ArrayList;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/* class used for bootstrapping
    * contains the list of active players, 
    * number of treasures and number of allowed players
    * also contains the youngest age of the last joined player
*/
public class RemoteTrackerObject extends UnicastRemoteObject implements RemoteTrackerInterface {

    private int N;
    private int K;
    private int youngestAge;
    private List<Player> activePlayers;

    public RemoteTrackerObject(int N, int K) throws RemoteException {
        super();
        this.N = N;
        this.K = K;
        this.youngestAge = 0;
        this.activePlayers = new ArrayList<Player>();
    }

    /* Remote object method for a new player get the active player list */
    public synchronized List<Player> getActivePlayers(String playerName, RemotePlayerInterface remotePlayerObject)
            throws RemoteException {

        Player newPlayer = new Player(playerName, ++youngestAge, remotePlayerObject);
        this.activePlayers.add(newPlayer);
        return this.activePlayers;
    }

    /* Remote object method to update the active player list */
    public synchronized void updateActivePlayerList(List<Player> player) {
        this.activePlayers = player;
    }

    /* Remote object method to get the number of treasures */
    public int getNumberOfTreasures() {
        return this.K;
    }

    /* Remote object method to get the number of players */
    public int getNumberOfPlayers() {
        return this.N;
    }

    /* Remote object method to get the youngest age */
    public int getYoungestAge() {
        return this.youngestAge;
    }
}
