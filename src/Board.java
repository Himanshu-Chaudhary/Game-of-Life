
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.ScrollBar;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;


/**
 * Himanshu Chaudhary
 * This is the GUI oof the game contains code to update the canvas in which the game is displayed
 * A board is initialized with default values and the required number of threads to calculate nbext generation is created at start and put into a barrier
 * this class implements animation timer which updates the board at a given time interval
 */

/**
 * Created by hIManshu on 2/9/2017.
 */
public class Board extends AnimationTimer
{
  static final int WIDTH = 10000;
  static final int HEIGHT = 10000;

  static boolean isPaused;
  private long lastFrameUpdate;
  double zoomstate;
  private int xOffSet = 0;
  private int yOffSet = 0;
  static byte[][] cells;

  private byte[][] updateCells;
  private int newx;
  private int newY;
  private boolean updating;
  private double previousvalue;
  private int tempYoffset;
  private int tempXoffset;
  private Color[] color = new Color[10];

  private double[] cureentLocation = new double[2];
  private int[] previousXY = new int[2];
  private int[] newLocation = new int[2];
  private int[] originalLocation = new int[2];
  private double xIncrement;
  private double yIncrement;
  static boolean justOneStep;


  private Canvas canvas;
  private ScrollBar scrollX;
  private ScrollBar scrollY;
  private double xSize;
  private int linewidth = 1;

  private int threadCount;
  private double maxZoom;
  private double minZoom;

  private final CyclicBarrier cb;
  Thread[] calculator;
  volatile static boolean running = false;
  private double y;
  private double x;
  private Random random;

  private int tempY;
  private int tempX;


  //int x1[][] = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,+1}};
  //int x2[][] = {{1,-1},{1,0},{1,1},{0,-1},{0,1}};
  private GraphicsContext gtx;
  private byte temp;


