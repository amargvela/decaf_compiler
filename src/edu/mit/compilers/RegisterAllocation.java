package edu.mit.compilers;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

import edu.mit.compilers.utils.Address;
import edu.mit.compilers.utils.StateAccumulator;

public class RegisterAllocation {

    public static Map<String, Integer> colors;
    public static Map<String, Set<String>> graph;

    public static void allocate(StateAccumulator accumulator) {
        Map<String, BasicBlock> cfgs = accumulator.dataflowVisitor.getMap();
        Map<String, Integer> variableCount = accumulator.dataflowVisitor.getVariableAccessCount();

        for (String methodName : cfgs.keySet()) {
            colors = new HashMap<String, Integer>();        // Variable_ID -> Color_ID
            graph = new HashMap<String, Set<String>>();     // Variable_ID -> Set[Variable_ID]
            List<Set<String>> liveness = VariableLiveness.findConcurances(methodName);
            
            // Remove global variables
            for (Set<String> l : liveness) {
                for (String s : new HashSet<String>(l)) {
                    String name = s.substring(0, s.lastIndexOf('_'));
                    if (accumulator.globalSymbolTable.exists(name) && accumulator.globalSymbolTable.get(name).getVariableId().equals(s)) {
                        l.remove(s);
                    }
                }
            }
            graphColoring(liveness);
            // Color_ID -> Color_Count
            Map<Integer, Integer> colorCount = new HashMap<Integer, Integer>();
            for (Map.Entry<String, Integer> e : colors.entrySet()) {
                if (!colorCount.containsKey(e.getValue())) {
                    colorCount.put(e.getValue(), 0);
                }
                colorCount.put(e.getValue(), colorCount.get(e.getValue()) + variableCount.get(e.getKey()));
            }
            // Entry[Color_ID, Color_Count]
            List<Map.Entry<Integer, Integer>> sortedColorCount = new ArrayList<Map.Entry<Integer, Integer>>(colorCount.entrySet());
            Collections.sort(sortedColorCount, new Comparator<Map.Entry<Integer, Integer>>() {
                @Override
                public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            for(int i = 0; i < 6 && i < sortedColorCount.size(); i++) {
                int col = sortedColorCount.get(i).getKey();
                for (Map.Entry<String, Integer> e : colors.entrySet()) {
                    if (e.getValue() == col) {
                        accumulator.varToReg.put(e.getKey(), Address.getParam(i));
                    }
                }
            }
            printInterferenceGraph(methodName);
        }
    }

    public static void graphColoring(List<Set<String>> lives) {
        for(Set<String> a : lives) {
            for(String s1 : a) {
                if(!graph.containsKey(s1)) {
                    graph.put(s1, new HashSet<String>());
                }
                for (String s2 : a) {
                    if (!s1.equals(s2)) {
                        graph.get(s1).add(s2);
                    }
                }
                colors.put(s1, -1);
            }
        }
        for (Map.Entry<String,Integer> s : colors.entrySet()) {
            Set<Integer> usedColors = new HashSet<Integer>();
            for (String s1 : graph.get(s.getKey())) {
                usedColors.add(colors.get(s1));
            }
            for (int i=0;;i++) {
                if (!usedColors.contains(i)) {
                    s.setValue(i);
                    break;
                }
            }
        }
    }
    
    // Testing
    
    static void printInterferenceGraph(String methodName) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(methodName + "_color.dot");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Color white = new Color(255, 255, 255);
        Map<Integer, float[]> hsb = new HashMap<Integer, float[]>();
        writer.write("graph " + methodName + " {\n");
        for (Map.Entry<String, Integer> s : colors.entrySet()) {
            Color color = generateRandomColor(white);
            if (!hsb.containsKey(s.getValue())) {
                float c[] = new float[3];
                Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), c);
                hsb.put(s.getValue(), c);
            }
            float c[] = hsb.get(s.getValue());
            writer.write(String.format("    %s [style=\"filled\", color=\"%f %f %f\"];\n", s.getKey(), c[0], c[1], c[2]));
        }
        writer.write("\n");

        for (String s1 : graph.keySet()) {
            for (String s2 : graph.get(s1)) {
                if (s1.compareTo(s2) > 0) {
                    writer.write(String.format("    %s -- %s;\n", s1, s2));
                }
            }
        }

        writer.write("}\n");

        writer.close();
    }

    public static Color generateRandomColor(Color mix) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }

        Color color = new Color(red, green, blue);
        return color;
    }
}
