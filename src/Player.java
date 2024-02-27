package src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.rmi.RemoteException;

/* 
    * class to represent player object 
    * age field  that acts a software clock to represent the player joining period
    * player id field that acts as a string identifier for the player
    * last heartbeat field stoes the player's last heartbeat
    * remote player object field that is used to communicate between the players
*/

public class Player implements Serializable {
    private int age;
    private String playerId;
    private LocalDateTime joinTime;
    private boolean isPlayerAlive;
    public RemotePlayerInterface remotePlayerObject;

    public Player(String playerId, int age, RemotePlayerInterface remotePlayerObject) throws RemoteException {
        this.age = age;
        this.playerId = playerId;
        this.isPlayerAlive = true;
        this.joinTime = LocalDateTime.now();
        this.remotePlayerObject = remotePlayerObject;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public int getAge() {
        return this.age;
    }

    public boolean IsPlayerAlive() {
        return this.isPlayerAlive;
    }

    public LocalDateTime getJoinTime() {
        return this.joinTime;
    }

    public void MarkPlayerAsDead() {
        this.isPlayerAlive = false;
    }
}
