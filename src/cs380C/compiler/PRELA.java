package cs380C.compiler;

import java.util.*;

public class PRELA extends LA {
	private Set<String> globals = new TreeSet<String>();
	private Set<String> gstruct = new TreeSet<String>();
	private HashMap<Integer, Set<String>> deexpr = new HashMap<Integer, Set<String>>();
	private HashMap<Integer, Set<String>> ueexpr = new HashMap<Integer, Set<String>>();
	private HashMap<Integer, Set<String>> killexpr;
	
	public PRELA(LinkedList<String> input, CFG cfg) {
		super(input, cfg);
	}

	@Override
	protected void setupAnalysis() {
		generateEXPR();
		killexpr = generateKILLEXPR();
	}

	@Override
	public void liveAnalysis() {
		// TODO Auto-generated method stub
	}
	
	private HashMap<Integer, Set<String>> generateKILLEXPR() {
		assert(deexpr != null);
		assert(ueexpr != null);
		// TODO Auto-generated method stub
		return null;
	}

	private void generateEXPR() {
		int numline = 1;
		
		Map<Float, String> cmds = new TreeMap<Float, String>();
		// Block / VarName / Expression
		HashMap<Integer, HashMap<String, String>> invars = new HashMap<Integer, HashMap<String, String>>();
		HashMap<Integer, HashMap<String, String>> outvars = new HashMap<Integer, HashMap<String, String>>();
		
		for(String line : cmdlist)
		{
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(DEFCMD.contains(cmd[0]))
			{
				String varname = evaluateArgs(cmds, cmd[2]);
				String expr = evaluateArgs(cmds, cmd[1]);
				List<String> args = getArgs(cmds, expr);
				
				int block = cfg.getCurrentBlock(numline);
				if(invars.containsKey(block) && modify(invars.get(block), args))
				{
					invars.get(block).remove(varname);
					
					if(ueexpr.containsKey(block))
						ueexpr.get(block).remove(varname);
				}
				else
				{
					if(!invars.containsKey(block))
						invars.put(block, new HashMap<String, String>());
					invars.get(block).put(varname, expr);
					
					if(!ueexpr.containsKey(block))
						ueexpr.put(block, new TreeSet<String>());
					ueexpr.get(block).add(varname);
				}
				
				if(!outvars.containsKey(block))
					outvars.put(block, new HashMap<String, String>());
				outvars.get(block).put(varname, expr);
			}
			else
			{
				updateFunction(cmds, cmd, numline);
			}
			++numline;
		}
		
		// TODO: Update DEEXPR using outvars variable
		numline = 1;
		for(String line : cmdlist)
		{
			String[] cmd = line.split(":")[1].trim().split("\\s");
			
			if(DEFCMD.contains(cmd[0]))
			{
				String varname = evaluateArgs(cmds, cmd[2]);
				String expr = evaluateArgs(cmds, cmd[1]);
				List<String> args = getArgs(cmds, expr);
				
				int block = cfg.getCurrentBlock(numline);
				if(outvars.containsKey(block) && !modify(outvars.get(block), args))
				{				
					if(!deexpr.containsKey(block))
						deexpr.put(block, new TreeSet<String>());
					deexpr.get(block).add(varname);
				}
			}
			++numline;
		}
	}	
	private boolean modify(HashMap<String, String> modifylist, List<String> args) {
		// TODO Determine whether any of the arguments are modified
		return false;
	}

	private List<String> getArgs(Map<Float, String> cmds, String expr) {
		// TODO Auto-generated method stub
		return null;
	}

	private String evaluateArgs(Map<Float, String> cmds, String arg) {
		if(arg.charAt(0) == '(' && arg.charAt(arg.length() - 1) == ')')
			return cmds.remove(Float.parseFloat(arg.substring(1, arg.length() - 1)));
		else if(arg.contains("#"))
			return arg.trim().split("#")[0];	
		else
			return arg;
	}

