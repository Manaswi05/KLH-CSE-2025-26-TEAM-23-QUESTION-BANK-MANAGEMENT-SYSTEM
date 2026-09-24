import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ZFunction {

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
    // 1. Z-FUNCTION ALGORITHM
    // Computes Z-array using [L, R] window technique in O(N)
    // Z[i] is the length of longest prefix match starting at i
    // =====================================================
    public static int[] computeZ(String s) {
        int n = s.length();
        int[] z = new int[n];
        int left = 0;
        int right = 0;

        for (int i = 1; i < n; i++) {
            if (i <= right) {
                // Inside current [L, R] window: copy previously computed value
                z[i] = Math.min(right - i + 1, z[i - left]);
            }

            // Attempt to expand match beyond current window
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) {
                z[i]++;
            }

            // Update window [L, R] if match extends further to the right
            if (i + z[i] - 1 > right) {
                left = i;
                right = i + z[i] - 1;
            }
        }
        return z;
    }

    // =====================================================
    // 2. KEYWORD SEARCH USING Z-FUNCTION
    // Constructs pattern: keyword + "$" + question
    // Searches case-insensitively
    // =====================================================
    public static boolean searchKeyword(String question, String keyword) {
        if (question == null || keyword == null || keyword.trim().isEmpty()) {
            return false;
        }

        String qLower = question.toLowerCase();
        String kLower = keyword.trim().toLowerCase();

        String combined = kLower + "$" + qLower;
        int[] z = computeZ(combined);

        int keywordLength = kLower.length();
        for (int i = keywordLength + 1; i < combined.length(); i++) {
            if (z[i] == keywordLength) {
                return true; // Match found
            }
        }
        return false;
    }

    // =====================================================
    // 3. SEARCH QUESTIONS ACROSS ENTIRE DATASET
    // =====================================================
    public static void searchQuestions(ArrayList<Question> questions, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("Error: Keyword cannot be empty.");
            return;
        }

        System.out.println("\n========== Z-FUNCTION SEARCH RESULTS ==========");
        System.out.println("Searching for keyword: \"" + keyword.trim() + "\"");
        System.out.println("Pattern format used   : keyword + \"$\" + question\n");

        int matchCount = 0;
        for (Question q : questions) {
            if (searchKeyword(q.question, keyword)) {
                matchCount++;
                System.out.println("----------------------------------------------");
                System.out.println("Question ID : " + q.id);
                System.out.println("Subject     : " + q.subject);
                System.out.println("Topic       : " + q.topic);
                System.out.println("Marks       : " + q.marks);
                System.out.println("Difficulty  : " + q.difficulty);
                System.out.println("Question    : " + q.question);
            }
        }

        System.out.println("----------------------------------------------");
        if (matchCount == 0) {
            System.out.println("No questions found matching keyword: \"" + keyword.trim() + "\"");
        } else {
            System.out.println("Total Matching Questions: " + matchCount);
        }
        System.out.println("==============================================\n");
    }

    // =====================================================
    // 4. Z-SCORE / Z-MAPPING FOR QUESTION MARKS
    // Formula: Z = (x - mean) / standard deviation
    // Uses all 50 questions from the dataset
    // =====================================================
    public static void calculateZScores(ArrayList<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            System.out.println("No questions available for Z-Score calculation.");
            return;
        }

        int n = questions.size();
        double sum = 0;
        for (Question q : questions) {
            sum += q.marks;
        }

        double mean = sum / n;

        double varianceSum = 0;
        for (Question q : questions) {
            varianceSum += Math.pow(q.marks - mean, 2);
        }
        double variance = varianceSum / n;
        double standardDeviation = Math.sqrt(variance);

        System.out.println("\n==============================================");
        System.out.println("          QUESTION MARKS Z-SCORES");
        System.out.println("==============================================");
        System.out.printf("Total Questions Evaluated : %d%n", n);
        System.out.printf("Mean Marks (Mean)         : %.2f%n", mean);
        System.out.printf("Standard Deviation (StdDev): %.4f%n", standardDeviation);
        System.out.println("----------------------------------------------");
        System.out.printf("%-15s %-10s %-12s %-25s%n", "Question ID", "Marks", "Z-Score", "Subject");
        System.out.println("----------------------------------------------");

        for (Question q : questions) {
            double zScore = (standardDeviation == 0) ? 0 : (q.marks - mean) / standardDeviation;
            System.out.printf("%-15s %-10d %-12.2f %-25s%n", q.id, q.marks, zScore, q.subject);
        }
        System.out.println("==============================================\n");
    }

    // =====================================================
    // 5. DISPLAY ALL QUESTIONS
    // =====================================================
    public static void displayAllQuestions(ArrayList<Question> questions) {
        System.out.println("\n==========================================================================================");
        System.out.println("                               ALL LOADED QUESTIONS");
        System.out.println("==========================================================================================");
        System.out.printf("%-6s | %-18s | %-16s | %-5s | %-8s | %-8s | %s%n",
                "ID", "Subject", "Topic", "Marks", "Type", "Diff", "Question");
        System.out.println("------------------------------------------------------------------------------------------");

        for (Question q : questions) {
            System.out.printf("%-6s | %-18s | %-16s | %-5d | %-8s | %-8s | %s%n",
                    q.id, q.subject, q.topic, q.marks, q.type, q.difficulty, q.question);
        }

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("Total Questions Loaded: " + questions.size());
        System.out.println("==========================================================================================\n");
    }

    // =====================================================
    // 6. LOAD DATASET FROM FILE
    // =====================================================
    public static ArrayList<Question> loadQuestions(String fileName) {
        ArrayList<Question> questions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String header = br.readLine(); // Read header line
            if (header == null) {
                System.out.println("Error: Dataset file is empty.");
                return questions;
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\t");
                if (parts.length >= 9) {
                    try {
                        int marks = Integer.parseInt(parts[3].trim());
                        Question q = new Question(
                                parts[0].trim(), // Question_ID
                                parts[1].trim(), // Subject
                                parts[2].trim(), // Topic
                                marks,           // Marks
                                parts[4].trim(), // Question_Type
                                parts[5].trim(), // Difficulty
                                parts[6].trim(), // Academic_Year
                                parts[7].trim(), // Keyword
                                parts[8].trim()  // Question
                        );
                        questions.add(q);
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Skipping row with invalid marks format: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: Could not read dataset file '" + fileName + "'.");
            System.out.println("Details: " + e.getMessage());
        }
        return questions;
    }

    // =====================================================
    // 7. MAIN MENU & DRIVER
    // =====================================================
    public static void main(String[] args) {
        String datasetFile = "question_bank_dataset.txt";
        ArrayList<Question> questions = loadQuestions(datasetFile);

        System.out.println();
        System.out.println("========================================");
        System.out.println("    QUESTION BANK MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("Total Questions Loaded: " + questions.size());

        if (questions.isEmpty()) {
            System.out.println("Error: No questions loaded from " + datasetFile);
            System.out.println("Please verify the dataset file exists and has correct permissions.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("========================================");
            System.out.println("    QUESTION BANK MANAGEMENT SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Display All Questions");
            System.out.println("2. Search Question using Z-Function");
            System.out.println("3. Search Question using Edit Distance");
            System.out.println("4. Show Z-Scores for Marks");
            System.out.println("5. Question Assignment using Network Flow");
            System.out.println("6. Exit");
            System.out.print("\nEnter your choice: ");

            String input = sc.nextLine().trim();
            int choice = -1;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\nInvalid input! Please enter a number between 1 and 6.\n");
                continue;
            }

            switch (choice) {
                case 1:
                    displayAllQuestions(questions);
                    break;

                case 2:
                    System.out.print("\nEnter keyword: ");
                    String zKeyword = sc.nextLine().trim();
                    searchQuestions(questions, zKeyword);
                    break;

                case 3:
                    System.out.print("\nEnter keyword: ");
                    String edKeyword = sc.nextLine().trim();
                    EditDistance.searchQuestion(questions, edKeyword);
                    break;

                case 4:
                    calculateZScores(questions);
                    break;

                case 5:
                    NetworkFlow.runQuestionAssignment();
                    break;

                case 6:
                    System.out.println("\nThank you for using Question Bank Management System. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid choice! Please choose an option from 1 to 6.\n");
                    break;
            }
        }
        sc.close();
    }
}
