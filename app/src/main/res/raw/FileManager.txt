import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Robert on 10/24/2016.
 */
public class FileManager {

	public Map<String, List<String>> getHeaders() {

		List<String> headers = null;
		
/*		File f = new File("bus_api_file1");
		try {
		    System.out.println(f.getCanonicalPath());
		} catch (IOException e) {
		    e.printStackTrace();
		}*/

		Map<String, List<String>> busFileMap = new HashMap<>();
		try {
			for (int i = 1; i <= 5; i++) {
				String fileName = "bus_api_file" + String.valueOf(i);
				String path = "C:\\Users\\Robert\\workspace_jee_rir\\Jose\\DataFiles\\" + fileName;
				BufferedReader br = new BufferedReader(new FileReader(path));
/*				br.readLine();
				String line = br.readLine();
				headers.add(line);*/
				headers = new ArrayList<>();
				String line;
				int count = 0;
				while((line = br.readLine()) != null /*&& count < 2*/){
					count++;
					headers.add(line);
				}
				busFileMap.put(fileName, headers);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return busFileMap;
	}
}
