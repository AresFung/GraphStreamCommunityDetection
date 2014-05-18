/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pdgs.utils;

import java.util.Iterator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class ExtractCommunities {
    
    /**
     * Find disjoined communities by BFS. The first community has number 1.
     *
     * @param graph     The graph that we will extract the communities.
     * @param fixedIDs  If we want some vertices to be in a custom community,
     *                  then this array should contain they IDs.
     * @return          The number of communities detected.
     */
    public static int BFS(Graph graph, Integer[] fixedIDs) {
        int communityNum = 0;
        
        if (fixedIDs.length > 0) { communityNum++; };
        for (Integer id : fixedIDs) {
            graph.getNode(id).setAttribute("visited", 1);
            graph.getNode(id).setAttribute("community", communityNum);
        }
        
        for (Node n : graph.getEachNode()) {
            if (!n.hasAttribute("visited")) {
                n.setAttribute("visited", 1);
                n.setAttribute("community", communityNum++);
                
                // Go for BFS
                Iterator<Node> breadth = n.getBreadthFirstIterator();
                while (breadth.hasNext()) {
                    Node next = breadth.next();
                    next.setAttribute("visited", 1);
                    next.setAttribute("community", communityNum);
                }
            }
        }
        
        // Delete visited attribute
        for (Node n : graph.getEachNode()) {
            n.removeAttribute("visited");
        }
        
        return communityNum;
    }
}
