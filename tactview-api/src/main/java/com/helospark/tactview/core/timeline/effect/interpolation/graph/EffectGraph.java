package com.helospark.tactview.core.timeline.effect.interpolation.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.helospark.tactview.core.decoder.framecache.GlobalMemoryManagerAccessor;
import com.helospark.tactview.core.timeline.StatelessVideoEffect;
import com.helospark.tactview.core.timeline.VisualTimelineClip;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.EffectGraphInputRequest;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphConnectionDescriptor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphNodeOutputMarker;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.InputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.StatelessEffectElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.VisualTimelineClipElement;
import com.helospark.tactview.core.timeline.image.ClipImage;
import com.helospark.tactview.core.timeline.image.ReadOnlyClipImage;

public class EffectGraph {
    private Map<GraphIndex, GraphElement> graphElements = new LinkedHashMap<>();
    private Map<ConnectionIndex, List<ConnectionIndex>> connections = new LinkedHashMap<>();

    public EffectGraph() {
    }

    public Map<GraphIndex, GraphElement> getGraphElements() {
        return graphElements;
    }

    public Map<ConnectionIndex, List<ConnectionIndex>> getConnections() {
        return connections;
    }

    public ReadOnlyClipImage evaluate(EffectGraphInputRequest request) {
        List<GraphIndex> outputNodes = graphElements.entrySet().stream().filter(a -> a.getValue() instanceof GraphNodeOutputMarker).map(a -> a.getKey()).collect(Collectors.toList());
        List<GraphIndex> inputNode = graphElements.entrySet().stream().filter(a -> a.getValue() instanceof InputElement).map(a -> a.getKey()).collect(Collectors.toList());

        if (outputNodes.isEmpty()) {
            return ClipImage.fromSize(request.expectedWidth, request.expectedHeight);
        }

        List<List<GraphIndex>> graphLayers = precomputeGraph(outputNodes, inputNode);

        Map<ConnectionIndex, ReadOnlyClipImage> images = new HashMap<>();
        for (int i = 0; i < graphLayers.size(); ++i) {
            List<GraphIndex> layer = graphLayers.get(i);

            for (int j = 0; j < layer.size(); ++j) {
                GraphIndex element = layer.get(j);

                Map<ConnectionIndex, ReadOnlyClipImage> connectionPointToImage = new LinkedHashMap<>();

                GraphElement graphElement = graphElements.get(element);
                for (var input : graphElement.inputs.keySet()) {
                    Optional<ConnectionIndex> output = findOutputForInput(input);
                    if (output.isPresent()) {
                        ReadOnlyClipImage image = images.get(output.get());
                        if (image != null) {
                            connectionPointToImage.put(input, image);
                        }
                    }
                }

                Map<ConnectionIndex, ReadOnlyClipImage> nodeResultImages = graphElement.render(connectionPointToImage, request);

                images.putAll(nodeResultImages);
            }
        }

        Optional<OutputElement> outputElement = outputNodes.stream().map(a -> (OutputElement) graphElements.get(a)).filter(a -> a instanceof OutputElement).findFirst();
        ReadOnlyClipImage resultImage = null;
        if (outputElement.isPresent()) {
            Optional<ReadOnlyClipImage> output = findOutputForInput(outputElement.get().getInputIndex())
                    .map(index -> images.get(index))
                    .map(img -> ClipImage.copyOf(img));
            resultImage = output.orElse(null);
        }

        images.values()
                .stream()
                .forEach(a -> GlobalMemoryManagerAccessor.memoryManager.returnBuffer(a.getBuffer()));

        if (resultImage == null) {
            resultImage = ClipImage.fromSize(request.expectedWidth, request.expectedHeight);
        }

        return resultImage;
    }

    private List<List<GraphIndex>> precomputeGraph(List<GraphIndex> outputNodes, List<GraphIndex> inputNode) {
        List<List<GraphIndex>> resultGraphLayers = new ArrayList<>();

        for (var outputNode : outputNodes) {
            Set<GraphIndex> nodesNeededForRender = findAllConnectedNodesStartingFrom(outputNode);

            if (!inputNode.isEmpty()) {
                resultGraphLayers.add(inputNode);
            }
            while (true) {
                List<GraphIndex> layer = findNodesWithAllInputsSatisfied(resultGraphLayers, nodesNeededForRender);

                if (layer.isEmpty()) {
                    break;
                }

                resultGraphLayers.add(layer);
            }
        }
        return resultGraphLayers;
    }

    private Set<GraphIndex> findAllConnectedNodesStartingFrom(GraphIndex fromNode) {
        Set<GraphIndex> nodesNeededForRender = new HashSet<>();

        Set<GraphIndex> newElementsAdded = new HashSet<>();
        newElementsAdded.add(fromNode);
        while (!newElementsAdded.isEmpty()) {
            Set<GraphIndex> newElementsAdded2 = new HashSet<>();
            for (GraphIndex element : newElementsAdded) {

                Set<GraphIndex> nodesRequiredForInput = graphElements.get(element).inputs
                        .keySet()
                        .stream()
                        .flatMap(edgeConnection -> findNodeWithInput(edgeConnection).stream())
                        .collect(Collectors.toSet());

                if (nodesNeededForRender.contains(element)) {
                    throw new RuntimeException("Loop detected");
                }

                nodesNeededForRender.add(element);
                newElementsAdded2.addAll(nodesRequiredForInput);
            }
            newElementsAdded.clear();
            newElementsAdded.addAll(newElementsAdded2);
        }
        return nodesNeededForRender;
    }

