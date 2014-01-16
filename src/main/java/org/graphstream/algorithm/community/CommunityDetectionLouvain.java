/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain {

    private Graph graph, graphPhase2;
    private HyperCommunityManager manager;
    private Map<String, HyperCommunity> communities;
    private Modularity modularity;
    private double maxModularity,
            newModularity,
            initialModularity,
            deltaQ,
            globalMaxQ,
            globalNewQ;
    private String oldCommunity,
            bestCommunity;
    // Used for colors.
    private Random color;
    private int r, g, b;
    private Iterator<Node> neighbours;
    private int step;
    private String fileName;

    public void init(String fileName) throws IOException, GraphParseException {

        step = 1;
        communities = new HashMap<String, HyperCommunity>();
        manager = new HyperCommunityManager();
        
        globalMaxQ = -2.0;
        globalNewQ = 0.0;
        this.fileName = fileName;
    }
    
    public void execute() throws IOException, GraphParseException {
        
        if(step == 1) {
            step1Init();
        } else {
            findCommunities();
            foldingCommunities();
        }
        
        step++;
    }
    
    public boolean isModularityMaximized() {
        return globalMaxQ >= globalNewQ;
    }
    
    public Graph step1Init() throws IOException, GraphParseException {
        
        graph = new SingleGraph("communities");
        graph.display(true);  // Display the nodes in a nice aesthetic way.
        graph.read(fileName); // Import from the text file.
        
        // Add an initial weight of 1.0 in each edge
        for (Edge edge : graph.getEdgeSet()) {
            edge.addAttribute("weight", 1.0);
        }
        
        modularity = new Modularity("community", "weight");
        modularity.init(graph);
        
        return graph;
    }

    public void findCommunities() {
        
	for (Node node : graph) {
            node.addAttribute("ui.label", node.getId()); // Add a label in every node.

            // Every node belongs to a different community.
            HyperCommunity community = manager.communityFactory();

            // Add community attribute to each node, so modularity alg can identify which
            // nodes belong to each community.
            node.addAttribute("community", community.getAttribute());

            communities.put(community.getAttribute(), community);
        }

        do {
            initialModularity = modularity.getMeasure();
            for (Node node : graph) {

                maxModularity = -2.0;
                newModularity = -2.0;
                oldCommunity = node.getAttribute("community");
                bestCommunity = oldCommunity;

                neighbours = node.getNeighborNodeIterator();
                System.out.println("Node " + node.getId());
                while (neighbours.hasNext()) {

                    Node neighbour = neighbours.next();

                    // Put the node in the neighbour's community.
                    node.changeAttribute("community", neighbour.getAttribute("community"));

                    // Calculate new modularity
                    newModularity = modularity.getMeasure();
                    System.out.println("To " + neighbour.getId() + " (belongs to community " + neighbour.getAttribute("community") + ")" + ", mod: " + newModularity);

                    // Find the community that if the node is transfered to, the modularity gain
                    // is the maximum.
                    // In case of tie, the breaking rule is always to take the one that was checked last.
                    if (newModularity > maxModularity) {
                        maxModularity = newModularity;
                        bestCommunity = neighbour.getAttribute("community");
                    }
                }

                // Move node to the best community (if not already in)
                if (node.getAttribute("community") != bestCommunity) {
                    node.changeAttribute("community", bestCommunity);
                }
                // Commented for the moment. Maybe it will be used later.
                // Count the inner and outer edges that the node that changes community is connected to, 
                // simmultaniously with the community calculation.
//                    if(node.getAttribute("community") != oldCommunity) {
//                        neighbours = node.getNeighborNodeIterator();
//                        while(neighbours.hasNext()) {
//                            String neighbourCommunity = neighbours.next().getAttribute("community");
//                            if(neighbourCommunity.equals(oldCommunity)) {
//                                innerEdgesToChange++;
//                            } else {
//                                if(outerEdgesToChange.containsKey(neighbourCommunity)) {
//                                    outerEdgesToChange.put(neighbourCommunity, outerEdgesToChange.get(neighbourCommunity) + 1);
//                                } else {
//                                    outerEdgesToChange.put(neighbourCommunity, 1);
//                                }
//                            }
//                        }
//                    }
//                    
//                    System.out.println("Inner Edges connected: " + innerEdgesToChange);
//                    System.out.println("Outer Edges connected: " + outerEdgesToChange);
//                    
//                    innerEdgesToChange = 0;
//                    outerEdgesToChange.clear();

                System.out.println("best community to go: " + node.getAttribute("community"));
                System.out.println("");
            }
            deltaQ = modularity.getMeasure() - initialModularity;
        } while (deltaQ > 0); // Loop until there is no improvement in modularity

        //globalNewQ = modularity.getMeasure();
        
        // Group nodes by community and count the edge types
        // TO-DO: it counts the inner edges double. Implement the method in HyperCommunityManager
        ListMultimap<String, Node> multimap = ArrayListMultimap.create();
        HyperCommunity community;
        for (Node node : graph) {
            multimap.put((String) node.getAttribute("community"), node);
            community = communities.get(node.getAttribute("community"));
            community.increaseNodesCount();
            neighbours = node.getNeighborNodeIterator();
            System.out.println("Node: " + node.getId() + " / neighbours communities: ");
            while (neighbours.hasNext()) {
                String neighbourCommunity = neighbours.next().getAttribute("community");
                System.out.print(neighbourCommunity + ", ");
                if (neighbourCommunity.equals(node.getAttribute("community"))) {
                    community.increaseInnerEdgesCount();
                } else {
                    community.increaseOuterEdgesCount(neighbourCommunity);
                }
            }
            communities.put(community.getAttribute(), community);
            System.out.println("");
        }


        // Remove from the map the communities with 0 nodes.
        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            Entry<String, HyperCommunity> entry = it.next();
            if (communities.get(entry.getKey()).getNodesCount() == 0) {
                it.remove();
            }
        }

        // Print the remaining communities
        System.out.println("Communities for next phase: ");
        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            Entry<String, HyperCommunity> entry = it.next();
            System.out.println(communities.get(entry.getKey()).getAttribute()
                    + " outers: " + communities.get(entry.getKey()).getOuterEdgesCount());
        }

        // Color the nodes of one community with the same (random) color.
        color = new Random();
        for (String communityId : multimap.keySet()) {
            r = color.nextInt(255);
            g = color.nextInt(255);
            b = color.nextInt(255);

            List<Node> communityNodes = multimap.get(communityId);
            Iterator<Node> iterator = communityNodes.iterator();
            while (iterator.hasNext()) {
                iterator.next().addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
            }
        }

    }

    public void foldingCommunities() {

        graphPhase2 = new SingleGraph("communitiesPhase2");
        graphPhase2.display(true);
        graphPhase2.setAutoCreate(true); // configuration to create nodes automatically
        // when edges are created.

        String edgeIdentifierWayOne,
                edgeIdentifierWayTwo;
        Entry<String, HyperCommunity> communityEntry;
        Entry<String, Integer> outerEdgeEntry;
        List<String> edgeIdentifiers = new ArrayList<String>();
        Edge edge;

        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            communityEntry = it.next();
            Map<String, Integer> outerEdges = communityEntry.getValue().getOuterEdgesCount();
            for (Iterator<Entry<String, Integer>> outerEdgesIt = outerEdges.entrySet().iterator(); outerEdgesIt.hasNext();) {
                outerEdgeEntry = outerEdgesIt.next();
                edgeIdentifierWayOne = communityEntry.getKey() + ":" + outerEdgeEntry.getKey();
                edgeIdentifierWayTwo = outerEdgeEntry.getKey() + ":" + communityEntry.getKey();
                if (!edgeIdentifiers.contains(edgeIdentifierWayOne) && !edgeIdentifiers.contains(edgeIdentifierWayTwo)) {
                    if (graphPhase2.getNode(communityEntry.getKey()) == null) {
                        graphPhase2.addNode(communityEntry.getKey()).addAttribute("ui.label", communityEntry.getKey());
                    }
                    if (graphPhase2.getNode(outerEdgeEntry.getKey()) == null) {
                        graphPhase2.addNode(outerEdgeEntry.getKey()).addAttribute("ui.label", outerEdgeEntry.getKey());
                    }
                    edge = graphPhase2.addEdge(edgeIdentifierWayOne,
                            communityEntry.getKey(),
                            outerEdgeEntry.getKey());
                    edge.addAttribute("weight", Double.parseDouble(String.valueOf(outerEdgeEntry.getValue())));
                    edge.addAttribute("ui.label", outerEdgeEntry.getValue());

                    edgeIdentifiers.add(edgeIdentifierWayOne);
                    edgeIdentifiers.add(edgeIdentifierWayTwo);
                }
            }
        }
    }
}
