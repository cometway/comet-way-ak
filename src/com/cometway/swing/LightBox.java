
package com.cometway.swing;

import com.cometway.ak.*;
import com.cometway.props.*;
import java.awt.*;
import java.util.*;


/**
* This agent displays a frame that contains Lights, or more accurately
* colored squares, that are drawn in a grid style array. The LightBox
* agent registers itself with the Service Manager so that it may be found
* and used by other agents to display state information.
*/

public class LightBox extends AbstractJFrameController
{
	public final static int BLACK = 0;
	public final static int WHITE = 1;
	public final static int RED = 2;
	public final static int GREEN = 3;
	public final static int BLUE = 4;
	public final static int YELLOW = 5;

	public final static int DIM_RED = 6;
	public final static int DIM_GREEN = 7;
	public final static int DIM_BLUE = 8;

	public final static int RED0 = 10;
	public final static int RED1 = 11;
	public final static int RED2 = 12;
	public final static int RED3 = 13;
	public final static int RED4 = 14;

	public final static int GREEN0 = 20;
	public final static int GREEN1 = 21;
	public final static int GREEN2 = 22;
	public final static int GREEN3 = 23;
	public final static int GREEN4 = 24;

	public final static int BLUE0 = 30;
	public final static int BLUE1 = 31;
	public final static int BLUE2 = 32;
	public final static int BLUE3 = 33;
	public final static int BLUE4 = 34;


	private final static Color[] RED_COLOR =
	{
		Color.black,
		new Color(0x440000),
		new Color(0x880000),
		new Color(0xBB0000),
		Color.red
	};

	private final static Color[] GREEN_COLOR =
	{
		Color.black,
		new Color(0x004400),
		new Color(0x008800),
		new Color(0x00BB00),
		Color.green
	};

	private final static Color[] BLUE_COLOR =
	{
		Color.black,
		new Color(0x000044),
		new Color(0x000088),
		new Color(0x0000BB),
		Color.blue
	};

	private static Random random = new Random();

	private int offset;
	private int cell_height;
	private int cell_width;
	private boolean cell_filled;
	private boolean cell_circles;
	private int grid_height;
	private int grid_width;
	private boolean grid_visible;
	private Insets insets;

	private int[] state;
	private boolean[] changed;
	private boolean exit;


	/**
	* Initializes the Props for this agent.
	*/

	public void initProps()
	{
		setDefault("service_name", "light_box");
		setDefault("frame_title", "Light Box");
		setDefault("frame_visible", "true");
		setDefault("cell_height", "16");
		setDefault("cell_width", "16");
		setDefault("cell_filled", "true");
		setDefault("cell_circles", "false");
		setDefault("grid_height", "32");
		setDefault("grid_width", "32");
		setDefault("grid_visible", "true");
		setDefault("offset", "13");
	}


	/**
	* Opens the LightBox frame and registers this agent with the Service Manager.
	*/

	public void start()
	{
		offset = getInteger("offset");
		cell_height = getInteger("cell_height");
		cell_width = getInteger("cell_width");
		cell_filled = getBoolean("cell_filled");
		cell_circles = getBoolean("cell_circles");
		grid_height = getInteger("grid_height");
		grid_width = getInteger("grid_width");
		grid_visible = getBoolean("grid_visible");

		int size = grid_height * grid_width;
		state = new int[size];
		changed = new boolean[size];

		openFrame();
		ServiceManager.register(getString("service_name"), this);
	}


	/**
	* Returns the current state of the specified cell.
	*/

	public int getCellState(int index)
	{
		return (state[index]);
	}


	/**
	* Returns the current state of the specified cell.
	*/

	public int getCellState(int h, int v)
	{
		return (state[h * grid_height + v]);
	}


	/**
	* Returns the index of the specified cell.
	*/

	public int getCellIndex(int h, int v)
	{
		return (h * grid_height + v);
	}


	/**
	* Returns the index of a random empty cell.
	*/

	public int getRandomEmptyCell()
	{
		int index = -1;
		int h = Math.abs(random.nextInt()) % grid_width;
		int v = Math.abs(random.nextInt()) % grid_height;
		int startIndex = getCellIndex(h, v);

		// If our random attempt fails, find the next adjacent empty cell.

		if (state[startIndex] != 0)
		{
			int i = startIndex;

			while (i < state.length)
			{
				if (state[i] == 0)
				{
					index = i;
					break;
				}

				i++;
			}

			if (index == -1)
			{
				for (i = 0; i < startIndex; i++)
				{
					if (state[i] == 0)
					{
						index = i;
						break;
					}
				}
			}
		}
		else
		{
			index = startIndex;
		}

		return (index);
	}


	/**
	* Creates and initializes the JFrame used to display the Light Box.
	*/

