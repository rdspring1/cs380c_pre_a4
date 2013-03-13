package cs380C.compiler;

import java.util.*;

// Dead Code Elimination
public class DCE extends Optimization {
	private static DCE instance;
	private DCE() { }
	
	public static DCE instance() 
	{
		if(instance == null)
			instance = new DCE();
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
		graph.updateCFG();
		DCELA dcela = new DCELA(cmdlist, graph);
		dcela.liveAnalysis();
		HashMap<Integer, Set<String>> analysis = dcela.getLiveVariables();
		
		ListIterator<String> iter = cmdlist.listIterator();
		while(iter.hasNext())
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			int blocknum = getBlockNum(graph, numline);
			
			if(blocknum > 0 && checkRemove(cmd, analysis.get(blocknum)))
			{
				removeLine(numline);
				return true;
			}
			++numline;
		}	
		return false;
	}
	
	protected boolean checkRemove(String[] cmd, Set<String> set) {
		// Determine if the command is live or dead
		if(DCELA.DEFCMD.contains(cmd[0]))
		{		
			if(cmd[2].contains("#") && !set.isEmpty())
				return !set.contains(cmd[2].split("#")[0]);
		}
		return false;
	}
}
