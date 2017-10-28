package visitor;

import java.util.ArrayList;
import java.util.HashMap;

public class StmtNode {
    public String stmtType;
    public int lineNumber;
    public ArrayList<StmtNode> successor;
    public String regAssigned;
    public ArrayList<String> use;
    public String def;
    public ArrayList<String> In;
    public ArrayList<String> Out;
    public ArrayList<String> Indash;
    public ArrayList<String> Outdash;
    public ArrayList<StmtNode> predecessor;
    public String stmtlabel;
    public String targetJumpLabel;

    public StmtNode(){
        stmtType = "";
        lineNumber = -1;
        successor = new ArrayList<>();
        regAssigned = "";
        use = new ArrayList<>();
        def = "";
        In = new ArrayList<>();
        Out = new ArrayList<>();
        predecessor = new ArrayList<>();
        stmtlabel = "";
        targetJumpLabel = "";
        Indash = new ArrayList<>();
        Outdash = new ArrayList<>();
    }
}
