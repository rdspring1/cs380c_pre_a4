package cs380C.compiler;

import java.util.*;

public class PRELA extends LA {
	private Map<Integer, Set<String>> deexpr;
	private Map<Integer, Set<String>> ueexpr;
	private Map<Integer, Set<String>> killexpr;
	private Map<String, Set<String>> exprs; // Variable / Expression Arguments
	private Map<Integer, Integer> localvar; // Function / Minimum Offset
	private Map<Integer, Set<String>> availin;
	private Map<Integer, Set<String>> availout;
	private Map<Integer, Set<String>> antin;
	private Map<Integer, Set<String>> antout;
	private Map<Integer, Map<Integer, Set<String>>> earliest;
	private Map<Integer, Set<String>> laterin;
	private Map<Integer, Map<Integer, Set<String>>> later;
	private Map<Integer, Map<Integer, Set<String>>> insert;
	private Map<Integer, Set<String>> delete;
	
	public PRELA(LinkedList<String> input, CFG cfg) {
		super(input, cfg);
	}

	@Override
	protected void setupAnalysis() {
		deexpr = new TreeMap<Integer, Set<String>>();
		ueexpr = new TreeMap<Integer, Set<String>>();
		killexpr = new TreeMap<Integer, Set<String>>();
		exprs = new TreeMap<String, Set<String>>();
		localvar = new HashMap<Integer, Integer>();
		generateEXPR();
	}

	@Override
	public void liveAnalysis() {
		generateAvail();
		generateAnt();
		generateEarliest();
		generateLater();
		generateInsert();
		generateDelete();
		return;
	}
	
