import java.util.*;

public class NetworkFlow {

    // =========================================================================
    // EDMONDS-KARP ALGORITHM (BFS-based implementation of Ford-Fulkerson)
    //
    // Finds shortest augmenting path in terms of edge count.
    // Time Complexity  : O(V * E^2)
    // Space Complexity : O(V^2) for adjacency matrix / residual graph
    // =========================================================================

    /**
     * Breadth-First Search (BFS) to find an augmenting path from source to sink in the residual graph.
     *
     * @param residual Residual capacity graph
     * @param source   Source node index
     * @param sink     Sink node index
     * @param parent   Array to store the path taken
     * @return true if an augmenting path exists, false otherwise
     */
    public static boolean bfs(int[][] residual, int source, int sink, int[] parent) {
        int n = residual.length;
        boolean[] visited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();

        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int current = queue.poll();

            for (int next = 0; next < n; next++) {
                // If adjacent node is unvisited and has available residual capacity
                if (!visited[next] && residual[current][next] > 0) {
                    queue.add(next);
                    visited[next] = true;
                    parent[next] = current;

                    if (next == sink) {
                        return true; // Reached sink, augmenting path found
                    }
                }
            }
        }
        return false;
    }

    /**
     * Reconstructs the augmenting path from sink back to source using parent pointers.
     */
    private static List<Integer> getPath(int[] parent, int source, int sink) {
        List<Integer> path = new ArrayList<>();
        int curr = sink;
        while (curr != -1) {
            path.add(curr);
            curr = parent[curr];
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Formats a path of node indices using human-readable names.
     */
    private static String formatPath(List<Integer> path, String[] nodeNames) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            int node = path.get(i);
            sb.append(nodeNames != null && node < nodeNames.length ? nodeNames[node] : "Node " + node);
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    /**
     * Executes Edmonds-Karp Maximum Flow algorithm, displaying augmenting paths,
     * bottleneck values, and residual graph updates.
     *
     * @param capacity  Original capacity matrix
     * @param source    Source node index
     * @param sink      Sink node index
     * @param nodeNames Optional array of descriptive node names
     * @param residual  Output residual graph matrix to record final state
     * @return Maximum flow value
     */
    public static int edmondsKarp(int[][] capacity, int source, int sink, String[] nodeNames, int[][] residual) {
        int n = capacity.length;

        // Initialize residual graph with original edge capacities
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                residual[i][j] = capacity[i][j];
            }
        }

        int[] parent = new int[n];
        int maxFlow = 0;
        int pathCount = 0;

        System.out.println("---------- FINDING AUGMENTING PATHS (BFS) ----------");

        // Continuously find augmenting paths while BFS reaches sink
        while (bfs(residual, source, sink, parent)) {
            pathCount++;

            // Step 1: Determine bottleneck capacity along the augmenting path
            int bottleneck = Integer.MAX_VALUE;
            int current = sink;
            while (current != source) {
                int previous = parent[current];
                bottleneck = Math.min(bottleneck, residual[previous][current]);
                current = previous;
            }

            // Display augmenting path information
            List<Integer> path = getPath(parent, source, sink);
            System.out.printf("Path %d : %s%n", pathCount, formatPath(path, nodeNames));
            System.out.printf("          Bottleneck Capacity = %d%n", bottleneck);

            // Step 2: Update residual capacities
            // Reduce forward edge capacity, increase reverse edge capacity
            current = sink;
            while (current != source) {
                int previous = parent[current];
                residual[previous][current] -= bottleneck;
                residual[current][previous] += bottleneck;
                current = previous;
            }

            maxFlow += bottleneck;
        }

        if (pathCount == 0) {
            System.out.println("No augmenting paths found from Source to Sink.");
        }
        System.out.println("-----------------------------------------------------");

        return maxFlow;
    }

    // =========================================================================
    // QUESTION BANK ASSIGNMENT PROBLEM
    //
    // Assigns students to suitable question categories / topics
    // using Maximum Bipartite Matching modeled via Network Flow.
    //
    // Graph Model:
    // Source (0)
    //   ↓
    // Students (1..4)
    //   ↓
    // Question Topics (5..8) [From question_bank_dataset.txt]
    //   ↓
    // Sink (9)
    // =========================================================================
    public static void runQuestionAssignment() {
        System.out.println("\n========================================================");
        System.out.println("   QUESTION BANK NETWORK FLOW ASSIGNMENT (EDMONDS-KARP)");
        System.out.println("========================================================");

        // Define entities from Question Bank
        String[] students = {
            "Student 1",
            "Student 2",
            "Student 3",
            "Student 4"
        };

        String[] topics = {
            "Regression (ML)",
            "SQL (DBMS)",
            "Sorting (DSA)",
            "Scheduling (OS)"
        };

        int numStudents = students.length;
        int numTopics = topics.length;

        // Node indexing:
        // 0: Source
        // 1..4: Students
        // 5..8: Topics
        // 9: Sink
        int source = 0;
        int sink = 1 + numStudents + numTopics;
        int totalNodes = sink + 1;

        String[] nodeNames = new String[totalNodes];
        nodeNames[source] = "Source";
        for (int i = 0; i < numStudents; i++) {
            nodeNames[1 + i] = students[i];
        }
        for (int j = 0; j < numTopics; j++) {
            nodeNames[1 + numStudents + j] = topics[j];
        }
        nodeNames[sink] = "Sink";

        int[][] capacity = new int[totalNodes][totalNodes];

        // 1. Source -> Students (capacity = 1: each student can receive 1 topic assignment)
        for (int i = 0; i < numStudents; i++) {
            capacity[source][1 + i] = 1;
        }

        // 2. Students -> Topics (Candidate preferences based on Question Bank domains)
        // Student 1 prefers: Regression, SQL
        capacity[1][5] = 1; // Student 1 -> Regression
        capacity[1][6] = 1; // Student 1 -> SQL

        // Student 2 prefers: SQL, Sorting
        capacity[2][6] = 1; // Student 2 -> SQL
        capacity[2][7] = 1; // Student 2 -> Sorting

        // Student 3 prefers: Sorting, Scheduling
        capacity[3][7] = 1; // Student 3 -> Sorting
        capacity[3][8] = 1; // Student 3 -> Scheduling

        // Student 4 prefers: Regression, Scheduling
        capacity[4][5] = 1; // Student 4 -> Regression
        capacity[4][8] = 1; // Student 4 -> Scheduling

        // 3. Topics -> Sink (capacity = 1: each topic slot is assigned at most once)
        for (int j = 0; j < numTopics; j++) {
            capacity[1 + numStudents + j][sink] = 1;
        }

        System.out.println("Assignment Problem Setup:");
        System.out.println("  * Source connects to 4 Students (capacity = 1 each)");
        System.out.println("  * Student 1 eligible for: Regression (ML), SQL (DBMS)");
        System.out.println("  * Student 2 eligible for: SQL (DBMS), Sorting (DSA)");
        System.out.println("  * Student 3 eligible for: Sorting (DSA), Scheduling (OS)");
        System.out.println("  * Student 4 eligible for: Regression (ML), Scheduling (OS)");
        System.out.println("  * 4 Question Topics connect to Sink (capacity = 1 each)");
        System.out.println();

        int[][] residual = new int[totalNodes][totalNodes];
        int maxFlow = edmondsKarp(capacity, source, sink, nodeNames, residual);

        System.out.printf("Maximum Flow (Total Assignments) = %d%n%n", maxFlow);

        // Derive assignments dynamically from the computed flow
        // Flow sent = capacity[u][v] - residual[u][v]
        System.out.println("========== FINAL STUDENT -> TOPIC ASSIGNMENTS ==========");
        int matchedCount = 0;
        for (int i = 0; i < numStudents; i++) {
            int studentNode = 1 + i;
            boolean assigned = false;
            for (int j = 0; j < numTopics; j++) {
                int topicNode = 1 + numStudents + j;
                int flow = capacity[studentNode][topicNode] - residual[studentNode][topicNode];
                if (flow > 0) {
                    System.out.printf("  [ASSIGNED] %-12s --->  %-20s [Flow = %d]%n",
                            students[i], topics[j], flow);
                    assigned = true;
                    matchedCount++;
                    break;
                }
            }
            if (!assigned) {
                System.out.printf("  [UNASSIGNED] %-12s --->  Unassigned%n", students[i]);
            }
        }
        System.out.println("========================================================\n");
    }

    // =========================================================================
    // MAIN METHOD
    // =========================================================================
    public static void main(String[] args) {
        runQuestionAssignment();
    }
}