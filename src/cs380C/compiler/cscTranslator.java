package cs380C.compiler;

import java.util.*;

public class cscTranslator
{
	// Class Variables
	// functionNumber : LineNumber
	private static HashMap<Float, Integer> function = new HashMap<Float, Integer>(); 
	// GP Offset : Global Name
	private static TreeMap<Integer, String> globals = new TreeMap<Integer, String>();
	// Struct Name : List of Struct Elements
	private static HashMap<String, TreeMap<Integer,String>> gstruct = new HashMap<String, TreeMap<Integer,String>>();
	// Struct Name : Size of Struct
	private static HashMap<String, Integer> gstructSize = new HashMap<String, Integer>();
	// Global Arrays
	private static LinkedList<String> garrays = new LinkedList<String>();
	private static final int LONGSIZE = 8;
	private static final int FP = 0;
	private static final int GP = 32768;
	private static int STRUCTCOUNT = 0;
	private static float numline = 1;

	public static String translator(Scanner r) throws Exception 
	{
		try 
		{
			String pgm = getProgram(r);
			return pgm;
		} 
		catch (Exception e) 
		{
			System.out.println("Fatal error: could not translate program");
			System.out.println(e.toString());
			return "STOP\n";
		}
	}
	private static String getProgram(Scanner r) throws Exception
	{
		StringBuilder body = new StringBuilder();
		if(r.hasNextLine() && r.nextLine().contains("nop")) {
			while(r.hasNextLine() && r.findInLine("nop") == null) {
				body.append(getFunction(r));
			}
		}
		else
		{
			throw new Exception("Incorrect File Format!");
		}

		StringBuilder pgm = new StringBuilder();
		pgm.append("#include <stdio.h>\n");
		pgm.append("#define WriteLine() printf(\"\\n\");\n");
		pgm.append("#define WriteLong(x) printf(\" %lld\", x);\n");
		pgm.append("#define ReadLong(a) if (fscanf(stdin, \"%lld\", &a) != 1) a = 0;\n");
		pgm.append("#define long long long\n");
		pgm.append("\n\n\n");
		pgm.append("// Global Declarations\n");
		pgm.append(getDeclarations(globals, gstruct, gstructSize, garrays, GP));
		pgm.append("\n//Functions\n");
		pgm.append(body.toString());
		return pgm.toString();
	}
	private static String getFunction(Scanner r) throws Exception
	{
		// Print Function
		TreeMap<Integer, String> params = new TreeMap<Integer, String>();
		TreeMap<Integer, String> locals = new TreeMap<Integer, String>();
		TreeMap<Float, String> cmds = new TreeMap<Float, String>();
		HashMap<String,TreeMap<Integer,String>> lstruct = new HashMap<String,TreeMap<Integer,String>>();
	    HashMap<String, Integer> lstructSize = new HashMap<String, Integer>();
	    LinkedList<String> larrays = new LinkedList<String>();
		int numparam = 0;
		boolean isMain = false;
		
		// Method Calls
		LinkedList<String> paramlist = new LinkedList<String>();
	
		// Parse Commands
		while(r.hasNextLine())
		{
			++numline;
			String original = r.nextLine();
			String[] line = original.split(":")[1].trim().split("\\s");
			if(line[0].equals("enter"))
			{
				function.put(numline, function.size() + 1); 
			}
			else if(line[0].equals("ret"))
			{
				numparam = Integer.parseInt(line[1]) / LONGSIZE;
				break;
			}
			else if(line[0].equals("write"))
			{
				cmds.put(numline, "WriteLong(" + evaluateArgs(line[1], locals, params, cmds) + ")" );
			}
			else if(line[0].equals("wrl"))
			{
				cmds.put(numline, "WriteLine()");
			}
			else if(line[0].equals("read"))
			{
				cmds.put(numline, "read");
			}
			else if(line[0].equals("load"))
			{
				String index = cmds.remove(Float.parseFloat(line[1].substring(1, line[1].length() - 1)));
				cmds.put(numline, index);
			}
			else if(line[0].equals("store"))
			{
				String rside = evaluateArgs(line[1], locals, params, cmds);
				String lside = evaluateArgs(line[2], locals, params, cmds);
				if(rside.charAt(0) == '(' && rside.charAt(rside.length() - 1)== ')')
					cmds.put(numline, lside + " = " + rside.substring(1, rside.length() - 1));
				else
					cmds.put(numline, lside + " = " + rside);	
			}
			else if(line[0].equals("move"))
			{
				String rside = evaluateArgs(line[1], locals, params, cmds);
				String lside = evaluateArgs(line[2], locals, params, cmds);
				if(rside.charAt(0) == '(' && rside.charAt(rside.length() - 1)== ')')
					cmds.put(numline, lside + " = " + rside.substring(1, rside.length() - 1));
				else if(rside.equals("read"))
					cmds.put(numline, "ReadLong(" + lside + ")");
				else
					cmds.put(numline, lside + " = " + rside);
			}
			else if(line[0].equals("param"))
			{
				paramlist.add(evaluateArgs(line[1], locals, params, cmds));
			}
			else if(line[0].equals("call"))
			{
				StringBuilder fcall = new StringBuilder();
				fcall.append("function_" + function.get(Float.parseFloat(line[1].substring(1, line[1].length() - 1))));
				fcall.append("(");

				int count = 0;
				for(String param : paramlist)
				{
					fcall.append(param);
					if(count != (paramlist.size() - 1))
						fcall.append(", ");
					++count;
				}
				fcall.append(")");
				cmds.put(numline, fcall.toString());
				paramlist.clear();
			}
			else if(line[0].equals("br"))
			{
				float currentline = Float.parseFloat(line[1].substring(1, line[1].length() - 1));
				if(currentline < numline)
				{
					while(!cmds.containsKey(currentline))
						++currentline;
					cmds.put(currentline, "while " + cmds.get(currentline).substring(3));
				}
				else
				{
					if(cmds.containsKey(numline + 0.5f))
						cmds.put(numline + 0.5f, cmds.get(numline + 0.5f) + "\nelse\n\t{");
					else
						cmds.put(numline + 0.5f, "else\n\t{");
					
					Float newkey = Float.parseFloat(line[1].substring(1, line[1].length() - 1)) + 0.5f;
					if(cmds.containsKey(newkey))
						cmds.put(newkey, cmds.get(newkey) + "\n}");
					else
						cmds.put(newkey, "}");
					
				}
			}
			else if(line[0].equals("entrypc"))
			{
				isMain = true;
			}
			else if(line[0].equals("add"))
			{
				String arg1 = evaluateArgs(line[1], locals, params, cmds);
				String arg2 = evaluateArgs(line[2], locals, params, cmds);
				
				if(globals.containsValue(arg1) && arg2.contains("offset")) // Global Struct
				{
					String structname = arg1;
					String item = arg2.substring(0, arg2.length() - 7);

					if(!gstruct.containsKey(structname))
						gstruct.put(structname, new TreeMap<Integer, String>());

					gstruct.get(structname).put(Integer.parseInt(line[2].split("#")[1]), structname + "." + item);
					cmds.put(numline, structname + "." + item);
				}
				else if(arg1.contains("FP") && arg2.contains("offset")) // Local Struct
				{
					String[] token1 = arg1.substring(1, arg1.length() - 1).split("\\s");
					String structname = token1[0].substring(0, token1[0].length() - 5);
					String[] token2 = line[2].split("#"); 
					String item = token2[0].substring(0, token2[0].length() - 7);

					if(!lstruct.containsKey(structname))
						lstruct.put(structname, new TreeMap<Integer, String>());

					lstruct.get(structname).put(Integer.parseInt(token2[1]), structname + "." + item);
					cmds.put(numline, structname + "." + item);
				}
				else if(arg2.contains("*") && globals.containsValue(arg1)) // Global Array
				{
					String arrayname = arg1.trim().split("\\s")[0];
					
					String[] index = arg2.substring(1, arg2.length() - 1).split("\\s");
					Integer size = Integer.parseInt(index[index.length - 1]) / LONGSIZE;
					String arrayindex = arg2.substring(1, arg2.lastIndexOf("*") - 1);
					
					gstructSize.put(arrayname, Integer.parseInt(arg2.substring(1, arg2.length()).substring(arg2.lastIndexOf("*") + 1, arg2.length() - 2)) / LONGSIZE);
					garrays.add(arrayname);
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
					Integer size = Integer.parseInt(index[index.length - 1]) / LONGSIZE;
					String arrayindex = arg2.substring(1, arg2.lastIndexOf("*") - 1);
					
					lstructSize.put(arrayname, size);
					larrays.add(arrayname);
					if(size != 1) // Multidimensional Array
						cmds.put(numline, arrayname + "[(" + arrayindex + " * " + size + ")]");
					else
						cmds.put(numline, arrayname + "[" + arrayindex + "]");
				}
				else if(arg2.contains("*") && isStruct(gstruct, arg1)) // Array Inside Struct - Global
				{
					String arrayname = cleanArrayName(arg1);
					String arrayindex = arg2.substring(1, arg2.length()).substring(0, arg2.lastIndexOf("*") - 2);
					gstructSize.put(getStructName(arg1), Integer.parseInt(arg2.substring(1, arg2.length()).substring(arg2.lastIndexOf("*") + 1, arg2.length() - 2)) / LONGSIZE);
					gstruct.put(getStructName(arg1), new TreeMap<Integer, String>());
					cmds.put(numline, arrayname + "[" + arrayindex + "]");
				}
				else if(arg2.contains("*") && isStruct(lstruct, arg1)) // Array Inside Struct - Local
				{
					String arrayname = cleanArrayName(arg1);
					String arrayindex = arg2.substring(1, arg2.length()).substring(0, arg2.lastIndexOf("*") - 2);
					lstructSize.put(getStructName(arg1), Integer.parseInt(arg2.substring(1, arg2.length()).substring(arg2.lastIndexOf("*") + 1, arg2.length() - 2)) / LONGSIZE);
					lstruct.put(getStructName(arg1), new TreeMap<Integer, String>());
					cmds.put(numline, arrayname + "[" + arrayindex + "]");
				}
				else if(arg1.contains("[") && arg1.lastIndexOf("]") == arg1.length() - 1 && arg2.contains("offset")) // Array of Structs
				{
					String structname = getStructName(arg1);
					String parent = structname;
					
					if(structname.contains("."))
						parent = structname.substring(0, structname.lastIndexOf("."));
					
					String[] token = line[2].split("#"); 
					String item = token[0].substring(0, token[0].length() - 7);

					if(globals.containsValue(parent))
					{
						if(!gstruct.containsKey(structname))
							gstruct.put(structname, new TreeMap<Integer, String>());

						gstruct.get(structname).put(Integer.parseInt(token[1]), structname + "." + item);
					}
					else
					{
						if(!lstruct.containsKey(structname))
							lstruct.put(structname, new TreeMap<Integer, String>());

						lstruct.get(structname).put(Integer.parseInt(token[1]), structname + "." + item);
					}

					cmds.put(numline, arg1 + "." + item);
				}
				else if(arg1.contains(".") && arg2.contains("offset")) // Nested Struct
				{
					String structname = arg1.substring(arg1.lastIndexOf(".") + 1, arg1.length());
					String[] token = line[2].split("#"); 
					String item = token[0].substring(0, token[0].length() - 7);
					
					if(globals.containsValue(structname))
					{
						if(!gstruct.containsKey(arg1))
							gstruct.put(arg1, new TreeMap<Integer, String>());

						gstruct.get(arg1).put(Integer.parseInt(token[1]),  structname + "." + item);
					}
					else
					{
						if(!lstruct.containsKey(arg1))
							lstruct.put(arg1, new TreeMap<Integer, String>());

						lstruct.get(arg1).put(Integer.parseInt(token[1]),  structname + "." + item);
					}

					cmds.put(numline, arg1 + "." + item);
				}
				else if(arg1.contains("[") && arg1.lastIndexOf("]") == arg1.length() - 1 && !arg2.contains("[") && !arg2.contains("[") && arg2.contains("*")) // Multidimensional Arrays
				{
					String arrayname = arg1.substring(0, arg1.indexOf("["));
					String oldindex = arg1.substring(arg1.indexOf("[") + 1, arg1.length() -  1);
					
					String[] index = arg2.substring(1, arg2.length() - 1).split("\\s");
					Integer size = Integer.parseInt(index[index.length - 1]) / LONGSIZE;
					String arrayindex = index[0];
					
					if(size != 1)
						cmds.put(numline, arrayname + "[(" + oldindex + " + " + arrayindex + ") * " + size + "]");
					else
						cmds.put(numline, arrayname + "[" + oldindex + " + " + arrayindex + "]");
				}
				else if(arg2.contains("GP")) // Global Variable
				{
					cmds.put(numline, arg1.substring(0, arg1.length() - 5));
					globals.put(Integer.parseInt(line[1].split("#")[1]), cmds.get(numline));
					locals.remove(Integer.parseInt(line[1].split("#")[1]));
				}
				else // Default
				{
					cmds.put(numline, "(" + arg1 + " + " + arg2 + ")");
				}
			}
			else if(line[0].equals("sub"))
			{
				cmds.put(numline, "(" + evaluateArgs(line[1], locals, params, cmds) + " - " + evaluateArgs(line[2], locals, params, cmds) + ")");
			}
			else if(line[0].equals("mul"))
			{
				cmds.put(numline, "(" + evaluateArgs(line[1], locals, params, cmds) + " * " + evaluateArgs(line[2], locals, params, cmds) + ")");
			}
			else if(line[0].equals("div"))
			{
				cmds.put(numline, "(" + evaluateArgs(line[1], locals, params, cmds) + " / " + evaluateArgs(line[2], locals, params, cmds) + ")");
			}
			else if(line[0].equals("mod"))
			{
				cmds.put(numline, "(" + evaluateArgs(line[1], locals, params, cmds) + " % " + evaluateArgs(line[2], locals, params, cmds) + ")");
			}
			else if(line[0].equals("neg"))
			{
				cmds.put(numline, "(-" + evaluateArgs(line[1], locals, params, cmds) + ")");
			}
			else if(line[0].equals("cmple"))
			{
				String[] nextline = r.nextLine().split(":")[1].trim().split("\\s");
				++numline;
				if(nextline[0].equals("blbc")) // <= - less than or equal
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " <= " + evaluateArgs(line[2], locals, params, cmds) + ") {");
				}
				else // > - greater than
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " > " + evaluateArgs(line[2], locals, params, cmds) + ") {");	
				}
				
				Float newkey = Float.parseFloat(nextline[2].substring(1, nextline[2].length() - 1)) - 0.5f;
				if(cmds.containsKey(newkey))
					cmds.put(newkey, cmds.get(newkey) + "\n}");
				else
					cmds.put(newkey, "}");
			}
			else if(line[0].equals("cmplt"))
			{
				String[] nextline = r.nextLine().split(":")[1].trim().split("\\s");
				++numline;
				if(nextline[0].equals("blbc")) // < - less than
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " < " + evaluateArgs(line[2], locals, params, cmds) + ") {");	
				}
				else // >= - greater than or equal
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " >= " + evaluateArgs(line[2], locals, params, cmds) + ") {");	
				}
				
				Float newkey = Float.parseFloat(nextline[2].substring(1, nextline[2].length() - 1)) - 0.5f;
				if(cmds.containsKey(newkey))
					cmds.put(newkey, cmds.get(newkey) + "\n}");
				else
					cmds.put(newkey, "}");
			}
			else if(line[0].equals("cmpeq"))
			{
				String[] nextline = r.nextLine().split(":")[1].trim().split("\\s");
				++numline;
				if(nextline[0].equals("blbc")) // == - equal
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " == " + evaluateArgs(line[2], locals, params, cmds) + ") {");	
				}
				else // != - not equal
				{
					cmds.put(numline - 1, "if (" + evaluateArgs(line[1], locals, params, cmds) + " != " + evaluateArgs(line[2], locals, params, cmds) + ") {");	
				}
				
				Float newkey = Float.parseFloat(nextline[2].substring(1, nextline[2].length() - 1)) - 0.5f;
				if(cmds.containsKey(newkey))
					cmds.put(newkey, cmds.get(newkey) + "\n}");
				else
					cmds.put(newkey, "}");
			}
			else
			{
				throw new Exception("Unknown 3addr command: " + original);
			}
		}
		return printFunction(isMain, numparam, locals, params, lstruct, lstructSize, larrays, cmds);
	}
	private static String getDeclarations(TreeMap<Integer, String> variables, HashMap<String, TreeMap<Integer,String>> structs, HashMap<String, Integer> structSize, LinkedList<String> arrays, int scope)
	{
		StringBuilder strdec = new StringBuilder();
		Integer prev = scope;
		
		for(Integer offset : variables.descendingKeySet())
		{			
			int size = Math.abs((offset - prev) / 8);
			if(structs.containsKey(variables.get(offset)))
			{
				strdec.append(getStructDeclaration(variables.get(offset), structs, structSize, size));
			}
			else if(size > 1 && arrays.contains(variables.get(offset))) // Array
			{
				strdec.append("\tlong " + variables.get(offset) + "[" + size + "];");
			}
			else // Variable
			{
				strdec.append("\tlong " + variables.get(offset) + ";");
			}
			strdec.append("\n");
			prev = offset;
		}
		return strdec.toString();
	}
	private static String cleanArrayName(String arg1) {
		String arrayindex = arg1.substring(arg1.indexOf("[") + 1, arg1.lastIndexOf("]"));
		if(arrayindex.indexOf("(") == 0 && arrayindex.indexOf(")") == arrayindex.length() - 1)
		{
			return arg1.substring(0, arg1.indexOf("[") + 1) + arrayindex.substring(1, arrayindex.lastIndexOf("*") - 1) + arg1.substring(arg1.lastIndexOf("]"));
		}
		else
		{
			return arg1;
		}
	}
	private static boolean isStruct(HashMap<String, TreeMap<Integer, String>> struct, String arg1) {
		if(arg1.contains("."))
		{
			String parent = arg1.substring(0, arg1.lastIndexOf("."));
			if(parent.indexOf("[") == 1 && parent.contains("]")) 
			{
				if(!struct.containsKey(getStructName(parent)))
					return false;
			}
			else if(!struct.containsKey(parent))
			{
				return false;
			}
			return true;
		}
		return false;
	}
	private static String getStructName(String arg1) {
		
		if(!arg1.contains("[") && !arg1.contains("]"))
		{
			return arg1;
		}
		else if(arg1.contains("[") && !arg1.contains("]"))
		{
			return arg1.substring(0, arg1.indexOf("["));
		}
		else
		{			
			int lb = arg1.lastIndexOf("[");
			int rb = arg1.lastIndexOf("]");
			return getStructName(arg1.substring(0, lb) + arg1.substring(rb + 1));
		}
	}
	private static String printFunction(boolean isMain, int numparam, TreeMap<Integer, String> locals, TreeMap<Integer, String> params, HashMap<String,TreeMap<Integer,String>> lstruct, HashMap<String, Integer> lstructSize, LinkedList<String> larrays, TreeMap<Float, String> cmds) {
		StringBuilder output = new StringBuilder();
		if(isMain)
			output.append("void main" + "(");
		else
			output.append("void function_" + function.size() + "(");

		// Add Parameters
		for(int i = numparam; i >= 1; --i)
		{
			if(params.containsKey((i * 8) + 8))
				output.append("long " + params.get((i * 8) + 8));
			else
				output.append("long param" + i);

			if(i != 1)
				output.append(", ");	
		}
		output.append(")\t\n{\n");
		
		// Add Local Variables
		output.append("// Local Variable Declarations\n");
		output.append(getDeclarations(locals, lstruct, lstructSize, larrays, FP));
		
		// Add Body Commands
		output.append("// Function Body\n");
		for(String cmd : cmds.values())
		{
			output.append("\t");
			output.append(cmd);
			if(!cmd.contains("{") && !cmd.contains("}"))
				output.append(";");
			output.append("\n");
		}	
		output.append("}\n");
		return output.toString();
	}
	private static String getStructDeclaration(String structname, HashMap<String, TreeMap<Integer,String>> structs, HashMap<String, Integer> structSize, int size)
	{
		++STRUCTCOUNT;
		StringBuilder strdec = new StringBuilder();
		Integer prev = 0;
		TreeMap<Integer,String> struct = structs.remove(structname);
		
		strdec.append("\tstruct STRUCT" + STRUCTCOUNT + "\t{\n");
		for(Integer offset : struct.descendingKeySet())
		{			
			int newsize = Math.abs((offset - prev) / 8);
			if(structs.containsKey(struct.get(offset)))
			{
				if(structs.get(struct.get(offset)).size() > 0)
					strdec.append(getStructDeclaration(struct.get(offset), structs, structSize, newsize));
				else
					strdec.append("\tlong " + struct.get(offset).substring(struct.get(offset).lastIndexOf(".") + 1) + "[" + structSize.get(structname.subSequence(0, structname.indexOf("."))) + "];");
			}
			else if(newsize > 1) // Array
			{
				strdec.append("\tlong " + struct.get(offset).substring(struct.get(offset).lastIndexOf(".") + 1) + "[" + newsize + "];");
			}
			else // Variable
			{
				strdec.append("\tlong " + struct.get(offset).substring(struct.get(offset).lastIndexOf(".") + 1) + ";");
			}
			strdec.append("\n");
			prev = offset;
		}

		String printname = structname.substring(structname.lastIndexOf(".") + 1);
		if(structSize.containsKey(structname))
			if(size > 0)
				strdec.append("\t}\t" + printname + "[" + (size / structSize.get(structname)) + "];");
			else
				strdec.append("\t}\t" + printname + "[1];");
		else
			strdec.append("\t}\t" + printname + ";");
		
		return strdec.toString();
	}
	private static String evaluateArgs(String arg, TreeMap<Integer, String> locals, TreeMap<Integer, String> params, TreeMap<Float, String> cmds)
	{
		if(arg.charAt(0) == '(' && arg.charAt(arg.length() - 1) == ')')
		{	
			String output = cmds.remove(Float.parseFloat(arg.substring(1, arg.length() - 1)));
			return output;
		}
		else if(arg.contains("#"))
		{
			String[] token = arg.trim().split("#");
			if(!token[0].contains("_offset"))
			{
				Integer offset = Integer.parseInt(token[1]);
				if(offset >= 0)
					params.put(offset, token[0]);
				else if(token[0].contains("_base"))
					locals.put(offset, token[0].substring(0, token[0].length() - 5));
				else
					locals.put(offset, token[0]);
			}
			return token[0];	
		}
		else
		{
			return arg;
		}
	}
}