  /**
   * @param canvas      is the canvas where the board is drawn
   * @param scrollX     is the Row scroll bar
   * @param scrollY     is the column scroll bar
   * @param threadCount is the thread count of the current board
   *                    <p>
   *                    This cuonstructor initializes variables for the current boars, which includes number of threads, size of canvas, zoom size. All the values are used to upadte the GUI
   */
  public Board(Canvas canvas, ScrollBar scrollX, ScrollBar scrollY, int threadCount)
  {
    isPaused = true;
    this.scrollX = scrollX;
    this.scrollY = scrollY;
    this.threadCount = threadCount;
    cells = new byte[HEIGHT + 2][WIDTH + 2];
    updateCells = new byte[HEIGHT + 2][WIDTH + 2];
    random = new Random();
    xOffSet = 0;
    yOffSet = 0;


    color[0] = Color.GRAY;
    color[1] = Color.rgb(143, 188, 143);
    color[2] = Color.rgb(60, 179, 113);
    color[3] = Color.rgb(32, 178, 170);
    color[4] = Color.rgb(46, 139, 87);
    color[5] = Color.rgb(128, 128, 0);
    color[6] = Color.rgb(85, 107, 47);
    color[7] = Color.rgb(107, 142, 35);
    color[8] = Color.rgb(0, 128, 0);
    color[9] = Color.rgb(0, 100, 0);


    newx = ((cells.length - 2));
    newY = ((cells[0].length - 2));
    gtx = canvas.getGraphicsContext2D();

    /**
     * This runnable method runs when all threads have calculated the next generation
     * It swaps the pointers of the updates cells with new cells then the worker threads write over the ols cells value.
     */
    cb = new CyclicBarrier(threadCount, new Runnable()
    {

      @Override
      public void run()
      {

        updateCells = Calculator.tempCells;
        Calculator.tempCells = cells;
        cells = updateCells;
        /** used for jump next step, this released the flag for the worker thread and after one generation locks it againg so that thew threads are waiting**/
        if (justOneStep)
        {
          Board.justOneStep = false;
          running = false;
        }
      }
    });


    int size = WIDTH / (threadCount);
    calculator = new Thread[threadCount];
    int start;
    int end;
    //threads are being initialized and assigned the borad splitted by columns
    for (int i = 0; i < threadCount; i++)
    {
      start = (i == 0) ? 1 : (size * i);
      end = (i == threadCount - 1) ? (size * (i + 1) + 1) : size * (i + 1);

      calculator[i] = new Thread(new Calculator(cb, start, end));
    }
    this.canvas = canvas;
    xSize = canvas.getWidth() / WIDTH;

    //the min and max zoom size is determined
    maxZoom = 50 / xSize;
    minZoom = 1 / xSize;

    zoomstate = 20 / xSize;


    updateBoardInfo(zoomstate);
    updateBoard(cells);


/**
 * this event is triggered by a mouse click and lets the user change the value if the game is paused
 *
 */
    canvas.setOnMouseClicked(e ->
    {
      if (Board.isPaused)
      {
        // gets the current mouse pointer and determines the cell location
        int[] cellLoacation = getCellLocation(e.getX(), e.getY());
        double y = cellLoacation[0] * xSize;
        double x = cellLoacation[1] * xSize;

        temp = cells[(cellLoacation[0]) + 1 + xOffSet][cellLoacation[1] + 1 + yOffSet];
        //color them accordingly to their state
        if (temp == 0)
        {

          Board.cells[(cellLoacation[0]) + 1 + xOffSet][cellLoacation[1] + 1 + yOffSet] = 1;
          gtx.setFill(color[1]);
          gtx.fillRect((x + 1), (y + 1), xSize - linewidth, xSize - linewidth);
        } else
        {
          Board.cells[(cellLoacation[0]) + 1 + xOffSet][cellLoacation[1] + 1 + yOffSet] = 0;
          gtx.setFill(color[0]);
          gtx.fillRect((x + 1), (y + 1), xSize - linewidth, xSize - linewidth);
        }
      }
    });

    /**
     * this event is triggered by scroll wheel of mouse and allows the user to zoom in out out relative to the mouse pointer
     */


    canvas.setOnScroll(e ->
    {
      //determines the next zoom state and increases or decreases the zoom by a factor of 10
      double zoomFactor = (e.getDeltaY() > 0) ? 10 : -10;
      double newState = zoomFactor + zoomstate;
      if (newState >= minZoom && newState < maxZoom)
      {
        zoomstate = newState;

        cureentLocation[0] = e.getX();
        cureentLocation[1] = e.getY();
        previousXY = getCellLocation(e.getX(), e.getY());

        updateBoardInfo(zoomFactor);
        updateBoard(cells);


      } else newState = zoomFactor - zoomstate;


    });

    /**
     * this event is triggered when ever the scrollbar to change rows is moved
     * This event calculates the xOffset which is used by the updateBoard method to draw on the screen
     */
    scrollX.valueProperty().addListener((ObservableValue<? extends Number> ov,
                                         Number old_val, Number new_val) ->
    {
      //if the values of the scrollbar is not being updated by other users
      if (!updating)
      {
        double currentScroll = scrollX.getValue();
        //determined the offset relative to the location pointed by zoom
        double result = currentScroll - tempYoffset - newLocation[1];
        //calcutaes the amount of cells that needs to be shifted
        int increment = (int) (result / xIncrement);
        //calcutaes the amount of cells that needs to be shifted relative to previous offset
        yOffSet = tempYoffset + increment;

        if (yOffSet + newY > WIDTH || currentScroll == WIDTH - 1) yOffSet = WIDTH - newY;
        if (yOffSet < 0 || currentScroll == 0)
        {
          yOffSet = 0;
        }
        updateBoard(cells);
      }
    });

    /**
     * this event is triggered when ever the scrollbar to change columns is moved
     * This event calculates the yOffset which is used by the updateBoard method to draw on the screen
     */
    scrollY.valueProperty().addListener((ObservableValue<? extends Number> ov,
                                         Number old_val, Number new_val) ->
    {
      //if the values of the scrollbar is not being updated by other users
      if (!updating)
      {
        //determined the offset relative to the location pointed by zoom
        double currentScroll = scrollY.getValue();
        //determined th enumber of rows that needs to be shifted
        double result = (currentScroll - tempXoffset - (newLocation[0]));


        int increment = (int) ((result) / yIncrement);

        //    if (currentScroll ==0 && (tempXoffset+increment)<0) increment=-tempXoffset;
        // if (currentScroll ==HEIGHT-1 && (tempXoffset+newx)<HEIGHT) increment=HEIGHT-tempXoffset+newx;

        xOffSet = tempXoffset + increment;

        if (xOffSet + newx > HEIGHT || currentScroll == HEIGHT - 1) xOffSet = HEIGHT - newx;
        if (xOffSet < 0 || currentScroll == 0) xOffSet = 0;
        updateBoard(cells);
      }
    });

    /**
     * This event is treiggered when the screeen has been resized
     */
    canvas.widthProperty().addListener(e ->
    {
      updateWidthSize();
    });

    /**
     * This event is treiggered when the screeen has been resized
     */
    canvas.heightProperty().addListener(e ->
    {
      updateHeightsize();
    });


  }