	private void generateDelete() {
		delete = new TreeMap<Integer, Set<String>>();
		
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(function != block)
				{
					Set<String> newset = new TreeSet<String>(ueexpr.get(block));
					newset.retainAll(laterin.get(block));
					delete.put(block, newset);
				}
			}
		}
	}

	private void generateInsert() {
		insert = setupEdgeSet();
		
		for(Integer i : insert.keySet())
		{
			Map<Integer, Set<String>> jset = insert.get(i);
			for(Integer j : jset.keySet())
			{
				Set<String> newset = new TreeSet<String>(laterin.get(j));
				newset.retainAll(later.get(i).get(j));
				jset.put(j, newset);
			}
		}
	}

	private void generateLater() {
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
				Map<Integer, Set<String>> jset = earliest.get(i);
				for(Integer j : jset.keySet())
				{
					Set<String> currentset = jset.remove(j);
					Set<String> newset = new TreeSet<String>(ueexpr.get(i));
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
						Set<String> currentset = laterin.remove(j);
						Set<String> newset = new TreeSet<String>(later.get(pred.iterator().next()).get(j));
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
		earliest = setupEdgeSet();
		boolean update = true;
		
		while(update)
		{
			update = false;
			for(Integer i : earliest.keySet())
			{
				if(cfg.containsFunction(i))
				{
					Map<Integer, Set<String>> jset = earliest.get(i);
					for(Integer j : jset.keySet())
					{
						Set<String> currentset = jset.remove(j);
						Set<String> newset = new TreeSet<String>(killexpr.get(i));
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
					Map<Integer, Set<String>> jset = earliest.get(i);
					for(Integer j : jset.keySet())
					{
						Set<String> currentset = jset.remove(j);
						Set<String> newset = new TreeSet<String>(antin.get(j));
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
					Set<String> availoutBefore = availout.remove(block);
					Set<String> availoutAfter = generateAvailOut(availin.get(block), deexpr.get(block), killexpr.get(block));
					if(!availoutBefore.equals(availoutAfter))
						update = true;
					availout.put(block, availoutAfter);
					
					// AVAILIN(block)
					Set<String> availinBefore = availout.remove(block);
					Set<String> availinAfter = generateAvailIn(function, block, cfg.getPred(block), availout);
					if(!availinBefore.equals(availinAfter))
						update = true;
					availin.put(block, availoutAfter);
				}
			}
		}
	}

	private Set<String> generateAvailIn(int function, int block, SortedSet<Integer> pred, Map<Integer, Set<String>> availout) {
		if(function == block)
		{
			return new TreeSet<String>();
		}
		else
		{
			Set<String> availin = new TreeSet<String>(availout.get(pred.first()));
			
			for(Integer predId : pred)
			{
				availin.retainAll(availout.get(predId));
			}
			return availin;
		}
	}

	private Set<String> generateAvailOut(Set<String> availin, Set<String> deexpr, Set<String> killexpr) {
		Set<String> lhs = new TreeSet<String>(availin);
		lhs.retainAll(killexpr);
		Set<String> rhs = new TreeSet<String>(deexpr);
		rhs.addAll(lhs);
		return rhs;
	}

	private Map<Integer, Set<String>> setupAvailIn() {
		Map<Integer, Set<String>> setup = new TreeMap<Integer, Set<String>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(function == block)
				{
					// N0 = empty set
					setup.put(block, new TreeSet<String>());
				}
				else
				{
					// N != N0 = set of all expressions
					setup.put(block, new TreeSet<String>(exprs.keySet()));
				}
			}
		}
		return setup;
	}
	
	private Map<Integer, Set<String>> setupAvailOut() {
		Map<Integer, Set<String>> setup = new TreeMap<Integer, Set<String>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
					setup.put(block, new TreeSet<String>());
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
					Set<String> antinBefore = antin.remove(block);
					Set<String> antinAfter = generateAntIn(antin.get(block), ueexpr.get(block), killexpr.get(block));
					if(!antinBefore.equals(antinAfter))
						update = true;
					antin.put(block, antinAfter);
					
					// antOUT(block)
					Set<String> antoutBefore = antout.remove(block);
					Set<String> antoutAfter = generateAntOut(cfg.getSucc(block), antout);
					if(!antoutBefore.equals(antoutAfter))
						update = true;
					antout.put(block, antoutAfter);
				}
			}
		}
	}

	private Set<String> generateAntOut(SortedSet<Integer> succ, Map<Integer, Set<String>> antin) {
		if(succ.isEmpty())
		{
			return new TreeSet<String>();
		}
		else
		{
			Set<String> antout = new TreeSet<String>(availout.get(succ.first()));
			for(Integer succId : succ)
			{
				antout.retainAll(availout.get(succId));
			}
			return antout;
		}
	}

	private Set<String> generateAntIn(Set<String> availin, Set<String> ueexpr, Set<String> killexpr) {
		Set<String> lhs = new TreeSet<String>(availin);
		lhs.retainAll(killexpr);
		Set<String> rhs = new TreeSet<String>(ueexpr);
		rhs.addAll(lhs);
		return rhs;
	}

	private Map<Integer, Set<String>> setupAntIn() {
		Map<Integer, Set<String>> setup = new TreeMap<Integer, Set<String>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
					setup.put(block, new TreeSet<String>());
			}
		}
		return setup;
	}

	private Map<Integer, Set<String>> setupAntOut() {
		Map<Integer, Set<String>> setup = new TreeMap<Integer, Set<String>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				if(cfg.getSucc(block).isEmpty())
				{
					// NEXIT = empty set
					setup.put(block, new TreeSet<String>());
				}
				else
				{
					// N != N0 = set of all expressions
					setup.put(block, new TreeSet<String>(exprs.keySet()));
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
			}
		}
	}

	private Set<String> generateKillBlock(Map<String, Integer> modify) {
		Set<String> killblock = new TreeSet<String>();
		
		for(String expr : exprs.keySet())
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
				
				exprs.put(exprname, args);
				
				if(!modify.containsKey(function))
					modify.put(function, new HashMap<String, Integer>());
				
				if(checkUEEXPR(modify.get(function), args))
				{
					int block = cfg.getCurrentBlock(numline[0]);
					if(!ueexpr.containsKey(block))
						ueexpr.put(block, new TreeSet<String>());
					ueexpr.get(block).add(exprname);
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
				String exprname = Integer.toString(linenum);
				if(checkDEEXPR(modify.get(function), exprs.get(exprname), linenum))
				{
					int block = cfg.getCurrentBlock(linenum);
					if(!ueexpr.containsKey(block))
						ueexpr.put(block, new TreeSet<String>());
					ueexpr.get(block).add(exprname);
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
	
	private Map<Integer, Map<Integer, Set<String>>> setupEdgeSet() {
		Map<Integer, Map<Integer, Set<String>>> setup = new TreeMap<Integer, Map<Integer, Set<String>>>();
		for(int function : cfg)
		{
			for(int startNode : cfg.getNodes(function))
			{
				Map<Integer, Set<String>> endNode = new HashMap<Integer, Set<String>>();
				for(int succ : cfg.getSucc(startNode))
				{
					endNode.put(succ, new TreeSet<String>());
				}
				setup.put(startNode, endNode);
			}
		}
		return setup;
	}
	
	private Map<Integer, Set<String>> setupNodeSet() {
		Map<Integer, Set<String>> setup = new TreeMap<Integer, Set<String>>();
		for(int function : cfg)
		{
			for(int block : cfg.getNodes(function))
			{
				setup.put(block, new TreeSet<String>());
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

}
