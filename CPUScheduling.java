import java.util.*;

// Process class to store process information
class Process {
    String pid;          // Process ID
    int arrivalTime;     // Time when process arrives
    int burstTime;       // Total CPU time required
    int remainingTime;   // Remaining CPU time (for preemptive algorithms)
    int waitingTime;     // Time spent waiting
    int turnaroundTime;  // Total time from arrival to completion

    // Constructor for Process
    public Process(String pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
    }
}

public class CPUScheduling {
    private static Scanner scanner = new Scanner(System.in);
    
    // Method to get process input from user
    private static List<Process> getProcessInput() {
        System.out.println("\n=============================");
        System.out.print("Enter number of processes: ");
        int n;
        // Input validation for number of processes
        while (true) {
            try {
                n = Integer.parseInt(scanner.nextLine());
                if (n <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a positive integer");
                System.out.print("Enter number of processes: ");  // Add this to reprint prompt after invalid input
            }
        }
        System.out.println("=============================");

        List<Process> processes = new ArrayList<>();
        // Get details for each process
        for (int i = 0; i < n; i++) {
            System.out.println("\n======== Process P" + (i + 1) + " ==========");
            int arrival, burst;
            // Input validation for arrival and burst times
            while (true) {
                try {
                    System.out.print("Arrival Time: ");
                    arrival = Integer.parseInt(scanner.nextLine());
                    System.out.print("Burst Time: ");
                    burst = Integer.parseInt(scanner.nextLine());
                    if (arrival < 0 || burst <= 0) {
                        System.out.println("Arrival time must be non-negative and burst time must be positive");
                        continue;
                    }
                    processes.add(new Process("P" + (i + 1), arrival, burst));
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Please enter valid integers");
                }
            }
            System.out.println("=============================");
        }
        return processes;
    }

    // Method to display scheduling results
    private static void displayResults(List<Process> processes, List<String> ganttChart, String algorithm) {
        System.out.println("\n======= " + algorithm + " Scheduling Results =======");
        System.out.println("\nGantt Chart:");
        System.out.println(String.join(" ", ganttChart));

        double totalWaiting = 0, totalTurnaround = 0;
        // Display individual process metrics
        System.out.println("\nProcess\tWaiting Time\tTurnaround Time");
        System.out.println("=============================");
        for (Process p : processes) {
            System.out.printf("%s\t%d\t\t%d%n", p.pid, p.waitingTime, p.turnaroundTime);
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }
        // Display average metrics
        System.out.println("=============================");
        System.out.printf("Average Waiting Time: %.2f%n", totalWaiting / processes.size());
        System.out.printf("Average Turnaround Time: %.2f%n", totalTurnaround / processes.size());
        System.out.println("=============================");
    }

    // FCFS (First-Come, First-Served) algorithm implementation
    private static void fcfs(List<Process> processes) {
        // Sort by arrival time
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();

        // Process each job in order
        for (Process p : processes) {
            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime;
            p.waitingTime = currentTime - p.arrivalTime;
            p.turnaroundTime = p.waitingTime + p.burstTime;
            ganttChart.add(p.pid + "(" + currentTime + "-" + (currentTime + p.burstTime) + ")");
            currentTime += p.burstTime;
        }

        displayResults(processes, ganttChart, "FCFS");
    }

    // SJF (Shortest-Job-First) algorithm implementation (non-preemptive)
    private static void sjf(List<Process> processes) {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();
        List<Process> completed = new ArrayList<>();
        List<Process> remaining = new ArrayList<>(processes);

        while (!remaining.isEmpty()) {
            List<Process> available = new ArrayList<>();
            // Find processes that have arrived
            for (Process p : remaining) {
                if (p.arrivalTime <= currentTime) available.add(p);
            }

            if (available.isEmpty()) {
                currentTime++;
                continue;
            }

            // Select process with shortest burst time
            Process currentProcess = Collections.min(available, Comparator.comparingInt(p -> p.burstTime));
            currentTime += currentProcess.burstTime;
            currentProcess.waitingTime = currentTime - currentProcess.arrivalTime - currentProcess.burstTime;
            currentProcess.turnaroundTime = currentTime - currentProcess.arrivalTime;
            ganttChart.add(currentProcess.pid + "(" + (currentTime - currentProcess.burstTime) + "-" + currentTime + ")");
            completed.add(currentProcess);
            remaining.remove(currentProcess);
        }

        displayResults(completed, ganttChart, "SJF");
    }

    // SRT (Shortest-Remaining-Time) algorithm implementation (preemptive)
    private static void srt(List<Process> processes) {
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();
        List<Process> completed = new ArrayList<>();
        List<Process> remaining = new ArrayList<>(processes);

        while (!remaining.isEmpty()) {
            List<Process> available = new ArrayList<>();
            // Find processes that have arrived
            for (Process p : remaining) {
                if (p.arrivalTime <= currentTime) available.add(p);
            }

            if (available.isEmpty()) {
                currentTime++;
                continue;
            }

            // Select process with shortest remaining time
            Process currentProcess = Collections.min(available, Comparator.comparingInt(p -> p.remainingTime));
            currentProcess.remainingTime--;
            ganttChart.add(currentProcess.pid + "(" + currentTime + "-" + (currentTime + 1) + ")");
            currentTime++;

            // Check if process is complete
            if (currentProcess.remainingTime == 0) {
                currentProcess.turnaroundTime = currentTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                completed.add(currentProcess);
                remaining.remove(currentProcess);
            }
        }

        displayResults(completed, ganttChart, "SRT");
    }

    // Round Robin algorithm implementation
    private static void roundRobin(List<Process> processes) {
        // Get time quantum with validation
        int quantum;
        while (true) {
            try {
                System.out.print("Enter time quantum: ");
                quantum = Integer.parseInt(scanner.nextLine());
                if (quantum <= 0) throw new NumberFormatException();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Quantum must be positive");
            }
        }

        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        List<String> ganttChart = new ArrayList<>();
        Queue<Process> queue = new LinkedList<>(processes);
        List<Process> completed = new ArrayList<>();

        while (!queue.isEmpty()) {
            Process currentProcess = queue.poll();
            if (currentTime < currentProcess.arrivalTime) currentTime = currentProcess.arrivalTime;

            // Execute for quantum or remaining time, whichever is smaller
            int executionTime = Math.min(quantum, currentProcess.remainingTime);
            ganttChart.add(currentProcess.pid + "(" + currentTime + "-" + (currentTime + executionTime) + ")");
            currentProcess.remainingTime -= executionTime;
            currentTime += executionTime;

            // Check if process is complete
            if (currentProcess.remainingTime == 0) {
                currentProcess.turnaroundTime = currentTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                completed.add(currentProcess);
            } else {
                queue.add(currentProcess);
            }
        }

        displayResults(completed, ganttChart, "Round Robin");
    }

    // Main method with menu-driven interface
    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=========== CPU Scheduling Simulator ===========");
            System.out.println("1. First-Come, First-Served (FCFS)");
            System.out.println("2. Shortest-Job-First (SJF)");
            System.out.println("3. Shortest-Remaining-Time (SRT)");
            System.out.println("4. Round Robin (RR)");
            System.out.println("================================================");
            System.out.println("5. Exit");
            System.out.println("================================================");

            int choice;
            try {
                System.out.print("Enter your choice (1-5): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice == 5) {
                    System.out.println("Exiting program...");
                    break;
                }
                if (choice < 1 || choice > 5) {
                    System.out.println("Invalid choice! Please select 1-5");
                    System.out.println("Press Enter to continue...");
                    scanner.nextLine();
                    continue;
                }

                List<Process> processes = getProcessInput();
                // Execute selected algorithm
                switch (choice) {
                    case 1: fcfs(processes); break;
                    case 2: sjf(processes); break;
                    case 3: srt(processes); break;
                    case 4: roundRobin(processes); break;
                }

                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer");
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }
}