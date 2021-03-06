package cs380C.compiler;

import java.util.Iterator;
import java.util.SortedSet;


public interface BaseCFG extends Iterable<Integer>
{
	public int getEndBlock(int numline);
	public int getPrevBlock(int numline);
	public int getNextBlock(int numline);
	public int getCurrentBlock(int numline);
	
	public int getPrevFunction(int numline);
	public int getNextFunction(int numline);
	public int getCurrentFunction(int numline);
	
	public SortedSet<Integer> getNodes(int function);
	public SortedSet<Integer> getEdges(int node);
	
	public SortedSet<Integer> getPred(int block);
	public SortedSet<Integer> getSucc(int block);
	
	public String toString();
	public Iterator<Integer> iterator();
	public boolean containsFunction(int id);
}
