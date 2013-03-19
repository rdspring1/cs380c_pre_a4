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
		this.cmdarray = new ArrayList<String>(input);
		while(update());
		removeUnusedReferences();
		return cmdlist;
	}

	private boolean update() {
		CFG graph = new CFG(cmdlist);
		PRELA prela = new PRELA(cmdlist, graph);
		Map<Integer, Map<Integer, Set<Integer>>> insert = prela.getInsert();
		Map<Integer, Set<Integer>> delete = prela.getDelete();
		
		Map<Integer, String> independentVar = generateIndependentSet(graph, prela.getOffset(), insert);
		updateIndependentVar(independentVar);
		LinkedList<String> newcmdlist = executeInsert(graph, insert, executeDelete(delete), independentVar);
		
		boolean check = newcmdlist.equals(cmdlist);
		cmdlist = newcmdlist;
		cmdarray = new ArrayList<String>(newcmdlist);
		return !check;
	}

	private void updateIndependentVar(Map<Integer, String> independentVar) {
		int linenum = 1;		
		ListIterator<String> iter = cmdlist.listIterator();
		while(iter.hasNext())
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			
			for(int n = 1; n < cmd.length; ++n)
			{
				if(cmd[n].contains("(") && cmd[n].contains(")"))
				{
					Integer var = Integer.valueOf(cmd[n].substring(1, cmd[n].length() - 1));
					if(independentVar.containsKey(var))
						cmd[n] = independentVar.get(var);
				}
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("    instr " + linenum + ": " + cmd[0]);
			for(int i = 1; i < cmd.length; ++i)
			{
				sb.append(" " + cmd[i]);
			}
			iter.set(sb.toString());		
			++linenum;
		}
	}

	private Map<Integer, String> generateIndependentSet(CFG graph, Map<Integer, Integer> offset, Map<Integer, Map<Integer, Set<Integer>>> insert) {
		Map<Integer, String> independent = new TreeMap<Integer, String>();
		for(int i : insert.keySet())
		{
			for(int j : insert.get(i).keySet())
			{
				Set<Integer> vars = new TreeSet<Integer>(insert.get(i).get(j));
				for(int line : insert.get(i).get(j))
				{
					String[] cmd = cmdarray.get(line).split(":")[1].trim().split("\\s");
					for(int n = 1; n < cmd.length; ++n)
					{
						if(cmd[n].contains("(") && cmd[n].contains(")"))
							vars.remove(Integer.valueOf(cmd[n].substring(1, cmd[n].length() - 1)));
					}
				}
				
				for(Integer var : vars)
				{
					offset.put(graph.getCurrentFunction(i), offset.get(graph.getCurrentFunction(i)) - cscTranslator.LONGSIZE);
					independent.put(var, "T" + var + "#" + graph.getCurrentFunction(i));
				}
			}
		}
		return independent;
	}

	private LinkedList<String> executeInsert(CFG graph, Map<Integer, Map<Integer, Set<Integer>>> insert, Map<Integer, String> deleteSet, Map<Integer, String> independentVar) {
		LinkedList<String> newcmdlist = new LinkedList<String>(cmdlist);
		for(int i : insert.keySet())
		{
			for(int j : insert.get(i).keySet())
			{
				if(graph.getSucc(i).size() == 1)
				{
					insert(newcmdlist, graph.getEndBlock(i), insert.get(i).get(j), deleteSet, independentVar);
				}
				else if(graph.getSucc(i).size() > 1 && graph.getPred(j).size() == 1)
				{
					insert(newcmdlist, j, insert.get(i).get(j), deleteSet, independentVar);
				}
				else if(graph.getSucc(i).size() > 1 && graph.getPred(j).size() > 1)
				{
					insert(newcmdlist, j, insert.get(i).get(j), deleteSet, independentVar);
				}
			}
		}
		return newcmdlist;
	}

	// Insert at End of I
	private void insert(LinkedList<String> newcmdlist, int insertline, Set<Integer> insertSet, Map<Integer, String> deleteSet, Map<Integer, String> independentVar) {
		for(int newline : insertSet)
		{
			String line = "    instr " + insertline + ": " + deleteSet.get(newline).split(":")[1].trim();
			addLine(newcmdlist, line, insertline);
			++insertline;
		}
	}

	private Map<Integer, String> executeDelete(Map<Integer, Set<Integer>> delete) {
		Map<Integer, String> deleteSet = new HashMap<Integer, String>(); 
		TreeSet<Integer> blocks = new TreeSet<Integer>(delete.keySet());
		
		for(Integer block : blocks.descendingSet())
		{
			TreeSet<Integer> deleteBlock = new TreeSet<Integer>(delete.get(block));
			for(Integer line : deleteBlock.descendingSet())
			{
				deleteSet.put(line, removeLine(line));
			}
		}
		cmdarray = new ArrayList<String>(cmdlist);
		return deleteSet;
	}

}
