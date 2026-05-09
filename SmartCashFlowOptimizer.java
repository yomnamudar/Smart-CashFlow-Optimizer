import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

class FinancialObligation implements Comparable<FinancialObligation> {
    String name;
    double amount;
    LocalDate dueDate;
    int penaltyLevel; 
    boolean isEssential;

    public FinancialObligation(String name, double amount, LocalDate dueDate, int penaltyLevel, boolean isEssential) {
        this.name = name;
        this.amount = amount;
        this.dueDate = dueDate;
        this.penaltyLevel = penaltyLevel;
        this.isEssential = isEssential;
    }

    public double getPriorityScore() {
        long daysRemaining = LocalDate.now().until(dueDate).getDays();
        // ضمان عدم وجود أولوية سالبة لضمان منطقية الترتيب
        return Math.max(0, (penaltyLevel * 15) - (daysRemaining * 2));
    }

    @Override
    public int compareTo(FinancialObligation other) {
        return Double.compare(other.getPriorityScore(), this.getPriorityScore());
    }
}

class LogNode {
    String action;
    LogNode next, prev;
    public LogNode(String action) { this.action = action; }
}

class UndoStep {
    double balanceBefore;
    FinancialObligation bill;
    public UndoStep(double balance, FinancialObligation bill) {
        this.balanceBefore = balance;
        this.bill = bill;
    }
}

public class SmartCashFlowOptimizer {
    private PriorityQueue<FinancialObligation> pendingBills = new PriorityQueue<>();
    private Stack<UndoStep> undoStack = new Stack<>();
    private LogNode headLog, tailLog;
    private double currentBalance;
    private final double EMERGENCY_RESERVE = 50.0;

    public void addObligation(FinancialObligation ob) {
        pendingBills.add(ob);
    }

    public void addLog(String message) {
        LogNode newNode = new LogNode(message);
        if (headLog == null) {
            headLog = tailLog = newNode;
        } else {
            tailLog.next = newNode;
            newNode.prev = tailLog;
            tailLog = newNode;
        }
    }

    public void runOptimization(double initialBalance) {
        this.currentBalance = initialBalance;
        addLog("Optimization started with initial balance: " + initialBalance);
        
        System.out.println("\n--- [ System is calculating the best payment path... ] ---");

        if (pendingBills.isEmpty()) {
            System.out.println("[!] Your bill list is empty. Nothing to optimize.");
            return;
        }

        while (!pendingBills.isEmpty()) {
            FinancialObligation topBill = pendingBills.peek();
            double potentialBalance = currentBalance - topBill.amount;

            if (currentBalance >= topBill.amount && (potentialBalance >= EMERGENCY_RESERVE || topBill.isEssential)) {
                undoStack.push(new UndoStep(currentBalance, topBill));
                currentBalance -= topBill.amount;
                pendingBills.poll();
                System.out.println("[SUCCESS] Decided to pay: " + topBill.name + " (" + topBill.amount + " JOD)");
                addLog("Paid: " + topBill.name);
            } else {
                String reason = (currentBalance < topBill.amount) ? "Insufficient funds" : "Emergency Reserve Protection";
                System.out.println("[ADVICE] Stop paying at: " + topBill.name + " due to " + reason);
                addLog("Halted at " + topBill.name + " (" + reason + ")");
                break;
            }
        }
        System.out.println(">>> Estimated remaining balance: " + currentBalance + " JOD");
    }

    public void undoLastAction() {
        if (!undoStack.isEmpty()) {
            UndoStep last = undoStack.pop();
            this.currentBalance = last.balanceBefore;
            pendingBills.add(last.bill);
            addLog("Undo payment for: " + last.bill.name);
            System.out.println("[UNDO] The payment for '" + last.bill.name + "' has been reversed.");
        } else {
            System.out.println("[!] No recent payments found to undo.");
        }
    }

    public void showDetailedHistory() {
        System.out.println("\n--- [ SYSTEM LOGS / TRANSACTION HISTORY ] ---");
        LogNode curr = headLog;
        if (curr == null) System.out.println("Log is empty.");
        while (curr != null) {
            System.out.print("[" + curr.action + "]");
            if (curr.next != null) System.out.print(" <-> ");
            curr = curr.next;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SmartCashFlowOptimizer engine = new SmartCashFlowOptimizer();

        // ترحيب البداية
        System.out.println("====================================================");
        System.out.println("   WELCOME TO: SMART CASH-FLOW OPTIMIZER v1.0   ");
        System.out.println("   Engineered by: Yomna & Noor (SWE Students)          ");
        System.out.println("====================================================");

        while (true) {
            try {
                System.out.println("\n--- [ MAIN CONTROL PANEL ] ---");
                System.out.println("A. [Add] Record a new financial obligation");
                System.out.println("B. [Run] Execute smart payment optimization");
                System.out.println("C. [Undo] Reverse the last payment decision");
                System.out.println("D. [History] View system logs & movement");
                System.out.println("E. [Exit] Shut down the system");
                System.out.print("Select an action (A-E): ");
                
                String choice = scanner.nextLine().toUpperCase();

                if (choice.equals("A")) {
                    System.out.println("\n>> Recording New Bill:");
                    System.out.print("   Description/Name: ");
                    String name = scanner.nextLine();
                    
                    System.out.print("   Amount (JOD): ");
                    double amt = Double.parseDouble(scanner.nextLine());
                    
                    System.out.print("   Due Date (YYYY-MM-DD): ");
                    LocalDate date = LocalDate.parse(scanner.nextLine());
                    
                    System.out.print("   Penalty Risk (1-10): ");
                    int penalty = Integer.parseInt(scanner.nextLine());
                    
                    System.out.print("   Is it Essential? (true/false): ");
                    boolean ess = Boolean.parseBoolean(scanner.nextLine());
                    
                    engine.addObligation(new FinancialObligation(name, amt, date, penalty, ess));
                    System.out.println("[System] Data captured successfully.");

                } else if (choice.equals("B")) {
                    System.out.print("\nEnter your current wallet balance to start: ");
                    double bal = Double.parseDouble(scanner.nextLine());
                    engine.runOptimization(bal);

                } else if (choice.equals("C")) {
                    engine.undoLastAction();

                } else if (choice.equals("D")) {
                    engine.showDetailedHistory();

                } else if (choice.equals("E")) {
                    System.out.println("Thank you for using Smart Cash-Flow Optimizer. Closing...");
                    break;
                } else {
                    System.out.println("[!] Unrecognized option. Please choose A, B, C, D, or E.");
                }
                
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Invalid numeric input. Please enter numbers only.");
            } catch (DateTimeParseException e) {
                System.out.println("[ERROR] Date format incorrect. Please use YYYY-MM-DD.");
            } catch (Exception e) {
                System.out.println("[ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }
}