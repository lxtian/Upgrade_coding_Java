import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.io.Console;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;


public class Bank {
    // database connection
    private Connection con = null;
    private PreparedStatement stmt = null;
    private ResultSet rs = null;

    int initialize_connection(){
        try{

            con = DriverManager.getConnection("jdbc:sqlite:Bank.db");
        }
        catch (Exception e){
            return 1;
        }
        return 0;
    }

    public void servecustomer(){
        if (initialize_connection() == 1){
            System.out.println("Sever error, try again");
            return;
        }
            
        while (true){
            Scanner input = new Scanner(System.in);
            System.out.print("Press 1 to create new account\n" +
                "Press 2 to log in account\n" + 
                "Press 3 to quit\n" +
                "Your Input:");
            try{
                int number = input.nextInt();
                if (number == 1){
                    create_account();
                }
                else if (number == 2){
                    login();
                }
                else if (number == 3){
                    break;
                }
            }
            catch (Exception e){
                System.out.println("Invalid Input, try again");
            }
        }
    }

    private String help_hash(String password){
        try{
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            byte[] messageDigestMD5 = messageDigest.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (byte bytes : messageDigestMD5) {
                stringBuffer.append(String.format("%02x", bytes & 0xff));
            }
            return stringBuffer.toString();
        }
        catch(Exception e){
            return null;
        }
    }

