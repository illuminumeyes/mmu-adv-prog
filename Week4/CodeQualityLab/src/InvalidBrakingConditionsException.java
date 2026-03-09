
public class InvalidBrakingConditionsException extends Exception {
	//don't bother JavaDoccing this property, I didn't teach serialisation to you yet...
	private static final long serialVersionUID = 6409338264440564814L;

	public InvalidBrakingConditionsException(String reason) {
		super(reason);
	}

}
