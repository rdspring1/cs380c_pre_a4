package cs380C.compiler;

import java.util.*;

public class PRELA extends LA {
	private Map<Integer, Set<String>> deexpr;
	private Map<Integer, Set<String>> ueexpr;
	private Map<Integer, Set<String>> killexpr;
	private Map<String, Set<String>> exprs; // Variable / Expression Arguments
	private Map<Integer, Integer> localvar; // Function / Minimum Offset
	
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
		// TODO Auto-generated method stub
		return;
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
}
