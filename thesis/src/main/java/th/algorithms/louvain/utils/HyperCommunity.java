package th.algorithms.louvain.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.graphstream.algorithm.community.Community;
import org.graphstream.graph.Node;

/**
 * The HyperCommunity adds additional functionality to the Community, to serve
 * the needs of the community detection algorithms.
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class HyperCommunity extends Community {
    
//    private int nodesCount;
    private int innerEdgesCount;
    private double innerEdgesWeightCount;
    //private Map<String,Integer> outerEdgesCount;
    private WeightMap edgeWeightToCommunity;
    private double totalEdgesWeight;
    private Set<Integer> communityNodes;
    private String cID;
    
    /**
     * Initializing global variables.
     */
    public HyperCommunity() {
        super();
//        this.nodesCount = 0;
        this.innerEdgesWeightCount = 0.0;
        this.edgeWeightToCommunity = new WeightMap(10);
        this.communityNodes = new HashSet<Integer>();
        this.cID = String.valueOf(Integer.parseInt(this.getId()) + 1); //to avoid having community id=0
        this.totalEdgesWeight = 0.0;
    }
    
    public void addNode(Integer nodeIndex) {
        this.communityNodes.add(nodeIndex);
    }
    
    public void removeNode(Integer nodeIndex) {
        this.communityNodes.remove(nodeIndex);
    }
    
    /**
     * Appends a set of node ids which represents the contents of the community.
     * @param newNodesSet the set to append.
     */
    public void addNodesSet(HashSet<Integer> newNodesSet) {
        this.communityNodes.addAll(newNodesSet);
    }
    
    /**
     * @return the set of nodes included in the community.
     */
    public Set<Integer> getCommunityNodes() {
        return this.communityNodes;
    }
    
    /**
     * Increase the node count by 1.
     */
//    public void increaseNodesCount() {
//        this.nodesCount++;
//    }
    
    /**
     * Decrease the node count by 1.
     */
//    public void descreaseNodeCount() {
//        this.nodesCount--;
//    }
    
    /**
     * @return the number of the nodes that the community has.
     */
    public int getNodesCount() {
        return this.communityNodes.size();
    }
    
    /**
     * The attribute id of the community is the community's id, plus 1, to avoid
     * having attributes starting from 0.
     * @return the community's attribute id.
     */
    public String getCID() {
        return this.cID;
    }

    /**
     * @return the edgeWeightToCommunity map
     */
    public WeightMap getEdgeWeightToCommunityMap() {
        return this.edgeWeightToCommunity;
    }
    
    /**
     * @param communityId
     * @return the edgeWeightToCommunity of the given community
     */
    public Double getEdgeWeightToCommunity(String communityId) {
        return this.edgeWeightToCommunity.getWeight(communityId);
    }
    
    /**
     * @param communityId the community that the outer edge is connected to.
     * Increase the edgeWeightToCommunity to the given community by 1.
     */
    public void increaseEdgeWeightToCommunity(String communityId) {
        this.increaseEdgeWeightToCommunity(communityId,1.0);
    }
    
    /**
     * Increase the edgeWeightToCommunity by the number of the input.
     * @param communityId the community that the outer edge is connected to.
     * @param number the number to increase the edgeWeightToCommunity
     */
    public void increaseEdgeWeightToCommunity(String communityId, Double number) {
            this.edgeWeightToCommunity.increase(communityId, number);
            this.totalEdgesWeight += number;
    }
    
    /**
     * @param communityId the community that the outer edge is connected to.
     * Decrease the edgeWeightToCommunity to the given community by 1.
     */
    public void decreaseEdgeWeightToCommunity(String communityId) {
        this.decreaseEdgeWeightToCommunity(communityId, 1.0);
    }
    
    /**
     * Decrease the edgeWeightToCommunity by the number of the input.
     * @param communityId the community that the outer edge is connected to.
     * @param number the number to decrease the edgeWeightToCommunity.
     */
    public void decreaseEdgeWeightToCommunity(String communityId, Double number) {
        this.edgeWeightToCommunity.decrease(communityId, number);
        this.totalEdgesWeight -= number;
        if(this.getEdgeWeightToCommunity(communityId) == 0.0){
            this.edgeWeightToCommunity.remove(communityId);
        }
//        this.edgeWeightToCommunity.put(communityId, this.edgeWeightToCommunity.get(communityId) - number);
    }

    /**
     * @return the number of inner edges that the community has.
     */
    public int getInnerEdgesCount() {
        return innerEdgesCount;
    }

    /**
     * Increase the inner edges counter by 1.
     */
    public void increaseInnerEdgesCount() {
        this.increaseInnerEdgesCount(1);
    }
    
    /**
     * Increase the inner edges counter by the number of the input.
     * @param number the number to increase the inner edges counter.
     */
    public void increaseInnerEdgesCount(int number) {
        this.innerEdgesCount += number;
    }
    
    /**
     * Decrease the inner edges counter by 1.
     */
    public void decreaseInnerEdgesCount() {
        this.decreaseInnerEdgesCount(1);
    }
    
    /**
     * Decrease the inner edges counter by the number of the input.
     * @param number the number to decrease the inner edges counter.
     */
    public void decreaseInnerEdgesCount(int number) {
        this.innerEdgesCount -= number;
    }
    
    /**
     * @return the innerEdgesWeightCount
     */
    public double getInnerEdgesWeightCount() {
        return this.innerEdgesWeightCount;
    }

    /**
     * Increase the innerEdgesWeightCount by 1.
     */
    public void increaseInnerEdgesWeightCount() {
        this.increaseInnerEdgesWeightCount(1);
    }
    
    /**
     * Increase the innerEdgesWeightCount by the number of the input.
     * @param number the number to increase the innerEdgesWeightCount
     */
    public void increaseInnerEdgesWeightCount(double number) {
        this.innerEdgesWeightCount += number;
    }
    
    /**
     * Decrease the innerEdgesWeightCount by 1.
     */
    public void decreaseInnerEdgesWeightCount() {
        this.decreaseInnerEdgesWeightCount(1);
    }
    
    /**
     * Decrease the innerEdgesWeightCount by the number of the input.
     * @param number the number to decrease the innerEdgesWeightCount
     */
    public void decreaseInnerEdgesWeightCount(double number) {
        this.innerEdgesWeightCount -= number;
    }
    
    
    public double getTotalEdgesWeight() {
        return this.totalEdgesWeight;
    }
    
    public void clearCommunityNodes() {
        this.communityNodes.clear();
    }
    
    /**
     * Divide the inner edges weight counter by 2 (in case they were calculated twice).
     */
//    public void finilizeInnerEdgesWeightCount() {
//        this.innerEdgesWeightCount/=2;
//    }
    
//    public Double getAllEdgesWeightCount() {
//        Double total = 0.0;
//        //Map.Entry<String, Double> edgeWeightToCommunity;
//        for(Entry<String, Double> edgeWeightToCommunity : this.edgeWeightToCommunity.entrySet()) {
//            total += edgeWeightToCommunity.getValue();
//        }
//        return total;
//    }
    
}
