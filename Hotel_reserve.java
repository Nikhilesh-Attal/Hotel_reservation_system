import java.sql.*;
import java.util.Scanner;
import java.util.HashSet;
import java.util.stream.IntStream;

public class Hotel_reserve {

    static Scanner s= new Scanner(System.in);
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String name = "root";
    private static final String pass = "nik@9982Attal";
    static int[] Total_rooms = IntStream.concat(
            IntStream.concat(IntStream.concat(IntStream.range(1, 10), IntStream.range(101, 120)),
                    IntStream.range(201, 220)),
            IntStream.concat(IntStream.range(301, 320), IntStream.range(401, 420))
    ).toArray();

    static HashSet<Integer> Booked_rooms = new HashSet<>();

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Using try-with-resources to ensure connection is closed

            Connection con = DriverManager.getConnection(url, name, pass);

            String sql = "select room_no from reservation;";
            try(Statement sta = con.createStatement();
                ResultSet res_set = sta.executeQuery(sql)){
                int occupy_room = res_set.getInt("Room_no");
                while(res_set.next()){
                    Booked_rooms.add(occupy_room);
                }
            }

            System.out.println("Welcome to Hotel Continent");
            System.out.println("Connected to system..... ");
            int cho;
            while(true){
                System.out.println("1: Reserve a room");
                System.out.println("2: View reservation");
                System.out.println("3: Get room number");
                System.out.println("4: Update reservation");
                System.out.println("5: Delete reservation");
                System.out.println("0: Exit");
                System.out.print("Please choose one option:-");
                cho = s.nextInt();

                switch (cho){
                    case 0:exit();      /*or we can use :- System.exit(0)  break;*/
                        s.close();
                        return;
                    case 1: reserve_room(con);
                        break;
                    case 2: view_reservation(con);
                        break;
                    case 3: get_room_no(con);
                        break;
                    case 4: update_reservation(con);
                        break;
                    case 5: delete_reservation(con);
                        break;
                    default: System.out.println("Please enter valid input");
                        //break;
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        catch(Exception e){
            throw new RuntimeException(e);      //this exception is InterruptedException
        }
    }

    public static void reserve_room(Connection con){
        try{
            System.out.print("Enter guest name :- ");
            s.nextLine();
            String name = s.nextLine();
            System.out.print("Please enter room number :- ");
            int room_no = s.nextInt();
            System.out.print("Enter contact number :- ");
            long contact_no = s.nextLong();
            System.out.println("Customer Name: " + name);  // Debugging: Check if name is not null or empty

            int alloated_room = check_room(room_no);
            String sql = "Insert into Reservation(Customer_name, Room_no, Contect_no) values('"+ name + "',"+ alloated_room +","+ contact_no +");";
            //these commented statement can be use in place of PreparedStatement
           /* String sql = "INSERT INTO Reservation(Customer_name, Room_no, Contect_no) VALUES (?, ?, ?);";
           PreparedStatement pstmt = con.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setInt(2, room_no);
                pstmt.setLong(3, contact_no);
                int affectedRows = pstmt.executeUpdate();*/
            //to check correct room number and room number is not repeated
            /*if(room>001 || room
             */

            try(Statement sta = con.createStatement()){
                int affectedRows = sta.executeUpdate(sql);

                if(affectedRows >0)
                    System.out.println("Reservation Successfully....");
                else
                    System.out.println("Reservation failed....");
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        catch(Exception e){
            System.out.println("Error in reserve_room :- "+e);      //e.printStackTrace();
        }
    }

    public static void view_reservation(Connection con){
        String sql = "select * from reservation;";

        try(Statement sta = con.createStatement();
            ResultSet resultSet = sta.executeQuery(sql)){
            System.out.println("Current Reservations");
            System.out.println("+--------------+---------------+------------+----------------+--------------------------+");
            System.out.println("Reservation ID |Guest          |Room number |Contact number  |Reservation time          |");
            System.out.println("+--------------+---------------+------------+----------------+--------------------------+");

            while(resultSet.next()){
                int reser_ID = resultSet.getInt("Res_ID");
                String name = resultSet.getString("Customer_name");
                int room_no = resultSet.getInt("Room_no");
                long no = resultSet.getLong("Contect_no");
                String time = resultSet.getTimestamp("Res_time").toString();

                //Format and display the reservation data in a table like format
                System.out.printf("| %-12d | %-13s | %10d | %-14d | %-24s |\n", reser_ID, name, room_no, no, time);
            }
            System.out.println("+--------------+---------------+------------+----------------+--------------------------+");
        }
        catch(Exception e){
            System.out.println("Error in view_reservation :- "+e);
        }
    }

    private static void get_room_no(Connection con){
        try{
            System.out.print("Enter reservation ID :- ");
            int ID = s.nextInt();
            s.nextLine();
            System.out.print("Enter guest name :- ");
            String name = s.nextLine();
            System.out.println("Guest name : "+name);

            String sql = "SELECT room_no FROM Reservation WHERE res_Id = " + ID + " AND Customer_name = '" + name + "';";

            try(Statement sta = con.createStatement();
                ResultSet res_set = sta.executeQuery(sql)){

                if(res_set.next()){
                    int room = res_set.getInt("Room_no");
                    System.out.println("Room number for reservation ID " + ID + " and Guest '" + name + "' is: " + room);

                }
                else{
                    System.out.println("Reservation is not found for the given reservation ID "+ID +" and guest name "+name);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error in get_room_no :- "+e);
        }
    }

    public static void update_reservation(Connection con){
        try {
            System.out.print("Enter reservation ID to update");
            int reservationID = s.nextInt();
            //s.nextLine();

            if(!reservationExists(con, reservationID)){
                System.out.println("Reservation ID id not found for the given ID :- "+reservationID);
                return;
            }

            System.out.println("Enter new guest name ");
            s.nextLine();
            String new_name = s.nextLine();
            System.out.println("Enter new room number ");
            int new_room_no = s.nextInt();
            System.out.println("Enter new contact number");
            long new_con_no = s.nextLong();

            String sql = "update reservation set Customer_name = '"+new_name + "', room_no = "+new_room_no+ ", Contect_no = "+ new_con_no+" where Res_ID = " +reservationID+ ";";
            try(Statement sta = con.createStatement()){
                int affectedRow = sta.executeUpdate(sql);

                if(affectedRow>0){
                    System.out.println("Reservation update successfully....");
                }
                else{
                    System.out.println("Reservation update failed");
                }
            }
        }
        catch (Exception e) {
            //throw new RuntimeException(e);
            System.out.println("Error update_reservation :- "+e);
        }
    }

    public static void delete_reservation(Connection con){
        try{
            System.out.println("Enter reservation ID");
            int reservationID = s.nextInt();

            if(!reservationExists(con, reservationID)){
                System.out.println("Reservation for reservationID :- " +reservationID+ " is not found");
                return;
            }

            String sql = "delete from Reservation where Res_ID = " +reservationID+ ";";

            try(Statement sta= con.createStatement()){
                int affectedRow = sta.executeUpdate(sql);

                if(affectedRow>0){
                    System.out.println("Reservation is successfully deleted...");
                }
                else{
                    System.out.println("Reservation is not deleted....");
                }
            }
        }
        catch (SQLException e) {
            //throw new RuntimeException(e);
            System.out.println("Error in delete_reservation :- "+e);
        }
    }

    private static boolean reservationExists(Connection con, int reservationID){
        try{
            String sql = "select Res_ID from reservation where Res_ID = "+reservationID+ ";";

            try(Statement sta = con.createStatement();
                ResultSet res_set = sta.executeQuery(sql);){
                return res_set.next();      //if their exists a result means reservation
            }
        }
        catch(Exception e){
            System.out.println("Error in reservationExists :- "+e);      //e.printStackTrace();
            return false;
        }
    }

    public static void exit() throws InterruptedException{
        System.out.print("Existing System");
        int i=0;
        while(i<5){
            System.out.print(".");
            Thread.sleep(1000);
            i++;
        }
        System.out.println("\nThanking for visiting Hotel Management system....");
    }

    private static int check_room(int room_no){
        int room = room_no;

        if (isValidRoom(room_no, Total_rooms)) {
            if (Booked_rooms.contains(room_no)) {
                System.out.println("Sorry, this room is already booked.\n Please enter a new room number.");
                int new_room = s.nextInt();
                check_room(new_room);
            } else {
                Booked_rooms.add(room_no);
                System.out.println("Room " + room_no + " has been successfully booked.");
            }
        } else {
            System.out.println("Invalid room number.\n Please enter a valid room number.");
            int new_room = s.nextInt();
            check_room(new_room);
        }
        return room;
    }

    public static boolean isValidRoom(int room_no, int[] total_rooms) {
        for (int room : total_rooms) {
            if (room == room_no) {
                checkForAvailableRoom(room_no);
                return true;
            }
        }
        return false;
    }

    private static int checkForAvailableRoom(int room){
        int available_room;
        available_room = room;

        for(Integer i: Booked_rooms){
            if(i == room){
                System.out.println("Sorry room is already booked. \n Please enter a new room number");
                available_room = s.nextInt();
                checkForAvailableRoom(available_room);
            }
        }
        return available_room;
    }
}