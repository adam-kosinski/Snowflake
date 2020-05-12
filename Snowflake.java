import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//code based on https://mathematica.stackexchange.com/questions/39361/how-to-generate-a-random-snowflake

public class Snowflake extends Application
{
  private ArrayList<ArrayList<Integer>> grid;
  private ArrayList<Double> p_freeze;
  private ArrayList<Double> p_melt;
  private Canvas canvas;

  private final double HEX_WIDTH;

  public Snowflake()
  {
    //create grid
    grid = new ArrayList<>();
    for(int r=0; r<11; r++)
    {
      ArrayList<Integer> row = new ArrayList<>();
      for(int c=0; c<11; c++)
      {
        row.add(0);
      }
      grid.add(row);
    }

    //set p_freeze and p_melt
    p_freeze = new ArrayList<>(Arrays.asList(1.0, 0.2, 0.1, 0.0, 0.2, 0.1, 0.1, 0.0, 0.1, 0.1, 1.0, 1.0, 0.0));
    p_melt = new ArrayList<>(Arrays.asList(0.0, 0.7, 0.5, 0.5, 0.0, 0.0, 0.0, 0.3, 0.5, 0.0, 0.2, 0.1, 0.0));
    //all the numbers need decimal points or it breaks

    canvas = new Canvas(600,600);
    HEX_WIDTH = 30;
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

  }

  private ArrayList<Boolean> getNeighborhood(int row, int col)
  {
    return null;
  }

  private void updateState(int row, int col)
  {

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
        int center_r = (int) Math.floor(grid.size()/2);
        int center_c = (int) Math.floor(grid.get(0).size()/2);

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
