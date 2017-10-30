package visitor;

import java.util.*;


public class CFG {
    public String procName;
    public ArrayList<StmtNode> Nodes;
    public HashMap<String,Pair> liveranges;
    public HashMap<Integer, StmtNode> lineInfo;
    public HashMap<Integer,String> labelInfo;
    public HashMap<String,Integer> auxlabelInfo;
    public ArrayList<String> registers;
    public HashMap<String,String> allocatedRegisters;
    public HashMap<String,Integer> stackLocation;
    public int stackSlotsRequired;
    public int numArgs;
    public int maxCallArgs;

    CFG(){
        procName = "";
        Nodes = new ArrayList<>();
        liveranges = new HashMap<>();
        lineInfo = new HashMap<>();
        labelInfo = new HashMap<>();
        auxlabelInfo = new HashMap<>();
        String[] reg = {"t9","t8","t7","t6","t5","t4","t3","t2","t1", "t0 ","s7","s6","s5","s4","s3", "s2", "s1","s0"};
        registers = new ArrayList<String>(Arrays.asList(reg));
        allocatedRegisters = new HashMap<>();
        stackLocation = new HashMap<>();
        stackSlotsRequired = 0;
        numArgs = 0;
        maxCallArgs = 0;
    }



    public void resolveSuccessor(){
        for(StmtNode stmtNode : Nodes){
            if(labelInfo.get(stmtNode.lineNumber)!=null)stmtNode.stmtlabel = labelInfo.get(stmtNode.lineNumber);
            if(!stmtNode.stmtType.equals("JUMP")&&!stmtNode.stmtType.equals("CJUMP")){
                stmtNode.successor.add(lineInfo.get(stmtNode.lineNumber+1));
            }
            else if(stmtNode.stmtType.equals("JUMP")){
                stmtNode.successor.add(lineInfo.get(auxlabelInfo.get(stmtNode.targetJumpLabel)));
            }
            else if(stmtNode.stmtType.equals("CJUMP")){
                stmtNode.successor.add(lineInfo.get(auxlabelInfo.get(stmtNode.targetJumpLabel)));
                stmtNode.successor.add(lineInfo.get(stmtNode.lineNumber+1));
            }


        }
    }


    public void computeINOUT(){
        for(StmtNode n : Nodes){
            n.In = new ArrayList<>();
            n.Out = new ArrayList<>();
        }

        while(true) {
            for (StmtNode n : Nodes) {
                n.Indash.addAll(n.In);
                n.Outdash.addAll(n.Out);
                ArrayList<String>  def = new ArrayList<String>();
                def.add(n.def);
                n.In = union(n.use,difference(n.Out,def));
                ArrayList<String> temp = new ArrayList<>();

                for(StmtNode stmtNode : n.successor){
                    if(stmtNode == null) break;
                    temp = union(temp,stmtNode.In);
                }
                n.Out = temp;
            }
            if(mystop()) return;
        }
    }

    public boolean mystop(){
        boolean flag = true;
        for(StmtNode n : Nodes){
            if(!areEqual(n.In,n.Indash) || !areEqual(n.Out,n.Outdash)) flag = false ;
        }
        return flag;
    }


    public boolean areEqual(ArrayList<String> l1 , ArrayList<String> l2){
        HashSet<String> s1 = new HashSet<>();
        s1.addAll(l1);
        HashSet<String> s2 = new HashSet<>();
        s2.addAll(l2);
        HashSet<String> s3 = new HashSet<>();
        s3.addAll(l1);

        s1.removeAll(s2);
        s2.removeAll(s3);
        if(s1.isEmpty() && s2.isEmpty()) return true;
        return false;
    }


    public ArrayList<String> union(ArrayList<String> l1 , ArrayList<String> l2){
        HashSet<String> s1 = new HashSet<>();
        s1.addAll(l1);
        s1.addAll(l2);
        ArrayList<String> res = new ArrayList<String>(s1);
        return res;
    }

    public ArrayList<String> difference(ArrayList<String> l1 , ArrayList<String> l2){
        HashSet<String> s1 = new HashSet<>();
        s1.addAll(l1);
        s1.removeAll(l2);
        ArrayList<String> res = new ArrayList<String>(s1);
        return res;
    }



    public void computeRanges(){
        computeINOUT();
        for(StmtNode n : Nodes){
            for(String str : n.In){
                if(!liveranges.containsKey(str)){
                    Pair pair = new Pair();
                    pair.startPoint = n.lineNumber;
                    pair.endPoint = n.lineNumber;
                    liveranges.put(str,pair);
                }
                else{
                    Pair pair = liveranges.get(str);
                    pair.endPoint = n.lineNumber;
                }
            }
        }
    }

    int slocation = 0;
    public void linearScan(){
        ArrayList<Range> active = new ArrayList<Range>();
        ArrayList<Range> intervals = new ArrayList<Range>();

        for(String str : liveranges.keySet()){
            Range range = new Range();
            range.varName = str;
            range.start = liveranges.get(str).startPoint;
            range.end = liveranges.get(str).endPoint;
            intervals.add(range);
        }
        intervals.sort(new SortbyStart());

        for(Range i : intervals) {
            expireOldIntervals(active,i);
            if (active.size() == 18) {
                spillAtInterval(active, i );
            }
            else{
                allocatedRegisters.put(i.varName,registers.get(0));
                registers.remove(0);
                active.add(i);
                active.sort(new SortbyEnd());
            }
        }
    }

    public void expireOldIntervals(ArrayList<Range> active , Range i){
        for(int j=0;j<active.size();j++ ){
            if(active.get(j).end>=i.start) return ;
            registers.add(registers.size()-1,allocatedRegisters.get(active.get(j).varName));
            active.remove(active.get(j));
        }
    }

    public void spillAtInterval(ArrayList<Range> active ,Range i){
        Range spill = active.get(active.size()-1);
        if(spill.end > i.end){
            allocatedRegisters.put(i.varName,allocatedRegisters.get(spill.varName));
            allocatedRegisters.remove(spill.varName);
            stackLocation.put(spill.varName,slocation);
            slocation++;
            active.remove(spill);
            active.add(i);
            active.sort(new SortbyEnd());
        }
        else stackLocation.put(i.varName,slocation);
        slocation++;
    }


}


class SortbyStart implements Comparator<Range> {

    public int compare(Range r1, Range r2){
        return r1.start-r2.start;
    }
}

class SortbyEnd implements Comparator<Range> {

    public int compare(Range r1, Range r2){
        return r1.end-r2.end;
    }
}

class Range{
    String varName;
    int start;
    int end;
    Range(){
        varName = "";
        start = 0;
        end = 0;
    }
}
