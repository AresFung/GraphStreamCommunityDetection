package th.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import th.algorithms.propinquitydynamics.utils.MutableInt;
import th.algorithms.propinquitydynamics.utils.PropinquityMap;
import static th.utils.Metrics.GetModularity;
import static th.utils.Metrics.GetNMI;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Statistics {

    public static class RangeABStatistics {

        private String toCSV = "";
        private String filePrefix = "";
        private PrintWriter writer = null;
        private final boolean overlapCommunities;
        private Queue<String> q;
        private String filename;

        private void initOverlap(String filename) {
            this.filePrefix = filename;

            this.q = new LinkedList<>(Arrays.asList("BFS", "MaxToMinNormal", "MaxToMinPdegree", "MaxToMinPSumP"));
        }

        private void initCommunity(String filename) throws FileNotFoundException, UnsupportedEncodingException {
            this.writer = new PrintWriter(filename, "UTF-8");

            this.writer.println("a,b,UncommunitizedVertices,NumberofIterations,"
                    + "BFScom,Overlap,SharkOverlap,TotalOverlap,NMI,Modularity,"
                    + "MTMNormalWeihtsCom,Overlap,SharkOverlap,TotalOverlap,NMI,Modularity,"
                    + "MTMP/degreeCom,Overlap,SharkOverlap,TotalOverlap,NMI,Modularity,"
                    + "MTMP/SumPCom,Overlap,SharkOverlap,TotalOverlap,NMI,Modularity");
        }

        private void appendOverlap(String str) {
            this.filename = this.filePrefix + "." + str.replaceAll(",", "\\.");
        }

        private void appendOverlap(Graph graph, int[] com_overlap, int sharkOverlaps) {
            String algorithm = this.q.poll();
            this.q.add(algorithm);

            try {
                FileUtils.DumpCommunities(graph, this.filename + com_overlap[0] + "." + com_overlap[1] + "."
                        + sharkOverlaps + "." + algorithm + ".txt", "community");
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void appendCommunity(String str) {
            this.toCSV += str;
        }

        private void appendCommunity(Graph graph, int[] com_overlap, int sharkOverlaps) {
            double nmi = GetNMI(graph);
            double modularity = GetModularity(graph);

            this.toCSV += com_overlap[0] + "," + com_overlap[1] + ","
                    + sharkOverlaps + "," + (sharkOverlaps + com_overlap[1])
                    + "," + nmi + "," + modularity + ",";
        }

        private void finishEntryCommunity() {
            this.writer.println(this.toCSV);
            this.toCSV = "";
        }

        private void finishCommunity() {
            this.writer.close();
        }

        /* 
         * Constructor
         */
        public RangeABStatistics(boolean overlapCommunities) {
            this.overlapCommunities = overlapCommunities;
        }

        public void init(String filename) throws FileNotFoundException, UnsupportedEncodingException {
            if (!this.overlapCommunities) {
                initCommunity(filename);
                return;
            }
            initOverlap(filename);
        }

        public void append(String str) {
            if (!this.overlapCommunities) {
                appendCommunity(str);
                return;
            }
            appendOverlap(str);
        }

        public void append(Graph graph, int[] com_overlap, int sharkOverlaps) {
            if (!this.overlapCommunities) {
                appendCommunity(graph, com_overlap, sharkOverlaps);
                return;
            }
            appendOverlap(graph, com_overlap, sharkOverlaps);
        }

        public void finishEntry() {
            if (!this.overlapCommunities) {
                finishEntryCommunity();
            }
        }

        public void finish() {
            if (!this.overlapCommunities) {
                finishCommunity();
            }
        }
    }

    public static int MaxPropinquityToGraph(String file) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read(file);
        return MaxPropinquityToGraph(graph);
    }

    public static int MaxPropinquityToGraph(Graph graph) {
        int maxProp = 0;

        try {
            for (Node n : graph) {
                PropinquityMap pm = (PropinquityMap) n.getAttribute("pm");
                for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                    Integer prop = row.getValue().get();
                    if (prop > maxProp) {
                        maxProp = prop;
                    }
                }
            }
        } catch (NullPointerException e) {
            if (graph.getNode(0).getAttribute("pm") == null) {
                PropinquityDynamics pd = new PropinquityDynamics();
                pd.init(graph);

                return MaxPropinquityToGraph(graph);
            } else {
                throw e;
            }
        }

        return maxProp;
    }

    public static double[] DegreeStatistics(Graph graph) {
        double maxDegree = 0.0, avgDegree = 0.0;
        for (Node n : graph) {
            if (n.getDegree() > maxDegree) {
                maxDegree = n.getDegree();
            }
            avgDegree += n.getDegree();
        }

        double[] output = {maxDegree, (avgDegree / graph.getNodeCount())};
        return output;
    }

    public static void PDStatistics(Graph graph, String graphName, int a, int b) throws FileNotFoundException, UnsupportedEncodingException {
        Map<Integer, Integer> totalPDstats = new TreeMap<>();
        Map<Integer, Integer> edgeWeights = new TreeMap<>();

        int maxDegree = -1, minDegree = Integer.MAX_VALUE, asum = 0, bsum = 0, nutralsum = 0,
                oneEdgeVertices = 0, largestNdList = 0, largestNiList = 0;

        for (Node n : graph) {
            // count graph degree
            int degree = n.getDegree();
            if (degree > maxDegree) {
                maxDegree = degree;
            } else if (degree < minDegree) {
                minDegree = degree;
            }

            if (degree == 1) {
                oneEdgeVertices++;
            }

            // total PD distribution count
            PropinquityMap pm = n.getAttribute("pm");
            pm.values().stream().forEach((i) -> {
                if (totalPDstats.containsKey(i.get())) {
                    totalPDstats.put(i.get(), totalPDstats.get(i.get()) + 1);
                } else {
                    totalPDstats.put(i.get(), 1);
                }
            });

            // count items that will be delete/stay/added
            Set<Integer> Nr = n.getAttribute("Nr");
            int NdListSize = 0, NiListSize = 0;
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer nodeIndex = row.getKey();
                Integer propinquity = row.getValue().get();

                if (propinquity <= a && Nr.contains(nodeIndex)) {
                    Nr.remove(nodeIndex);
                    asum++;
                    NdListSize++;
                } else if (propinquity >= b && !Nr.contains(nodeIndex)) {
                    bsum++;
                    NiListSize++;
                }
            }
            if (NdListSize > largestNdList) {
                largestNdList = NdListSize;
            }
            if (NiListSize > largestNiList) {
                largestNiList = NiListSize;
            }
            nutralsum += Nr.size();
        }

        for (Edge e : graph.getEachEdge()) {
            int prop = ((PropinquityMap) e.getNode0().getAttribute("pm")).getInt(e.getNode1().getIndex());
            if (edgeWeights.containsKey(prop)) {
                edgeWeights.put(prop, edgeWeights.get(prop) + 1);
            } else {
                edgeWeights.put(prop, 1);
            }
        }

        System.out.println("Propinquity dynamics statistics");
        System.out.println("==================");
        System.out.println("#vertices: " + graph.getNodeCount());
        System.out.println("#edges: " + graph.getEdgeCount());
        System.out.println("# of one edge vertices: " + oneEdgeVertices);
        System.out.println("Max degree: " + maxDegree);
        System.out.println("Min degree: " + minDegree);
        System.out.println("==================");
        System.out.println("After initialize with a=" + a + " and b=" + b);
        System.out.println("#items that will be delete: " + asum / 2);
        System.out.println("Largest Nd list: " + largestNdList);
        System.out.println("#items that will be stay as are: " + nutralsum / 2);
        System.out.println("#items that will be added: " + bsum / 2);
        System.out.println("Largest Ni list: " + largestNiList);
        System.out.println("==================");

        try (PrintWriter writer = new PrintWriter("../exports/" + graphName + "-edgesPD.csv", "UTF-8")) {
            writer.println("number of edges,propinquity value");
            edgeWeights.entrySet().stream().forEach((entry) -> {
                writer.println((entry.getValue()) + "," + entry.getKey());
            });
        }

        try (PrintWriter writer2 = new PrintWriter("../exports/" + graphName + "-PD.csv", "UTF-8")) {
            writer2.println("number of entries,propinquity value");
            totalPDstats.entrySet().stream().forEach((entry) -> {
                writer2.println((entry.getValue() / 2) + "," + entry.getKey());
            });
        }
    }

    /**
     * Find the maximum propinquity value of each node to any other.
     *
     * @param graph The input graph.
     * @param graphName The name of the graph to be used for the export file.
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     */
    public static void maxPDToAnyNode(Graph graph, String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        Map<Integer, Integer> maxPDPerNode = new TreeMap<>();
        for (Node n : graph) {
            Integer localMaxPD = 0;
            PropinquityMap pm = (PropinquityMap) n.getAttribute("pm");
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer pdValue = row.getValue().get();
                if (pdValue > localMaxPD) {
                    localMaxPD = pdValue;
                }
            }
            maxPDPerNode.put(n.getIndex(), localMaxPD);
        }
        try (PrintWriter writer = new PrintWriter("../exports/" + graphName + "-maxPDToAnyNode.csv", "UTF-8")) {
            writer.println("node index,max propinquity value");
            maxPDPerNode.entrySet().stream().forEach((entry) -> {
                writer.println(entry.getKey() + "," + (entry.getValue()));
            });
        }
    }

    /**
     * Find the maximum propinquity value of each node to any neighbor.
     *
     * @param graph The input graph.
     * @param graphName The name of the graph to be used for the export file.
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void maxPDToAnyNeighbor(Graph graph, String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        Map<Integer, Integer> maxPDPerNeighbor = new TreeMap<>();
        for (Node n : graph) {
            Integer localMaxPD = 0;
            PropinquityMap pm = (PropinquityMap) n.getAttribute("pm");
            Iterator<Node> neighborNodeIterator = n.getNeighborNodeIterator();
            while (neighborNodeIterator.hasNext()) {
                Node nn = neighborNodeIterator.next();
                if (pm.containsKey(nn.getIndex())) {
                    Integer pdValue = pm.get(nn.getIndex()).get();
                    if (pdValue > localMaxPD) {
                        localMaxPD = pdValue;
                    }
                }
            }
            maxPDPerNeighbor.put(n.getIndex(), localMaxPD);
        }
        try (PrintWriter writer = new PrintWriter("../exports/" + graphName + "-maxPDToNeighbor.csv", "UTF-8")) {
            writer.println("node index,max propinquity value");
            maxPDPerNeighbor.entrySet().stream().forEach((entry) -> {
                writer.println(entry.getKey() + "," + (entry.getValue()));
            });
        }
    }

    public static void exportNodePDStatistics(Graph graph, String filename) throws IOException, GraphParseException {

        String[] ids = {"6927"}; //erdos
//        String[] ids = {"39", "28", "30"}; //dolphins
//        String[] ids = {"17", "11", "3","14","9"}; //karate

        for (String id : ids) {

            Node n = graph.getNode(id);

            PropinquityMap pm = (PropinquityMap) n.getAttribute("pm");
//            System.out.println("prop map: " + pm);
//            System.out.println("neigbours: ");
            Iterator<Node> neighborNodeIterator = n.getNeighborNodeIterator();
            while (neighborNodeIterator.hasNext()) {
                System.out.print(neighborNodeIterator.next().getId() + ", ");
            }

            try (PrintWriter writer = new PrintWriter("../exports/" + filename + "_" + "node" + (id) + "-propinquityMap.csv", "UTF-8")) {
                writer.println("node index,propinquity to any,propinquity to neighbor");

                for (Map.Entry<Integer, MutableInt> entry : pm.entrySet()) {
//                Iterator<String> attributeKeyIterator = graph.getNode(entry.getKey()).getAttributeKeyIterator();
//                while (attributeKeyIterator.hasNext()) {
//                    System.out.println(attributeKeyIterator.next());
//                }
                    String name = graph.getNode(entry.getKey()).getAttribute("ui.label");
                    if (n.getEdgeBetween(entry.getKey()) == null) {
                        writer.println(name + "," + entry.getValue().get() + ",0");
                    } else {
                        writer.println(name + "," + entry.getValue().get() + "," + entry.getValue().get());
                    }
                }
            }

        }
    }
}
