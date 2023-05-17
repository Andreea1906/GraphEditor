package com.example.tema2;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class GraphEditor extends Application {
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Edge> edges = new HashMap<>();
    private GraphPane graphPane;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        HBox toolbar = createToolbar();
        graphPane = new GraphPane();

        root.setTop(toolbar);
        root.setCenter(graphPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Graph Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox();
        TextField keyField = new TextField();
        TextField valueField = new TextField();
        Button addNodeButton = new Button("Add Node");
        Button addEdgeButton = new Button("Add Edge");
        Button deleteButton = new Button("Delete");

        addNodeButton.setOnAction(event -> {
            String key = keyField.getText();
            String value = valueField.getText();
            if (key.isEmpty() || value.isEmpty()) {
                showAlert("Error", "Key and value must not be empty.");
            } else if (nodes.containsKey(key)) {
                showAlert("Error", "Node with the same key already exists.");
            } else {
                Node node = new Node(key, value);
                nodes.put(key, node);
                graphPane.addNode(node);
                keyField.clear();
                valueField.clear();
            }
        });

        addEdgeButton.setOnAction(event -> {
            String key = keyField.getText();
            String value = valueField.getText();
            if (key.isEmpty() || value.isEmpty()) {
                showAlert("Error", "Key and value must not be empty.");
            } else if (!nodes.containsKey(key)) {
                showAlert("Error", "Node with the specified key does not exist.");
            } else if (edges.containsKey(key)) {
                showAlert("Error", "Edge with the same key already exists.");
            } else {
                Edge edge = new Edge(key, value);
                edges.put(key, edge);
                graphPane.addEdge(edge);
                keyField.clear();
                valueField.clear();
            }
        });

        deleteButton.setOnAction(event -> {
            graphPane.deleteSelected();
        });

        toolbar.getChildren().addAll(keyField, valueField, addNodeButton, addEdgeButton, deleteButton);
        toolbar.setAlignment(Pos.CENTER);
        toolbar.setSpacing(10);
        return toolbar;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private class Node extends Circle {
        private String key;
        private String value;

        private TextField keyField;
        private TextField valueField;

        public Node(String key, String value) {
            super(30);
            this.key = key;
            this.value = value;

            setFill(javafx.scene.paint.Color.RED);
            setStroke(javafx.scene.paint.Color.BLACK);
            setStrokeWidth(2);

            keyField = new TextField(key);
            valueField = new TextField(value);

            keyField.setMinWidth(50);
            valueField.setMinWidth(50);

            keyField.setOnAction(event -> updateKey());
            valueField.setOnAction(event -> updateValue());

            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 2) {
                        updateKey();
                        updateValue();
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    graphPane.setSelectedNode(this);
                }
            });
        }

        private void updateKey() {
            String newKey = keyField.getText();
            if (newKey.isEmpty()) {
                showAlert("Error", "Key must not be empty.");
            } else if (nodes.containsKey(newKey) && !newKey.equals(key)) {
                showAlert("Error", "Node with the same key already exists.");
            } else {
                nodes.remove(key);
                key = newKey;
                nodes.put(key, this);
            }
            keyField.setText(key);
        }

        private void updateValue() {
            String newValue = valueField.getText();
            if (newValue.isEmpty()) {
                showAlert("Error", "Value must not be empty.");
            } else {
                value = newValue;
            }
            valueField.setText(value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public TextField getKeyField() {
            return keyField;
        }

        public TextField getValueField() {
            return valueField;
        }
    }

    private class Edge extends Line {
        private String key;
        private String value;

        public Edge(String key, String value) {
            super();
            this.key = key;
            this.value = value;
            setStroke(javafx.scene.paint.Color.BLACK);
            setStrokeWidth(2);

            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    graphPane.setSelectedEdge(this);
                }
            });
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    private class GraphPane extends javafx.scene.layout.Pane {
        private Node selectedNode;
        private Edge selectedEdge;

        public void addNode(Node node) {
            getChildren().addAll(node, node.getKeyField(), node.getValueField());
            node.setCenterX(Math.random() * getWidth());
            node.setCenterY(Math.random() * getHeight());
            node.getKeyField().setLayoutX(node.getCenterX() - 0.5 * node.getKeyField().getWidth());
            node.getKeyField().setLayoutY(node.getCenterY() - 60);
            node.getValueField().setLayoutX(node.getCenterX() - 0.5 * node.getValueField().getWidth());
            node.getValueField().setLayoutY(node.getCenterY() - 30);
        }

        public void addEdge(Edge edge) {
            String startNodeKey = edge.getKey();
            if (!nodes.containsKey(startNodeKey)) {
                showAlert("Error", "Start node with the specified key does not exist.");
                return;
            }

            Node startNode = nodes.get(startNodeKey);
            startNode.toFront();

            setOnMouseClicked(event -> {
                if (event.getTarget() instanceof Node) {
                    Node endNode = (Node) event.getTarget();
                    String endNodeKey = endNode.getKey();
                    if (!nodes.containsKey(endNodeKey)) {
                        showAlert("Error", "End node with the specified key does not exist.");
                    } else {
                        if (!edges.containsKey(endNodeKey)) {
                            Edge newEdge = new Edge(edge.getKey(), edge.getValue());
                            newEdge.setStartX(startNode.getCenterX());
                            newEdge.setStartY(startNode.getCenterY());
                            newEdge.setEndX(endNode.getCenterX());
                            newEdge.setEndY(endNode.getCenterY());
                            edges.put(endNodeKey, newEdge);
                            getChildren().add(newEdge);
                        } else {
                            showAlert("Error", "An edge between the nodes already exists.");
                        }
                    }
                }
            });
        }

        public void deleteSelected() {
            if (selectedNode != null) {
                String key = selectedNode.getKey();
                nodes.remove(key);
                getChildren().removeAll(selectedNode, selectedNode.getKeyField(), selectedNode.getValueField());
                selectedNode = null;
            } else if (selectedEdge != null) {
                String key = selectedEdge.getKey();
                edges.remove(key);
                getChildren().remove(selectedEdge);
                selectedEdge = null;
            }
        }

        public void setSelectedNode(Node node) {
            if (selectedNode != null) {
                selectedNode.setStroke(javafx.scene.paint.Color.BLACK);
            }
            selectedNode = node;
            selectedNode.setStroke(javafx.scene.paint.Color.BLUE);
        }

        public void setSelectedEdge(Edge edge) {
            if (selectedEdge != null) {
                selectedEdge.setStroke(javafx.scene.paint.Color.BLACK);
            }
            selectedEdge = edge;
            selectedEdge.setStroke(javafx.scene.paint.Color.BLUE);
        }
    }
}




