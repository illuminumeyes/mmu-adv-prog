import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



public class BookingManager{
    private List<Booking> bookings;

    public BookingManager(){}


    public void addBooking(Booking booking){
        this.bookings.add(booking);
    }

    public int getNumberOfBookings(){
        return this.bookings.size();
    }

    public double getTotalBookingValue(){
        double val = 0;
        for (Booking booking:this.bookings){
            val =+ booking.getTotalCost();
        }
        return val;
    }

    public void clearBookings(){
        // Find size of list, (i - 1) is last element of list, remove said element and decrement
        for (int i = this.booking.size(), i > 0, i--){
            this.booking.remove(i-1);
        }
    }

    public List<Booking> getAllBookings(){
        return this.bookings;
    }

    public List<Booking> getAllBookingsInMonth(String month){
        LocalDate date = null;

        // List of Formats to cover different string inputs, whether that's a number char
        // Or a short form month or long form month
        DateTimeFormatter[] formatters = {
        DateTimeFormatter.ofPattern("dd MMM yyyy"),      // Short: 01 Aug 2026
        DateTimeFormatter.ofPattern("dd MMMM yyyy"),     // Long: 01 August 2026
        DateTimeFormatter.ofPattern("dd M yyyy"),        // Integer: 01 8 2026
        DateTimeFormatter.ofPattern("dd MM yyyy")        // Padded: 01 08 2026
        };
        for (DateTimeFormatter formatter : formatters){
            try{
            String monthToParse = "01 " + month + " 2026";
            date = LocalDate.parse(monthToParse, formatter);
            break;
            }
            catch(DateTimeParseException e) {
                // Catch exception and go to next formatter
            }
        }

        List<Booking> bookingsInMonth = new ArrayList<Booking>();
        for (Booking booking:this.bookings){
            if (date.getMonth() == booking.getStartDate().getMonth()){
                bookingsInMonth.add(booking);
            }
        }
    }
    return bookingsInMonth;

}