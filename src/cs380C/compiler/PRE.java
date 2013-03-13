package cs380C.compiler;

import java.util.*;

public class PRE extends Optimization {	
	private static PRE instance;
	private PRE() { }
	
	public static PRE instance() 
	{
		if(instance == null)
			instance = new PRE();
		return instance;
	}
	
	@Override
	public LinkedList<String> performOptimization(LinkedList<String> input) {
		this.cmdlist = input;
		while(update());
		removeUnusedReferences();
		return cmdlist;
	}

	private boolean update() {
		int numline = 1;
		CFG graph = new CFG(cmdlist);
		PRELA prela = new PRELA(cmdlist, graph);
		prela.liveAnalysis();
		// TODO: Setup live analysis 
		
		ListIterator<String> iter = cmdlist.listIterator();
		while(iter.hasNext())
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			int blocknum = getBlockNum(graph, numline);
			
			// TODO: Update Command List - PRE
			++numline;
		}	
		return false;
	}

	protected boolean checkRemove() {
		// TODO Auto-generated method stub
		return false;
	}

}
