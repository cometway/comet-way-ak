package com.cometway.biolife;import com.cometway.ak.*;import com.cometway.props.*;import com.cometway.swing.*;import java.util.*;public class SlideAgent extends ServiceAgent implements Runnable{	static Random random = new Random();	protected int grid_height;	protected int grid_width;	protected GeneticAgentInterface[] agentList;	protected LightBox microscope;	public void initProps()	{		props.setDefault("service_name", "slide");		props.setDefault("sleep_time", "2000");		props.setDefault("grid_height", "32");		props.setDefault("grid_width", "32");	}	public void start()	{		grid_height = props.getInteger("grid_height");		grid_width = props.getInteger("grid_width");		agentList = new GeneticAgentInterface[grid_height * grid_width];		microscope = (LightBox) ServiceManager.getService("microscope");		register();		Thread t = new Thread(this);		t.start();	}	public void run()	{		while (true)		{debug("thump...");			for (int i = 0; i < agentList.length; i++)			{				GeneticAgentInterface agent = agentList[i];					if (agent != null)				{debug("thump[" + i + "] = " + agent);					try					{						agent.heartbeat();					}					catch (Exception e)					{						agent.error("heartbeat failed", e);					}				}			}				microscope.drawChangedCells();				try			{				Thread.sleep(props.getInteger("sleep_time"));			}			catch (Exception ee)			{				error("run", ee);			}		}	}	public GeneticAgentInterface getAgent(int index)	{		return (agentList[index]);	}	public GeneticAgentInterface getAgent(int h, int v)	{		return (agentList[h * grid_width + v]);	}	public int getAgentIndex(int h, int v)	{		return (h * grid_height + v);	}	public int getRandomEmptyIndex()	{		int index = -1;		int h = Math.abs(random.nextInt()) % grid_width;		int v = Math.abs(random.nextInt()) % grid_height;		int startIndex = getAgentIndex(h, v);		// If our random attempt fails, find the next adjacent empty index.		if (agentList[startIndex] != null)		{			int i = startIndex;			while (i < agentList.length)			{				if (agentList[i] == null)				{					index = i;					break;				}				i++;			}			if (index == -1)			{				for (i = 0; i < startIndex; i++)				{					if (agentList[i] == null)					{						index = i;						break;					}				}			}		}		else		{			index = startIndex;		}		return (index);	}	public GeneticAgentInterface getNeighbor(int index, int direction)	{		return (agentList[getNeighborIndex(index, direction)]);	}	public int getNeighborIndex(int index, int direction)	{		int x = index / grid_height;		int y = mod(index, grid_height);		switch (direction)		{			// north west			case 7:				x = mod(x - 1, grid_width);			// north			case 0:				y = mod(y - 1, grid_height);			break;			// north east			case 1:				y = mod(y - 1, grid_height);			// east			case 2:				x = mod(x + 1, grid_width);			break;			// south east			case 3:				x = mod(x + 1, grid_width);			// south			case 4:				y = mod(y + 1, grid_height);			break;			// south west			case 5:				y = mod(y + 1, grid_height);			// west			case 6:				x = mod(x - 1, grid_width);			break;		}			return (x * grid_height + y);	}	public static int mod(int val, int modulus)	{		int result = val % modulus;		if (result < 0)			result += modulus;		return result;	}	public void placeAgentRandomly(GeneticAgentInterface agent)	{		int index = getRandomEmptyIndex();		setAgent(index, agent);	}	public void removeAgent(int index, GeneticAgentInterface agent)	{		debug("removeAgent(" + index + ", " + agent + ")");		if (agentList[index] == agent)		{			agentList[index] = null;		}	}	public void setAgent(int index, GeneticAgentInterface agent)	{		debug("setAgent(" + index + ", " + agent + ")");		agentList[index] = agent;	}}