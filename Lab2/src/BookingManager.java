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
        
    }


}