package cs380C.compiler;

import java.util.*;

public abstract class LA {
	public static List<String> DEFCMD = Arrays.asList("store", "move");
	
	protected LinkedList<String> cmdlist = new LinkedList<String>();
	protected CFG cfg;
	
	protected LinkedHashMap<Integer, Set<String>> in = new LinkedHashMap<Integer, Set<String>>();
	protected LinkedHashMap<Integer, Set<String>> out = new LinkedHashMap<Integer, Set<String>>();
	
	public LA(LinkedList<String> input, CFG cfg)
	{
		this.cmdlist = input;
		this.cfg = cfg;
		setupAnalysis();
		setupBlocks();
	}
	
	private void setupBlocks()
	{
		Iterator<Integer> funcIter = cfg.iterator();
		while(funcIter.hasNext())
		{
			Integer func = funcIter.next();
			SortedSet<Integer> nodes = cfg.getNodes(func);
			for(Integer block : nodes)
			{
				in.put(block, new TreeSet<String>());
				out.put(block, new TreeSet<String>());
			}
		}
	}
	
	/**
	 * @return Perform Live Analysis
	 */
	public abstract void liveAnalysis();
	
	protected abstract void setupAnalysis();
}
