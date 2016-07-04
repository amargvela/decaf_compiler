package edu.mit.compilers;

import java.io.FileNotFoundException;
import java.util.*;

import edu.mit.compilers.utils.StateAccumulator;

public class VariableLiveness {

    private static final boolean DEBUG = true;
    private static Map<String, List<Set<String>>> allConcurances = new HashMap<String, List<Set<String>>>();

    public static void analyze(StateAccumulator accumulator)
            throws FileNotFoundException {
        Map<String, BasicBlock> cfgs = accumulator.dataflowVisitor.getMap();
        
        Set<String> globalVariables = new HashSet<String>();
        for (LocalDescriptor global : accumulator.globalSymbolTable.getAllDescriptors()) {
            globalVariables.add(global.getVariableId());
        }
        
        for (String methodName : cfgs.keySet()) {
            for (BasicBlock block : cfgs.get(methodName).getNodes()) {
                block.generateUseAndDef(new HashSet<String>(globalVariables));
            }
            
            generateInsAndOuts(cfgs.get(methodName), new HashSet<String>(globalVariables));
            
            List<Set<String>> concurances = new ArrayList<Set<String>>();
            for (BasicBlock block : cfgs.get(methodName).getNodes()) {
                concurances.addAll(block.livenessAtStatements(new HashSet<String>(globalVariables)));
            }
            
            List<Set<String>> realConcurances = new ArrayList<Set<String>>();
            for (Set<String> conc: concurances) {
                if (!conc.isEmpty()) {
                    realConcurances.add(conc);
                }
            }
            allConcurances.put(methodName, realConcurances);
        }
    }
    
    public static List<Set<String>> findConcurances(String methodName) {
        return allConcurances.get(methodName);
    }

    private static void generateInsAndOuts(BasicBlock entryNode, HashSet<String> globalVariables) {
        Set<BasicBlock> allNodes = new HashSet<BasicBlock>();
        Set<BasicBlock> leafNodes = new HashSet<BasicBlock>();
        findAllNodes(entryNode, allNodes, leafNodes);
        
        for (BasicBlock node : leafNodes) {
            node.setLiveOut(globalVariables);
            Set<String> newIn = node.getLiveOut();
            newIn.removeAll(node.getDef());
            newIn.addAll(node.getUse());
            node.setLiveIn(newIn);
        }
        
        Set<BasicBlock> changed = new HashSet<BasicBlock>(allNodes);
        changed.removeAll(leafNodes);

        while (!changed.isEmpty()) {
            BasicBlock curr = null;
            for (BasicBlock node : changed) {
                curr = node;
                break;
            }
            changed.remove(curr);
            
            Set<String> newOut = new HashSet<String>();
            for (BasicBlock succ : curr.getSuccessors()) {
                    newOut.addAll(succ.getLiveIn());
            }
            curr.setLiveOut(newOut);

            Set<String> newIn = curr.getLiveOut();
            newIn.removeAll(curr.getDef());
            newIn.addAll(curr.getUse());
            boolean outChanged = !(curr.getLiveIn().containsAll(newIn) && newIn.containsAll(curr.getLiveIn()));
            curr.setLiveIn(newIn);

            if (outChanged) {
                changed.addAll(curr.getPredecessors());
            }
        }
//        System.out.println(entryNode.getLiveIn().toString());
//        System.out.println(entryNode.getLiveOut().toString());
        
    }

    private static void findAllNodes(BasicBlock entryNode, Set<BasicBlock> allNodes, Set<BasicBlock> leafNodes) {
        if (allNodes.contains(entryNode)) {
            return;
        }
        if (entryNode.getSuccessors().size() == 0) {
            leafNodes.add(entryNode);
        }
        allNodes.add(entryNode);
        for (BasicBlock succ : entryNode.getSuccessors()) {
            findAllNodes(succ, allNodes, leafNodes);
        }
    }
}