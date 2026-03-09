
public class Main {

	public static void main(String[] args) {
		try {
			BrakingDistanceEstimator calc = new BrakingDistanceEstimator(
					BrakingConditions.WEATHER_DRY,
					BrakingConditions.LOADING_DRIVER_PASSENGER,
					BrakingConditions.BRAKES_COLD
			);
			
			int[] speeds = { 20, 40, 60, 80, 100 };
			for(int i=0;i<speeds.length;i++) {
				System.out.println("Braking distance for: " + speeds[i] + "kph: " + calc.getBrakingDistanceFor(speeds[i]) + "m");
			}
		} catch (InvalidBrakingConditionsException e) {
			e.printStackTrace();
		}
	}

}
