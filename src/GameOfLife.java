import javafx.animation.Animation;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
//import javafx.scene.layout.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * himanshu chaudhary
 * this class sets up the gui and places the buttons, canvas  in the gui
 * this class also determines when the button needs to be disbaled and also handles killing the threads if a new board needs to be created
 */
public class GameOfLife extends Application

{
  private ScrollBar scrollX;
  private ScrollBar scrollY;
  private Canvas canvas;
  private Button generateBtn;
  private Button startBtn;
  private Button pauseBtn;
  private Button randomBtn;
  private Button gliderBtn;
  private Button allAliveButEdges;
  private Button upperCheckerBtn;
  private Button nextBtn;
  private Button someThingCool;
  private ComboBox threadNo;
  private Label threadNoLabel;
  private Label presets;
  boolean played;

  Board gameoflife;

  /**
   * @param state the state of the button (is eithe rtrue or false)
   *              this method chanes the state of button which have common behaviour
   */
  void changeControls(Boolean state)
  {
    generateBtn.setDisable(state);
    gliderBtn.setDisable(state);
    allAliveButEdges.setDisable(state);
    randomBtn.setDisable(state);
    threadNo.setDisable(state);
    upperCheckerBtn.setDisable(state);
    someThingCool.setDisable(state);
  }

  /**
   * starts the board with initiali values
   * the constructor of Board class is called with values read from the GUI
   */
  void startBoard()
  {
    int threadCount = Integer.parseInt(threadNo.getSelectionModel().getSelectedItem().toString());
    gameoflife = new Board(canvas, scrollX, scrollY, threadCount);
    gameoflife.justOneStep = false;
    Calculator.initializeCells();
    startBtn.setDisable(false);
    pauseBtn.setDisable(true);
    nextBtn.setDisable(true);
    if (played)
    {
      for (Thread thread : gameoflife.calculator)
      {
        thread.stop();
      }
    }
  }

  /**
   * @param primaryStage is the main scene where the components are displayed
   * @throws Exception this constructor sets up the GUI by creating the required buttons, lablles and the layout
   */
  public void start(Stage primaryStage) throws Exception
  {

    generateBtn = new Button("Reset");
    pauseBtn = new Button("Pause");
    randomBtn = new Button(("Random"));
    gliderBtn = new Button((("Glider Gun")));
    upperCheckerBtn = new Button(("Upper Checker"));
    allAliveButEdges = new Button("All alive Except Edges");
    nextBtn = new Button(("Next"));
    someThingCool = new Button("Cool Pattern");
    threadNo = new ComboBox();
    threadNoLabel = new Label("No. of Threads");
    presets = new Label("Presets");
    threadNo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8);
    threadNo.getSelectionModel().selectFirst();
    final int minwidth = 420;
    final int minHeight = 470;


    startBtn = new Button("Start Game");
    canvas = new Canvas(800, 800);

    generateBtn.setMaxWidth(Double.MAX_VALUE);
    pauseBtn.setMaxWidth(Double.MAX_VALUE);
    startBtn.setMaxWidth(Double.MAX_VALUE);
    gliderBtn.setMaxWidth(Double.MAX_VALUE);
    allAliveButEdges.setMaxWidth(Double.MAX_VALUE);
    threadNoLabel.setMaxWidth(Double.MAX_VALUE);
    presets.setMaxWidth(Double.MAX_VALUE);
    randomBtn.setMaxWidth(Double.MAX_VALUE);
    nextBtn.setMaxWidth(Double.MAX_VALUE);
    upperCheckerBtn.setMaxWidth(Double.MAX_VALUE);


    startBtn.setDisable(true);
    pauseBtn.setDisable(true);
    nextBtn.setDisable(true);


    scrollX = new ScrollBar();
    scrollY = new ScrollBar();

    scrollX.setMinWidth(20);
    scrollX.setMinWidth(20);
    scrollY.setOrientation(Orientation.VERTICAL);

    scrollX.setMin(0);
    scrollX.setMax(Board.WIDTH - 1);
    scrollX.setUnitIncrement(1);
    scrollX.setBlockIncrement(1);


    scrollY.setMin(0);
    scrollY.setMax(Board.HEIGHT - 1);
    scrollY.setUnitIncrement(1);
    scrollY.setBlockIncrement(1);

    // canvas box contains the canvas and the Y scrollbar
    HBox canvasBox = new HBox(10);

    GridPane buttonBox = new GridPane();

    buttonBox.setHgap(10);
    buttonBox.setVgap(10);
    buttonBox.setPadding(new Insets(0, 10, 0, 10));


