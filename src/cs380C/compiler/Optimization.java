package cs380C.compiler;

import java.util.*;

public abstract class Optimization {
	private static List<String> REGDEF = Arrays.asList("add", "sub", "mul", "div", "mod", "neg", "cmpeq", "cmple", "cmplt", "load", "read");
	protected LinkedList<String> cmdlist;
	
	public abstract LinkedList<String> performOptimization(LinkedList<String> input);
	
	/**
	 * Remove unnecessary instructions by finding unused register references
	 */
	protected void removeUnusedReferences() {
		SortedSet<Integer> def = new TreeSet<Integer>();
		int linenum = 1;
		for(String line : cmdlist)
		{
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(REGDEF.contains(cmd[0]))
				def.add(linenum);
			
			for(int i = 1; i < cmd.length; ++i)
			{
				if(cmd[i].contains("(") && cmd[i].contains(")"))
					def.remove(Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1)));
			}
			++linenum;
		}
	
		for(int line : def)
			removeLine(line);
	}
	
	/**
	 * 	Remove line from command list and update line numbers and references
	 * @param linechange - the line number that will be removed from the command list
	 */
	protected void removeLine(int linechange) {
		ListIterator<String> iter = cmdlist.listIterator();
		int numline = 1;
		while(iter.hasNext())
		{
			String line = iter.next();
			if(numline == linechange)
				iter.remove();
			else
				iter.set(updateLine(line, numline, linechange));
			++numline;
		}
	}
	
	/**
	 * 
	 * Update the line number and references in the line
	 * @param line - the command at the given line number
	 * @param numline - current line number
	 * @param linechange - line number 
	 * @return Updated version of the command
	 */
	protected String updateLine(String line, int numline, int linechange) {
		int linenum  = Integer.valueOf(line.split(":")[0].trim().split("\\s")[1]);
		
		if(numline >= linechange)
			--linenum;
		
		String[] cmd = line.split(":")[1].trim().split("\\s");
		for(int i = 1; i < cmd.length; ++i)
		{
			if(cmd[i].contains("(") && cmd[i].contains(")") && numline >= linechange)
			{
				int reference = Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1));
				--reference;
				cmd[i] = "(" + reference + ")"; 
			}
			else if(cmd[i].contains("[") && cmd[i].contains("]"))
			{
				int reference = Integer.valueOf(cmd[i].substring(1, cmd[i].length() - 1));
				
				if(reference > linechange)
				{
					--reference;
					cmd[i] = "[" + reference + "]";
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("    instr " + linenum + ": " + cmd[0]);
		for(int i = 1; i < cmd.length; ++i)
		{
			sb.append(" " + cmd[i]);
		}
		return sb.toString();
	}

	/**
	 * @param graph
	 * @param numline
	 * @return the block number for the current line
	 */
	protected int getBlockNum(CFG graph, int numline) {
		int function = graph.getCurrentFunction(numline);
		
		if(function != -1)
			return graph.getCurrentBlock(function, numline);
		else
			return -1;
	}
}