  /**
   * updates the widthSize by calcutaing the new number of cells that needs to be displayed
   */
  private void updateWidthSize()
  {
    tempY = (int) (canvas.getWidth() / xSize);
    canvas.setWidth(tempY * xSize);
    //only updates if the change is more than or equal to a cell
    if (tempY != newY)
    {
      newY = tempY;
      updateBoard(cells);
    }


  }

  /**
   * updates the widthSize by calcutaing the new number of cells that needs to be displayed
   */
  private void updateHeightsize()
  {
    tempX = (int) (canvas.getHeight() / xSize);
    canvas.setHeight(tempX * xSize);
    //only updates if the change is more than or equal to a cell
    if (tempX != newx)
    {
      newx = tempX;

      updateBoard(cells);
    }


  }

  /**
   * @param zoomfactor is the zoom factor (either poitive or negative)
   *                   the zoomfactor is used to calculated the new size of grids relative to the zoomstate
   *                   the zxoomfactor determines if the user is zooming in or out
   */
  private void updateBoardInfo(double zoomfactor)
  {
    //calulates new number of rows and columns to display along with the size of individual cells
    newx = (int) ((cells.length - 2) / zoomstate);
    newY = (int) ((cells[0].length - 2) / zoomstate);
    xSize = (this.canvas.getWidth()) / newY;
    // if the size of the pixel is less than 5, then the grid lines wont be displayed
    if (xSize <= 5) linewidth = 0;
    else linewidth = 1;

    //if its a zoom
    if (zoomfactor > 0)
    {
      //new locations are determined relative to the mouse pointer
      newLocation[1] = (int) (cureentLocation[0] / xSize);
      newLocation[0] = (int) ((cureentLocation[1] / xSize));
      //the yoffset and xoffset are calculated by the difference of the new and old locations of the cell pointed by the mouse
      // the position of the cell is kept the same
      xOffSet += ((previousXY[0] - newLocation[0]));
      yOffSet += previousXY[1] - newLocation[1];

      //the temp offsets are used in scrolling, so zooming and scolling doesnot affect each other
      tempYoffset = yOffSet;
      tempXoffset = xOffSet;

      //if the zoom is greater or smaller than the grid size, default vaklues are set
      if (xOffSet + newx > (HEIGHT + 1)) xOffSet = (HEIGHT + 1) - newx;
      if (xOffSet < 0) yOffSet = 0;

      if (yOffSet + newY > WIDTH) yOffSet = WIDTH - newY;
      if (yOffSet < 0) yOffSet = 0;

    }
    //if its a zoom out
    else
    {
      // the offsets are calculates similarly as zoom in but the positon cannot be same as the relative mouse pointer
      // at tha case the closest value is set
      originalLocation[0] = previousXY[0] + (xOffSet);
      originalLocation[1] = previousXY[1] + yOffSet;

      newLocation[1] = (int) (cureentLocation[0] / xSize);
      newLocation[0] = (int) ((cureentLocation[1] / xSize));

      //if the new values are not posiible to show, the closes or the original values are set
      if ((originalLocation[0] - newLocation[0]) < 0)
      {
        newLocation[0] = originalLocation[0];
      }
      if ((originalLocation[1] - newLocation[1]) < 0)
      {

        newLocation[1] = originalLocation[1];
      }

      xOffSet = (originalLocation[0] - newLocation[0]);
      yOffSet = originalLocation[1] - newLocation[1];

      //if the values ore out of boundry at any point, the values are changed
      if (xOffSet + newx > (HEIGHT + 1)) xOffSet = (HEIGHT + 1) - newx;
      if (xOffSet < 0) xOffSet = 0;

      if (yOffSet + newY > WIDTH) yOffSet = WIDTH - newY;
      if (yOffSet < 0) yOffSet = 0;

      //the temp offset values are calculated for scrolling
      tempYoffset = yOffSet;
      tempXoffset = xOffSet;

    }

    //updating is done to move the scrollbar relative to zoom and the mouse pointer
    updating = true;
    //the current values selected by the mouse pointer is set
    scrollX.setValue(yOffSet + newLocation[1]);
    previousvalue = (yOffSet + newLocation[1]);

    //the required increment in the scroll bar is calculated and set
    //these increments are then used to determine weather to move a  cell  or not
    double increment = ((double) WIDTH / (WIDTH - newY + 1));


    xIncrement = increment;
    yIncrement = ((double) HEIGHT / (HEIGHT - newx + 1));

    scrollX.setVisibleAmount(newY);


    scrollY.setValue(xOffSet + (newLocation[0]));
    scrollY.setVisibleAmount(newx);
    updating = false;
  }

  /**
   * @param x is the current x pointer
   * @param y is the current y pointer
   * @return returns the cell the mouse is pointing at that time
   */
  private int[] getCellLocation(double x, double y)
  {
    //the current size of cells is used to determine the location of the cells
    int[] toReturn = new int[2];

    toReturn[1] = (int) (x / xSize);
    toReturn[0] = (int) (y / xSize);
    return toReturn;
  }