    buttonBox.add(presets, 0, 0);
    buttonBox.add(generateBtn, 1, 0);
    buttonBox.add(gliderBtn, 2, 0);
    buttonBox.add(allAliveButEdges, 3, 0);
    buttonBox.add(upperCheckerBtn, 5, 0);
    buttonBox.add(someThingCool, 6, 0);
    buttonBox.add(randomBtn, 4, 0);
    buttonBox.add(startBtn, 0, 1);
    buttonBox.add(pauseBtn, 1, 1);
    buttonBox.add(nextBtn, 2, 1);
    buttonBox.add(threadNoLabel, 3, 1);
    buttonBox.add(threadNo, 4, 1);


    //layout is a Vbox and contains the buttons , canvas box and the X scrollbar
    VBox layout = new VBox(10);
    canvasBox.getChildren().addAll(canvas, scrollY);
    layout.getChildren().addAll(buttonBox, canvasBox, scrollX);

    StackPane root = new StackPane();
    root.setPadding(new Insets(10, 10, 10, 10));
    primaryStage.setMinWidth(minwidth);
    primaryStage.setMinHeight(minHeight);
    root.getChildren().add(layout);
    primaryStage.setTitle("Game Of Life");

    /**
     * it change the width of the canvas whenever the screen is resized
     */
    root.widthProperty().addListener(e ->
    {

      canvas.setWidth(root.getWidth() - 30);


    });

    /**
     * it change the height of the canvas whenever the screen is resized
     */
    root.heightProperty().addListener(e ->
    {
      canvas.setHeight(root.getHeight() - 120);

    });

    primaryStage.setScene(new Scene(root, canvas.getWidth() + 50, canvas.getHeight() + 140));


    generateBtn.setOnAction(e ->
    {

      startBoard();
    });

    /**
     * sets up the random layout by creating a new board
     */
    randomBtn.setOnAction(e ->
    {
      startBoard();
      gameoflife.loadRandom();
      gameoflife.updateBoard(gameoflife.cells);


    });

    /**
     * sets up the all alive but edges layout by creating a new board
     */
    allAliveButEdges.setOnAction(e ->
    {
      startBoard();
      gameoflife.loadAllAliveButEdges();
      gameoflife.updateBoard(gameoflife.cells);


    });

    someThingCool.setOnAction(e ->
    {
      startBoard();
      gameoflife.loadSomethingCool();
      gameoflife.updateBoard(gameoflife.cells);


    });


    /**
     * sets up the glider gun layout by creating a new board
     */


    gliderBtn.setOnAction(e ->
    {
      startBoard();
      gameoflife.loadGliderGun();
      gameoflife.updateBoard(gameoflife.cells);
    });

    upperCheckerBtn.setOnAction(e ->
    {
      startBoard();
      gameoflife.loadUpperCheckerboard();
      gameoflife.updateBoard(gameoflife.cells);
    });


    /**
     * starts the board, removes th eflag for the threads and ddisables required buttons
     * the threada are also started by this button click
     */
    startBtn.setOnAction((e ->
    {
      gameoflife.justOneStep = false;
      Board.isPaused = false;


      played = true;
      Calculator.initializeCells();
      Board.running = true;
      changeControls(true);
      pauseBtn.setDisable(false);
      pauseBtn.setText("Pause");
      startBtn.setDisable(true);
      for (Thread thread : gameoflife.calculator)
      {
        thread.start();

      }
      gameoflife.start();


    }));

    /**
     * this button pauses the Gui BY SETTING THE running flag to false
     * and sets the running flag to true back again when it it pressed again
     * this button acts as both play and pause
     */

    pauseBtn.setOnAction((e ->
    {

      if (pauseBtn.getText().equals("Pause"))
      {
        Board.running = false;
        pauseBtn.setText("Play");
        changeControls(false);
        nextBtn.setDisable(false);
        Board.isPaused = true;

      } else
      {
        Board.running = true;
        pauseBtn.setText("Pause");
        changeControls(true);
        nextBtn.setDisable(true);
        Board.isPaused = false;
      }
    }));

    /**
     * this buttons increments the generaton by onc step by
     * changing values of jumponestep and runing boolean
     */

    nextBtn.setOnAction(e ->
    {

      Board.justOneStep = true;

      gameoflife.running = true;
    });
    /** closes all threads when program is closed**/

    primaryStage.setOnCloseRequest(event ->
    {
      System.exit(0);
    });

    startBoard();
    gameoflife.loadRandom();
    gameoflife.updateBoard(Board.cells);
    primaryStage.show();

  }


  /**
   * is the main method which calls the constructor
   *
   * @param args
   */
  public static void main(String[] args)
  {
    launch(args);
  }
}
