package food2fork.com.findmyrecipe.exceptions;

/*
 * Representerer en exception som aldri skal forekomme, fordi det medf�rer at app'en har en bug.
 */
public class AppErrorException extends Exception {

	public AppErrorException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
