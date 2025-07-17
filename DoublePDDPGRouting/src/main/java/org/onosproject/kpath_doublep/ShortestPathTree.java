/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.kpath_doublep;

import java.util.HashMap;

/**
 *
 * @author marwa
 */
public class ShortestPathTree {
    private HashMap<String,DijkstraNode> nodes;
    private final String root;

    public ShortestPathTree() {
        this.nodes = new HashMap<String, DijkstraNode>();
        this.root = "";
    }

    public ShortestPathTree(String root) {
        this.nodes = new HashMap<String, DijkstraNode>();
        this.root = root;
    }

    public HashMap<String, DijkstraNode> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, DijkstraNode> nodes) {
        this.nodes = nodes;
    }

    public String getRoot() {
        return root;
    }

    public void add(DijkstraNode newNode) {
        nodes.put(newNode.getLabel(),newNode);
    }

    public void setParentOf(String node, String parent) {
//        if (parent != null && !nodes.containsKey(parent)) {
//            System.out.println("Warning: parent node not present in tree.");
//        }
        if (!nodes.containsKey(node))
            nodes.put(node,new DijkstraNode(node));

        nodes.get(node).setParent(parent);

    }

    public String getParentOf(String node) {
        if (nodes.containsKey(node))
            return nodes.get(node).getParent();
        else
            return null;
    }
}
