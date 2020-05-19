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
import java.util.concurrent.*;
import java.util.Optional;

//code based on https://mathematica.stackexchange.com/questions/39361/how-to-generate-a-random-snowflake

public class Snowflake extends Application
{
  private int n_iterations;
  private long delay_between_iterations;
  private int grid_width;
  private int grid_height;
  private final double HEX_WIDTH;
  private ArrayList<Double> p_freeze;
  private ArrayList<Double> p_melt;

  private static Random rand;
  double cur_random; //stores a random value from 0 to 1, updated each time doIteration() is called
  double iterations_done;

  private ArrayList<ArrayList<Integer>> grid;
  private ArrayList<ArrayList<Integer>> grid_buffer;
  private int center_r;
  private int center_c;

  private Canvas canvas;

  static
  {
	rand = new Random();
  }

  public Snowflake()
  {
    n_iterations = 20;
    delay_between_iterations = 150; //long type, in ms
    grid_width = n_iterations*2 + 1;
    grid_height = n_iterations*2 + 1;
    // p 1 to 14 from stack overflow combos except first combo is all melted
    //p_freeze = new ArrayList<>(Arrays.asList(0.0, 1.0, 0.4, 0.33, 0.5, 0.7, 0.4, 0.4, 0.66, 0.5, 0.6, 0.6, 0.83, 0.2));
    //p_melt = new ArrayList<>(Arrays.asList(0.5, 0.0, 0.6, 0.66, 0.5, 0.3, 0.6, 0.6, 0.33, 0.5, 0.4, 0.4, 0.17, 0.8));
    p_freeze = new ArrayList<>(Arrays.asList(0.0, 1.0, 0.2, 0.1, 0.0, 0.2, 0.1, 0.1, 0.0, 0.1, 0.1, 1.0, 1.0, 0.0));
    p_melt = new ArrayList<>(Arrays.asList(0.5, 1.0, 0.3, 0.5, 0.5, 1.0, 1.0, 1.0, 0.7, 0.5, 1.0, 0.8, 0.9, 1.0));

    //all the numbers need decimal points or it breaks
    HEX_WIDTH = 5;

    cur_random = 0.5; //default, never used though

    center_r = (int) Math.floor(grid_height/2);
    center_c = (int) Math.floor(grid_width/2);

    canvas = new Canvas(600,600);

    setup();
}

  private void setup()
  {
    iterations_done = 0;

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
      grid_buffer.add(row);
    }
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

    //animate the snowflake growth
      Runnable task = new Runnable(){
      public void run(){
        if(iterations_done >= n_iterations) {
          //don't do anything - doesn't stop the runnable from executing, but nothing happens - intended behavior to allow restart to work
        }
        else {
          doIteration();
        }
      }
    };
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(task, delay_between_iterations, delay_between_iterations, TimeUnit.MILLISECONDS);

