import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class EditDistance {

    // =====================================================
    // 1. LEVENSHTEIN DISTANCE
    // Insert + Delete + Substitute
    // Wagner-Fischer Dynamic Programming
    // Time Complexity: O(m * n), Space Complexity: O(m * n)
    // =====================================================
    public static int levenshteinDistance(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        // First row: cost of inserting characters to form prefix of b
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // First column: cost of deleting characters from prefix of a
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        // Fill DP table using Wagner-Fischer recurrence
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int deletion = dp[i - 1][j];
                    int insertion = dp[i][j - 1];
                    int substitution = dp[i - 1][j - 1];

                    dp[i][j] = 1 + Math.min(deletion, Math.min(insertion, substitution));
                }
            }
        }

        return dp[m][n];
    }

    // =====================================================
    // 2. DAMERAU-LEVENSHTEIN DISTANCE
    // Insert + Delete + Substitute + Transpose of adjacent chars
    // Time Complexity: O(m * n), Space Complexity: O(m * n)
    // =====================================================
    public static int damerauLevenshteinDistance(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                int deletion = dp[i - 1][j] + 1;
                int insertion = dp[i][j - 1] + 1;
                int substitution = dp[i - 1][j - 1] + cost;

                dp[i][j] = Math.min(deletion, Math.min(insertion, substitution));

                // Adjacent character transposition check
                if (i > 1 && j > 1 &&
                    a.charAt(i - 1) == b.charAt(j - 2) &&
                    a.charAt(i - 2) == b.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }

        return dp[m][n];
    }

    // =====================================================
    // 3. WEIGHTED EDIT DISTANCE
    // Insertion Cost = 1, Deletion Cost = 1, Substitution Cost = 2
    // Time Complexity: O(m * n), Space Complexity: O(m * n)
    // =====================================================
    public static int weightedEditDistance(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        int m = a.length();
        int n = b.length();

        int[][] dp = new int[m + 1][n + 1];

        int insertionCost = 1;
        int deletionCost = 1;
        int substitutionCost = 2;

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j * insertionCost;
        }

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i * deletionCost;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : substitutionCost;

                int deletion = dp[i - 1][j] + deletionCost;
                int insertion = dp[i][j - 1] + insertionCost;
                int substitution = dp[i - 1][j - 1] + cost;

                dp[i][j] = Math.min(deletion, Math.min(insertion, substitution));
            }
        }

        return dp[m][n];
    }

    // =====================================================
    // QUESTION RECORD CLASS
    // =====================================================
    public static class Question {
        public String id;
        public String subject;
        public String topic;
        public int marks;
        public String type;
        public String difficulty;
        public String year;
        public String keyword;
        public String question;

        public Question(String id, String subject, String topic, int marks,
                        String type, String difficulty, String year,
                        String keyword, String question) {
            this.id = id;
            this.subject = subject;
            this.topic = topic;
            this.marks = marks;
            this.type = type;
            this.difficulty = difficulty;
            this.year = year;
            this.keyword = keyword;
            this.question = question;
        }
    }

    // =====================================================
    // LOAD QUESTIONS FROM DATASET
    // =====================================================
    public static ArrayList<Question> loadQuestions(String fileName) {
        ArrayList<Question> questions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\t");
                if (data.length >= 9) {
                    try {
                        int marks = Integer.parseInt(data[3].trim());
                        Question q = new Question(
                                data[0].trim(), data[1].trim(), data[2].trim(),
                                marks, data[4].trim(), data[5].trim(),
                                data[6].trim(), data[7].trim(), data[8].trim()
                        );
                        questions.add(q);
                    } catch (NumberFormatException e) {
                        // Skip unparseable marks row
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading dataset file: " + fileName);
            System.out.println(e.getMessage());
        }
        return questions;
    }

    // =====================================================
    // SEARCH QUESTION USING EDIT DISTANCE (FOR ZFunction.Question)
    // =====================================================
    public static void searchQuestion(ArrayList<ZFunction.Question> questions, String userKeyword) {
        if (userKeyword == null || userKeyword.trim().isEmpty()) {
            System.out.println("Error: Keyword cannot be empty.");
            return;
        }

        String searchWord = userKeyword.trim().toLowerCase();
        int minimumDistance = Integer.MAX_VALUE;
        ZFunction.Question bestQuestion = null;

        for (ZFunction.Question q : questions) {
            String keyword = q.keyword.toLowerCase();
            int distance = levenshteinDistance(searchWord, keyword);

            if (distance < minimumDistance) {
                minimumDistance = distance;
                bestQuestion = q;
            }
        }

        if (bestQuestion != null) {
            System.out.println();
            System.out.println("========== BEST MATCH ==========");
            System.out.println("Entered Keyword : " + userKeyword.trim());
            System.out.println("Matched Keyword : " + bestQuestion.keyword);
            System.out.println("Edit Distance   : " + minimumDistance);
            System.out.println("Question ID     : " + bestQuestion.id);
            System.out.println("Subject         : " + bestQuestion.subject);
            System.out.println("Topic           : " + bestQuestion.topic);
            System.out.println("Marks           : " + bestQuestion.marks);
            System.out.println("Difficulty      : " + bestQuestion.difficulty);
            System.out.println("Question        : " + bestQuestion.question);
            System.out.println("================================\n");
        } else {
            System.out.println("No questions found.");
        }
    }

    // Overload for EditDistance.Question
    public static void searchQuestionLocal(ArrayList<Question> questions, String userKeyword) {
        if (userKeyword == null || userKeyword.trim().isEmpty()) {
            System.out.println("Error: Keyword cannot be empty.");
            return;
        }

        String searchWord = userKeyword.trim().toLowerCase();
        int minimumDistance = Integer.MAX_VALUE;
        Question bestQuestion = null;

        for (Question q : questions) {
            String keyword = q.keyword.toLowerCase();
            int distance = levenshteinDistance(searchWord, keyword);

            if (distance < minimumDistance) {
                minimumDistance = distance;
                bestQuestion = q;
            }
        }

        if (bestQuestion != null) {
            System.out.println();
            System.out.println("========== BEST MATCH ==========");
            System.out.println("Entered Keyword : " + userKeyword.trim());
            System.out.println("Matched Keyword : " + bestQuestion.keyword);
            System.out.println("Edit Distance   : " + minimumDistance);
            System.out.println("Question ID     : " + bestQuestion.id);
            System.out.println("Subject         : " + bestQuestion.subject);
            System.out.println("Topic           : " + bestQuestion.topic);
            System.out.println("Marks           : " + bestQuestion.marks);
            System.out.println("Difficulty      : " + bestQuestion.difficulty);
            System.out.println("Question        : " + bestQuestion.question);
            System.out.println("================================\n");
        } else {
            System.out.println("No questions found.");
        }
    }

    // =====================================================
    // RUN ALGORITHM TESTS
    // =====================================================
    public static void runAlgorithmTest() {
        System.out.println("---------- ALGORITHM TEST ----------");
        String a = "AB";
        String b = "BA";

        System.out.println("Input Strings: a = \"" + a + "\", b = \"" + b + "\"");
        System.out.println("Levenshtein (AB -> BA)          : " + levenshteinDistance(a, b));
        System.out.println("Damerau-Levenshtein (AB -> BA)  : " + damerauLevenshteinDistance(a, b));
        System.out.println("Weighted Edit Distance (AB -> BA): " + weightedEditDistance(a, b));
        System.out.println("Wagner-Fischer: Dynamic Programming algorithm used for Levenshtein distance.");
        System.out.println("Note: Damerau-Levenshtein counts adjacent swap (transposition) as 1 operation.");
        System.out.println("------------------------------------\n");
    }

    // =====================================================
    // MAIN METHOD
    // =====================================================
    public static void main(String[] args) {
        String datasetFile = "question_bank_dataset.txt";
        ArrayList<Question> questions = loadQuestions(datasetFile);

        System.out.println();
        System.out.println("======================================");
        System.out.println("   QUESTION BANK MANAGEMENT SYSTEM");
        System.out.println("   EDIT DISTANCE KEYWORD MATCHER");
        System.out.println("======================================");
        System.out.println("Total Questions Loaded: " + questions.size());

        if (questions.isEmpty()) {
            System.out.println("Warning: No questions loaded from " + datasetFile);
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("\nEnter keyword to search: ");
        String keyword = sc.nextLine().trim();

        searchQuestionLocal(questions, keyword);

        runAlgorithmTest();

        sc.close();
    }
}