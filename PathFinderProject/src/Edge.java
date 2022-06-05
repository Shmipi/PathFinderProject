// PROG2 VT2022, Inlämningsuppgift, del 1
// Grupp 333
// Sarah Abbas Jawad saab3689
// Özge Alkan ozal4139
// Ludvig Burud lubu5130

import java.util.Objects;
import java.io.Serializable;

public class Edge<T> implements Serializable {
    private final T destination;
    private final String name;
    private int weight;

    public Edge(T destination, String name, int weight) {
        this.destination = Objects.requireNonNull(destination);
        this.name = Objects.requireNonNull(name);

        if(weight < 0){
            throw new IllegalArgumentException();
        }
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public T getDestination() {
        return destination;
    }

    public void setWeight(int newWeight) {
        if(newWeight < 0) {
            throw new IllegalArgumentException();
        } else {
            weight = newWeight;
        }
    }

    @Override
    public String toString() {
        return "to " + destination + " by " + name + " takes " + weight;
    }

    public String print() {
        return name + ";" + weight;
    }
}
