import syntaxtree.*;
import visitor.*;

import java.util.HashMap;

public class Main {
   public static void main(String [] args) {
      try {
         Node root = new microIRParser(System.in).Goal();
         //System.out.println("Program parsed successfully");
         HashMap<String,CFG>globalCFGs = (HashMap<String,CFG>) root.accept(new CFGBuilder(),null);
         root.accept(new RegisterAllocation(),globalCFGs);
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 