    private List<GraphIndex> findNodesWithAllInputsSatisfied(List<List<GraphIndex>> layers, Set<GraphIndex> nodesNeededForRender) {
        List<GraphIndex> result = new ArrayList<>();
        for (GraphIndex element : nodesNeededForRender) {
            if (anyLayerAlreadyContains(layers, element)) {
                continue;
            }

            GraphElement graphElement = graphElements.get(element);

            Map<ConnectionIndex, GraphConnectionDescriptor> inputs = graphElement.inputs;

            List<GraphIndex> connectedInputNodes = inputs.keySet()
                    .stream()
                    .flatMap(a -> findNodeWithInput(a).stream())
                    .collect(Collectors.toList());

            boolean alreadyContains = connectedInputNodes.stream().allMatch(a -> anyLayerAlreadyContains(layers, a));

            if (alreadyContains) {
                result.add(element);
            }

        }

        return result;
    }

    private Optional<GraphIndex> findNodeWithInput(ConnectionIndex input) {
        return connections.entrySet()
                .stream()
                .filter(a -> a.getValue().contains(input))
                .flatMap(a -> findGrapIndexForInput(a.getKey()).stream())
                .findFirst();
    }

    private Optional<ConnectionIndex> findOutputForInput(ConnectionIndex input) {
        return connections.entrySet()
                .stream()
                .filter(a -> a.getValue().contains(input))
                .map(a -> a.getKey())
                .findFirst();
    }

    private Optional<GraphIndex> findGrapIndexForInput(ConnectionIndex key) {
        for (var element : graphElements.entrySet()) {
            if (element.getValue().outputs.containsKey(key)) {
                return Optional.of(element.getKey());
            }
        }
        return Optional.empty();
    }

    private boolean anyLayerAlreadyContains(List<List<GraphIndex>> layers, GraphIndex toFind) {
        return layers.stream()
                .flatMap(a -> a.stream())
                .filter(a -> a.equals(toFind))
                .findFirst()
                .isPresent();
    }

    public EffectGraph deepClone() {
        EffectGraph result = new EffectGraph();
        result.connections = this.connections; //.entrySet().stream().collect(Collectors.toMap(a -> a.getKey(), a -> new ArrayList<>(a.getValue()))); // TODO
        result.graphElements = this.graphElements.entrySet().stream()
                .collect(Collectors.toMap(a -> a.getKey(), a -> a.getValue().deepClone()));

        return result;
    }

    public void autoArrangeUi() {
        List<GraphIndex> optionalOutputNode = graphElements.entrySet().stream().filter(a -> a.getValue() instanceof GraphNodeOutputMarker).map(a -> a.getKey()).collect(Collectors.toList());
        List<GraphIndex> inputNode = graphElements.entrySet().stream().filter(a -> a.getValue() instanceof InputElement).map(a -> a.getKey()).collect(Collectors.toList());

        if (optionalOutputNode.isEmpty()) {
            return;
        }

        List<List<GraphIndex>> graphLayers = precomputeGraph(optionalOutputNode, inputNode);

        double startX = -graphLayers.size() / 2.0 * 200.0;
        for (int i = 0; i < graphLayers.size(); ++i) {
            double x = startX + i * 200.0;

            double startY = -graphLayers.get(i).size() / 2.0 * 50.0;
            for (int j = 0; j < graphLayers.get(i).size(); ++j) {
                double y = startY + j * 50.0;

                GraphIndex graphElement = graphLayers.get(i).get(j);
                graphElements.get(graphElement).x = x;
                graphElements.get(graphElement).y = y;
            }
        }

    }

    public GraphIndex addProceduralClip(VisualTimelineClip visualTimelineClip) {
        GraphIndex graphIndex = GraphIndex.random();
        graphElements.put(graphIndex, new VisualTimelineClipElement(visualTimelineClip));
        return graphIndex;
    }

    @Override
    public String toString() {
        return "EffectGraph [graphElements=" + graphElements + ", connections=" + connections + "]";
    }

    public GraphIndex addEffect(StatelessVideoEffect effect) {
        GraphIndex graphIndex = GraphIndex.random();
        graphElements.put(graphIndex, new StatelessEffectElement(effect));

        return graphIndex;
    }

    public GraphIndex addNode(GraphElement graphElement) {
        GraphIndex graphIndex = GraphIndex.random();
        graphElements.put(graphIndex, graphElement);

        return graphIndex;
    }

    public void removeElementById(GraphIndex graphAddedNode) {
        GraphElement removedElement = graphElements.remove(graphAddedNode);
        if (removedElement != null) {
            removedElement.outputs.keySet().stream()
                    .forEach(a -> {
                        connections.remove(a);
                    });
            removedElement.inputs.keySet()
                    .stream()
                    .forEach(a -> {
                        for (var value : connections.values()) {
                            value.remove(a);
                        }
                    });
        }
    }

}
