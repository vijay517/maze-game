package src;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.time.format.DateTimeFormatter;
import javax.swing.table.DefaultTableModel;

public class Game {

    public static int N;
    public static int K;
    public static Player player;
    public static boolean isLeader;
    public static boolean isBackupLeader;
    public static GameState gameState;
    public static List<Player> activePlayerList;
    public static RemoteTrackerInterface trackerRemoteObject;

    public JFrame jframe;
    public JPanel gridPanel;
    public JPanel serverPanel;
    public JLabel[][] grid;
    public JTable scoreTable;

    public void contactTracker(String trackerIP, int portNumber, String playerId)
            throws RemoteException, Exception, NotBoundException {

        // Contact Tracker Logic: get the bootstrap object from the tracker
        Registry registry = LocateRegistry.getRegistry(trackerIP);
        Game.trackerRemoteObject = (RemoteTrackerInterface) registry.lookup("remoteTrackerObject");

        // Contact Tracker Logic: export remote player object
        RemotePlayerInterface remotePlayerObject = (RemotePlayerInterface) new RemotePlayerObject();

        // Contact Tracker Logic: get active player list from the tracker
        List<Player> activePlayerList = trackerRemoteObject.getActivePlayers(playerId, remotePlayerObject);
        int N = trackerRemoteObject.getNumberOfPlayers();
        int K = trackerRemoteObject.getNumberOfTreasures();

        // Contact Tracker Logic: if the active player list is null, the game is full
        if (activePlayerList == null) {
            System.out.println("Game is full. Try again later.");
            System.exit(0);
        }

        // Intialise the class parameters
        Game.N = N;
        Game.K = K;
        Game.activePlayerList = activePlayerList;
        Game.isLeader = activePlayerList.size() == 1;
        Game.player = activePlayerList.get(activePlayerList.size() - 1);

        // Debugging: Print active player list
        System.out.println("N: " + Game.N + " K: " + Game.K);
        Game.activePlayerList
                .forEach(p -> System.out.println("player name: " + p.getPlayerId() + " player age: " + p.getAge()));
    }

    public void initJframe() {
        // get the current player, primary server and backup server ids
        String currentPlayerName = Game.player.getPlayerId();
        String primaryServerID = Game.activePlayerList.get(0).getPlayerId();
        String backupServerID = Game.activePlayerList.size() > 1 ? Game.activePlayerList.get(1).getPlayerId() : null;
        int nPlayers = Game.activePlayerList.size();

        String[] columnsNames = { "Player", "Score" };
        String[][] scr = new String[nPlayers + 1][2];

        jframe.setTitle(currentPlayerName + " - Game Started At: "
                + Game.player.getJoinTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        jframe.setSize(500, 500);

        serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));

        serverPanel.add(new JLabel("Primary " + primaryServerID));
        if (backupServerID != null) {
            serverPanel.add(new JLabel("Backup " + backupServerID));
        }
        JPanel leftpanel = new JPanel(new BorderLayout());
        leftpanel.add(serverPanel, BorderLayout.NORTH);

        scoreTable = new JTable(scr, columnsNames);
        scoreTable.setPreferredScrollableViewportSize(scoreTable.getPreferredSize());
        JScrollPane tableScrollPane = new JScrollPane(scoreTable);
        leftpanel.add(tableScrollPane, BorderLayout.SOUTH);

