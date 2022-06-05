// PROG2 VT2022, Inlämningsuppgift, del 1
// Grupp 333
// Sarah Abbas Jawad saab3689
// Özge Alkan ozal4139
// Ludvig Burud lubu5130

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Node extends Circle {

    private String name;
    private double x;
    private double y;

    public Node(String name, double x, double y) {
        super(x, y, 10);
        this.x = x;
        this.y = y;
        this.name = name;
        paintUnselected();
    }

    public void paintUnselected(){
        this.setFill(Color.BLUE);
    }

    public void paintSelected(){
        this.setFill(Color.RED);
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return name;
    }

    public String print() {
        return name + ";" + x + ";" + y + ";";
    }
}