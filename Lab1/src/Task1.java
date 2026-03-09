import java.util.Scanner;

public class Task1{
	
	
	public static void main(String[] args) {
		int userAge = 0;
		String userName = "";
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your name: ");
		userName = scanner.nextLine();
		System.out.println("Enter your age: ");
		userAge = scanner.nextInt();
		
		if (userAge == 16 || userAge == 17)
			System.out.println("Hello " + userName + "! You are eligible for the 16-17 Savers and 16-25 Railcard");
		else if (userAge >= 18 && userAge <=25)
			System.out.println("Hello " + userName + "! You are eligible for the 16-25 Railcard");
		else if (userAge >=26 && userAge <=30)
			System.out.println("Hello " + userName + "! You are eligible for the 26-30 Railcard");
		else if (userAge >= 60)
			System.out.println("Hello " + userName + "! You are eligible for the Senior Railcard");
		else
			System.out.println("Hello " + userName + "! Please check our website for non-age related railcards!");
		scanner.close();
	}
	
}