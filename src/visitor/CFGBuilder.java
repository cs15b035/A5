package visitor;

import syntaxtree.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

public class CFGBuilder<R,A> extends GJDepthFirst<R,A> {
    //
    // Auto class visitors--probably don't need to be overridden.
    //


    public HashMap<String,CFG> globalCFGS = new HashMap<String,CFG>();

    int currentLineNumber = 1;
    CFG currentCFG;

    public R visit(NodeList n, A argu) {
        R _ret=null;
        int _count=0;
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
        }
        return _ret;
    }

    public R visit(NodeListOptional n, A argu) {
        if ( n.present() ) {
            R _ret=null;
            int _count=0;
            ArrayList<R> list = new ArrayList<>();
            for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
                list.add(e.nextElement().accept(this,argu));
                _count++;
            }
            return (R)list;
        }
        else
            return null;
    }

    public R visit(NodeOptional n, A argu) {
        if ( n.present() )
            return n.node.accept(this,argu);
        else
            return null;
    }

    public R visit(NodeSequence n, A argu) {
        R _ret=null;
        int _count=0;
        for ( Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this,argu);
            _count++;
        }
        return _ret;
    }

    public R visit(NodeToken n, A argu) { return null; }

    //
    // User-generated visitor methods below
    //

    /**
     * f0 -> "MAIN"
     * f1 -> StmtList()
     * f2 -> "END"
     * f3 -> ( Procedure() )*
     * f4 -> <EOF>
     */
    public R visit(Goal n, A argu) {
        R _ret=null;
        currentCFG = new CFG();
        currentCFG.procName = "MAIN";
        currentCFG.numArgs = 0;
        globalCFGS.put(currentCFG.procName,currentCFG);
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        currentCFG.resolveSuccessor();
        currentCFG.computeRanges();
        currentCFG.linearScan();
        //currentCFG.printCFG();


        currentCFG = null;
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);


        _ret = (R)globalCFGS;
        return _ret;
    }

    /**
     * f0 -> ( ( Label() )? Stmt() )*
     */
    public R visit(StmtList n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Label()
     * f1 -> "["
     * f2 -> IntegerLiteral()
     * f3 -> "]"
     * f4 -> StmtExp()
     */
    public R visit(Procedure n, A argu) {
        R _ret=null;
        currentLineNumber = 1;
        currentCFG = new CFG();
        currentCFG.numArgs = Integer.parseInt(n.f2.f0.tokenImage);
        currentCFG.procName = n.f0.f0.tokenImage;
        globalCFGS.put(currentCFG.procName,currentCFG);
       // n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        n.f3.accept(this, argu);
        n.f4.accept(this, argu);
        currentCFG.resolveSuccessor();
        currentCFG.computeRanges();
        currentCFG.linearScan();
       // currentCFG.printCFG();
        currentCFG = null;
        return _ret;
    }

    /**
     * f0 -> NoOpStmt()
     *       | ErrorStmt()
     *       | CJumpStmt()
     *       | JumpStmt()
     *       | HStoreStmt()
     *       | HLoadStmt()
     *       | MoveStmt()
     *       | PrintStmt()
     */
    public R visit(Stmt n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "NOOP"
     */
    public R visit(NoOpStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "NOOP";
        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "ERROR"
     */
    public R visit(ErrorStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.stmtType = "ERROR";
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);
        currentCFG.Nodes.add(stmtNode);
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "CJUMP"
     * f1 -> Temp()
     * f2 -> Label()
     */
    public R visit(CJumpStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "CJUMP";
        stmtNode.targetJumpLabel = n.f2.f0.tokenImage;

        n.f0.accept(this, argu);
        stmtNode.use.add((String) n.f1.accept(this, argu));
        //n.f2.accept(this, argu);

        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);

        return _ret;
    }

    /**
     * f0 -> "JUMP"
     * f1 -> Label()
     */
    public R visit(JumpStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "JUMP";
        stmtNode.targetJumpLabel = n.f1.f0.tokenImage;

        n.f0.accept(this, argu);
       // n.f1.accept(this, argu);

        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);

        return _ret;
    }

    /**
     * f0 -> "HSTORE"
     * f1 -> Temp()
     * f2 -> IntegerLiteral()
     * f3 -> Temp()
     */
    public R visit(HStoreStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "HSTORE";

        n.f0.accept(this, argu);

        stmtNode.use.add((String) n.f1.accept(this, argu));

        n.f2.accept(this, argu);

        stmtNode.use.add((String) n.f3.accept(this, argu));

        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);

        return _ret;
    }

    /**
     * f0 -> "HLOAD"
     * f1 -> Temp()
     * f2 -> Temp()
     * f3 -> IntegerLiteral()
     */
    public R visit(HLoadStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "HLOAD";

        n.f0.accept(this, argu);

        stmtNode.def = (String) n.f1.accept(this, argu);
        stmtNode.use.add((String) n.f2.accept(this, argu));

        n.f3.accept(this, argu);


        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);
        return _ret;
    }

    /**
     * f0 -> "MOVE"
     * f1 -> Temp()
     * f2 -> Exp()
     */
    public R visit(MoveStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "MOVE";
        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);




        n.f0.accept(this, argu);

       stmtNode.def = (String) n.f1.accept(this, argu);

        ArrayList<String> myArrayList = (ArrayList<String>)n.f2.accept(this, argu);
        stmtNode.use.addAll(myArrayList);
        return _ret;
    }

    /**
     * f0 -> "PRINT"
     * f1 -> SimpleExp()
     */
    public R visit(PrintStmt n, A argu) {
        R _ret=null;

        StmtNode stmtNode = new StmtNode();
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;
        stmtNode.stmtType = "PRINT";




        n.f0.accept(this, argu);
        ArrayList<String> myArrayList = (ArrayList<String>) n.f1.accept(this, argu);
        stmtNode.use.addAll(myArrayList);

        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);
        return _ret;
    }

    /**
     * f0 -> Call()
     *       | HAllocate()
     *       | BinOp()
     *       | SimpleExp()
     */
    public R visit(Exp n, A argu) {
        R _ret=null;
        _ret = n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> "BEGIN"
     * f1 -> StmtList()
     * f2 -> "RETURN"
     * f3 -> SimpleExp()
     * f4 -> "END"
     */
    public R visit(StmtExp n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        n.f2.accept(this, argu);

        StmtNode stmtNode = new StmtNode();
        stmtNode.stmtType = "RETURN";
        stmtNode.lineNumber = currentLineNumber;
        currentLineNumber++;


        stmtNode.use.addAll((ArrayList<String>)n.f3.accept(this, argu));
        n.f4.accept(this, argu);

        currentCFG.Nodes.add(stmtNode);
        currentCFG.lineInfo.put(stmtNode.lineNumber,stmtNode);
        return _ret;
    }

    /**
     * f0 -> "CALL"
     * f1 -> SimpleExp()
     * f2 -> "("
     * f3 -> ( Temp() )*
     * f4 -> ")"
     */
    public R visit(Call n, A argu) {
        R _ret=null;


        n.f0.accept(this, argu);
       ArrayList<String> arr = (ArrayList<String>) n.f1.accept(this, argu);
        n.f2.accept(this, argu);
        ArrayList<String> myArraylist = (ArrayList<String>)n.f3.accept(this, argu);
        if(myArraylist == null) myArraylist = new ArrayList<String>();
        currentCFG.maxCallArgs = Math.max(currentCFG.maxCallArgs,myArraylist.size());
        n.f4.accept(this, argu);
        if(arr != null && myArraylist!=null){
            myArraylist.add(0,arr.get(0));
        }
        _ret = (R)myArraylist;

        return _ret;
    }

    /**
     * f0 -> "HALLOCATE"
     * f1 -> SimpleExp()
     */
    public R visit(HAllocate n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
       ArrayList<String> arr = (ArrayList<String>) n.f1.accept(this, argu);

       _ret = (R)arr;
        return _ret;
    }

    /**
     * f0 -> Operator()
     * f1 -> Temp()
     * f2 -> SimpleExp()
     */
    public R visit(BinOp n, A argu) {
        R _ret=null;

        ArrayList<String> myArrayList = new ArrayList<>();


        n.f0.accept(this, argu);
        myArrayList.add((String) n.f1.accept(this, argu));
       ArrayList<String> arr = (ArrayList<String>) n.f2.accept(this, argu);
       if(arr!=null && arr.size()>=1){
           myArrayList.add(0,arr.get(0));
       }
       _ret = (R)myArrayList;
        return _ret;
    }

    /**
     * f0 -> "LE"
     *       | "NE"
     *       | "PLUS"
     *       | "MINUS"
     *       | "TIMES"
     *       | "DIV"
     */
    public R visit(Operator n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> Temp()
     *       | IntegerLiteral()
     *       | Label()
     */
    boolean myflag = false;
    public R visit(SimpleExp n, A argu) {
        R _ret=null;
        myflag = true;
        _ret = n.f0.accept(this, argu);
        ArrayList<String> myArrayList = new ArrayList<>();
        if(_ret != null){
            myArrayList.add((String)_ret);
        }
        _ret = (R)myArrayList;
        myflag = false;
        return _ret;
    }

    /**
     * f0 -> "TEMP"
     * f1 -> IntegerLiteral()
     */
    public R visit(Temp n, A argu) {
        R _ret=null;

        String str = "TEMP ";
        str = str + n.f1.f0.tokenImage;

        _ret = (R)str;
        n.f0.accept(this, argu);
        n.f1.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
    public R visit(IntegerLiteral n, A argu) {
        R _ret=null;
        n.f0.accept(this, argu);
        return _ret;
    }

    /**
     * f0 -> <IDENTIFIER>
     */
    public R visit(Label n, A argu) {
        R _ret=null;
        if(myflag == true) return null;
        currentCFG.auxlabelInfo.put(n.f0.tokenImage,currentLineNumber);
        currentCFG.labelInfo.put(currentLineNumber,n.f0.tokenImage);

        n.f0.accept(this, argu);
        return _ret;
    }





}
