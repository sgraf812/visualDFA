package dfa.analyses.constantfoldingTests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import codeprocessor.CodeProcessor;
import codeprocessor.GraphBuilder;
import dfa.analyses.ConstantFoldingElement;
import dfa.analyses.ConstantFoldingElement.Value;
import dfa.analyses.ConstantFoldingInitializer;
import dfa.analyses.ConstantFoldingJoin;
import dfa.analyses.ConstantFoldingTransition;
import dfa.analysesTests.LocalAliasMap;
import dfa.framework.BlockState;
import dfa.framework.SimpleBlockGraph;
import dfaTests.TestMethod;
import dfaTests.TestUtils;
import dfaTests.ValueHelper;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class TestTransition01 {

    private static TestUtils<Value> tu = new TestUtils<Value>();

    private static SimpleBlockGraph bgAllConstant;
    private static SimpleBlockGraph bgAllConstantConflict;
    private static SimpleBlockGraph bgNonConstant;
    private static SimpleBlockGraph bgIntArithmetic;
    private static SimpleBlockGraph bgIntBitOps;

    @BeforeClass
    public static void setUp() {
        TestMethod testMethodAllConstant = getCodeAllConstant();
        CodeProcessor cp = new CodeProcessor(testMethodAllConstant.method);
        Assert.assertTrue(cp.wasSuccessful());

        GraphBuilder gb = new GraphBuilder(cp.getPath(), cp.getClassName());
        bgAllConstant = gb.buildGraph(testMethodAllConstant.signature);

        TestMethod testMethodAllConstantConflict = getCodeAllConstantConflict();
        cp = new CodeProcessor(testMethodAllConstantConflict.method);
        Assert.assertTrue(cp.wasSuccessful());

        gb = new GraphBuilder(cp.getPath(), cp.getClassName());
        bgAllConstantConflict = gb.buildGraph(testMethodAllConstantConflict.signature);

        TestMethod testMethodNonConstant = getCodeNonConstant();
        cp = new CodeProcessor(testMethodNonConstant.method);
        Assert.assertTrue(cp.wasSuccessful());

        gb = new GraphBuilder(cp.getPath(), cp.getClassName());
        bgNonConstant = gb.buildGraph(testMethodNonConstant.signature);

        TestMethod testMethodIntArithmetic = getCodeIntArithmetic();
        cp = new CodeProcessor(testMethodIntArithmetic.method);
        Assert.assertTrue(cp.wasSuccessful());

        gb = new GraphBuilder(cp.getPath(), cp.getClassName());
        bgIntArithmetic = gb.buildGraph(testMethodIntArithmetic.signature);

        TestMethod testMethodIntBitOps = getCodeIntBitOps();
        cp = new CodeProcessor(testMethodIntBitOps.method);
        Assert.assertTrue(cp.wasSuccessful());

        gb = new GraphBuilder(cp.getPath(), cp.getClassName());
        bgIntBitOps = gb.buildGraph(testMethodIntBitOps.signature);
    }

    @Test
    public void testAllConstant() {
        tu.setPrint(false);

        Assert.assertEquals(1, bgAllConstant.getBlocks().size());

        Block onlyBlock = bgAllConstant.getBlocks().get(0);

        tu.printInfo("---- this is the only block ---- ");
        List<Unit> units = tu.getUnitsFromBlock(onlyBlock);
        tu.printInfo(tu.unitsToString(units));

        LocalAliasMap<Value> aliasMap = buildVariableAliasMapAllConstant();
        tu.printInfo("\n" + aliasMap);

        ConstantFoldingInitializer cfInit = new ConstantFoldingInitializer(bgAllConstant);
        Map<Block, BlockState<ConstantFoldingElement>> initMap = cfInit.getInitialStates();

        ConstantFoldingElement initInState = initMap.get(onlyBlock).getInState();
        ConstantFoldingElement initOutState = initMap.get(onlyBlock).getOutState();

        tu.printInfo("\n" + "---- the initial in-state ----");
        tu.printInfo(initInState.getStringRepresentation());

        ConstantFoldingElement currentCfe = initInState;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        tu.printInfo("\n" + "---- the initial out-state ----");
        tu.printInfo(initOutState.getStringRepresentation());

        currentCfe = initOutState;
        tu.assertLocalValue(Value.getBottom(), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "y", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "z", aliasMap, currentCfe);

        ConstantFoldingTransition cfTransition = new ConstantFoldingTransition();

        ConstantFoldingElement cfe01 = cfTransition.transition(initInState, units.get(0));
        currentCfe = cfe01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe02 = cfTransition.transition(cfe01, units.get(1));
        currentCfe = cfe02;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe03 = cfTransition.transition(cfe02, units.get(2));
        currentCfe = cfe03;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe04 = cfTransition.transition(cfe03, units.get(3));
        currentCfe = cfe04;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe05 = cfTransition.transition(cfe04, units.get(4));
        currentCfe = cfe05;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe06 = cfTransition.transition(cfe05, units.get(5));
        currentCfe = cfe06;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(16), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfe07 = cfTransition.transition(cfe06, units.get(6));
        currentCfe = cfe07;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(16), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(4), "z", aliasMap, currentCfe);
    }

    private static TestMethod getCodeAllConstant() {
        String signature = "void test_allConstant()";
        // @formatter:off
		String method = 
		        "public void test_allConstant() {"
		                + "int one = 1;"      // to prevent Java from narrowing small constant ints to byte
		                + "int x = 0 * one;" 
		                + "int y = 4 * one;" 
		                + "int z = x + y;" 
		                + "x = z * y;" 
                + "}";
		// @formatter:on
        return new TestMethod(signature, method);
    }

    private LocalAliasMap<Value> buildVariableAliasMapAllConstant() {
        Block onlyBlock = bgAllConstant.getBlocks().get(0);
        Body body = onlyBlock.getBody();
        Chain<Local> locals = body.getLocals();

        LocalAliasMap<Value> aliasMap = new LocalAliasMap<Value>(body.getLocals());
        Iterator<Local> localIt = locals.iterator();
        while (localIt.hasNext()) {
            Local l = localIt.next();
            String localName = l.getName();
            if (localName.equals("l1")) {
                aliasMap.setAlias("l1", "one");
            } else if (localName.equals("l2")) {
                aliasMap.setAlias("l2", "x");
            } else if (localName.equals("l3")) {
                aliasMap.setAlias("l3", "y");
            } else if (localName.equals("l4")) {
                aliasMap.setAlias("l4", "z");
            }
        }

        return aliasMap;
    }

    @Test
    public void testAllConstantConflict() {
        tu.setPrint(false);

        List<Block> blocks = bgAllConstantConflict.getBlocks();
        Assert.assertEquals(4, blocks.size());

        Block startBlock = bgAllConstantConflict.getHeads().get(0);
        Block trueBranchBlock = bgAllConstantConflict.getBlocks().get(1);
        Block falseBranchBlock = bgAllConstantConflict.getBlocks().get(2);
        Block endBlock = bgAllConstantConflict.getTails().get(0);

        tu.printInfo("---- this is the start-block ---- ");
        List<Unit> unitsStartBlock = tu.getUnitsFromBlock(startBlock);
        tu.printInfo(tu.unitsToString(unitsStartBlock));

        tu.printInfo("\n ---- this is the true-branch-block ---- ");
        List<Unit> unitsTrueBranchBlock = tu.getUnitsFromBlock(trueBranchBlock);
        tu.printInfo(tu.unitsToString(unitsTrueBranchBlock));

        tu.printInfo("\n ---- this is the false-branch-block ---- ");
        List<Unit> unitsFalseBranchBlock = tu.getUnitsFromBlock(falseBranchBlock);
        tu.printInfo(tu.unitsToString(unitsFalseBranchBlock));

        tu.printInfo("\n ---- this is the end-block ---- ");
        List<Unit> unitsEndBlock = tu.getUnitsFromBlock(endBlock);
        tu.printInfo(tu.unitsToString(unitsEndBlock));

        LocalAliasMap<Value> aliasMap = buildVariableAliasMapAllConstantConflict();
        tu.printInfo("\n" + aliasMap);

        ConstantFoldingInitializer cfInit = new ConstantFoldingInitializer(bgAllConstantConflict);
        Map<Block, BlockState<ConstantFoldingElement>> initMap = cfInit.getInitialStates();

        // begin testing transitions for start-block
        ConstantFoldingElement initInStateStartBlock = initMap.get(startBlock).getInState();
        ConstantFoldingElement initOutStateStartBlock = initMap.get(startBlock).getOutState();

        tu.printInfo("\n" + "---- the initial in-state of start-block ----");
        tu.printInfo(initInStateStartBlock.getStringRepresentation());

        ConstantFoldingElement currentCfe = initInStateStartBlock;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        tu.printInfo("\n" + "---- the initial out-state of start-block ----");
        tu.printInfo(initOutStateStartBlock.getStringRepresentation());

        currentCfe = initOutStateStartBlock;
        tu.assertLocalValue(Value.getBottom(), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "y", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "z", aliasMap, currentCfe);

        ConstantFoldingTransition cfTransition = new ConstantFoldingTransition();

        ConstantFoldingElement cfeStart01 = cfTransition.transition(initInStateStartBlock, unitsStartBlock.get(0));
        currentCfe = cfeStart01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfeStart02 = cfTransition.transition(cfeStart01, unitsStartBlock.get(1));
        currentCfe = cfeStart02;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfeStart03 = cfTransition.transition(cfeStart02, unitsStartBlock.get(2));
        currentCfe = cfeStart03;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(17), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);
        // end testing transitions for start-block

        // begin testing transitions for true-branch
        ConstantFoldingElement cfeTrueBranch01 = cfTransition.transition(cfeStart03, unitsTrueBranchBlock.get(0));
        currentCfe = cfeTrueBranch01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfeTrueBranch02 = cfTransition.transition(cfeTrueBranch01, unitsTrueBranchBlock.get(1));
        currentCfe = cfeTrueBranch02;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);
        // end testing transitions for true-branch

        // begin testing transitions for false-branch
        ConstantFoldingElement cfeFalseBranch01 = cfTransition.transition(cfeStart03, unitsFalseBranchBlock.get(0));
        currentCfe = cfeFalseBranch01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);
        // end testing transitions for false-branch

        // do the join of the branches
        ConstantFoldingJoin cfJoin = new ConstantFoldingJoin();
        Set<ConstantFoldingElement> toJoin = new HashSet<ConstantFoldingElement>();
        toJoin.add(cfeTrueBranch02);
        toJoin.add(cfeFalseBranch01);

        ConstantFoldingElement joinResult = cfJoin.join(toJoin);
        currentCfe = joinResult;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        // begin testing transitions for end-block
        ConstantFoldingElement cfeEnd01 = cfTransition.transition(joinResult, unitsEndBlock.get(0));
        currentCfe = cfeEnd01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe); // here 0 * TOP should be 0
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "z", aliasMap, currentCfe);

        ConstantFoldingElement cfeEnd02 = cfTransition.transition(cfeEnd01, unitsEndBlock.get(1));
        currentCfe = cfeEnd02;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "z", aliasMap, currentCfe); // here TOP + y should be TOP

        ConstantFoldingElement cfeEnd03 = cfTransition.transition(cfeEnd02, unitsEndBlock.get(2));
        currentCfe = cfeEnd03;
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "z", aliasMap, currentCfe);
        // end testing transitions for end-block
    }

    private static TestMethod getCodeAllConstantConflict() {
        String signature = "void test_allConstantConflict()";
        // @formatter:off
        String method = 
            "void test_allConstantConflict() {" 
                + "int one = 1;" // to prevent Java from narrowing small constant ints to byte
                + "int x = 17 * one;" 
                + "if (one == 1) {" 
                + "x = one;" 
                + "} else {" 
                + "x = 0 * one;" 
                + "}"
                + "int y = 0 * x;" 
                + "int z = x + y;" 
            + "}";
        // @formatter:on
        return new TestMethod(signature, method);
    }

    private LocalAliasMap<Value> buildVariableAliasMapAllConstantConflict() {
        Block onlyBlock = bgAllConstant.getBlocks().get(0);
        Body body = onlyBlock.getBody();
        Chain<Local> locals = body.getLocals();

        LocalAliasMap<Value> aliasMap = new LocalAliasMap<Value>(body.getLocals());
        Iterator<Local> localIt = locals.iterator();
        while (localIt.hasNext()) {
            Local l = localIt.next();
            String localName = l.getName();
            if (localName.equals("l1")) {
                aliasMap.setAlias("l1", "one");
            } else if (localName.equals("l2")) {
                aliasMap.setAlias("l2", "x");
            } else if (localName.equals("l3")) {
                aliasMap.setAlias("l3", "y");
            } else if (localName.equals("l4")) {
                aliasMap.setAlias("l4", "z");
            }
        }

        return aliasMap;
    }

    @Test
    public void testNonConstant() {
        tu.setPrint(false);

        List<Block> blocks = bgNonConstant.getBlocks();
        Assert.assertEquals(1, blocks.size());

        Block onlyBlock = blocks.get(0);

        tu.printInfo("---- this is the only block ---- ");
        List<Unit> units = tu.getUnitsFromBlock(onlyBlock);

        LocalAliasMap<Value> aliasMap = buildVariableAliasMapNonConstant();
        tu.printInfo("\n" + aliasMap);

        ConstantFoldingInitializer cfInit = new ConstantFoldingInitializer(bgNonConstant);
        Map<Block, BlockState<ConstantFoldingElement>> initMap = cfInit.getInitialStates();

        ConstantFoldingElement initInStateStartBlock = initMap.get(onlyBlock).getInState();
        ConstantFoldingElement initOutStateStartBlock = initMap.get(onlyBlock).getOutState();

        tu.printInfo("\n" + "---- the initial in-state of start-block ----");
        tu.printInfo(initInStateStartBlock.getStringRepresentation());

        ConstantFoldingElement currentCfe = initInStateStartBlock;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        tu.printInfo("\n" + "---- the initial out-state of start-block ----");
        tu.printInfo(initOutStateStartBlock.getStringRepresentation());

        currentCfe = initOutStateStartBlock;
        tu.assertLocalValue(Value.getBottom(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "y", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "$i0", aliasMap, currentCfe);

        ConstantFoldingTransition cfTransition = new ConstantFoldingTransition();

        ConstantFoldingElement cfe01 = cfTransition.transition(initInStateStartBlock, units.get(0));
        currentCfe = cfe01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        ConstantFoldingElement cfe02 = cfTransition.transition(cfe01, units.get(1));
        currentCfe = cfe02;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe); // p is a parameter and should become TOP
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        ConstantFoldingElement cfe03 = cfTransition.transition(cfe02, units.get(2));
        currentCfe = cfe03;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        ConstantFoldingElement cfe04 = cfTransition.transition(cfe03, units.get(3));
        currentCfe = cfe04;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe); // 1 + TOP = TOP
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        ConstantFoldingElement cfe05 = cfTransition.transition(cfe04, units.get(4));
        currentCfe = cfe05;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe); // 0 * TOP = 0

        ConstantFoldingElement cfe06 = cfTransition.transition(cfe05, units.get(5));
        currentCfe = cfe06;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(5), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);

        ConstantFoldingElement cfe07 = cfTransition.transition(cfe06, units.get(6));
        currentCfe = cfe07;
        tu.assertLocalValue(Value.getTop(), "p", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "x", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(5), "y", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "$i0", aliasMap, currentCfe);
    }

    private static TestMethod getCodeNonConstant() {
        String signature = "int test_nonConstant(int)";
        // @formatter:off
        String method = 
                "int test_nonConstant(int p) {" 
                        + "int one = 1;" // to prevent Java from narrowing small constant ints to byte
                        + "int x = p + one;" 
                        + "int y = p * 0 + 5;" 
                        + "return y;" 
                    + "}";
        // @formatter:on
        return new TestMethod(signature, method);
    }

    private LocalAliasMap<Value> buildVariableAliasMapNonConstant() {
        Block onlyBlock = bgNonConstant.getBlocks().get(0);
        Body body = onlyBlock.getBody();
        Chain<Local> locals = body.getLocals();

        LocalAliasMap<Value> aliasMap = new LocalAliasMap<Value>(body.getLocals());
        Iterator<Local> localIt = locals.iterator();
        while (localIt.hasNext()) {
            Local l = localIt.next();
            String localName = l.getName();
            if (localName.equals("l1")) {
                aliasMap.setAlias("l1", "p");
            } else if (localName.equals("l2")) {
                aliasMap.setAlias("l2", "one");
            } else if (localName.equals("l3")) {
                aliasMap.setAlias("l3", "x");
            } else if (localName.equals("l4")) {
                aliasMap.setAlias("l4", "y");
            }
        }

        return aliasMap;
    }

    // systematic tests grouped by operations

    @Test
    public void testIntArithmetic() {
        tu.setPrint(false);

        List<Block> blocks = bgIntArithmetic.getBlocks();
        Assert.assertEquals(1, blocks.size());

        Block onlyBlock = blocks.get(0);

        tu.printInfo("---- this is the only block ---- ");
        List<Unit> units = tu.getUnitsFromBlock(onlyBlock);
        tu.printInfo(tu.unitsToString(units));

        LocalAliasMap<Value> aliasMap = buildVariableAliasMapIntArithmetic();
        tu.printInfo("\n" + aliasMap);

        ConstantFoldingInitializer cfInit = new ConstantFoldingInitializer(bgIntArithmetic);
        Map<Block, BlockState<ConstantFoldingElement>> initMap = cfInit.getInitialStates();

        ConstantFoldingElement initInStateStartBlock = initMap.get(onlyBlock).getInState();
        ConstantFoldingElement initOutStateStartBlock = initMap.get(onlyBlock).getOutState();

        tu.printInfo("\n" + "---- the initial in-state of start-block ----");
        tu.printInfo(initInStateStartBlock.getStringRepresentation());

        ConstantFoldingElement currentCfe = initInStateStartBlock;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        tu.printInfo("\n" + "---- the initial out-state of start-block ----");
        tu.printInfo(initOutStateStartBlock.getStringRepresentation());

        currentCfe = initOutStateStartBlock;
        tu.assertLocalValue(Value.getBottom(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "r", aliasMap, currentCfe);

        ConstantFoldingTransition cfTransition = new ConstantFoldingTransition();

        ConstantFoldingElement cfe01 = cfTransition.transition(initInStateStartBlock, units.get(0));
        currentCfe = cfe01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe02 = cfTransition.transition(cfe01, units.get(1));
        currentCfe = cfe02;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe03 = cfTransition.transition(cfe02, units.get(2));
        currentCfe = cfe03;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe04 = cfTransition.transition(cfe03, units.get(3));
        currentCfe = cfe04;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe05 = cfTransition.transition(cfe04, units.get(4));
        currentCfe = cfe05;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe06 = cfTransition.transition(cfe05, units.get(5));
        currentCfe = cfe06;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        // here the real arithmetic test starts
        ConstantFoldingElement cfe07 = cfTransition.transition(cfe06, units.get(6));
        currentCfe = cfe07;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17333), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe08 = cfTransition.transition(cfe07, units.get(7));
        currentCfe = cfe08;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(17533), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe09 = cfTransition.transition(cfe08, units.get(8));
        currentCfe = cfe09;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1743300), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe10 = cfTransition.transition(cfe09, units.get(9));
        currentCfe = cfe10;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-174), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe11 = cfTransition.transition(cfe10, units.get(10));
        currentCfe = cfe11;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe12 = cfTransition.transition(cfe11, units.get(11));
        currentCfe = cfe12;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-33), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe13 = cfTransition.transition(cfe12, units.get(12));
        currentCfe = cfe13;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe14 = cfTransition.transition(cfe13, units.get(13));
        currentCfe = cfe14;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe15 = cfTransition.transition(cfe14, units.get(14));
        currentCfe = cfe15;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe16 = cfTransition.transition(cfe15, units.get(15));
        currentCfe = cfe16;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe17 = cfTransition.transition(cfe16, units.get(16));
        currentCfe = cfe17;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe18 = cfTransition.transition(cfe17, units.get(17));
        currentCfe = cfe18;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe19 = cfTransition.transition(cfe18, units.get(18));
        currentCfe = cfe19;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe20 = cfTransition.transition(cfe19, units.get(19));
        currentCfe = cfe20;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe21 = cfTransition.transition(cfe20, units.get(20));
        currentCfe = cfe21;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe22 = cfTransition.transition(cfe21, units.get(21));
        currentCfe = cfe22;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe); // top * 0 = 0

        ConstantFoldingElement cfe23 = cfTransition.transition(cfe22, units.get(22));
        currentCfe = cfe23;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe24 = cfTransition.transition(cfe23, units.get(23));
        currentCfe = cfe24;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-17433), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(100), "d", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);
    }

    private static TestMethod getCodeIntArithmetic() {
        String signature = "void test_intArithmetic(int)";
        // @formatter:off
        String method = 
                "void test_intArithmetic(int top) {"
                        + "int one = 1;" // to prevent Java from narrowing small constant ints to byte
                        + "int zero = 0 * 1;" 
                        + "int c = -17433;" 
                        + "int d = 100;" 
                        + "int r = c + d;" 
                        + "r = d - c;"
                        + "r = c * d;" 
                        + "r = c / d;" 
                        + "r = zero / d;" 
                        + "r = c % d;" 
                        + "r = d / zero;" 
                        + "r = d % zero;"
                        + "r = top + c;" 
                        + "r = top - c;" 
                        + "r = top * c;" 
                        + "r = top / c;" 
                        + "r = top % c;" 
                        + "r = c / top;"
                        + "r = c % top;" 
                        + "r = zero * top;" 
                        + "r = zero / top;" 
                    + "}";
        // @formatter:on
        return new TestMethod(signature, method);
    }

    private LocalAliasMap<Value> buildVariableAliasMapIntArithmetic() {
        Block onlyBlock = bgIntArithmetic.getBlocks().get(0);
        Body body = onlyBlock.getBody();
        Chain<Local> locals = body.getLocals();

        LocalAliasMap<Value> aliasMap = new LocalAliasMap<Value>(body.getLocals());
        Iterator<Local> localIt = locals.iterator();
        while (localIt.hasNext()) {
            Local l = localIt.next();
            String localName = l.getName();
            if (localName.equals("l1")) {
                aliasMap.setAlias("l1", "top");
            } else if (localName.equals("l2")) {
                aliasMap.setAlias("l2", "one");
            } else if (localName.equals("l3")) {
                aliasMap.setAlias("l3", "zero");
            } else if (localName.equals("l4")) {
                aliasMap.setAlias("l4", "c");
            } else if (localName.equals("l5")) {
                aliasMap.setAlias("l5", "d");
            } else if (localName.equals("l6")) {
                aliasMap.setAlias("l6", "r");
            }
        }

        return aliasMap;
    }

    @Test
    public void testIntBitOps() {
        tu.setPrint(false);

        List<Block> blocks = bgIntBitOps.getBlocks();
        Assert.assertEquals(1, blocks.size());

        Block onlyBlock = blocks.get(0);

        tu.printInfo("---- this is the only block ---- ");
        List<Unit> units = tu.getUnitsFromBlock(onlyBlock);
        tu.printInfo(tu.unitsToString(units));

        LocalAliasMap<Value> aliasMap = buildVariableAliasMapIntBitOps();
        tu.printInfo("\n" + aliasMap);

        ConstantFoldingInitializer cfInit = new ConstantFoldingInitializer(bgIntBitOps);
        Map<Block, BlockState<ConstantFoldingElement>> initMap = cfInit.getInitialStates();

        ConstantFoldingElement initInStateStartBlock = initMap.get(onlyBlock).getInState();
        ConstantFoldingElement initOutStateStartBlock = initMap.get(onlyBlock).getOutState();

        tu.printInfo("\n" + "---- the initial in-state of start-block ----");
        tu.printInfo(initInStateStartBlock.getStringRepresentation());

        ConstantFoldingElement currentCfe = initInStateStartBlock;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        tu.printInfo("\n" + "---- the initial out-state of start-block ----");
        tu.printInfo(initOutStateStartBlock.getStringRepresentation());

        currentCfe = initOutStateStartBlock;
        tu.assertLocalValue(Value.getBottom(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "one", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "b", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getBottom(), "r", aliasMap, currentCfe);

        ConstantFoldingTransition cfTransition = new ConstantFoldingTransition();

        ConstantFoldingElement cfe01 = cfTransition.transition(initInStateStartBlock, units.get(0));
        currentCfe = cfe01;
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe02 = cfTransition.transition(cfe01, units.get(1));
        currentCfe = cfe02;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe03 = cfTransition.transition(cfe02, units.get(2));
        currentCfe = cfe03;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe04 = cfTransition.transition(cfe03, units.get(3));
        currentCfe = cfe04;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe05 = cfTransition.transition(cfe04, units.get(4));
        currentCfe = cfe05;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe06 = cfTransition.transition(cfe05, units.get(5));
        currentCfe = cfe06;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe07 = cfTransition.transition(cfe06, units.get(6));
        currentCfe = cfe07;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        // here the real bit-operations test starts
        ConstantFoldingElement cfe08 = cfTransition.transition(cfe07, units.get(7));
        currentCfe = cfe08;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC29D), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe09 = cfTransition.transition(cfe08, units.get(8));
        currentCfe = cfe09;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe10 = cfTransition.transition(cfe09, units.get(9));
        currentCfe = cfe10;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe11 = cfTransition.transition(cfe10, units.get(10));
        currentCfe = cfe11;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe12 = cfTransition.transition(cfe11, units.get(11));
        currentCfe = cfe12;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe13 = cfTransition.transition(cfe12, units.get(12));
        currentCfe = cfe13;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe14 = cfTransition.transition(cfe13, units.get(13));
        currentCfe = cfe14;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC29D), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe15 = cfTransition.transition(cfe14, units.get(14));
        currentCfe = cfe15;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe16 = cfTransition.transition(cfe15, units.get(15));
        currentCfe = cfe16;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe17 = cfTransition.transition(cfe16, units.get(16));
        currentCfe = cfe17;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0xFFFFFFF2), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe18 = cfTransition.transition(cfe17, units.get(17));
        currentCfe = cfe18;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x81903D6F), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe19 = cfTransition.transition(cfe18, units.get(18));
        currentCfe = cfe19;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe20 = cfTransition.transition(cfe19, units.get(19));
        currentCfe = cfe20;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0xF9BF0A40), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe21 = cfTransition.transition(cfe20, units.get(20));
        currentCfe = cfe21;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0xF8520000), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe22 = cfTransition.transition(cfe21, units.get(21));
        currentCfe = cfe22;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0xF37E1480), "r", aliasMap, currentCfe); // Java-specific shift

        ConstantFoldingElement cfe23 = cfTransition.transition(cfe22, units.get(22));
        currentCfe = cfe23;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe24 = cfTransition.transition(cfe23, units.get(23));
        currentCfe = cfe24;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe25 = cfTransition.transition(cfe24, units.get(24));
        currentCfe = cfe25;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x1F9BF0A4), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe26 = cfTransition.transition(cfe25, units.get(25));
        currentCfe = cfe26;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x0003F37E), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe27 = cfTransition.transition(cfe26, units.get(26));
        currentCfe = cfe27;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x0FCDF852), "r", aliasMap, currentCfe); // Java-specific shift

        ConstantFoldingElement cfe28 = cfTransition.transition(cfe27, units.get(27));
        currentCfe = cfe28;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe29 = cfTransition.transition(cfe28, units.get(28));
        currentCfe = cfe29;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        // set c to -13 (so that unsigned right-shifts make sense)
        ConstantFoldingElement cfe30 = cfTransition.transition(cfe29, units.get(29));
        currentCfe = cfe30;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe31 = cfTransition.transition(cfe30, units.get(30));
        currentCfe = cfe31;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x3FFFFFFC), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe32 = cfTransition.transition(cfe31, units.get(31));
        currentCfe = cfe32;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x1FFF), "r", aliasMap, currentCfe); // Java-specific shift

        ConstantFoldingElement cfe33 = cfTransition.transition(cfe32, units.get(32));
        currentCfe = cfe33;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x1FFFFFFE), "r", aliasMap, currentCfe); // Java-specific shift

        ConstantFoldingElement cfe34 = cfTransition.transition(cfe33, units.get(33));
        currentCfe = cfe34;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);

        ConstantFoldingElement cfe35 = cfTransition.transition(cfe34, units.get(34));
        currentCfe = cfe35;
        tu.assertLocalValue(Value.getTop(), "top", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(1), "one", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0), "zero", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-1), "allOnes", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(0x7E6FC290), "b", aliasMap, currentCfe);
        tu.assertLocalValue(ValueHelper.getCfIntValue(-13), "c", aliasMap, currentCfe);
        tu.assertLocalValue(Value.getTop(), "r", aliasMap, currentCfe);
    }

    private static TestMethod getCodeIntBitOps() {
        String signature = "void test_intBitOps(int)";
        // @formatter:off
        String method = 
                "void test_intBitOps(int top) {" 
                    + "int one = 1;" // to prevent Java from narrowing small constant ints to byte
                    + "int zero = 0 * 1;" 
                    + "int allOnes = 0xFFFFFFFF;" 
                    + "int b = 0x7E6FC290 * one;" 
                    + "int c = 13 * one;"
                    + "int r = c | b;" 
                    + "r = top | b;" 
                    + "r = top | allOnes;" 
                    + "r = c & b;" 
                    + "r = top & b;"
                    + "r = top & zero;" 
                    + "r = c ^ b;" 
                    + "r = c ^ top;" 
                    + "r = c ^ zero;" 
                    + "r = c ^ allOnes;" + "r = ~b;"
                    + "r = ~top;" 
                    + "r = b << 2;" 
                    + "r = b << c;" 
                    + "r = b << 67;" 
                    + "r = b << top;" 
                    + "r = top << 17;"
                    + "r = b >> 2;" 
                    + "r = b >> c;" 
                    + "r = b >> 67;" 
                    + "r = b >> top;" 
                    + "r = top >> 17;" 
                    + "c = -13 * one;" // adjust
                    + "r = c >>> 2;" 
                    + "r = c >>> c;" 
                    + "r = c >>> 67;" 
                    + "r = c >>> top;" 
                    + "r = top >>> 17;" 
                + "}";
        // @formatter:on
        return new TestMethod(signature, method);
    }

    private LocalAliasMap<Value> buildVariableAliasMapIntBitOps() {
        Block onlyBlock = bgIntBitOps.getBlocks().get(0);
        Body body = onlyBlock.getBody();
        Chain<Local> locals = body.getLocals();

        LocalAliasMap<Value> aliasMap = new LocalAliasMap<Value>(body.getLocals());
        Iterator<Local> localIt = locals.iterator();
        while (localIt.hasNext()) {
            Local l = localIt.next();
            String localName = l.getName();
            if (localName.equals("l1")) {
                aliasMap.setAlias("l1", "top");
            } else if (localName.equals("l2")) {
                aliasMap.setAlias("l2", "one");
            } else if (localName.equals("l3")) {
                aliasMap.setAlias("l3", "zero");
            } else if (localName.equals("l4")) {
                aliasMap.setAlias("l4", "allOnes");
            } else if (localName.equals("l5")) {
                aliasMap.setAlias("l5", "b");
            } else if (localName.equals("l6")) {
                aliasMap.setAlias("l6", "c");
            } else if (localName.equals("l7")) {
                aliasMap.setAlias("l7", "r");
            }
        }

        return aliasMap;
    }

}
