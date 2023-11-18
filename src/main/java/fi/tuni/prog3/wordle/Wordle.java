package fi.tuni.prog3.wordle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.application.Platform;

public class Wordle extends Application {
    private GridPane grid;
    private int currentRow;
    private int currentColumn;
    private List<String> wordList = null;
    private int currentWordIndex = 0;
    private String gameMasterWord;
    private TextField[][] cellFields;
    private TextField messageField;
    private boolean gameEnded = false;
    private int lastLetterColumn;
    private Button newGameBtn;

    @Override
    public void start(Stage primaryStage) {
        
        initializeGame();
        grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(5);
        grid.setVgap(5);
        newGameBtn = new Button("Start new game");
        messageField = new TextField(); 
        messageField.setEditable(false);
        messageField.setId("infoBox");
        newGameBtn.setId("newGameBtn");
        newGameBtn.setOnAction(event -> resetGame());
        newGameBtn.setFocusTraversable(false);

        HBox topBar = new HBox(newGameBtn, messageField);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER);
        topBar.setSpacing(5);
        HBox.setMargin(newGameBtn, new Insets(0, 0, 0, 10));

        VBox root = new VBox(topBar, grid); 
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);

        startNewGame();
        Scene scene = new Scene(root, 500, 500);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #99a682;");
        primaryStage.setScene(scene);

        primaryStage.show();
        Platform.runLater(() -> cellFields[0][0].requestFocus());
    }

    private void startNewGame() {
        gameEnded = false;
        chooseWord();
        initializeGrid();
    }

    private void chooseWord() {
        if (currentWordIndex < wordList.size()) {
            gameMasterWord = wordList.get(currentWordIndex).toUpperCase();
        }
    }

    private List<String> loadWordListFromFile(String filename) {
        List<String> wordList = null;

        try {
            wordList = Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace(); 
        }

        return wordList;
    }
    private void initializeGrid() {
        int cellSize = 40;
        cellFields = new TextField[6][gameMasterWord.length()];
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < gameMasterWord.length(); col++) {
                TextField cell = new TextField();
                cell.setId(row + "_" + col);
                cell.setMinSize(cellSize, cellSize);
                cell.setMaxSize(cellSize, cellSize);
                cell.setEditable(true);
                cell.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                grid.add(cell, col, row); 
                cell.setOnKeyPressed(event -> {
                    if(!gameEnded) {
                        event.consume();
                        handleCellKeyPress(event, cell);
                    }
                });
                cellFields[row][col] = cell;
                
                cell.setOnKeyTyped(event -> {
                    if (!gameEnded && Character.isLetter(event.getCharacter().charAt(0))) {
                        event.consume();
                        String pressedLetter = event.getCharacter().toUpperCase();
                        String guessedWord = getCurrentGuess() != "" ? getCurrentGuess().substring(1) : "";
                        String newGuess = guessedWord + pressedLetter;
                        setLettersInCells(newGuess);
                    }
                });
            }
        }
        grid.setMaxHeight(400);
        grid.setMaxWidth(400);
        Platform.runLater(() -> cellFields[0][0].requestFocus());
    }

    private void setLettersInCells(String guess) {
        clearCells(currentRow);
        for (int i = 0; i < guess.length(); i++) {
            String letter = String.valueOf(guess.charAt(i));
            if (i < gameMasterWord.length())
                cellFields[currentRow][i].setText(letter);
        }
    }

    private void clearCells(int currentRow) {
        for (int i = 0; i < gameMasterWord.length(); i++) {
            cellFields[currentRow][i].clear();
        }
    }

    private void initializeGame() {
        currentRow = 0;
        currentColumn = 0;
        wordList = loadWordListFromFile("words.txt");
    }


    private void handleBackspace() {
        if (currentColumn >= 0) {
            String guessedWord = getCurrentGuess();
            String newGuess = guessedWord.substring(0, guessedWord.length() - 1);
            setLettersInCells(newGuess);
        }
    }

    private void handleCellKeyPress(KeyEvent event, TextField cell) {
        if (gameEnded) {
            event.consume();
            return;
        }
        if (event.getCode() == KeyCode.BACK_SPACE) {
            handleBackspace();
        } else if (event.getCode() == KeyCode.ENTER) {
            handleSubmit();
        }
    }

    private void handleSubmit() {
        //printCellColors();
        String guess = getCurrentGuess();
        if (guess.length() < gameMasterWord.length() - 1) {
            messageField.setText("Give a complete word before pressing Enter!");
            return;
        } else {
            for (int i = 0; i < gameMasterWord.length(); i++) {
                String masterLetter = String.valueOf(gameMasterWord.charAt(i));
                String guessLetter = String.valueOf(guess.charAt(i));
                Color cellColor;

                if (masterLetter.equals(guessLetter)) {
                    cellFields[currentRow][i].setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                } else if (gameMasterWord.contains(guessLetter)) {
                    cellFields[currentRow][i].setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    cellFields[currentRow][i].setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }

            if (guess.equals(gameMasterWord)) {
                messageField.setText("Congratulations, you won!");
                gameEnded = true;
                Platform.runLater(() -> newGameBtn.requestFocus());
            } else {
                currentRow++;
                if (currentRow < 6) {
                    currentColumn = 0;
                    cellFields[currentRow][currentColumn].requestFocus();
                } else {
                    messageField.setText("Game over, you lost!");
                    gameEnded = true;
                    Platform.runLater(() -> newGameBtn.requestFocus());
                }
            }
        }
    }

    private void resetGame() {
        currentRow = 0;
        currentColumn = 0;
        currentWordIndex++;
        gameEnded = true;
        grid.getChildren().clear();
        messageField.clear();
        startNewGame(); 
    }

    private String getCurrentGuess() {
        StringBuilder guessBuilder = new StringBuilder("");
        for (int i = 0; i < gameMasterWord.length(); i++) {
            if (cellFields[currentRow][i] != null) guessBuilder.append(cellFields[currentRow][i].getText());
        }
        return guessBuilder.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
}