    canvas.setOnMouseClicked(e->{setup();}); //restart the animation when you click
  }

  private void doIteration()
  {
    cur_random = rand.nextDouble();

    //iterates through all the cells, calling updateState() on all of them
    for(int r=0; r<grid.size(); r++)
    {
      for(int c=0; c<grid.get(0).size(); c++)
      {
          if(! (r == center_r && c == center_c)) //never change the center one
          {
            if(grid.get(r).get(c) == 1 || getNeighborhood(r,c).contains(1)) //optimization check
            {
              int new_state = updateState(r,c); //1 if freeze, 0 if melt
              grid_buffer.get(r).set(c, new_state);
            }
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
    iterations_done += 1;
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
    ArrayList<String> combos = new ArrayList<String>(Arrays.asList("000000","000001","000011","000101","000111","001001","001011","001101","001111", "010101", "010111","011011","011111","111111"));
    String neighborStatesString = "";

    int combo_numb = 0;

    for(int a = 0; a < neighborStates.size(); a ++)
    {
      	String temp = Integer.toString(neighborStates.get(a));
		neighborStatesString += temp;
    }

    test_loop:
    for(int j = 0; j < neighborStatesString.length(); j ++) //rotate order of neighborStatesString
    {

      neighborStatesString = neighborStatesString.substring(1) + neighborStatesString.substring(0,1);

          for(int l = 0; l < combos.size(); l ++) //iterate thru combos X-X
          {

            if(neighborStatesString.equals(combos.get(l)))
            {
              combo_numb = l;
              break test_loop;
            }
          }
     }

    //get factor to multiply probabilities by: 0 at the center, 1 near the tips
    double[] coords = getHexCoords(row, col);
    double[] center = {canvas.getWidth()/2, canvas.getHeight()/2};
    double dist_from_center = Math.hypot(coords[0]-center[0], coords[1]-center[1]);
    dist_from_center /= HEX_WIDTH;
    double factor = iterations_done == 0 ? 1 : dist_from_center / iterations_done;
    factor = factor*factor; //scaling quadratically works better for not changing the center too much, still ranges 0 to 1

    double combo_pFreeze = factor*p_freeze.get(combo_numb);
    double combo_pMelt = factor*p_melt.get(combo_numb);
    if(grid.get(row).get(col) == 0)
    {
      if(cur_random < combo_pFreeze)
      {
          return 1;
      }
      return 0;
    }
    else //if frozen
    {
      if(cur_random < combo_pMelt)
      {
        return 0;
      }
      return 1;
    }

  }

  private void drawGrid()
  {

    GraphicsContext ctx = canvas.getGraphicsContext2D();
	ctx.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
    ctx.setFill(Color.BLACK);
    ctx.fillRect(0,0,canvas.getWidth(),canvas.getHeight());

	//iterate through grid, drawing all the hexagons
    for(int r=0; r<grid.size(); r++)
    {
      for(int c=0; c<grid.get(0).size(); c++)
      {
        //draw hexagon
        double[] coords = getHexCoords(r, c);
        drawHexagon(ctx, coords[0], coords[1], grid.get(r).get(c));
      }
    }
  }

  private double[] getHexCoords(int r, int c)
  {
    double center_x = canvas.getWidth()/2;
    double center_y = canvas.getHeight()/2;

    double x = center_x + (c - center_c)*HEX_WIDTH;
    double y_offset = HEX_WIDTH*(3/(2*Math.sqrt(3)));
    double y = center_y + (r - center_r)*y_offset;
    if((r-center_r) % 2 != 0)
    {
      x += HEX_WIDTH/2;
    }

    double[] out = {x, y};
    return out;
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
      ctx.setFill(getColor(x,y));
      ctx.fillPolygon(x_points, y_points, 6);
    }
    //ctx.strokePolygon(x_points, y_points, 6); //6 points
  }

  private Paint getColor(double x, double y) //x and y are pixel coordinates of center of hexagon
  {
    double center_x = canvas.getWidth()/2;
    double center_y = canvas.getHeight()/2;

    double dx = x-center_x;
    double dy = y-center_y;
    double r = Math.hypot(dx,dy);
    if(r < 0.0001)
    {
      return Color.WHITE;
    }

    //determine radial fraction for interpolation
    double theta = Math.acos(dx/r);
    if(dy < 0){theta = 2*Math.PI-theta;}
    double radial_fraction = 2*Math.abs((theta % (Math.PI/3.0)) / (Math.PI/3.0) - 0.5);

    //determine center fraction for interpolation
    double center_fraction = r/(iterations_done*HEX_WIDTH);
    if(center_fraction < 0.25){center_fraction = 0;}
    else if(center_fraction > 0.75){center_fraction = 1;}
    else {center_fraction = 2*(center_fraction-0.25);}

    //interpolate and return
    Color tint = Color.FUCHSIA.interpolate(Color.ROYALBLUE, center_fraction);
    return tint.interpolate(Color.WHITE, radial_fraction);
  }
}
