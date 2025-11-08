# Shortest Path Visualizer

JavaFX application for visualizing and comparing shortest path algorithms (Dijkstra's, Bellman-Ford, A*) in weighted graphs with step-by-step animation.

## Features
- Interactive graph visualization using JGraphX
- Three shortest path algorithms: Dijkstra's, Bellman-Ford, A*
- Step-by-step algorithm execution with play/pause/step controls
- Random weighted graph generation
- Performance metrics and algorithm comparison
- Shortest path highlighting with animation
- Interactive node selection via mouse clicks
- Comprehensive keyboard shortcuts for efficient control
- Responsive layout with minimum window size constraints
- Tooltips and help text for all controls
- Clear All function to reset application state
- Visual feedback for node selection and algorithm execution

## Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

## Build Instructions
- Clone repository
- Run `mvn clean install` to build
- Run `mvn javafx:run` to launch application

## Usage Guide

### Getting Started
1. Launch application: `mvn javafx:run`
2. The application starts with a sample graph (5 nodes, 5 edges)
3. Use the top control panel to generate random graphs or run algorithms

### Generating Random Graphs
1. Enter parameters:
   - Nodes: Number of nodes (1-100)
   - Density: Edge density (0.0-1.0, where 0.0 = minimal spanning tree, 1.0 = complete graph)
   - Min/Max Weight: Edge weight range (positive numbers)
2. Click "Generate Random Graph"
3. The graph will be displayed in the center panel with nodes arranged in a circular layout

### Running Algorithms
1. Select algorithm from dropdown: Dijkstra, Bellman-Ford, or A*
2. Specify source and target nodes:
   - Type node IDs directly in text fields (e.g., "A", "N0")
   - OR click 📍 button next to field, then click a node on the graph
3. Click "Run Algorithm" to execute selected algorithm
4. Click "Run All Algorithms" to execute all three and compare results
5. Results appear in the Metrics Panel (right side)

### Animation Controls
- ▶ Play: Start automatic step-by-step animation
- ⏸ Pause: Pause animation
- ⏮ Step Back: Go to previous step
- ⏭ Step Forward: Go to next step
- ⏹ Reset: Reset animation to first step
- Speed Slider: Adjust playback speed (0.5x to 3.0x)

### Understanding the Visualization
- **Node Colors:**
  - Light Blue: Unvisited node
  - Light Green: Visited/processed node
  - Yellow: Currently processing node
  - Medium Green: Source node
  - Red: Target node
- **Edge Colors:**
  - Gray: Normal edge
  - Bold Green: Edge in shortest path
- **Metrics Panel:**
  - Shows execution time, nodes visited, path cost, and path sequence
  - Comparison table displays results from all three algorithms when "Run All" is used

## Keyboard Shortcuts
- Space: Toggle play/pause animation
- Left Arrow: Step backward
- Right Arrow: Step forward
- Home: Reset animation to start
- Escape: Cancel node selection mode
- Ctrl+G: Focus on Generate button
- Ctrl+R: Focus on Run Algorithm button

## Troubleshooting

### Common Issues

1. **"Source/Target node not found" error:**
   - Ensure node IDs match exactly (case-sensitive)
   - Use the 📍 button to select nodes visually
   - Check that the graph has been generated

2. **"No path found" message:**
   - The graph may be disconnected (source and target in separate components)
   - Try generating a new graph with higher edge density
   - Verify source and target are different nodes

3. **Graph not displaying:**
   - Ensure Java 21+ is installed
   - Check that JavaFX and JGraphX dependencies are resolved: `mvn clean install`
   - Verify no firewall blocking Swing/JavaFX rendering

4. **Animation not playing:**
   - Ensure an algorithm has been executed first ("Run Algorithm" or "Run All")
   - Check that animation is not already at the end (use Reset button)
   - Verify Play button is enabled (not grayed out)

5. **CSS styles not loading:**
   - Ensure `src/main/resources/styles.css` exists
   - Check that resources are included in build: `mvn clean package`
   - Application will work without CSS (using fallback inline styles)

## Performance Notes
- Graphs with 100+ nodes may take longer to render
- Bellman-Ford is slower than Dijkstra/A* for large graphs (O(VE) vs O((V+E)logV))
- Animation speed can be increased using the speed slider

## Algorithm Comparison

### Dijkstra's Algorithm
- Best for: Graphs with non-negative edge weights
- Time Complexity: O((V+E) log V)
- Guarantees: Optimal shortest path
- Use Case: General-purpose shortest path in road networks, routing

### Bellman-Ford Algorithm
- Best for: Graphs with negative edge weights
- Time Complexity: O(VE)
- Guarantees: Optimal shortest path, detects negative cycles
- Use Case: Currency arbitrage, network routing with costs

### A* Algorithm
- Best for: Graphs with spatial/geometric properties
- Time Complexity: O((V+E) log V) (often faster in practice)
- Guarantees: Optimal shortest path (with admissible heuristic)
- Use Case: Game pathfinding, GPS navigation, robotics
- Note: Uses Euclidean distance heuristic based on node coordinates

### Tips for Comparison
- Use "Run All Algorithms" to compare performance on the same graph
- Check the Comparison table in Metrics Panel for execution time and nodes visited
- A* typically visits fewer nodes than Dijkstra due to heuristic guidance
- Bellman-Ford is slower but handles negative weights

## Project Structure
- `src/main/java/com/shortestpath/model/` - Graph data structures (Node, Edge, WeightedGraph)
- `src/main/java/com/shortestpath/algorithm/` - Shortest path algorithm implementations
- `src/main/java/com/shortestpath/ui/` - JavaFX UI components and visualization
- `src/main/java/com/shortestpath/util/` - Utility classes (random graph generator)

## License
To be determined

## Author
To be filled
