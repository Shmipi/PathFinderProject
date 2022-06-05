// PROG2 VT2022, Inlämningsuppgift, del 1
// Grupp 333
// Sarah Abbas Jawad saab3689
// Özge Alkan ozal4139
// Ludvig Burud lubu5130

import java.util.*;
import java.util.HashSet;
import java.io.Serializable;

public class ListGraph<T> implements Graph<T>, Serializable {

    private final Map<T, Set<Edge<T>>> hashMapNodes = new HashMap<>();

    public void add(T newNode) {
        hashMapNodes.putIfAbsent(newNode, new HashSet<>());
    }

    public void remove(T nodeToRemove) {
        if (hashMapNodes.containsKey(nodeToRemove)) {

            Set<Edge<T>> copyOfEdges = new HashSet<>(getEdgesFrom(nodeToRemove));

            for (Edge<T> edge : copyOfEdges) {

                disconnect(nodeToRemove, edge.getDestination());
            }
            hashMapNodes.remove(nodeToRemove);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void connect(T a, T b, String name, int weight) {

        if (hashMapNodes.containsKey(a) && hashMapNodes.containsKey(b)) {
            if (weight < 0) {
                throw new IllegalArgumentException();
            } else if(getEdgeBetween(a, b) != null || getEdgeBetween(b, a) != null) {
                throw new IllegalStateException();
            } else {
                hashMapNodes.get(a).add(new Edge<T>(b, name, weight));
                hashMapNodes.get(b).add(new Edge<T>(a, name, weight));
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    public void disconnect (T a, T b) {

        if(hashMapNodes.containsKey(a) && hashMapNodes.containsKey(b)) {

            if(getEdgeBetween(a, b) == null || getEdgeBetween(b, a) == null) {
                throw new IllegalStateException();
            } else {
                hashMapNodes.get(a).remove(getEdgeBetween(a, b));
                hashMapNodes.get(b).remove(getEdgeBetween(b, a));
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    public void setConnectionWeight(T a, T b, int weight) {
        if (hashMapNodes.containsKey(a) && hashMapNodes.containsKey(b)){
            if (getEdgeBetween(a, b) == null || getEdgeBetween(b, a) == null) {
                throw new NoSuchElementException();
            }
            getEdgeBetween(a, b).setWeight(weight);
            getEdgeBetween(b, a).setWeight(weight);
        } else {
            throw new NoSuchElementException();
        }
    }

    public Set<T> getNodes() {
        return Set.copyOf(hashMapNodes.keySet());
    }

    public Set<Edge<T>> getEdgesFrom(T a) {
        if(hashMapNodes.containsKey(a)) {
            return hashMapNodes.get(a);
        }
        throw new NoSuchElementException();
    }

    public Edge<T> getEdgeBetween (T a, T b) {

        if (!hashMapNodes.containsKey(a) || !hashMapNodes.containsKey(b)) {
            throw new NoSuchElementException();
        }
        for (Edge<T> edge : hashMapNodes.get(a)) {
            if(edge.getDestination().equals(b)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T node : hashMapNodes.keySet()) {
            sb.append(node).append(": ").append(hashMapNodes.get(node)).append("\n");
        }
        return sb.toString();
    }

    public boolean pathExists(T a, T b) {

        if (hashMapNodes.containsKey(a) && hashMapNodes.containsKey(b)) {

            Set<T> visited = new HashSet<>();

            depthFirstSearch(a, visited);

            if(visited.contains(b)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private void depthFirstSearch(T a, Set<T> visited) {

        visited.add(a);

        for(Edge<T> edge : hashMapNodes.get(a)) {

            if(!visited.contains(edge.getDestination())) {

                depthFirstSearch(edge.getDestination(), visited);
            }
        }
    }

    public List<Edge<T>> getPath(T a, T b) {

        Map<T, T> connection = new HashMap<>();

        depthFirstConnection(a, null, connection);

        if(!connection.containsKey(b)) {
            return null;
        }
        return gatherPath(a, b, connection);
    }

    private List<Edge<T>> gatherPath(T a, T b, Map<T, T> connection) {

        LinkedList<Edge<T>> pathBetween = new LinkedList<>();

        T currentNode = b;

        while (!currentNode.equals(a)) {
            T next = connection.get(currentNode);
            Edge<T> edgeBack = getEdgeBetween(next, currentNode);
            pathBetween.addFirst(edgeBack);
            currentNode = next;
        }
        return Collections.unmodifiableList(pathBetween);
    }

    private void depthFirstConnection(T b, T a, Map<T, T> connection) {
        connection.put(b, a);

        for(Edge<T> edge : hashMapNodes.get(b)) {
            if(!connection.containsKey(edge.getDestination())){
                depthFirstConnection(edge.getDestination(), b, connection);
            }
        }
    }
}
