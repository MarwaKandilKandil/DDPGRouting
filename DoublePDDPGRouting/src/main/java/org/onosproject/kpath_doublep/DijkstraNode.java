/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.kpath_doublep;

import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author marwa
 */
public class DijkstraNode extends Node implements Comparable<DijkstraNode> {
    private double dist = Double.MAX_VALUE;
    private int depth;

    public DijkstraNode(double dist) {
        super();
        this.dist = dist;
    }

    public DijkstraNode(String label) {
        super(label);
        this.dist = 0.0;
    }

    public DijkstraNode(String label, double dist) {
        super(label);
        this.dist = dist;
    }

    public DijkstraNode(String label, double dist, int depth, String parent) {
        super(label);
        this.dist = dist;
        this.depth = depth;
        super.addEdge(parent,0.0);
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setParent(String parent) {
        super.neighbors = new HashMap<String, Double>();
        super.neighbors.put(parent,0.0);
    }

    public String getParent() {
        Set<String> neighborLabels = super.neighbors.keySet();
        if (neighborLabels.size() > 1) {
            return null;
        }
        if (neighborLabels.size() < 1) {
            return null;
        }
        return super.neighbors.keySet().iterator().next();
    }

    public int compareTo(DijkstraNode comparedNode) {
        double distance1 = this.dist;
        double distance2 = comparedNode.getDist();
        if (distance1 == distance2)
            return 0;
        if (distance1 > distance2)
            return 1;
        return -1;
    }

    public boolean equals(DijkstraNode comparedNode) {
        return this.getLabel().equals(comparedNode.getLabel());
    }
}