	protected void openFrame()
	{
		int frame_height = (grid_height * cell_height) + (offset * 2);
		int frame_width = (grid_width * cell_width) + (offset * 2);

		/* The values returned by getInsets() is wrong until the frame is shown. */

		frame = createFrame();
		insets = frame.getInsets();

		frame.setBackground(Color.black);
		frame.setResizable(false);
		frame.setSize(frame_width, frame_height);
		frame.setVisible(true);

		/* The values returned by getInsets() should be correct now. */

		insets = frame.getInsets();
		frame_height = (grid_height * cell_height) + (offset * 2) + insets.top + insets.bottom;
		frame_width = (grid_width * cell_width) + (offset * 2) + insets.left + insets.right;
		frame.setSize(frame_width, frame_height);

		frame.addWindowListener(createWindowListener());
		frame.setTitle(getString("frame_title"));
		frame.setVisible(getBoolean("frame_visible"));
	}


	/** This method is called to draw the entire frame contents. */

	public void drawFrame(Graphics g)
	{
		if (grid_visible)
		{
			int height = grid_height * cell_height;
			int width = grid_width * cell_width;

			g.setColor(Color.gray);
			g.drawRect(insets.left + offset, insets.top + offset, width, height);

			for (int x = 1; x < grid_width; x++)
			{
				int h = x * cell_width + insets.left + offset;

				g.drawLine(h, insets.top + offset, h, insets.top + offset + height);
			}

			for (int y = 1; y < grid_height; y++)
			{
				int v = y * cell_height + insets.top + offset;

				g.drawLine(insets.left + offset, v, insets.left + offset + width, v);
			}
		}

		drawCells(g, false);
	}


	/** This method draws a single cell based on its stored state. */

	protected void drawCell(Graphics g, int h, int v)
	{
		int cellState = state[h * grid_height + v];

		switch (cellState)
		{
			default:
			case BLACK:
				g.setColor(Color.black);
			break;

			case WHITE:
				g.setColor(Color.white);
			break;

			case RED:
				g.setColor(RED_COLOR[4]);
			break;

			case GREEN:
				g.setColor(GREEN_COLOR[4]);
			break;

			case BLUE:
				g.setColor(BLUE_COLOR[4]);
			break;

			case YELLOW:
				g.setColor(Color.yellow);
			break;

			case DIM_RED:
				g.setColor(RED_COLOR[2]);
			break;

			case DIM_GREEN:
				g.setColor(GREEN_COLOR[2]);
			break;

			case DIM_BLUE:
				g.setColor(BLUE_COLOR[2]);
			break;

			case RED0: case RED1: case RED2: case RED3: case RED4:
				g.setColor(RED_COLOR[cellState - 10]);
			break;

			case GREEN0: case GREEN1: case GREEN2: case GREEN3: case GREEN4:
				g.setColor(GREEN_COLOR[cellState - 20]);
			break;

			case BLUE0: case BLUE1: case BLUE2: case BLUE3: case BLUE4:
				g.setColor(BLUE_COLOR[cellState - 30]);
			break;
		}

		if (cellState == 0)
		{
			g.fillRect(h * cell_width + insets.left + offset + 1, v * cell_height + insets.top + offset + 1, cell_width - 1, cell_height - 1);
		}
		else
		{
			if (cell_filled)
			{
				if (cell_circles)
				{
					g.fillOval(h * cell_width + insets.left + offset + 1, v * cell_height + insets.top + offset + 1, cell_width - 1, cell_height - 1);
				}
				else
				{
					g.fillRect(h * cell_width + insets.left + offset + 1, v * cell_height + insets.top + offset + 1, cell_width - 1, cell_height - 1);
				}
			}
			else
			{
				if (cell_circles)
				{
					g.drawOval(h * cell_width + insets.left + offset + 1, v * cell_height + insets.top + offset + 1, cell_width - 2, cell_height - 2);
				}
				else
				{
					g.drawRect(h * cell_width + insets.left + offset + 1, v * cell_height + insets.top + offset + 1, cell_width - 2, cell_height - 2);
				}
			}
		}
	}


	/** This method draws all of the cells. It will only draw changed ones if specified. */

	protected void drawCells(Graphics g, boolean changedOnly)
	{
		int index = 0;

		for (int h = 0; h < grid_width; h++)
		{
			for (int v = 0; v < grid_height; v++)
			{
				if ((changedOnly == false) || changed[index])
				{
					drawCell(g, h, v);
					changed[index] = false;
				}

				index++;
			}
		}
	}


	/** Called by clients to immediately draw all changed cells. */

	public void drawChangedCells()
	{
		Graphics g = frame.getGraphics();

		drawCells(g, true);
	}


	/**
	* Called by clients to set the state of a specific cell
	* designated by its index in the array of cells.
	*/

	public void setCellState(int index, int newState)
	{
		int previousState = state[index];

		if (previousState != newState)
		{
			state[index] = newState;
			changed[index] = true;
		}
	}


	/**
	* Called by clients to set the state of a specific cell
	* designated by horizontal and vertical coordinates.
	*/

	public void setCellState(int h, int v, int newState)
	{
		int index = h * grid_height + v;

		debug("setCellState(" + h + ", " + v + ", " + newState + ") index = " + index);

		setCellState(index, newState);
	}
}


