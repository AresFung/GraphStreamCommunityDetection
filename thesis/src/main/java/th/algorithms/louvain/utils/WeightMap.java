package th.algorithms.louvain.utils;

import java.util.HashMap;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class WeightMap extends HashMap<String, Double> {

    public WeightMap(int cap) {
        super(cap);
    }
    
    public void increase(String communityId, Double weight) {
        Double currentWeight = this.get(communityId);
        if (currentWeight == null) {
            this.put(communityId, weight);
        } else {
            currentWeight += weight;
        }
    }
    
    public void decrease(String communityId, Double weight) {
        Double currentWeight = this.get(communityId);
        if (currentWeight != null) {
            currentWeight -= weight;
        }
    }
    
    public Double getWeight(String communityId) {
        return this.get(communityId);
    }
}