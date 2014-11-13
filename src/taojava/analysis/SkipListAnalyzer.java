package taojava.analysis;

import java.io.PrintWriter;

import taojava.util.SkipList;

/**
 * Quick and dirty analysis of SkipLists.
 *
 * @author Samuel A. Rebelsky
 */
public class SkipListAnalyzer
{

  public static void main(String[] args)
  {
    PrintWriter pen = new PrintWriter(System.out, true);
    SortedListAnalyzer.analyze(pen, new SkipList<Integer>(.25, 5), 32000, 20);
    pen.close();
  } // main(String[])

} // SkipListAnalyzer
