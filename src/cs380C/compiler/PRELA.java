package cs380C.compiler;

import java.util.*;

public class PRELA extends LA {
	private Map<Integer, Set<Integer>> deexpr;
	private Map<Integer, Set<Integer>> ueexpr;
	private Map<Integer, Set<Integer>> killexpr;
	private Map<Integer, Set<String>> exprs; // Variable / Expression Arguments
	private Map<Integer, Integer> localvar; // Function / Minimum Offset
	private Map<Integer, Set<Integer>> availin;
	private Map<Integer, Set<Integer>> availout;
	private Map<Integer, Set<Integer>> antin;
	private Map<Integer, Set<Integer>> antout;
	private Map<Integer, Map<Integer, Set<Integer>>> earliest;
	private Map<Integer, Set<Integer>> laterin;
	private Map<Integer, Map<Integer, Set<Integer>>> later;
	private Map<Integer, Map<Integer, Set<Integer>>> insert;
	private Map<Integer, Set<Integer>> delete;
	
	public PRELA(LinkedList<String> input, CFG cfg) {
		super(input, cfg);
	}

	@Override
	protected void setupAnalysis() {
		deexpr = new TreeMap<Integer, Set<Integer>>();
		ueexpr = new TreeMap<Integer, Set<Integer>>();
		killexpr = new TreeMap<Integer, Set<Integer>>();
		exprs = new TreeMap<Integer, Set<String>>();
		localvar = new HashMap<Integer, Integer>();
		generateEXPR();
	}

	protected void liveAnalysis() {
		generateAvail();
		generateAnt();
		generateEarliest();
		generateLater();
		generateInsert();
		generateDelete();
	}
	
