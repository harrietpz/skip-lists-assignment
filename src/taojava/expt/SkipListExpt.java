package taojava.expt;

import java.io.PrintWriter;

import taojava.util.SkipList;

/**
 * An experiment with SkipLists.
 * 
 * @author Samuel A. Rebelsky
 */
public class SkipListExpt
{
  public static void main(String[] args)
    throws Exception
  {
    PrintWriter pen = new PrintWriter(System.out, true);
    SortedListExpt.stringExperiment(pen, new SkipList<String>(.25,5));
    pen.flush();
  } // main(String[])
} // class SkipListExpt

