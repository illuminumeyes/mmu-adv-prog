import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.*;


class CopyFiles{
	static String cwd = "/home/b47633cw/git/software-eng-degree/Y2/adv-prog/Week7/src/txt_files/";
	public static void main(String[] args) throws IOException {
			copyWebURL();
		
		}
	
	public static void copyLines() throws IOException {
		try (BufferedReader br = Files.newBufferedReader(Paths.get(cwd+"copyLines.txt"))){
			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(cwd+"output.txt"))){
				String line;
				while ((line=br.readLine())!= null) {
					if (line.charAt(0) == '+'){
						bw.write(line);
						bw.newLine();
					}
				}
			}
		}
		catch (IOException ioe) {
			System.err.println("IO Exception: " + ioe);
		}
				
	}
	
	
	public static void copyWebURL() throws IOException {
		
		try {
			URI uri = new URI("http://www.kriswelsh.com");
			URLConnection con = uri.toURL().openConnection();
			try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))){
				String line = "";
				while((line = br.readLine())!= null) {
					System.out.println(line);
				}
			}
		}
		catch (IOException | URISyntaxException ioe) {
			ioe.printStackTrace();
		}
				
	}
}