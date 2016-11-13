import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Robert on 10/21/2016.
 */
public class RIPTAAPIConsolidation {

	public static void main(String[] args) {
		Map<String, List<String>> busFiles = getBusFile();
		Map<String, List<String>> headerMap = getHeader(busFiles);
		long startBodyMap = System.currentTimeMillis();
		Map<String, Map<Integer, List<String>>> bodyMap = getBody(busFiles);
		long endBodyMap = System.currentTimeMillis();
		long totalBodyMap = (endBodyMap - startBodyMap)/1000;
		System.out.println("startBodyMap " + startBodyMap);
		System.out.println("endBodyMap " + endBodyMap);
		System.out.println("totalBodyMap " + totalBodyMap);
		String sacredBleu = "";
	}

	private static Map<String, List<String>> getHeader(
			Map<String, List<String>> busFiles) {
		Map<String, List<String>> headerMap = new HashMap<>();
		int headerCount = 0;
		List<String> headerList = null;
		for (List<String> busFile : busFiles.values()) {
			headerList = new ArrayList<>();
			String[] titleArray = busFile.get(0).split(",");
			for (int i = 0; i < titleArray.length; i++) {
				headerList.add(titleArray[i]);
			}
			headerMap.put("header" + String.valueOf(headerCount), headerList);
			headerCount++;
		}

		return headerMap;
	}

	private static Map<String, Map<Integer, List<String>>> getBody(
			Map<String, List<String>> busFiles) {
		Map<String, Map<Integer, List<String>>> bodyMap = new HashMap<>();
		Map<Integer, List<String>> listMap = null;
		int bodyCount = 0;
		List<String> bodyList = null;
		for (List<String> busFile : busFiles.values()) {
			
			listMap = new HashMap<>();
			int fileCount = 0;
			for (String body : busFile) {
				bodyList = new ArrayList<>();
				if (fileCount > 0) {
					String[] bodyArray = body.split(",");
					for (int i = 0; i < bodyArray.length; i++) {
						bodyList.add(bodyArray[i]);
					}
					listMap.put(fileCount, bodyList);
				}
				fileCount++;
			}

			bodyMap.put("header" + String.valueOf(bodyCount), listMap);
			bodyCount++;
		}
		return bodyMap;
	}

	public static Map<String, List<String>> getBusFile() {
		List<String> headers = null;
		Map<String, List<String>> busFileMap = new HashMap<>();
		try {
			for (int i = 1; i <= 5; i++) {
				String fileName = "bus_api_file" + String.valueOf(i);
				String path = "C:\\Users\\Robert\\workspace_jee_rir\\Jose\\DataFiles\\"
						+ fileName;
				BufferedReader br = new BufferedReader(new FileReader(path));
				String line;
				int count = 0;
				headers = new ArrayList<>();
				while ((line = br.readLine()) != null /* && count < 2 */) {
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

	private static List<String> getOverlappingTitles(List<String> titles) {
		List<String> overlappingTitleList = new ArrayList<>();
		Set<String> overlappingTitleSet = new HashSet<>();
		for (int i = 1; i < titles.size(); i++) {
			String nextTitle = titles.get(i);
			if (!overlappingTitleSet.add(nextTitle)) {
				overlappingTitleList.add(nextTitle);
			}
		}
		return overlappingTitleList;
	}
}
