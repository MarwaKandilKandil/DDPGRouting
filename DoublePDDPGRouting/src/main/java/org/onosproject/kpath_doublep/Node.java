/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.kpath_doublep;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author marwa
 */
public class Node {
       protected String label;
    protected HashMap<String,Double> neighbors; // adjacency list, with HashMap for each edge weight

    public Node() {
        neighbors = new HashMap();
    }

    public Node(String label) {
        this.label = label;
        neighbors = new HashMap();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public HashMap<String, Double> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(HashMap<String, Double> neighbors) {
        this.neighbors = neighbors;
    }

    public void addEdge(String toNodeLabel,Double weight) {
        neighbors.put(toNodeLabel, weight);
    }

    public double removeEdge(String toNodeLabel) {
        if (neighbors.containsKey(toNodeLabel)) {
            double weight = neighbors.get(toNodeLabel);
            neighbors.remove(toNodeLabel);
            return weight;
        }

        return Double.MAX_VALUE;
    }

    public Set<String> getAdjacencyList() {
        return neighbors.keySet();
    }

    public LinkedList<Edge> getEdges() {
        LinkedList<Edge> edges = new LinkedList<Edge>();
        for (String toNodeLabel : neighbors.keySet()) {
            edges.add(new Edge(label,toNodeLabel,neighbors.get(toNodeLabel)));
        }

        return edges;
    }
    
    public String toString() {
        StringBuilder nodeStringB = new StringBuilder();
        nodeStringB.append(label);
        nodeStringB.append(": {");
        Set<String> adjacencyList = this.getAdjacencyList();
        Iterator<String> alIt = adjacencyList.iterator();
        HashMap<String, Double> neighbors = this.getNeighbors();
        while (alIt.hasNext()) {
            String neighborLabel = alIt.next();
            nodeStringB.append(neighborLabel.toString());
            nodeStringB.append(": ");
            nodeStringB.append(neighbors.get(neighborLabel));
            if (alIt.hasNext())
                nodeStringB.append(", ");
        }
        nodeStringB.append("}");
        nodeStringB.append("\n");

        return nodeStringB.toString();
    }
}