	private void updateFunction(Map<Float, String> cmds, String[] line, float numline)
	{
		TreeMap<Integer, String> params = new TreeMap<Integer, String>();
		TreeMap<Integer, String> locals = new TreeMap<Integer, String>();
		Set<String> lstruct = new TreeSet<String>();
		
		if(line[0].equals("add"))
		{
			String arg1 = cscTranslator.evaluateArgs(line[1], locals, params, cmds);
			String arg2 = cscTranslator.evaluateArgs(line[2], locals, params, cmds);
			
			if(globals.contains(arg1) && arg2.contains("offset")) // Global Struct
			{
				String structname = arg1;
				String item = arg2.substring(0, arg2.length() - 7);
				gstruct.add(structname);
				cmds.put(numline, structname + "." + item);
			}
			else if(arg1.contains("FP") && arg2.contains("offset")) // Local Struct
			{
				String[] token1 = arg1.substring(1, arg1.length() - 1).split("\\s");
				String structname = token1[0].substring(0, token1[0].length() - 5);
				String[] token2 = line[2].split("#"); 
				String item = token2[0].substring(0, token2[0].length() - 7);
				lstruct.add(structname);
				cmds.put(numline, structname + "." + item);
			}
			else if(arg2.contains("*") && globals.contains(arg1)) // Global Array
			{
				String arrayname = arg1.trim().split("\\s")[0];
				
				String[] index = arg2.substring(1, arg2.length() - 1).split("\\s");
				Integer size = Integer.parseInt(index[index.length - 1]) / cscTranslator.LONGSIZE;
				String arrayindex = arg2.substring(1, arg2.lastIndexOf("*") - 1);

				if(size != 1) // Multidimensional Array
					cmds.put(numline, arrayname + "[(" + arrayindex + " * " + size + ")]");
				else
					cmds.put(numline, arrayname + "[" + arrayindex + "]");
			}
			else if(arg2.contains("*") && arg1.contains("FP")) // Local Array
			{
				String arrayname = arg1.substring(1, arg1.length() - 1).split("\\s")[0];
				arrayname = arrayname.split("\\s")[0].substring(0, arrayname.length() - 5);
				
				String[] index = arg2.substring(1, arg2.length() - 1).split("\\s");
				Integer size = Integer.parseInt(index[index.length - 1]) / cscTranslator.LONGSIZE;
				String arrayindex = arg2.substring(1, arg2.lastIndexOf("*") - 1);

				if(size != 1) // Multidimensional Array
					cmds.put(numline, arrayname + "[(" + arrayindex + " * " + size + ")]");
				else
					cmds.put(numline, arrayname + "[" + arrayindex + "]");
			}
			else if(arg2.contains("*") && cscTranslator.isStruct(gstruct, arg1)) // Array Inside Struct - Global
			{
				String arrayname = cscTranslator.cleanArrayName(arg1);
				String arrayindex = arg2.substring(1, arg2.length()).substring(0, arg2.lastIndexOf("*") - 2);
				gstruct.add(cscTranslator.getStructName(arg1));
				cmds.put(numline, arrayname + "[" + arrayindex + "]");
			}
			else if(arg2.contains("*") && cscTranslator.isStruct(lstruct, arg1)) // Array Inside Struct - Local
			{
				String arrayname = cscTranslator.cleanArrayName(arg1);
				String arrayindex = arg2.substring(1, arg2.length()).substring(0, arg2.lastIndexOf("*") - 2);
				lstruct.add(cscTranslator.getStructName(arg1));
				cmds.put(numline, arrayname + "[" + arrayindex + "]");
			}
			else if(arg1.contains("[") && arg1.lastIndexOf("]") == arg1.length() - 1 && arg2.contains("offset")) // Array of Structs
			{
				String structname = cscTranslator.getStructName(arg1);
				String parent = structname;
				
				if(structname.contains("."))
					parent = structname.substring(0, structname.lastIndexOf("."));
				
				String[] token = line[2].split("#"); 
				String item = token[0].substring(0, token[0].length() - 7);

				if(globals.contains(parent))
					gstruct.add(structname);
				else
					lstruct.add(structname);

				cmds.put(numline, arg1 + "." + item);
			}
			else if(arg1.contains(".") && arg2.contains("offset")) // Nested Struct
			{
				String structname = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length());
				String[] token = line[2].split("#"); 
				String item = token[0].substring(0, token[0].length() - 7);
				
				if(globals.contains(structname))
					gstruct.add(arg1);
				else
					lstruct.add(arg1);

				cmds.put(numline, arg1 + "." + item);
			}
			else if(arg1.contains("[") && arg1.lastIndexOf("]") == arg1.length() - 1 && !arg2.contains("[") && !arg2.contains("[") && arg2.contains("*")) // Multidimensional Arrays
			{
				String arrayname = arg1.substring(0, arg1.indexOf("["));
				String oldindex = arg1.substring(arg1.indexOf("[") + 1, arg1.length() -  1);
				
				String[] index = arg2.substring(1, arg2.length() - 1).split("\\s");
				Integer size = Integer.parseInt(index[index.length - 1]) / cscTranslator.LONGSIZE;
				String arrayindex = index[0];
				
				if(size != 1)
					cmds.put(numline, arrayname + "[(" + oldindex + " + " + arrayindex + ") * " + size + "]");
				else
					cmds.put(numline, arrayname + "[" + oldindex + " + " + arrayindex + "]");
			}
			else if(arg2.contains("GP")) // Global Variable
			{
				cmds.put(numline, arg1.substring(0, arg1.length() - 5));
				globals.add(cmds.get(numline));
				locals.remove(cmds.get(numline));
			}
			else // Default
			{
				cmds.put(numline, "(" + arg1 + " + " + arg2 + ")");
			}
		}
		else if(line[0].equals("sub"))
		{
			cmds.put(numline, "(" + cscTranslator.evaluateArgs(line[1], locals, params, cmds) + " - " + cscTranslator.evaluateArgs(line[2], locals, params, cmds) + ")");
		}
		else if(line[0].equals("mul"))
		{
			cmds.put(numline, "(" + cscTranslator.evaluateArgs(line[1], locals, params, cmds) + " * " + cscTranslator.evaluateArgs(line[2], locals, params, cmds) + ")");
		}
		else if(line[0].equals("div"))
		{
			cmds.put(numline, "(" + cscTranslator.evaluateArgs(line[1], locals, params, cmds) + " / " + cscTranslator.evaluateArgs(line[2], locals, params, cmds) + ")");
		}
		else if(line[0].equals("mod"))
		{
			cmds.put(numline, "(" + cscTranslator.evaluateArgs(line[1], locals, params, cmds) + " % " + cscTranslator.evaluateArgs(line[2], locals, params, cmds) + ")");
		}
		else if(line[0].equals("neg"))
		{
			cmds.put(numline, "(-" + cscTranslator.evaluateArgs(line[1], locals, params, cmds) + ")");
		}
	} 
}
