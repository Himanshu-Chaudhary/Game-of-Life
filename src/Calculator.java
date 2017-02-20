import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Himanshu Chaudhary
 * this class calculates the next generation and implements threads, the run method has the await method where all the threads wait for the next generation to complete.
 * The grid is divided by columns, each thread is assigned to a seperate piece of column
 * as soon as the next generation has been updates, the code in runnable method of cyclic barrier in Board class  runs (which changes the pointers)
 */
public class Calculator implements Runnable
{
  static byte[][] tempCells = new byte[Board.HEIGHT + 2][Board.WIDTH + 2];
  int startColumn;
  int endColumn;
  private CyclicBarrier barrier;

  /**
   * @param barrier     is the barrier that syncs the thread and causes them to wait after update of each generation
   * @param startColumn is the start column assigned to each thread to update
   * @param endColumn   is the end column asssigned to each thread to complete
   */
  public Calculator(CyclicBarrier barrier, int startColumn, int endColumn)
  {
    this.barrier = barrier;
    this.startColumn = startColumn;
    this.endColumn = endColumn;
  }

  /**
   * This method is calculates calculates the next generation for that particular thread and also checks if the GUI is paused by the user
   */

  @Override
  public void run()
  {
    while (true)
    {
      if (Board.running)
      {     // if the board has been paused by the GUI
        nextGeneration(startColumn, endColumn);
        try
        {
          barrier.await();
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        } catch (BrokenBarrierException e)
        {
          e.printStackTrace();
        }
      } else
      {
        try
        {
          Thread.sleep(500); // thread checks if the Board.running flag has been set to true every 500millisecond so improve perfromance
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * initializes the temp cells if new game is played
   */
  static void initializeCells()
  {
    tempCells = new byte[Board.HEIGHT + 2][Board.WIDTH + 2];
  }


  /**
   * @param startColumn is the start column assigned to each thread to update
   * @param endColumns  is the end column assigned to each thread to update
   *                    <p>
   *                    This function updates the static Temp byte arry to next generation which is themn used to update the GUI
   */
  void nextGeneration(int startColumn, int endColumns)
  {
    for (int i = 1; i < (Board.HEIGHT + 1); i++)
    {
      for (int j = startColumn; j < endColumns; j++)
      {

        /* if any neighbour is greater than 1, totalAlive is increased by 1*/
        int TotalAliveNumber = (isAlive(Board.cells[i - 1][j - 1]) & 1) +
        (isAlive(Board.cells[i - 1][j]) & 1) + (isAlive(Board.cells[i - 1][j + 1]) & 1) +
        (isAlive(Board.cells[i][j - 1]) & 1) + (isAlive(Board.cells[i][j + 1]) & 1) +
        (isAlive(Board.cells[i + 1][j - 1]) & 1) + (isAlive(Board.cells[i + 1][j]) & 1) +
        (isAlive(Board.cells[i + 1][j + 1]) & 1);

        tempCells[i][j] = updateCell(Board.cells[i][j], TotalAliveNumber);
      }
    }

  }

  /**
   * @param current the current cell which needs to be checked if it is alive or not
   * @return 1 if cell is alive and 0 if not
   */
  byte isAlive(byte current)
  {
    if (current == 0) return 0;
    return 1;
  }

  /**
   * @param current     is the cell that needs to be updated
   * @param aliveNumber id the total numbers of neighbours alive
   * @return the updates value of cells based on the conditions of the game
   */
  byte updateCell(byte current, int aliveNumber)
  {
    if (aliveNumber == 3 && current == 0)
    {

      return 1;
    }

    if (current >= 1 && (aliveNumber == 3 || aliveNumber == 2))
    {
      if (current == 9) return current;
      return (byte) (current + 1);
    }

    if (aliveNumber > 3 || aliveNumber <= 2)
    {
      return 0;
    }


    return 0;
  }

}
