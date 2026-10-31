package edu.sussex.tictactoe;

import edu.sussex.tictactoe.model.ReplayModel;
import edu.sussex.tictactoe.repository.GameRepository;
import edu.sussex.tictactoe.service.LiveGameService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

/**
 * JavaFX GUI for the Tic Tac Toe game using existing backend classes.
 */
public class GameWindow extends Application {
    private LiveGameService service;
    private ReplayModel replay;
    private Button[] buttons;
    private Label statusLabel;
    private Connection connection; // Field to own and close JDBC connection
    private Button retryComputerMoveButton; // Retry computer move button
    private VBox root; // Store reference for keyboard handling
    private boolean inReplayMode = false; // Track if we're in replay mode
    private MenuButton savedGamesMenu; // Saved games menu button
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize database connection and service
        try {
            setupDatabase();
        } catch (Exception e) {
            // Show useful startup feedback in UI for missing settings/connection failure 
            showStartupError("Failed to initialize database: " + e.getMessage());
            return;
        }
        
        // Create initial UI with saved games menu
        createInitialUI(primaryStage);
        
        // Update UI to reflect initial state
        updateUI();
        
        // Update the saved games menu (it will be empty initially but will get populated when opened)
        updateSavedGamesMenu();
    }

    private MenuButton opponentModeMenu;  // For selecting opponent mode
    
    private void createInitialUI(Stage primaryStage) {
        try {
            // Create the live-game and historical-replay controls.
            
            // Create UI components
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(5);
            
            buttons = new Button[9];
            for (int i = 0; i < 9; i++) {
                final int position = i;
                buttons[i] = new Button("");
                buttons[i].setPrefSize(60, 60);
                // Add accessible text for screen readers and testing
                buttons[i].setAccessibleText("Position " + i + ", Empty");
                buttons[i].setOnAction(e -> handleHumanMove(position));
                grid.add(buttons[i], i % 3, i / 3);
            }
            
            statusLabel = new Label("Ready to start game");
            // Make status label wrap at a sensible width
            statusLabel.setWrapText(true);
            statusLabel.setMaxWidth(360); // Set preferred max width for wrapping
            
            Button newGameButton = new Button("New Game");
            newGameButton.setOnAction(e -> startNewGame());
            
            // Create opponent mode selection menu button  
            opponentModeMenu = new MenuButton("Next opponent: Random");
            MenuItem randomItem = new MenuItem("Random");
            randomItem.setOnAction(e -> selectOpponentMode("random"));
            MenuItem strongerItem = new MenuItem("Stronger"); 
            strongerItem.setOnAction(e -> selectOpponentMode("stronger"));
            
            opponentModeMenu.getItems().addAll(randomItem, strongerItem);
            
            // Create retry computer move button
            retryComputerMoveButton = new Button("Retry Computer Move");
            retryComputerMoveButton.setOnAction(e -> retryComputerMove());
            retryComputerMoveButton.setDisable(true); // Initially disabled
            
            // Create Saved Games MenuButton - initially empty with placeholder
            savedGamesMenu = new MenuButton("Saved Games");
            
            // Add a disabled placeholder item to the menu
            MenuItem placeholderItem = new MenuItem("Loading saved games...");
            placeholderItem.setDisable(true);
            savedGamesMenu.getItems().add(placeholderItem);
            
            // Wire the menu to update when shown (either by mouse or keyboard)
            savedGamesMenu.setOnShowing(event -> updateSavedGamesMenu());
            
            VBox root = new VBox(10);
            root.setPadding(new Insets(10));
            
            // Add the saved games menu
            root.getChildren().addAll(statusLabel, grid, opponentModeMenu, newGameButton, retryComputerMoveButton, savedGamesMenu);
            
            Scene scene = new Scene(root);
            // Add keyboard handler to the scene so we can capture arrow keys
            scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            
            primaryStage.setTitle("Tic-Tac-Toe");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
            // Store reference for later use in key handling
            this.root = root;
        } catch (Exception e) {
            showStartupError("Failed to create UI: " + e.getMessage());
        }
    }
    
    private void setupDatabase() throws SQLException {
        try {
            // Load database properties from db.properties file 
            Properties props = new Properties();
            if (Files.exists(Paths.get("db.properties"))) {
                try (var inputStream = Files.newInputStream(Paths.get("db.properties"))) {
                    props.load(inputStream);
                }
            } else {
                throw new RuntimeException("Missing db.properties file. Please create it with database connection details.");
            }
            
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username"); 
            String password = props.getProperty("db.password");
            
            if (url == null || url.isEmpty()) {
                throw new RuntimeException("Database URL not found in db.properties");
            }
            if (username == null || username.isEmpty()) {
                throw new RuntimeException("Database username not found in db.properties");
            }
            if (password == null || password.isEmpty()) {
                throw new RuntimeException("Database password not found in db.properties");
            }
            
            // Establish database connection
            connection = DriverManager.getConnection(url, username, password);
            
            // Create real JDBC repository with the established connection
            GameRepository repo = new GameRepository(connection);
            service = new LiveGameService(repo, new java.util.Random());
            replay = new ReplayModel(repo);
            
        } catch (IOException e) {
            throw new SQLException("Failed to read database properties file", e);
        } catch (Exception e) {
            throw new SQLException("Database setup failed: " + e.getMessage(), e);
        }
    }
    
    private void updateSavedGamesMenu() {
        // Clear existing items and add placeholder while loading
        savedGamesMenu.getItems().clear();
        
        try {
            List<edu.sussex.tictactoe.model.Game> games = null;
            try {
                if (connection != null) {
                    games = new GameRepository(connection).listGames();
                }
            } catch (SQLException e) {
                System.err.println("Failed to load games: " + e.getMessage());
                MenuItem errorItem = new MenuItem("Error loading games");
                errorItem.setDisable(true);
                savedGamesMenu.getItems().add(errorItem);
                return;
            }
            
            if (games != null && !games.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                for (edu.sussex.tictactoe.model.Game game : games) {
                    String menuItemText = "ID: " + game.getId() + ", Time: " + game.getStartTime().format(formatter) + ", Outcome: " + game.getOutcome();
                    final long gameId = game.getId();
                    MenuItem menuItem = new MenuItem(menuItemText);
                    menuItem.setOnAction(event -> startReplay(gameId));
                    savedGamesMenu.getItems().add(menuItem);
                }
            } else {
                // No saved games - show placeholder
                MenuItem noGamesItem = new MenuItem("No saved games");
                noGamesItem.setDisable(true);
                savedGamesMenu.getItems().add(noGamesItem);
            }
        } catch (Exception e) {
            System.err.println("Error updating saved games menu: " + e.getMessage());
            MenuItem errorItem = new MenuItem("Error loading games");
            errorItem.setDisable(true);
            savedGamesMenu.getItems().add(errorItem);
        }
    }
    
    private void handleKeyPress(KeyEvent event) {
        // Only process right arrow key when in replay mode
        if (event.getCode() == KeyCode.RIGHT && inReplayMode) {
            if (replay.hasMoreMoves()) {
                // Step forward one move in replay
                boolean stepped = replay.step();
                if (stepped) {
                    updateUI(); // Update UI to show the new board position
                }
            } else {
                // At end of game - no action needed, just consume event 
            }
            event.consume(); // Prevent other handlers from processing this key
        }
    }
    
    private void startReplay(long gameId) {
        try {
            if (replay == null) return;
            
            // Load the game for replay first, then set mode flag only on success
            replay.loadGame(gameId);
            inReplayMode = true;  // Enable replay mode ONLY after load succeeds
            
            // Update UI to show empty board and replay instructions
            updateUI();
            
            // Request focus back to root so keyboard navigation works properly
            if (root != null) {
                root.requestFocus();
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading game for replay: " + e.getMessage());
            statusLabel.setText("Failed to load game for replay: " + e.getMessage());
        }
    }

    // Application shutdown closes the owned JDBC connection.
    @Override 
    public void stop() throws Exception {
        // Close the JDBC connection when application stops
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        super.stop();
    }
    
    private void showStartupError(String message) {
        // Show useful startup feedback in the UI for missing settings/connection failure
        Stage errorStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        Label errorLabel = new Label(message);
        errorLabel.setWrapText(true); // Make label wrap so it fits
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> javafx.application.Platform.exit()); // Use Platform.exit instead of System.exit
        
        root.getChildren().addAll(errorLabel, exitButton);
        Scene scene = new Scene(root, 400, 200);
        errorStage.setTitle("Startup Error - Tic-Tac-Toe");
        errorStage.setScene(scene);
        errorStage.setResizable(false);
        errorStage.show();
    }
    
    private void handleHumanMove(int position) {
        if (service == null) return;
        
        try {
            // If we're in replay mode, ignore human moves
            if (inReplayMode) {
                statusLabel.setText("Replaying game - human moves are disabled");
                return;
            }
            
            // Make human move through service - this will validate turn and save to database
            boolean success = service.makeHumanMove(position);
            
            if (!success) {
                statusLabel.setText("Invalid move or not your turn!");
                updateUI();
                return;
            }
            
            updateUI();
            
            // Check if game ended after human move
            int status = service.getGameStatus();
            if (status == GameModel.IN_PROGRESS && service.getCurrentPlayer() == GameModel.COMPUTER) {
                // Computer's turn - make a computer move through the service to ensure consistency  
                boolean computerSuccess = service.makeComputerMove();
                if (!computerSuccess) {
                    // If computer move fails, enable retry button and update UI
                    statusLabel.setText("Computer move failed! Click Retry to try again.");
                    enableRetryButton(true);
                } else {
                    updateUI();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during human move: " + e.getMessage());
            // On errors render the service's committed state and show an honest error message
            updateUI(); // Call updateUI FIRST, then set error label so it doesn't get erased
            statusLabel.setText("Save failed! Recovery required.");
        }
    }
    
    private String selectedOpponentMode = "random";  // Default mode
    
    private void selectOpponentMode(String mode) {
        if (!"random".equals(mode) && !"stronger".equals(mode)) {
            return; // Invalid mode, ignore
        }
        
        selectedOpponentMode = mode;
        opponentModeMenu.setText("Next opponent: " + mode.substring(0, 1).toUpperCase() + mode.substring(1));
    }
    
    private void startNewGame() {
        if (service == null) return;
        
        try {
            // Start new game through service - this will save the game to database
            service.startGame(selectedOpponentMode);
            
            // Only set replay mode false AFTER service.startGame succeeds
            inReplayMode = false;
            
            updateUI();
            statusLabel.setText("Human (X) to play");
            enableRetryButton(false); // Disable retry button when starting a new game
        } catch (SQLException e) {
            System.err.println("Database error starting new game: " + e.getMessage());
            statusLabel.setText("Failed to start new game!");
            // Failed new-game creation preserves the current board - so we don't update UI here
        }
    }
    
    private void retryComputerMove() {
        if (service == null) return;
        
        // Guard against replay mode - replay must never cause writes 
        if (inReplayMode) {
            statusLabel.setText("Cannot retry computer move during replay!");
            return;
        }
        
        try {
            // Call service's makeComputerMove method when retrying computer move
            boolean success = service.makeComputerMove();
            if (!success) {
                statusLabel.setText("Retry failed! Try again or start a new game.");
            } else {
                statusLabel.setText("Computer move retried successfully!");
                enableRetryButton(false); // Disable retry button after successful retry
                updateUI(); // Update UI to reflect the computer move and current state
            }
        } catch (SQLException e) {
            System.err.println("Database error during computer move retry: " + e.getMessage());
            statusLabel.setText("Retry failed! Please start a new game.");
        }
    }
    
    private void enableRetryButton(boolean enabled) {
        // Enable/disable the retry button based on conditions
        if (retryComputerMoveButton != null) {
            retryComputerMoveButton.setDisable(!enabled);
        }
    }
    
    private void updateUI() {
        if (service == null) return;
        
        // Update board buttons - disable based on game state 
        int[] board = inReplayMode ? replay.getBoard() : service.getBoard();
        
        for (int i = 0; i < 9; i++) {
            switch (board[i]) {
                case GameModel.EMPTY:
                    // Disable button if no active game, game is finished, or it's O's turn
                    boolean disableButton = 
                        inReplayMode || // In replay mode, always disable buttons  
                        service.getCurrentGameId() == -1 || // No saved game ID  
                        service.getGameStatus() != GameModel.IN_PROGRESS && service.getGameStatus() != GameModel.GAME_NOT_STARTED ||
                        service.getCurrentPlayer() == GameModel.COMPUTER; // O's turn
                    
                    buttons[i].setText("");
                    buttons[i].setDisable(disableButton);
                    // Update accessible text for screen readers
                    buttons[i].setAccessibleText("Position " + i + ", Empty");
                    break;
                case GameModel.HUMAN:
                    buttons[i].setText("X");
                    buttons[i].setDisable(true); // Human moves are already played
                    // Update accessible text for screen readers
                    buttons[i].setAccessibleText("Position " + i + ", X");
                    break;
                case GameModel.COMPUTER:
                    buttons[i].setText("O");
                    buttons[i].setDisable(true); // Computer moves are already played
                    // Update accessible text for screen readers
                    buttons[i].setAccessibleText("Position " + i + ", O");
                    break;
            }
        }
        
        // Handle replay logic early in updateUI
        if (inReplayMode) {
            // Disable retry and all board buttons during replay
            enableRetryButton(false);
            
            for (Button button : buttons) {
                button.setDisable(true);  // All buttons disabled in replay mode
            }
            
            // Check if there are more moves to step through
            if (replay.hasMoreMoves()) {
                statusLabel.setText("Replaying game - press Right Arrow to step through moves");
            } else {
                String originalOutcome = replay.getOriginalOutcome();
                if ("IN_PROGRESS".equals(originalOutcome)) {
                    statusLabel.setText("Replay complete. Game was unfinished.");
                } else {
                    statusLabel.setText("Replay complete. Original outcome: " + originalOutcome);
                }
            }
            
            // Return early to avoid live-game logic
            return;
        }
        
        // Update status label and retry button based on game state (live game logic)
        int status = service.getGameStatus();
        switch (status) {
            case GameModel.GAME_NOT_STARTED:
                statusLabel.setText("Ready to start game");
                enableRetryButton(false);
                break;
            case GameModel.IN_PROGRESS:
                if (service.getCurrentPlayer() == GameModel.HUMAN) {
                    statusLabel.setText("Human (X) to play");
                    enableRetryButton(false);
                } else {
                    statusLabel.setText("Computer (O) to play");
                    // Enable retry button only when there's an active saved game, 
                    // status is IN_PROGRESS, and current player is COMPUTER
                    boolean shouldEnable = service.getCurrentGameId() != -1 && 
                                          service.getGameStatus() == GameModel.IN_PROGRESS &&
                                          service.getCurrentPlayer() == GameModel.COMPUTER;
                    enableRetryButton(shouldEnable);
                }
                break;
            case GameModel.HUMAN_WINS:
                statusLabel.setText("Human wins!");
                enableRetryButton(false);
                break;
            case GameModel.COMPUTER_WINS:
                statusLabel.setText("Computer wins!");
                enableRetryButton(false);
                break;
            case GameModel.DRAW:
                statusLabel.setText("Draw!");
                enableRetryButton(false);
                break;
        }
    }

    /**
     * Main entry point launches the JavaFX application.
     */
    public static void main(String[] args) {
        // Normal launch of the JavaFX application
        Application.launch(GameWindow.class, args);
    }
}