    private void login(){
        Console console = System.console();
        Scanner input = new Scanner(System.in);
        System.out.print("Enter you username:");
        String username = input.next();
        // keep logging in process
        String password = new String(console.readPassword("Please enter your password: "));
        // check password from database
        try{
            password = help_hash(password);
            // check from database
            stmt = con.prepareStatement("select password from User where username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            String return_password = rs.getString("password");
            if ( !return_password.equals(password)){
                System.out.println("Authentication Failure, please try again");
                return;
            }
            stmt.close();
        }
        catch(Exception e){
            System.out.println("Authentication Failure, please try again");
            return;
        }      
        // to this step means log in successfully
        // redirect to account information
        try{
            stmt = con.prepareStatement("INSERT INTO UserLog VALUES(?)");
            stmt.setString(1, username);
            stmt.executeUpdate();
            stmt.close();
        }
        catch(Exception e){
            System.out.println("Not allowed to log in the same account at the same time!");
            return;
        }
        System.out.println("Login successful");
        Account_description(username);

    }

    private  void Account_description(String username){
        float amount = 0;
        while(true){
            // display account amount
            try{
                stmt = con.prepareStatement("select amount from Account where username = ?");
                stmt.setString(1, username);
                rs = stmt.executeQuery();
                amount = rs.getFloat("amount");
                stmt.close();
                System.out.println();
                System.out.println("Account " + username + " amount: " + Float.toString(amount));
            }
            catch(Exception e){
                System.out.println("Account information retrive fail, retry");
                break;
            }
            // account choices
            Scanner input = new Scanner(System.in);
            System.out.print("Press 1 to deposit\n" +
                "Press 2 to withdraw\n" + 
                "Press 3 to transfer to other account\n" +
                "Press 4 to retrieve recent transaction record\n" +
                "Press 5 to log out\n" +
                "Your Input:");
            try{
                int number = input.nextInt();
                System.out.println();
                if (number == 1){
                    deposit_into_account(username);
                }
                else if (number == 2){
                    withdraw_from_account(username);
                }
                else if (number == 3){
                    transfer_to_other(username);
                }
                else if (number == 4){
                    retrieve_account_transaction(username);
                }
                // log out
                else if (number == 5){
                    try{
                        stmt = con.prepareStatement("DELETE FROM UserLog WHERE username = ?");
                        stmt.setString(1, username);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                    catch(Exception e){
                        System.out.println("Logout Failure! Ask account manager to manually log out");
                    }
                    break;
                }
            }
            catch (Exception e){
                System.out.println("Invalid Input, try again");
            }
        }
    }


    private void deposit_into_account(String username){
        try{
            Scanner input = new Scanner(System.in);
            System.out.print("Type the deposit amount:");
            // amount is the amount on the account
            float amount = input.nextFloat();
            if (amount < 0){
                System.out.println("Amount must be positive");
                return;
            }
            // update the account
            stmt = con.prepareStatement("Update Account Set amount = amount + ? where username = ?");
            stmt.setFloat(1, amount);
            stmt.setString(2, username);
            stmt.executeUpdate();
            stmt.close();
            // Insert into transaction
            // create timestamp
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            stmt = con.prepareStatement("INSERT INTO TransactionRecord VALUES(?, ?, ?, ?)");
            stmt.setString(1, username);
            stmt.setTimestamp(2, ourJavaTimestampObject);
            stmt.setString(3, "D");
            stmt.setFloat(4, amount);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (Exception e){
            System.out.println(e);
            System.out.println("Invalid Input, try again");
        }
    }

    private void withdraw_from_account(String username){
        float amount = 0;
        try{
            Scanner input = new Scanner(System.in);
            System.out.print("Type the withdraw amount:");
            float withdrawamount = input.nextFloat();
            // get current amount
            try{
                stmt = con.prepareStatement("select amount from Account where username = ?");
                stmt.setString(1, username);
                rs = stmt.executeQuery();
                amount = rs.getFloat("amount");
                stmt.close();
            }
            catch(Exception e){
                System.out.println("Account information retrive fail, retry");
                return;
            }
            // withdraw money
            if (withdrawamount < 0){
                System.out.println("Can't withdraw negative number");
                return;
            }
            if (withdrawamount > amount){
                System.out.println("Can't withdraw more than what you have");
                return;
            }
            float new_amount = amount - withdrawamount;
            // update account
            stmt = con.prepareStatement("Update Account Set amount = ? where username = ?");
            stmt.setFloat(1, new_amount);
            stmt.setString(2, username);
            stmt.executeUpdate();
            stmt.close();
            // insert into transaction
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            stmt = con.prepareStatement("INSERT INTO TransactionRecord VALUES(?, ?, ?, ?)");
            stmt.setString(1, username);
            stmt.setTimestamp(2, ourJavaTimestampObject);
            stmt.setString(3, "W");
            stmt.setFloat(4, withdrawamount);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (Exception e){
            System.out.println("Invalid Input, try again");
        }
    }

    private void transfer_to_other(String username){
        float amount = 0;
        System.out.print("Type the account username to transfer to:");
        try{
            Scanner input = new Scanner(System.in);
            String t_username = input.next();
            // same username
            if (t_username.equals(username)){
                System.out.println("Not allowed to transfer to yourself");
                return;
            }
            // check in database whether the username exists
            stmt = con.prepareStatement("select count(*) from User where username = ?");
            stmt.setString(1, t_username);
            rs = stmt.executeQuery();
            int count = rs.getInt("count(*)");
            stmt.close();
            // doesn't exist
            if (count == 0){
                System.out.println("Account " + t_username + " doesn't exist");
                return;
            }
            // can transfer
            System.out.print("Type the amount to transfer out:");
            float amount_out = input.nextFloat();

            // get current amount
            try{
                stmt = con.prepareStatement("select amount from Account where username = ?");
                stmt.setString(1, username);
                rs = stmt.executeQuery();
                amount = rs.getFloat("amount");
                stmt.close();
            }
            catch(Exception e){
                System.out.println("Account information retrive fail, retry");
                return;
            }

            // amount out check
            if (amount_out < 0){
                System.out.println("Can't transfer out a negative number");
                return;
            }

            if (amount_out > amount){
                System.out.println("Can't transfer out more than what you have");
                return;
            }
            float new_amount = amount - amount_out;
            // update user account
            stmt = con.prepareStatement("Update Account Set amount = ? where username = ?");
            stmt.setFloat(1, new_amount);
            stmt.setString(2, username);
            stmt.executeUpdate();
            stmt.close();
            // insert into transaction
            Calendar calendar = Calendar.getInstance();
            java.sql.Timestamp ourJavaTimestampObject = new java.sql.Timestamp(calendar.getTime().getTime());
            stmt = con.prepareStatement("INSERT INTO TransactionRecord VALUES(?, ?, ?, ?)");
            stmt.setString(1, username);
            stmt.setTimestamp(2, ourJavaTimestampObject);
            stmt.setString(3, "TO");
            stmt.setFloat(4, amount_out);
            stmt.executeUpdate();
            stmt.close();
            // update transfer in account
            stmt = con.prepareStatement("Update Account Set amount = amount + ? where username = ?");
            stmt.setFloat(1, amount_out);
            stmt.setString(2, t_username);
            stmt.executeUpdate();
            stmt.close();
            // insert into transaction
            java.sql.Timestamp ourJavaTimestampObject2 = new java.sql.Timestamp(calendar.getTime().getTime());
            stmt = con.prepareStatement("INSERT INTO TransactionRecord VALUES(?, ?, ?, ?)");
            stmt.setString(1, t_username);
            stmt.setTimestamp(2, ourJavaTimestampObject2);
            stmt.setString(3, "TI");
            stmt.setFloat(4, amount_out);
            stmt.executeUpdate();
            stmt.close();
        }
        catch (Exception e){
            System.out.println("Invalid Input, try again");
        }
    }

    private void retrieve_account_transaction(String username){
        try{
            Scanner input = new Scanner(System.in);
            System.out.print("Type the N transaction you want to see:");
            int number = input.nextInt();
            // select transactions from database
            stmt = con.prepareStatement("select * from TransactionRecord where username = ? order by transactime DESC limit ?");
            stmt.setString(1, username);
            stmt.setInt(2, number);
            rs = stmt.executeQuery();
            System.out.println("Date                       \tType\tAmount");
            while (rs.next()) {
                String transactype = rs.getString("Type");
                // get timestamp and convert to date
                Timestamp transactiontime = rs.getTimestamp("transactime");
                Date date = new Date(transactiontime.getTime());
                float transactionamount = rs.getFloat("amount");
                System.out.println(date + "\t" + transactype + "\t" + Float.toString(transactionamount));
            }
            stmt.close();
        }
        catch (Exception e){
            System.out.println("Invalid Input, try again");
        }
    }

    private void create_account(){
        Scanner input = new Scanner(System.in);
        Console console = System.console();
        // store the hash value as password
        System.out.print("Create your username:");
        String username = input.next();
        // check in database whether this is a unique username
        try{
            stmt = con.prepareStatement("select count(*) from User where username = ?");
            stmt.setString(1, username);
            rs = stmt.executeQuery();        
            int res = rs.getInt("count(*)");
            stmt.close();
            // not unique
            if (res > 0){
                System.out.println("username not unique, please retry");
                return;
            }
         }
        catch(Exception e){
            System.out.println("connection lost, try again later");
            return;
        }


        // hide input
        String password = new String(console.readPassword("Please enter your password: "));
        try{
            password = help_hash(password);
            stmt = con.prepareStatement("INSERT INTO User VALUES(?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            stmt = con.prepareStatement("INSERT INTO Account VALUES(?, ?)");
            stmt.setString(1, username);
            stmt.setFloat(2, 0);
            stmt.executeUpdate();
            stmt.close();
        }
        catch(Exception e){
            System.out.println("password create fail, try again later");
            return;
        }
    }
}
