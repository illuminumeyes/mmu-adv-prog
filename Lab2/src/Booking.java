import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Booking{
    private LocalDate startDate;
    private int numOfNights;
    private double costPerNight;

    public Booking(LocalDate date, int numOfNights, double costPerNight){
        this.startDate = date;
        this.numOfNights = numOfNights;
        this.costPerNight = costPerNight;
    }


    public LocalDate getStartDate(){
        return this.startDate;
    }

    public void setStartDate(LocalDate date){
        this.startDate = date;
    }

    public int getNumberOfNights(){
        return this.numOfNights;
    }

    public void setNumberOfNights(int nights){
        this.numOfNights = nights;
    }

    public double getCostPerNight(){
        return costPerNight;
    }

    public void setCostPerNight(double cost){
        this.costPerNight = cost;
    }

    public double getTotalCost(){
        return round((this.costPerNight * (double) this.numOfNights), 2);
    }


}