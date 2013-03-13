package cs380C.compiler;

import java.util.Iterator;
import java.util.SortedSet;


public interface BaseCFG extends Iterable<Integer>
{
	public int getPrevBlock(int numline);
	public int getNextBlock(int numline);
	public int getCurrentBlock(int numline);
	
	public int getPrevFunction(int numline);
	public int getNextFunction(int numline);
	public int getCurrentFunction(int numline);
	
	public SortedSet<Integer> getNodes(int function);
	public SortedSet<Integer> getEdges(int node);
	
	public String toString();
	public Iterator<Integer> iterator();
}
