package src;

import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.io.Serializable;
import java.lang.StringBuilder;
import java.util.Scanner;

class Cell implements Serializable {
    private String playerId;
    private boolean containsTreasure;

    public void addTreasure() {
        this.containsTreasure = true;
    }

    public void removeTreasure() {
        this.containsTreasure = false;
    }

    public boolean containsTreasure() {
        return this.containsTreasure;
    }

    public void addPlayer(String playerId) {
        this.playerId = playerId;
    }

    public boolean containsPlayer() {
        return this.playerId != null;
    }

    public String getPlayer() {
        return this.playerId;
    }

    public void removePlayer() {
        this.playerId = null;
    }
}

class CellPosition implements Serializable {
    private int row;
    private int col;

    CellPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

}

public class GameState implements Serializable {

    private int N;
    private int K;
    private Cell[][] maze;
    private int currentTreasureCount;
    private HashMap<String, Integer> playersScores;
    private HashMap<String, CellPosition> playerPositions;

    public GameState(int N, int K) {
        this.N = N;
        this.K = K;
        this.maze = new Cell[N][N];
        this.currentTreasureCount = 0;
        this.playersScores = new HashMap<String, Integer>();
        this.playerPositions = new HashMap<String, CellPosition>();
    }

    public void initialiseMaze() {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                this.maze[row][col] = new Cell();
            }
        }
        createTreasures();
    }

    public void UpdateGameState(GameState g) {
        this.maze = g.maze;
        this.currentTreasureCount = g.currentTreasureCount;
        this.playersScores = g.playersScores;
        this.playerPositions = g.playerPositions;
    }

    private void createTreasures() {
        Random random = new Random();
        while (currentTreasureCount < K) {
            int row = random.nextInt(this.N);
            int col = random.nextInt(this.N);
            if (!maze[row][col].containsTreasure()) {
                createTreasure(row, col);
                currentTreasureCount++;
            }
        }
    }

    private void createTreasure(int row, int col) {
        this.maze[row][col].addTreasure();
    }

    public void refillTreasures() {
        createTreasures();
    }

    public Cell[][] getMaze() {
        return this.maze;
    }

    public void addPlayerToMaze(String playerId) {
        Random random = new Random();
        int row = random.nextInt(this.N);
        int col = random.nextInt(this.N);
        while (this.maze[row][col].containsTreasure() || this.maze[row][col].containsPlayer()) {
            row = random.nextInt(this.N);
            col = random.nextInt(this.N);
        }
        this.maze[row][col].addPlayer(playerId);
        this.playerPositions.put(playerId, new CellPosition(row, col));
        this.playersScores.put(playerId, 0);
    }

    public void removePlayerFromMaze(String playerId) {
        if (!this.playerPositions.containsKey(playerId)) {
            return;
        }

        // get cell position of the player
        CellPosition playerPosition = this.playerPositions.get(playerId);
        int row = playerPosition.getRow();
        int col = playerPosition.getCol();

        // remove the player from the maze, score list and position list
        this.maze[row][col].removePlayer();
        this.playersScores.remove(playerId);
        this.playerPositions.remove(playerId);
    }

    public boolean move(MOVE moveDirection, String playerId) {
        CellPosition playerPosition = this.playerPositions.get(playerId);
        int row = playerPosition.getRow();
        int col = playerPosition.getCol();

        switch (moveDirection) {
            case NORTH:
                row = row - 1;
                break;
            case SOUTH:
                row = row + 1;
                break;
            case EAST:
                col = col + 1;
                break;
            case WEST:
                col = col - 1;
                break;
            default:
                return false;
        }

        if (row < 0 || row >= this.N || col < 0 || col >= this.N) {
            System.out.println("Invalid move");
            return false;
        }

        if (this.maze[row][col].containsPlayer()) {
            System.out.printf("Player %s already exists in the cell\n", this.maze[row][col].getPlayer());
            return false;
        }

        int oldRow = playerPosition.getRow();
        int oldCol = playerPosition.getCol();
        this.maze[oldRow][oldCol].removePlayer();
        this.maze[row][col].addPlayer(playerId);
        this.playerPositions.put(playerId, new CellPosition(row, col));

        if (this.maze[row][col].containsTreasure()) {
            this.incrementScoreByOne(playerId);
            this.maze[row][col].removeTreasure();
            this.currentTreasureCount--;
            this.refillTreasures();
        }

        return true;
    }

    public boolean incrementScoreByOne(String playerId) {
        if (this.playersScores.containsKey(playerId)) {
            this.playersScores.put(playerId, this.playersScores.get(playerId) + 1);
            return true;
        }
        return false;
    }

    public HashMap<String, Integer> getPlayerScores() {
        return this.playersScores;
    }

    public HashMap<String, CellPosition> getPlayerPositions() {
        return this.playerPositions;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\n********************\n");

        // Append infomration about the game state
        stringBuilder.append("Currently there are " + this.playerPositions.size() + " players and "
                + this.currentTreasureCount + " treasures in the maze\n");

        // Append information about players' scores
        stringBuilder.append("\n**** Player Scores ****\n");
        for (Map.Entry<String, Integer> entry : playersScores.entrySet()) {
            stringBuilder.append("Player ID: ").append(entry.getKey())
                    .append(", Score: ").append(entry.getValue()).append("\n");
        }

        // Append information about players' positions
        stringBuilder.append("\n**** Player Positions ****\n");
        for (Map.Entry<String, CellPosition> entry : playerPositions.entrySet()) {
            stringBuilder.append("Player ID: ").append(entry.getKey())
                    .append(", Position: (").append(entry.getValue().getRow())
                    .append(", ").append(entry.getValue().getCol()).append(")\n");
        }

        // Print the maze
        stringBuilder.append("\n**** Maze ****\n");
        for (int i = 0; i < N + 1; i++) {
            if (i == 0) {
                stringBuilder.append(" ");
            } else {
                int r = i - 1;
                stringBuilder.append("   " + r + "   ");
            }

        }
        stringBuilder.append("\n");
        for (int i = 0; i < N; i++) {
            stringBuilder.append("" + i + "  ");
            for (int j = 0; j < N; j++) {
                if (maze[i][j].containsTreasure() && maze[i][j].containsPlayer()) {
                    stringBuilder.append("(T," + maze[i][j].getPlayer() + ")");
                } else if (maze[i][j].containsPlayer()) {
                    stringBuilder.append(maze[i][j].getPlayer() + "    ");
                } else if (maze[i][j].containsTreasure()) {
                    stringBuilder.append("T     ");
                } else {
                    stringBuilder.append("..    ");
                }
                stringBuilder.append(" ");
            }
            stringBuilder.append("\n");
        }

        stringBuilder.append("\n********************\n");

        return stringBuilder.toString();
    }

    public static void main(String args[]) {
        GameState gameState = new GameState(5, 5);
        gameState.initialiseMaze();
        gameState.addPlayerToMaze("P1");
        gameState.addPlayerToMaze("P2");
        gameState.addPlayerToMaze("P3");
        System.out.println(gameState);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter move: ");
                String move = scanner.nextLine();
                if (move.equals("n")) {
                    gameState.move(MOVE.NORTH, "P1");
                } else if (move.equals("s")) {
                    gameState.move(MOVE.SOUTH, "P1");
                } else if (move.equals("e")) {
                    gameState.move(MOVE.EAST, "P1");
                } else if (move.equals("w")) {
                    gameState.move(MOVE.WEST, "P1");
                } else {
                    gameState.removePlayerFromMaze("P1");
                    break;
                }
                System.out.println(gameState);
            }
        }
    }
}