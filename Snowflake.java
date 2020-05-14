import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

//code based on https://mathematica.stackexchange.com/questions/39361/how-to-generate-a-random-snowflake

public class Snowflake extends Application
{
  private int grid_width;
  private int grid_height;
  private final double HEX_WIDTH;
  private ArrayList<Double> p_freeze;
  private ArrayList<Double> p_melt;

  double cur_random; //stores a random value from 0 to 1, updated each time doIteration() is called

  private ArrayList<ArrayList<Integer>> grid;
  private ArrayList<ArrayList<Integer>> grid_buffer;
  private int center_r;
  private int center_c;
  private ArrayList<ArrayList<Boolean>> updated_cells; //copy of the grid, storing whether the cell has been updated (0=no, 1=yes)

  private Canvas canvas;


  public Snowflake()
  {
    grid_width = 31;
    grid_height = 31;
    p_freeze = new ArrayList<>(Arrays.asList(1.0, 0.2, 0.1, 0.0, 0.2, 0.1, 0.1, 0.0, 0.1, 0.1, 1.0, 1.0, 0.0));
    p_melt = new ArrayList<>(Arrays.asList(0.0, 0.7, 0.5, 0.5, 0.0, 0.0, 0.0, 0.3, 0.5, 0.0, 0.2, 0.1, 0.0));
    //all the numbers need decimal points or it breaks
    HEX_WIDTH = 10;

    cur_random = 0.5; //default, never used though

    //create grid
    grid = new ArrayList<>();
    for(int r=0; r<grid_height; r++)
    {
      ArrayList<Integer> row = new ArrayList<>();
      for(int c=0; c<grid_width; c++)
      {
        row.add(0);
      }
      grid.add(row);
    }
    center_r = (int) Math.floor(grid.size()/2);
    center_c = (int) Math.floor(grid.get(0).size()/2);
    grid.get(center_r).set(center_c, 1); //start with a frozen particle in the center

    //make grid_buffer
    grid_buffer = new ArrayList<>();
    for(int r=0; r<grid_height; r++)
    {
      ArrayList<Integer> row = new ArrayList<>();
      for(int c=0; c<grid_width; c++)
      {
        row.add(grid.get(r).get(c));
      }
      grid.add(row);
    }

    //create updated_cells array List
    updated_cells = new ArrayList<>();
    for(int r=0; r<grid_height; r++)
    {
      ArrayList<Boolean> row = new ArrayList<>();
      for(int c=0; c<grid_width; c++)
      {
        row.add(false);
      }
      updated_cells.add(row);
    }
    updated_cells.get(center_r).set(center_c, true); //the middle cell starts filled



    canvas = new Canvas(600,600);
}
  @Override
  public void start(Stage stage)
  {
    drawGrid();

    //display stuff
    Group group = new Group(canvas);
    Scene scene = new Scene(group, 600, 600);
    stage.setScene(scene);
    stage.show();
  }

  private void doIteration()
  {
    Random rand = new Random();
    cur_random = rand.nextDouble();

    //iterates through all the cells, calling updateState() on all the ones we haven't updated before
    for(int r=0; r<grid.size(); r++)
    {
      for(int c=0; c<grid.get(0).size(); c++)
      {
        if(updated_cells.get(r).get(c) == false && getNeighborhood(r,c).contains(1))
        {
          int new_state = updateState(r,c); //1 if freeze, 0 if melt
          grid_buffer.get(r).set(c, new_state);
          updated_cells.get(r).set(c, true);
        }
      }
    }
    //copy grid buffer over to main grid
    for(int r=0; r<grid_height; r++)
    {
      for(int c=0; c<grid_width; c++)
      {
        grid.get(r).set(c, grid_buffer.get(r).get(c));
      }
    }

    drawGrid();
  }

  private ArrayList<Integer> getNeighborhood(int row, int col)
  {
    //returns an arraylist of the values of the neighbors, in order

    boolean shifted = (row-center_r) % 2 != 0;
    ArrayList<Integer> neighborhood = new ArrayList<>();
    //add the cells, starting from right, going counterclockwise
    try{neighborhood.add(grid.get(row).get(col+1));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    try{neighborhood.add(grid.get(row-1).get(shifted? col+1 : col));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    try{neighborhood.add(grid.get(row-1).get(shifted? col : col-1));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    try{neighborhood.add(grid.get(row).get(col-1));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    try{neighborhood.add(grid.get(row+1).get(shifted? col : col-1));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    try{neighborhood.add(grid.get(row+1).get(shifted? col+1 : col));}
    catch(IndexOutOfBoundsException ex){neighborhood.add(0);}

    return neighborhood;
  }

  private int updateState(int row, int col) //return 1 if freeze, 0 if melt
  {
    ArrayList<Integer> neighborStates = getNeighborhood(row,col);
    ArrayList<String> combos = new ArrayList<String>(Arrays.asList("000001","000011","000101","000111","001001","001011","001101","001111", "010101", "010111","011011","011111","111111"));
    String neighborStatesString = "";

    int combo_numb = 0;

    for(int a = 0; a < neighborStates.size(); a ++)
    {
      	String temp = Integer.toString(neighborStates.get(a));
	neighborStatesString += temp;
    }

    for(int j = 0; j < neighborStatesString.length(); j ++) //rotate order of neighborStatesString
    {
    	String stateCombos = neighborStatesString.substring(1) + neighborStatesString.substring(0,1);
        
          for(int l = 0; l < combos.size(); l ++) //iterate thru combos X-X
          {
            if(stateCombos.equals(combos.get(l)))
            {
              combo_numb = l;
            }
          }
     }


    double combo_pFreeze = p_freeze.get(combo_numb);
    double combo_pMelt = p_melt.get(combo_numb);
    if(cur_random < combo_pFreeze)
    {
      return 1;
    }
    else if(cur_random > combo_pMelt)
    {
      return 0;
    }
    else
    {
      return 1;
    }

  }

  private void drawGrid()
  {
    double center_x = canvas.getWidth()/2;
    double center_y = canvas.getHeight()/2;
    GraphicsContext ctx = canvas.getGraphicsContext2D();
    for(int r=0; r<grid.size(); r++)
    {
      for(int c=0; c<grid.get(0).size(); c++)
      {
        //draw hexagon
        double x = center_x + (c - center_c)*HEX_WIDTH;
        double y_offset = HEX_WIDTH*(3/(2*Math.sqrt(3)));
        double y = center_y + (r - center_r)*y_offset;
        if((r-center_r) % 2 != 0)
        {
          x += HEX_WIDTH/2;
        }
        drawHexagon(ctx, x, y, grid.get(r).get(c));
      }
    }
  }

  private void drawHexagon(GraphicsContext ctx, double x, double y, int grid_value) //x,y of center of hexagon
  {
    //get points starting straight up and going clockwise
    double[] x_points = {
      x,
      x-0.5*HEX_WIDTH,
      x-0.5*HEX_WIDTH,
      x,
      x+0.5*HEX_WIDTH,
      x+0.5*HEX_WIDTH
    };
    double[] y_points = {
      y-HEX_WIDTH/Math.sqrt(3),
      y-HEX_WIDTH/(2*Math.sqrt(3)),
      y+HEX_WIDTH/(2*Math.sqrt(3)),
      y+HEX_WIDTH/Math.sqrt(3),
      y+HEX_WIDTH/(2*Math.sqrt(3)),
      y-HEX_WIDTH/(2*Math.sqrt(3))
    };
    if(grid_value != 0)
    {
      ctx.fillPolygon(x_points, y_points, 6);
    }
    ctx.strokePolygon(x_points, y_points, 6); //6 points
  }
}
