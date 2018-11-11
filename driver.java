import java.util.Scanner;
class Driver {
    public static void main(String[] args){
    	
        while (true) {
        	Scanner input = new Scanner(System.in);
            System.out.print("Press 1 to go to Bank services \n"+
            	"Press 2 to quit\n" +
            	"Your Input:");
            try{
            	// receive the input
            	int number = input.nextInt();
            	// quit the function
            	if (number == 2){
            		System.out.println("Goodbye!");
            		break;
            	}
            	// go the log in
            	if (number == 1){
            		Bank newcustomer = new Bank();
            		newcustomer.servecustomer();
            	}
            }
            catch (Exception e){
            	System.out.println("Invalid Input, try again");
            }

        }
    }
}