  /**
   * @param currentBoard is the board whose value is read and drawn
   */
  void updateBoard(byte[][] currentBoard)
  {

    gtx = canvas.getGraphicsContext2D();
    //determindes if the grid lines needs to be displayed or not
    if (linewidth == 1) gtx.setFill(Color.BLACK);
    else gtx.setFill(Color.GRAY);
    gtx.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

    // only loops at the current values of cells that needs to be drawn
    for (int i = 1; i <= newx; i++)
    {
      for (int j = 1; j <= newY; j++)
      {
        //uses the offset to read the values of of the cells that needs to be drawn
        if (currentBoard[i + xOffSet][j + yOffSet] >= 1) gtx.setFill(color[currentBoard[i + xOffSet][j + yOffSet]]);
        else gtx.setFill(Color.GRAY);
        y = ((i - 1) * xSize) + linewidth;
        x = (((j - 1) * xSize) + linewidth);
        gtx.fillRect(x, y, xSize - linewidth, xSize - linewidth);
      }
    }


  }

  /**
   * loads the random preset to the board with 50% chance or survival of each cell
   */
  void loadRandom()
  {
    for (int i = 1; i < cells.length; i++)
    {
      for (int j = 1; j < cells[0].length; j++)
      {
        if (random.nextInt(2) == 1)
        {
          cells[i][j] = 1;
        }


      }
    }
  }

  /**
   * loads the is alive but edges presets where only the border values are dead
   */
  void loadAllAliveButEdges()
  {
    for (int i = 2; i < cells.length - 2; i++)
    {
      for (int j = 2; j < cells[0].length - 2; j++)
      {

        cells[i][j] = 1;


      }
    }
  }

  void loadSomethingCool()
  {
    for (int i = 2; i < cells.length - 2; i++)
    {

      for (int j = 2; j < cells[0].length - 2; j++)
      {

        cells[i][j] = 1;


        if (random.nextInt(2) == 1) j = j + random.nextInt(5);
      }
      if (random.nextInt(2) == 1) i = i + random.nextInt(10);

    }

  }


  void loadUpperCheckerboard()
  {
    int oddOrEven = 0;
    int count = 0;
    int icount = 0;
    int jStartValue = 2;
    boolean flag = true;
    for (int i = 2; i < cells.length - 2; i++)
    {
      for (int j = jStartValue; j < cells[0].length - 2; j++)
      {

        cells[i][j] = 1;

        j++;
        count++;
        if (count == 7)
        {
          j += 2;
          count = 0;

        }


      }
      if (icount == 7)
      {
        icount = 0;
      } else icount++;
      count = icount;
      if (oddOrEven == 0) oddOrEven = 1;
      else oddOrEven = 0;
      jStartValue++;
      //count;;

    }
  }

  /**
   * loads the glider gun pattern, the cell locations are hard coded
   */

  void loadGliderGun()
  {
    cells[7][2] = 1;
    cells[8][2] = 1;
    cells[7][3] = 1;
    cells[8][3] = 1;

    cells[7][12] = 1;
    cells[6][13] = 1;
    cells[5][14] = 1;
    cells[5][15] = 1;
    cells[8][12] = 1;

    cells[8][16] = 1;
    cells[6][17] = 1;
    cells[10][17] = 1;

    cells[9][18] = 1;
    cells[8][18] = 1;
    cells[8][19] = 1;
    cells[7][18] = 1;

    cells[7][22] = 1;
    cells[6][22] = 1;
    cells[5][22] = 1;
    cells[4][24] = 1;
    cells[4][26] = 1;
    cells[3][26] = 1;

    cells[7][23] = 1;
    cells[6][23] = 1;
    cells[5][23] = 1;
    cells[8][24] = 1;
    cells[8][26] = 1;
    cells[9][26] = 1;

    cells[5][36] = 1;
    cells[6][36] = 1;
    cells[5][37] = 1;
    cells[6][37] = 1;


    cells[9][12] = 1;
    cells[10][13] = 1;
    cells[11][14] = 1;
    cells[11][15] = 1;

  }

  /**
   * @param now is the current toime
   *            this method is overriider method og animation timer class
   *            this method starts when the start button is clicked and updates the board is the threads are runinng and calculating next generation
   */
  @Override
  public void handle(long now)
  {
    if ((now - lastFrameUpdate) > 500_000)
    {
      if (Board.running)
      {
        updateBoard(cells);
        long lastFrameUpdate = now;
      }

    }
  }
}