        leftpanel.setPreferredSize(new Dimension(100, leftpanel.getPreferredSize().height));
        jframe.add(leftpanel, BorderLayout.WEST);
        jframe.setVisible(true);
        jframe.revalidate();
        jframe.repaint();
    }

    public void addGrid() {
        gridPanel.setLayout(new GridLayout(Game.N, Game.N));
        gridPanel.setPreferredSize(new Dimension(350, 350));
        gridPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        grid = new JLabel[Game.N][Game.N];
        System.out.println("Adding grid");
        for (int i = 0; i < Game.N; i++) {
            for (int j = 0; j < Game.N; j++) {
                grid[i][j] = new JLabel();
                grid[i][j].setBorder(new LineBorder(Color.BLACK));
                grid[i][j].setBounds(10, 10, 10, 10);
                grid[i][j].setOpaque(true);
                grid[i][j].setHorizontalAlignment(SwingConstants.CENTER); // Center horizontally
                grid[i][j].setVerticalAlignment(SwingConstants.CENTER); // Center vertically
                gridPanel.add(grid[i][j]);
            }
        }
        gridPanel.setBackground(Color.YELLOW);
        jframe.add(gridPanel, BorderLayout.CENTER);
        jframe.setVisible(true);
    }

    public void updateUI() {
        // Prepare the data for the table
        String[] columnsNames = { "Player", "Score" };
        int nPlayers = Game.activePlayerList.size();

        String[][] scr = new String[nPlayers + 1][2];

        HashMap<String, Integer> scores = Game.gameState.getPlayerScores();
        int index = 0;
        for (HashMap.Entry<String, Integer> entry : scores.entrySet()) {
            scr[index][0] = entry.getKey();
            scr[index][1] = entry.getValue().toString();
            index++;
        }

        // Set new table model
        scoreTable.setModel(new DefaultTableModel(scr, columnsNames));

        this.serverPanel.removeAll();
        JLabel pr = new JLabel("Primary " + Game.activePlayerList.get(0).getPlayerId());
        serverPanel.add(pr);
        if (nPlayers > 1) {
            JLabel sr = new JLabel("Backup " + Game.activePlayerList.get(1).getPlayerId());
            serverPanel.add(sr);
        }
        cleargrid();

        Cell[][] maze = Game.gameState.getMaze();
        for (int r = 0; r < Game.N; r++) {
            for (int c = 0; c < Game.N; c++) {
                if (maze[r][c].containsTreasure()) {
                    grid[r][c].setText("*");
                }
            }
        }

        Game.gameState.getPlayerPositions().forEach((key, value) -> {
            grid[value.getRow()][value.getCol()].setText(key);
        });

        jframe.setVisible(true);
        jframe.revalidate();
        jframe.repaint();
    }

    public void cleargrid() {
        for (int i = 0; i < Game.N; i++) {
            for (int j = 0; j < Game.N; j++) {
                grid[i][j].setText("");
            }
        }
    }

    public synchronized void setupGame() {
        if (Game.isLeader) { // if the player is the leader, start the game
            System.out.println("I am the leader");
            Game.gameState = new GameState(Game.N, Game.K);
            Game.gameState.initialiseMaze();
            Game.gameState.addPlayerToMaze(Game.player.getPlayerId());
        } else { // else contact the leader for registration
            for (Player leader : Game.activePlayerList) {
                System.out.println("Trying to contact leader : " + leader.getPlayerId() + " for registration");
                try {
                    if (Game.player.getPlayerId().equals(leader.getPlayerId())) {
                        Game.player.remotePlayerObject.setLeader();
                        Game.gameState = new GameState(Game.N, Game.K);
                        Game.gameState.initialiseMaze();
                        Game.gameState.addPlayerToMaze(Game.player.getPlayerId());
                    } else {
                        Game.gameState = leader.remotePlayerObject.registerPlayer(Game.player);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: java Game [Ip-address] [port-number] [player-id]");
            System.exit(1);
        }

        try {

            Scanner scanner = new Scanner((System.in));
            // parse input arguments
            String trackerIP = args[0];
            int portNumber = Integer.parseInt(args[1]);
            String playerId = args[2];

            // Contact Tracker
            Game game = new Game();
            game.contactTracker(trackerIP, portNumber, playerId);

            // Game Setup
            game.setupGame();

            // print game state
            System.out.println(Game.gameState);

            // setup UI
            game.jframe = new JFrame();
            game.gridPanel = new JPanel();
            game.initJframe();
            game.addGrid();
            game.updateUI();

            // periodically run the check heart beat function
            Timer timer1 = new Timer();
            timer1.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        Game.player.remotePlayerObject.checkHeartBeat();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 500);

            // update the UI of the leader and backup leader
            Timer timer2 = new Timer();
            timer2.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    try {
                        if (Game.isLeader || Game.isBackupLeader) {
                            game.updateUI();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 0, 500);

            // logic to handle the game (move)
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("0") || input.equals("1") || input.equals("2") || input.equals("3")
                        || input.equals("4")) {
                    for (Player player : Game.activePlayerList) {
                        try {
                            if (player.remotePlayerObject.isLeader()) {
                                GameState g = player.remotePlayerObject.makeAMove(input,
                                        Game.player.getPlayerId());
                                Game.gameState.UpdateGameState(g);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else if (input.equals("9")) {
                    for (Player player : Game.activePlayerList) {
                        try {
                            if (player.remotePlayerObject.isLeader()) {
                                GameState g = player.remotePlayerObject.makeAMove(input,
                                        Game.player.getPlayerId());
                                Game.gameState.UpdateGameState(g);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Game.player.MarkPlayerAsDead();
                    }
                } else {
                    System.out.println("Invalid move----");
                }
                System.out.println(Game.gameState);
                game.updateUI();
            }

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}