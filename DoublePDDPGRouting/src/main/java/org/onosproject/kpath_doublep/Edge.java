/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.onosproject.kpath_doublep;

/**
 *
 * @author marwa
 */
public class Edge implements Cloneable {
    private String fromNode;
    private String toNode;
    private double weight;

    public Edge() {
        this.fromNode = null;
        this.toNode = null;
        this.weight = Double.MAX_VALUE;
    }

    public Edge(String fromNode, String toNode, double weight) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.weight = weight;
    }

    public String getFromNode() {
        return fromNode;
    }

    public void setFromNode(String fromNode) {
        this.fromNode = fromNode;
    }

    public String getToNode() {
        return toNode;
    }

    public void setToNode(String toNode) {
        this.toNode = toNode;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Edge clone() {
        return new Edge(fromNode, toNode, weight);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(fromNode);
        sb.append(",");
        sb.append(toNode);
        sb.append("){");
        sb.append(weight);
        sb.append("}");

        return sb.toString();
    }

    public boolean equals(Edge edge2) {
        if (hasSameEndpoints(edge2) && weight == edge2.getWeight())
            return true;

        return false;
    }

    public boolean hasSameEndpoints(Edge edge2) {
        if (fromNode.equals(edge2.getFromNode()) && toNode.equals(edge2.getToNode()))
            return true;

        return false;
    }
}