	private void generateDelete() {
		assert(laterin != null);
		delete = new TreeMap<Integer, Set<Integer>>();
		
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(function != block)
				{
					Set<Integer> newset = new TreeSet<Integer>(ueexpr.get(block));
					newset.retainAll(laterin.get(block));
					delete.put(block, newset);
				}
			}
		}
	}

	private void generateInsert() {
		assert(later != null);
		assert(laterin != null);
		insert = setupEdgeSet();
		
		for(Integer i : insert.keySet())
		{
			Map<Integer, Set<Integer>> jset = insert.get(i);
			for(Integer j : jset.keySet())
			{
				Set<Integer> newset = new TreeSet<Integer>(laterin.get(j));
				newset.retainAll(later.get(i).get(j));
				jset.put(j, newset);
			}
		}
	}

	private void generateLater() {
		assert(earliest != null);
		
		List<Integer> endset = setupEndSet();
		laterin = setupNodeSet();
		later = setupEdgeSet();
		boolean update = true;
		
		while(update)
		{
			update = false;
			for(Integer i : later.keySet())
			{				
				// LATER
				Map<Integer, Set<Integer>> jset = earliest.get(i);
				for(Integer j : jset.keySet())
				{
					Set<Integer> currentset = jset.get(j);
					Set<Integer> newset = new TreeSet<Integer>(ueexpr.get(i));
					newset.retainAll(laterin.get(i));
					newset.addAll(earliest.get(i).get(j));
					jset.put(j, newset);
					
					if(!newset.equals(currentset))
						update = true;
				}
			}
			
			// LATERIN
			for(Integer j : endset)
			{
				if(!cfg.containsFunction(j))
				{
					Set<Integer> pred = cfg.getPred(j);
					if(!pred.isEmpty())
					{
						Set<Integer> currentset = laterin.get(j);
						Set<Integer> newset = new TreeSet<Integer>(later.get(pred.iterator().next()).get(j));
						for(Integer i : pred)
						{
							newset.retainAll(later.get(i).get(j));
						}
						laterin.put(j, newset);
						
						if(!currentset.equals(newset))
							update = true;
					}
				}
			}
		}
	}

	private void generateEarliest() {
		assert(antin != null);
		assert(antout != null);
		assert(availin != null);
		assert(availout != null);
		
		earliest = setupEdgeSet();
		boolean update = true;
		
		while(update)
		{
			update = false;
			for(Integer i : earliest.keySet())
			{
				if(cfg.containsFunction(i))
				{
					Map<Integer, Set<Integer>> jset = earliest.get(i);
					for(Integer j : jset.keySet())
					{
						Set<Integer> currentset = jset.get(j);
						Set<Integer> newset = new TreeSet<Integer>(killexpr.get(i));
						newset.retainAll(antout.get(i));
						newset.addAll(availout.get(i));
						newset.addAll(antin.get(j));
						jset.put(j, newset);
						
						if(!newset.equals(currentset))
							update = true;
					}
				}
				else
				{
					Map<Integer, Set<Integer>> jset = earliest.get(i);
					for(Integer j : jset.keySet())
					{
						Set<Integer> currentset = jset.get(j);
						Set<Integer> newset = new TreeSet<Integer>(antin.get(j));
						newset.retainAll(availout.get(i));
						jset.put(j, newset);
						
						if(!newset.equals(currentset))
							update = true;
					}
				}
			}
		}
	}

	private void generateAvail() {
		availin = setupAvailIn();
		availout = setupAvailOut();
		boolean update = true;
		
		while(update)
		{
			update = false;
			for(int function : cfg)
			{
				for(int block : cfg.getNodes(function))
				{
					// AVAILOUT(block)
					Set<Integer> availoutBefore = availout.get(block);
					Set<Integer> availoutAfter = generateAvailOut(availin.get(block), deexpr.get(block), killexpr.get(block));
					availout.put(block, availoutAfter);
					
					if(!availoutBefore.equals(availoutAfter))
						update = true;
					
					// AVAILIN(block)
					Set<Integer> availinBefore = availin.get(block);
					Set<Integer> availinAfter = generateAvailIn(function, block, cfg.getPred(block), availout);
					availin.put(block, availoutAfter);
					
					if(!availinBefore.equals(availinAfter))
						update = true;
				}
			}
		}
	}

	private Set<Integer> generateAvailIn(int function, int block, SortedSet<Integer> pred, Map<Integer, Set<Integer>> availout) {
		if(function == block)
		{
			return new TreeSet<Integer>();
		}
		else
		{
			Set<Integer> availin = new TreeSet<Integer>(availout.get(pred.first()));
			for(Integer predId : pred)
			{
				availin.retainAll(availout.get(predId));
			}
			return availin;
		}
	}

	private Set<Integer> generateAvailOut(Set<Integer> availin, Set<Integer> deexpr, Set<Integer> killexpr) {
		Set<Integer> lhs = new TreeSet<Integer>(availin);
		lhs.retainAll(killexpr);
		Set<Integer> rhs = new TreeSet<Integer>(deexpr);
		rhs.addAll(lhs);
		return rhs;
	}

	private Map<Integer, Set<Integer>> setupAvailIn() {
		Map<Integer, Set<Integer>> setup = new TreeMap<Integer, Set<Integer>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(function == block)
				{
					// N0 = empty set
					setup.put(block, new TreeSet<Integer>());
				}
				else
				{
					// N != N0 = set of all expressions
					setup.put(block, new TreeSet<Integer>(exprs.keySet()));
				}
			}
		}
		return setup;
	}
	
	private Map<Integer, Set<Integer>> setupAvailOut() {
		Map<Integer, Set<Integer>> setup = new TreeMap<Integer, Set<Integer>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
					setup.put(block, new TreeSet<Integer>());
			}
		}
		return setup;
	}

	private void generateAnt() {
		antout = setupAntOut();
		antin = setupAntIn();
		boolean update = true;
		
		while(update)
		{
			update = false;
			for(int function : cfg)
			{
				for(int block : cfg.getNodes(function))
				{
					// AVAILIN(block)
					Set<Integer> antinBefore = antin.get(block);
					Set<Integer> antinAfter = generateAntIn(antout.get(block), ueexpr.get(block), killexpr.get(block));
					antin.put(block, antinAfter);
					if(!antinBefore.equals(antinAfter))
						update = true;

					
					// ANTOUT(block)
					Set<Integer> antoutBefore = antout.get(block);
					Set<Integer> antoutAfter = generateAntOut(cfg.getSucc(block), antin);
					antout.put(block, antoutAfter);
					if(!antoutBefore.equals(antoutAfter))
						update = true;
				}
			}
		}
	}

	private Set<Integer> generateAntOut(SortedSet<Integer> succ, Map<Integer, Set<Integer>> antin) {
		if(succ.isEmpty())
		{
			return new TreeSet<Integer>();
		}
		else
		{
			Set<Integer> antout = new TreeSet<Integer>(availout.get(succ.first()));
			for(Integer succId : succ)
			{
				antout.retainAll(availout.get(succId));
			}
			return antout;
		}
	}

	private Set<Integer> generateAntIn(Set<Integer> antout, Set<Integer> ueexpr, Set<Integer> killexpr) {
		Set<Integer> lhs = new TreeSet<Integer>(antout);
		lhs.retainAll(killexpr);
		Set<Integer> rhs = new TreeSet<Integer>(ueexpr);
		rhs.addAll(lhs);
		return rhs;
	}

	private Map<Integer, Set<Integer>> setupAntIn() {
		Map<Integer, Set<Integer>> setup = new TreeMap<Integer, Set<Integer>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
					setup.put(block, new TreeSet<Integer>());
			}
		}
		return setup;
	}

	private Map<Integer, Set<Integer>> setupAntOut() {
		Map<Integer, Set<Integer>> setup = new TreeMap<Integer, Set<Integer>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(cfg.getSucc(block).isEmpty())
				{
					// NEXIT = empty set
					setup.put(block, new TreeSet<Integer>());
				}
				else
				{
					// N != N0 = set of all expressions
					setup.put(block, new TreeSet<Integer>(exprs.keySet()));
				}
			}
		}
		return setup;
	}

	private void generateKILLEXPR(Map<Integer, Map<String, Integer>> modify) {
		assert(!deexpr.isEmpty());
		assert(!ueexpr.isEmpty());
		
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(modify.containsKey(block))
					killexpr.put(block, generateKillBlock(modify.get(block)));
				else
					killexpr.put(block, new TreeSet<Integer>());
			}
		}
	}

	private Set<Integer> generateKillBlock(Map<String, Integer> modify) {
		Set<Integer> killblock = new TreeSet<Integer>();
		for(Integer expr : exprs.keySet())
		{
			for(String exprArg : exprs.get(expr))
			{
				if(modify.containsKey(exprArg))
					killblock.add(expr);
			}
		}
		return killblock;
	}

	private void generateEXPR() {
		int[] numline = {1};
		deexpr = setupNodeSet();
		ueexpr = setupNodeSet();
		// Block / Argument / Line Number of Last Modification
		Map<Integer, Map<String, Integer>> modify = new HashMap<Integer, Map<String, Integer>>();
		
		for(ListIterator<String> iter = cmdlist.listIterator(); iter.hasNext();)
		{
			String[] cmd = iter.next().split(":")[1].trim().split("\\s");
			Set<String> args = new TreeSet<String>();
			int function = cfg.getCurrentFunction(numline[0]);
			
			if(DEFCMD.contains(cmd[0]))
			{
				evaluateArgs(cmd[1], function);
				evaluateArgs(cmd[2], function);
				
				if(!modify.containsKey(function))
					modify.put(function, new HashMap<String, Integer>());
				modify.get(function).put(cmd[2], numline[0]);
			}
			else if(EXPRCMD.contains(cmd[0]))
			{
				String exprname = Integer.toString(numline[0]);
				if(evaluateArgs(cmd[1], function))
					args.add(cmd[1]);
				
				if(evaluateArgs(cmd[2], function))
					args.add(cmd[2]);
				
				exprs.put(numline[0], args);
				
				if(!modify.containsKey(function))
					modify.put(function, new HashMap<String, Integer>());
				
				if(checkUEEXPR(modify.get(function), args))
				{
					int block = cfg.getCurrentBlock(numline[0]);
					ueexpr.get(block).add(numline[0]);
				}
				modify.get(function).put(exprname, numline[0]);
			}
			++numline[0];
		}

		int linenum = 1;
		for(String line : cmdlist) 
		{
			String[] cmd = line.split(":")[1].trim().split("\\s");
			if(EXPRCMD.contains(cmd[0]))
			{
				int function = cfg.getCurrentFunction(numline[0]);
				if(checkDEEXPR(modify.get(function), exprs.get(linenum), linenum))
				{
					int block = cfg.getCurrentBlock(linenum);
					deexpr.get(block).add(numline[0]);
				}
			}
			++linenum;
		}
		
		generateKILLEXPR(modify);
	}

	private boolean checkDEEXPR(Map<String, Integer> modify, Set<String> args, int linenum) {
		for(String arg : args)
		{
			if(!modify.containsKey(arg) || modify.get(arg) > linenum)
				return false;
		}
		return true;
	}

	private boolean checkUEEXPR(Map<String, Integer> modify, Set<String> args) {
		for(String arg : args)
		{
			if(!modify.containsKey(arg))
				return false;
		}
		return true;
	}

	private boolean evaluateArgs(String arg, int function) {
		if(arg.contains("#"))
		{
			int offset = Integer.valueOf(arg.split("#")[1]);
			
			if(localvar.containsKey(function))
				localvar.put(function, Math.min(localvar.get(function), offset));
			else
				localvar.put(function, offset);
			
			return true;
		}
		else if(arg.contains("(") && arg.contains(")"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private Map<Integer, Map<Integer, Set<Integer>>> setupEdgeSet() {
		Map<Integer, Map<Integer, Set<Integer>>> setup = new TreeMap<Integer, Map<Integer, Set<Integer>>>();
		for(int function : cfg)
		{
			for(int startNode : cfg.getNodes(function))
			{
				Map<Integer, Set<Integer>> endNode = new HashMap<Integer, Set<Integer>>();
				for(int succ : cfg.getSucc(startNode))
				{
					endNode.put(succ, new TreeSet<Integer>());
				}
				setup.put(startNode, endNode);
			}
		}
		return setup;
	}
	
	private Map<Integer, Set<Integer>> setupNodeSet() {
		Map<Integer, Set<Integer>> setup = new TreeMap<Integer, Set<Integer>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				setup.put(block, new TreeSet<Integer>());
			}
		}
		return setup;
	}
	

	private List<Integer> setupEndSet() {
		List<Integer> setup = new LinkedList<Integer>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				setup.addAll(cfg.getSucc(block));
			}
		}
		return setup;
	}

	public Map<Integer, Map<Integer, Set<Integer>>> getInsert() {
		if(insert == null)
			liveAnalysis();
		return insert;
	}

	public Map<Integer, Set<Integer>> getDelete() {
		if(delete == null)
			liveAnalysis();
		return delete;
	}

	public Map<Integer, Integer> getOffset()
	{
		return localvar;
	}
}
