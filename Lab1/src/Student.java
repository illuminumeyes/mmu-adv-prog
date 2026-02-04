public class Student{
	String name;
	ModuleMark[] marks;
	double overallScore;
	
	public Student(String name) {
		this.name = name;
	}
	public void setMarks(ModuleMark[] marks) {
		this.marks = marks;
	}
	public double calcOverallGrade() {
		double totalGrade = 0;
		for (ModuleMark mm : marks) {
			totalGrade += mm.calcUnitGrade();
		}
		this.overallScore = totalGrade;
		return this.overallScore/marks.length;
	}
	
	public String calcOverallClassification() {
		if (this.overallScore < 40)
			return "Fail";
		else if (this.overallScore >=40 && this.overallScore <50)
			return "III";
		else if (this.overallScore >=50 && this.overallScore <60)
			return "II(II)";
		else if (this.overallScore >=60 && this.overallScore <70)
			return "II(I)";
		else 
			return "I";
	}
	
	
	
	public static void main(String[] args) {
		
	}
}