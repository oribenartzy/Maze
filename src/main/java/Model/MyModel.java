package Model;

import Client.Client;
import Client.IClientStrategy;
import IO.MyDecompressorInputStream;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import algorithms.mazeGenerators.Maze;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.input.KeyCode;
import Server.Server;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import sample.Main;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyModel extends Observable implements IModel {

    private static MyModel myModel; //singelton

    public static MyModel getInstance() {
        if (myModel == null) {
            myModel = new MyModel();
        }
        return myModel;
    }
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Solution mazeSolution;
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;
    private Maze maze;
    private int[][] mazeArray;
    private int charPositionRow;
    private int charPositionCol;
    private int goalPosRow;
    private int goalPosCol;
    private boolean wonGame;
    private ArrayList<AState> mazeSolutionSteps;

    private  MediaPlayer mediaPlayer;


    public int[][] getMaze() {
        return mazeArray;
    }

    public ArrayList<AState> getSolution() {
        return mazeSolutionSteps;
    }

    public int getGoalPosRow() {
        return goalPosRow;
    }

    public int getGoalPosCol() {
        return goalPosCol;
    }

    public int getCharacterPositionRow() {
        return charPositionRow;
    }

    public int getCharacterPositionCol() {
        return charPositionCol;
    }

    public boolean isWonGame() {
        return wonGame;
    }


    //constructor
    private MyModel() {
        mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        mazeGeneratingServer.start();
        solveSearchProblemServer.start();
    }



    @Override
    public void generateMaze(int row, int col) {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, (IClientStrategy) (inFromServer, outToServer) -> {
                try {
                    ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                    ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                    toServer.flush();
                    int[] mazeDimensions = new int[]{row, col};
                    toServer.writeObject(mazeDimensions);
                    toServer.flush();
                    byte[] compressedMaze = (byte[]) fromServer.readObject();
                    InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                    byte[] decompressedMaze = new byte[(row * col) + 24];
                    is.read(decompressedMaze);
                    maze = new Maze(decompressedMaze);
                    mazeArray = maze.getMat();
                    charPositionRow = maze.getStartPosition().getRowIndex();
                    charPositionCol = maze.getStartPosition().getColumnIndex();
                    goalPosRow = maze.getGoalPosition().getRowIndex();
                    goalPosCol = maze.getGoalPosition().getColumnIndex();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            client.communicateWithServer();
            wonGame = false;
            setChanged();
            notifyObservers("generate");
            solveMaze();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopServers() {
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
    }

    @Override
    public void solveMaze() {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, (IClientStrategy) (inFromServer, outToServer) -> {
                try {
                    ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                    ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                    toServer.flush();
//                    newStart = new Position(charPositionRow,charPositionCol);

//                    newStart = new Position(StartRow,StartCol);
//                    maze.setStart(newStart);
                    toServer.writeObject(maze);
                    toServer.flush();
                    mazeSolution = (Solution)fromServer.readObject();
                    mazeSolutionSteps = mazeSolution.getSolutionPath();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            client.communicateWithServer();
            //setChanged();
            //notifyObservers("solve");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void moveCharacter(KeyCode movement) {
        //solved = false;
        int row = getCharacterPositionRow();
        int col = getCharacterPositionCol();
        int sizeRow = getMaze().length;
        int sizeCol = getMaze()[0].length;
        switch (movement) {
            case UP:
                if( row>0 && getMaze()[row - 1][col] != 1) {
                    charPositionRow--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case DOWN:
                if( row<sizeRow-1 && getMaze()[row + 1][col] != 1) {
                    charPositionRow++;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case RIGHT:
                if(col < sizeCol-1 && getMaze()[row][col + 1] != 1){
                    charPositionCol++;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case LEFT:
                if(col>0 && getMaze()[row][col - 1] != 1){
                    charPositionCol--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD8:
                if( row>0 && getMaze()[row - 1][col] != 1) {
                    charPositionRow--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD2:
                if( row<sizeRow-1 && getMaze()[row + 1][col] != 1) {
                    charPositionRow++;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD6:
                if(col < sizeCol-1 && getMaze()[row][col + 1] != 1){
                    charPositionCol++;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD4:
                if(col>0 && getMaze()[row][col - 1] != 1){
                    charPositionCol--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD3:
                if( row< sizeRow-1 && col < sizeCol-1&& getMaze()[row + 1][col + 1] != 1) {
                    charPositionRow++;
                    charPositionCol++;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD1:
                if( row<sizeRow-1&& col>0 && getMaze()[row + 1][col-1] != 1) {
                    charPositionRow++;
                    charPositionCol--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD7:
                if(col >0&& row>0 && getMaze()[row-1][col - 1] != 1){
                    charPositionCol--;
                    charPositionRow--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
            case NUMPAD9:
                if(row>0 &&   col<sizeCol-1 && getMaze()[row-1][col + 1] != 1){
                    charPositionCol++;
                    charPositionRow--;
                    getMaze()[charPositionRow][charPositionCol] = 2;
                }
                else
                {
                    playAudio("resources/Music/break.mp3");
                }
                break;
        }
        //check if won the game
        if (charPositionRow == goalPosRow && charPositionCol == goalPosCol){ wonGame = true; }
        setChanged();
        notifyObservers("move");
    }

    protected void playAudio(String audio) {
        String musicFile = audio;     // For example
        Media sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
    }

    public void save() {
        JFileChooser file = new JFileChooser();
        if (file.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File f = file.getSelectedFile();
            try{
                FileOutputStream fos = new FileOutputStream(new File(f.getAbsolutePath()));
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(maze);
                oos.writeObject(mazeSolution);
                oos.writeObject(charPositionRow);
                oos.writeObject(charPositionCol);
                fos.flush();
                fos.close();
                oos.flush();
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load(){
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();

            try {
                FileInputStream Input = new FileInputStream(new File(f.getAbsolutePath()));
                ObjectInputStream Out = new ObjectInputStream(Input);
                maze = (Maze)Out.readObject();
                mazeSolution = (Solution) Out.readObject();
                mazeSolutionSteps = mazeSolution.getSolutionPath();
                charPositionRow = (int) Out.readObject();
                charPositionCol = (int) Out.readObject();
                mazeArray = maze.getMat();
                charPositionRow = maze.getStartPosition().getRowIndex();
                charPositionCol = maze.getStartPosition().getColumnIndex();
                goalPosRow = maze.getGoalPosition().getRowIndex();
                goalPosCol = maze.getGoalPosition().getColumnIndex();
                wonGame = false;
                Input.close();
                Out.close();

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //solved = false;
        setChanged();
        notifyObservers("generate");
    }

    public void exitGame(){
        stopServers();
        Main.exitGame();
    }

    public void close() {
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }
}
