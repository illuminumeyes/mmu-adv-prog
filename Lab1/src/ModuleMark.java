public class ModuleMark{
	double _coursework1;
	double _coursework2;
	double avg;
	int numAssessments;
	
	public ModuleMark(double coursework1, double coursework2){
		this._coursework1 = coursework1;
		this._coursework2 = coursework2;
	}
	
	public double calcUnitGrade() {
		this.avg = (this._coursework1 + this._coursework2) / 2;
		return this.avg;
	}
	
	public String calcUnitClass() {
		if (this.avg < 40)
			return "Fail";
		else if (this.avg >=40 && this.avg <50)
			return "III";
		else if (this.avg >=50 && this.avg <60)
			return "II(II)";
		else if (this.avg >=60 && this.avg <70)
			return "II(I)";
		else 
			return "I";
	}
	
	
	
	public static void main(String[] args) {
		ModuleMark[] mm = new ModuleMark[4];
		
		mm[0]= new ModuleMark(59, 80);
		mm[1]= new ModuleMark(32, 68);
		mm[2]= new ModuleMark(71, 40);
		mm[3]= new ModuleMark(45, 91);
		
		for (int i=0; i < mm.length; i++) {
			System.out.println(mm[i].calcUnitGrade());
			System.out.println(mm[i].calcUnitClass());
		}
	}
}