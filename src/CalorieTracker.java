import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.net.Socket;
import java.net.ServerSocket;

public class CalorieTracker {
    private ArrayList<Meal> meals;
    private Stack<Meal> mealStack;
    private HashMap<LocalDate, Integer> dailyCalories;
    private MealSearchTree mealSearchTree;
    private CalorieTrackerServer server;

    public CalorieTracker() {
        meals = new ArrayList<>();
        mealStack = new Stack<>();
        dailyCalories = new HashMap<>();
        mealSearchTree = new MealSearchTree();
        server = new CalorieTrackerServer();
    }

    public void addMeal(Meal meal) {
        meals.add(meal);
        mealStack.push(meal);
        dailyCalories.putIfAbsent(meal.getDate(), 0);
        dailyCalories.put(meal.getDate(), dailyCalories.get(meal.getDate()) + meal.getCalories());
        mealSearchTree.insert(meal);
    }

    public int calculateTotalCalories(LocalDate date) {
        return dailyCalories.getOrDefault(date, 0);
    }

    public int calculateTotalCaloriesForDay(LocalDate date) {
        int totalCalories = 0;
        for (Meal meal : meals) {
            if (meal.getDate().equals(date)) {
                totalCalories += meal.getCalories();
            }
        }
        return totalCalories;
    }

    public Meal searchByName(String name) {
        return mealSearchTree.search(name);
    }

    public ArrayList<Meal> getMeals() {
        return meals;
    }

    public void sortMeals() {
        mergesort(0, meals.size() - 1);
    }

    private void mergesort(int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergesort(left, mid);
            mergesort(mid + 1, right);
            merge(left, mid, right);
        }
    }

    private void merge(int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        ArrayList<Meal> leftArray = new ArrayList<>(n1);
        ArrayList<Meal> rightArray = new ArrayList<>(n2);

        for (int i = 0; i < n1; ++i) {
            leftArray.add(meals.get(left + i));
        }
        for (int j = 0; j < n2; ++j) {
            rightArray.add(meals.get(mid + 1 + j));
        }

        int i = 0, j = 0;

        int k = left;

        while (i < n1 && j < n2) {
            if (leftArray.get(i).getCalories() <= rightArray.get(j).getCalories()) {
                meals.set(k, leftArray.get(i));
                i++;
            } else {
                meals.set(k, rightArray.get(j));
                j++;
            }
            k++;
        }

        while (i < n1) {
            meals.set(k, leftArray.get(i));
            i++;
            k++;
        }

        while (j < n2) {
            meals.set(k, rightArray.get(j));
            j++;
            k++;
        }
    }

    public Stack<Meal> getMealStack() {
        return mealStack;
    }

    public HashMap<LocalDate, Integer> getDailyCalories() {
        return dailyCalories;
    }

    public void saveMealsToFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            for (Meal meal : meals) {
                writer.write(meal.toString() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMealsFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                int calories = Integer.parseInt(parts[1]);
                LocalDate date = LocalDate.parse(parts[2]);
                Meal meal = new Meal(name, calories, date);
                addMeal(meal);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CalorieTracker tracker = new CalorieTracker();
        String inputFile = "meals.txt";
        String outputFile = "sorted_meals.txt";

        tracker.loadMealsFromFile(inputFile);
        tracker.sortMeals();
        tracker.saveMealsToFile(outputFile);

        new Thread(tracker.server).start();

        CalorieTrackerGUI.launch(CalorieTrackerGUI.class, args);
    }
}

class FoodItem {
    private String name;
    private int calories;

    public FoodItem(String name, int calories) {
        this.name = name;
        this.calories = calories;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}

class Meal extends FoodItem {
    private LocalDate date;

    public Meal(String name, int calories, LocalDate date) {
        super(name, calories);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return getName() + "," + getCalories() + "," + date.toString();
    }
}

class TreeNode {
    Meal meal;
    TreeNode left;
    TreeNode right;

    public TreeNode(Meal meal) {
        this.meal = meal;
        this.left = null;
        this.right = null;
    }
}

class MealSearchTree {
    private TreeNode root;

    public MealSearchTree() {
        this.root = null;
    }

    public void insert(Meal meal) {
        root = insertRecursive(root, meal);
    }

    private TreeNode insertRecursive(TreeNode root, Meal meal) {
        if (root == null) {
            return new TreeNode(meal);
        }

        if (meal.getName().compareTo(root.meal.getName()) < 0) {
            root.left = insertRecursive(root.left, meal);
        } else if (meal.getName().compareTo(root.meal.getName()) > 0) {
            root.right = insertRecursive(root.right, meal);
        }

        return root;
    }

    public Meal search(String name) {
        return searchRecursive(root, name);
    }

    private Meal searchRecursive(TreeNode root, String name) {
        if (root == null || root.meal.getName().equals(name)) {
            return (root != null) ? root.meal : null;
        }

        if (name.compareTo(root.meal.getName()) < 0) {
            return searchRecursive(root.left, name);
        } else {
            return searchRecursive(root.right, name);
        }
    }
}

class CalorieTrackerServer implements Runnable {
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Received from client: " + inputLine);
                    out.println("Server received: " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




