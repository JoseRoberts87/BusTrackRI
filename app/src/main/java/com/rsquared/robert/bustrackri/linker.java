package com.rsquared.robert.bustrackri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Linker {

	public static void main(String[] args) {
		long startMain = System.currentTimeMillis();
		String folderPath = "C:\\Users\\R^2\\AndroidStudioProjects\\BusTrackRI\\app\\src\\main\\res\\raw\\google_transit";
		List<String> filePathList = getFilePathList(folderPath);
		/*
		 * Map<String, List<String>> fileDataMap = readFiles(filePathList);
		 * Map<String, String[]> headerMap = getHeaderMap(fileDataMap);
		 * 
		 * // long startBodyMap = System.currentTimeMillis(); Map<String,
		 * Map<Integer, String[]>> bodyMap = getBodyMap(fileDataMap); String[]
		 * headerArray = (String[]) headerMap.values().toArray();
		 * 
		 * List<String> duplicateHeader = getOverlappingTitles(headerArray);
		 */
		/*
		 * long endBodyMap = System.currentTimeMillis(); long totalBodyMap =
		 * (endBodyMap - startBodyMap)/1000;
		 */

		/*
		 * long endMain = System.currentTimeMillis(); long totalMain = (endMain
		 * - startMain)/1000;
		 * 
		 * System.out.println("startBodyMap " + startBodyMap);
		 * System.out.println("endBodyMap " + endBodyMap);
		 * System.out.println("totalBodyMap " + totalBodyMap);
		 * System.out.println("startMain " + startMain);
		 * System.out.println("endMain " + endMain);
		 * System.out.println("totalMain " + totalMain);
		 */

		String[] tripIDs = getTripIDs();

		Map<String, List<String>> fileDataMap = readTheFiles(filePathList);
		Map<String, String[]> headerMap = getHeaderMap(fileDataMap);
		Map<String, Map<Integer, String[]>> bodyMap = getBodyMap(fileDataMap);

		List<String[]> fieldInBody = getFieldInBody(bodyMap, 3);
		
//		String [] stopsIDs = getStopIds(fieldInBody);
		/*
		 * for(int i = 0; i < fieldInBody.size(); i++){
		 * System.out.println(fieldInBody.get(i)); }
		 */

//		writeFieldsToFile(fieldInBody);
		long endMain = System.currentTimeMillis();
		long totalMain = (endMain - startMain) / 1000;
		/*
		 * System.out.println("startMain " + startMain);
		 * System.out.println("endMain " + endMain);
		 * System.out.println("totalMain " + totalMain);
		 */

	}

	private static String[] getStopIds(List<String[]> fieldInBody) {
		
		Set<Object> nonDuplicateIDs = new HashSet<>();
		
		for(int i = 0; i < fieldInBody.size(); i++){
			if(nonDuplicateIDs.add(fieldInBody.get(i)[3])){
				System.out.println(fieldInBody.get(i)[3]);
			}
		}
		
		return (String[]) nonDuplicateIDs.toArray();
	}

	private static void writeFieldsToFile(List<String[]> fieldInBody) {

		FileWriter fw;
		try {
			fw = new FileWriter("file.txt");
			for (int i = 0; i < fieldInBody.size(); i++) {
				fw.write(Arrays.toString(fieldInBody.get(i)) + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String[]> getFieldInBody(Map<String, Map<Integer, String[]>> bodyMap, int index) {

		List<String[]> fieldsInBody = new ArrayList<>();
		Set<String[]> fieldsInBodySet = new HashSet<>();
		Map<Integer, String[]> fieldsMap = bodyMap.get("bus_api_file2.txt");

		String[] tripIDs = getTripIDs();
		String[] stopIDs = getStopIDs();
		
		Set<Object> nonDuplicateIDs = new HashSet<>();
		
/*		for (int i = 0; i < tripIDs.length; i++) {

			for (int j = 0; j < fieldsMap.size(); j++) {
				String[] fields = fieldsMap.get(Integer.valueOf(j));
				if (fields != null && fields[index].trim().equalsIgnoreCase(tripIDs[i].trim())) {
					if(fieldsInBodySet.add(fieldsMap.get(j))){
//						System.out.println(Arrays.toString(fieldsMap.get(j)));
					}
					if(nonDuplicateIDs.add(fieldsMap.get(j)[3])){
//						System.out.println(fieldsMap.get(j)[3]);
					}
				}
			}
		}*/
		int array1[][]= new int[5][5];
		int array2[][]= new int[5][5];

		int totalAmountOfDifferences = 0;

		for(int i = 0; i < array1[i].length; i++){
		    totalAmountOfDifferences =+ diffArray(array1[i], array2[i]);
		}

//		diffArray(array1[], array2[])
		
		for (int i = 0; i < stopIDs.length; i++) {

			for (int j = 0; j < fieldsMap.size(); j++) {
				String[] fields = fieldsMap.get(Integer.valueOf(j));
				if (fields != null && fields[index].trim().equalsIgnoreCase(stopIDs[i].trim())) {
					if(fieldsInBodySet.add(fieldsMap.get(j))){
						System.out.println(Arrays.toString(fieldsMap.get(j)));
					}
					if(nonDuplicateIDs.add(fieldsMap.get(j)[3])){
//						System.out.println(fieldsMap.get(j)[3]);
					}
				}
			}
		}
		fieldsInBody.addAll(fieldsInBodySet);
		return fieldsInBody;
	}

	private static int diffArray(int[] is, int[] is2) {
		int totalAmountOfDifferences = 0;
		return totalAmountOfDifferences;
	}

	private static String[] getTripIDs() {

		String[] tripIDArray = { "	1984242-SE2014-hsep1407-Sunday-01_merged_983837	",
				"	1984244-SE2014-hsep1407-Sunday-01_merged_988242	", "	1989798-SE2014-hsep1401-Weekday-02_merged_989291	",
				"	1989812-SE2014-hsep1401-Weekday-02_merged_986531	", "	1984255-SE2014-hsep1407-Sunday-01_merged_991061	",
				"	1989805-SE2014-hsep1401-Weekday-02_merged_985397	", "	1989807-SE2014-hsep1401-Weekday-02_merged_985896	",
				"	1984241-SE2014-hsep1407-Sunday-01_merged_988742	", "	1989794-SE2014-hsep1401-Weekday-02_merged_982902	",
				"	1989846-SE2014-hsep1401-Weekday-02_merged_986897	", "	1989797-SE2014-hsep1401-Weekday-02_merged_984211	",
				"	1989801-SE2014-hsep1401-Weekday-02_merged_991149	", "	1989815-SE2014-hsep1401-Weekday-02_merged_989658	",
				"	1989787-SE2014-hsep1401-Weekday-02_merged_984127	", "	1982624-SE2014-hsep1406-Saturday-01_merged_981735	",
				"	1984263-SE2014-hsep1407-Sunday-01_merged_984039	", "	1989807-SE2014-hsep1401-Weekday-02_merged_980244	",
				"	1984235-SE2014-hsep1407-Sunday-01_merged_989681	", "	1982614-SE2014-hsep1406-Saturday-01_merged_990902	",
				"	1982596-SE2014-hsep1406-Saturday-01_merged_987461	", "	1984248-SE2014-hsep1407-Sunday-01_merged_990820	",
				"	1984240-SE2014-hsep1407-Sunday-01_merged_991096	", "	1989826-SE2014-hsep1401-Weekday-02_merged_989403	",
				"	1989814-SE2014-hsep1401-Weekday-02_merged_990951	", "	1989825-SE2014-hsep1401-Weekday-02_merged_980443	",
				"	1989810-SE2014-hsep1401-Weekday-02_merged_989989	", "	1989801-SE2014-hsep1401-Weekday-02_merged_985497	",
				"	1982624-SE2014-hsep1406-Saturday-01_merged_987387	", "	1989803-SE2014-hsep1401-Weekday-02_merged_981458	",
				"	1989843-SE2014-hsep1401-Weekday-02_merged_984622	", "	1984234-SE2014-hsep1407-Sunday-01_merged_988126	",
				"	1982596-SE2014-hsep1406-Saturday-01_merged_981809	", "	1989827-SE2014-hsep1401-Weekday-02_merged_991332	",
				"	1989835-SE2014-hsep1401-Weekday-02_merged_981023	", "	1989832-SE2014-hsep1401-Weekday-02_merged_983509	",
				"	1984258-SE2014-hsep1407-Sunday-01_merged_988033	", "	1989828-SE2014-hsep1401-Weekday-02_merged_983859	",
				"	1984251-SE2014-hsep1407-Sunday-01_merged_981986	", "	1990452-SE2014-hsep1401-Weekday-02_merged_990453	",
				"	1982614-SE2014-hsep1406-Saturday-01_merged_985250	", "	1989808-SE2014-hsep1401-Weekday-02_merged_988072	",
				"	1989788-SE2014-hsep1401-Weekday-02_merged_986281	", "	1989833-SE2014-hsep1401-Weekday-02_merged_987835	",
				"	1982618-SE2014-hsep1406-Saturday-01_merged_987718	", "	1989780-SE2014-hsep1401-Weekday-02_merged_983736	",
				"	1989790-SE2014-hsep1401-Weekday-02_merged_990178	", "	1984251-SE2014-hsep1407-Sunday-01_merged_987638	",
				"	1989824-SE2014-hsep1401-Weekday-02_merged_988332	", "	1982601-SE2014-hsep1406-Saturday-01_merged_983972	",
				"	1989802-SE2014-hsep1401-Weekday-02_merged_981540	", "	1982599-SE2014-hsep1406-Saturday-01_merged_982941	",
				"	1989794-SE2014-hsep1401-Weekday-02_merged_988554	", "	1989786-SE2014-hsep1401-Weekday-02_merged_985022	",
				"	1982604-SE2014-hsep1406-Saturday-01_merged_982132	", "	1989842-SE2014-hsep1401-Weekday-02_merged_986320	",
				"	1984262-SE2014-hsep1407-Sunday-01_merged_981278	", "	1982627-SE2014-hsep1406-Saturday-01_merged_991188	",
				"	1982620-SE2014-hsep1406-Saturday-01_merged_991036	", "	1984243-SE2014-hsep1407-Sunday-01_merged_981611	",
				"	1984237-SE2014-hsep1407-Sunday-01_merged_980155	", "	1989811-SE2014-hsep1401-Weekday-02_merged_987944	",
				"	1984261-SE2014-hsep1407-Sunday-01_merged_988382	", "	1984252-SE2014-hsep1407-Sunday-01_merged_986386	",
				"	1989791-SE2014-hsep1401-Weekday-02_merged_989126	", "	1989783-SE2014-hsep1401-Weekday-02_merged_989573	",
				"	1989813-SE2014-hsep1401-Weekday-02_merged_982026	", "	1989795-SE2014-hsep1401-Weekday-02_merged_988303	",
				"	1984235-SE2014-hsep1407-Sunday-01_merged_984029	", "	1989841-SE2014-hsep1401-Weekday-02_merged_986550	",
				"	1982628-SE2014-hsep1406-Saturday-01_merged_989053	", "	1982611-SE2014-hsep1406-Saturday-01_merged_986832	",
				"	1984264-SE2014-hsep1407-Sunday-01_merged_980150	", "	1982619-SE2014-hsep1406-Saturday-01_merged_985507	",
				"	1989809-SE2014-hsep1401-Weekday-02_merged_982353	", "	1989833-SE2014-hsep1401-Weekday-02_merged_982183	",
				"	1989800-SE2014-hsep1401-Weekday-02_merged_984115	", "	1989811-SE2014-hsep1401-Weekday-02_merged_982292	",
				"	1982593-SE2014-hsep1406-Saturday-01_merged_989290	", "	1989831-SE2014-hsep1401-Weekday-02_merged_985570	",
				"	1989812-SE2014-hsep1401-Weekday-02_merged_980879	", "	1984250-SE2014-hsep1407-Sunday-01_merged_985455	",
				"	1989837-SE2014-hsep1401-Weekday-02_merged_980464	", "	1984247-SE2014-hsep1407-Sunday-01_merged_990229	",
				"	1982604-SE2014-hsep1406-Saturday-01_merged_987784	", "	1982591-SE2014-hsep1406-Saturday-01_merged_989776	",
				"	1982615-SE2014-hsep1406-Saturday-01_merged_991330	", "	1982616-SE2014-hsep1406-Saturday-01_merged_984148	",
				"	1984243-SE2014-hsep1407-Sunday-01_merged_987263	", "	1982607-SE2014-hsep1406-Saturday-01_merged_981980	",
				"	1984238-SE2014-hsep1407-Sunday-01_merged_985961	", "	1982600-SE2014-hsep1406-Saturday-01_merged_981394	",
				"	1982621-SE2014-hsep1406-Saturday-01_merged_984166	", "	1989834-SE2014-hsep1401-Weekday-02_merged_991081	",
				"	1982613-SE2014-hsep1406-Saturday-01_merged_982477	", "	1982617-SE2014-hsep1406-Saturday-01_merged_984371	",
				"	1984254-SE2014-hsep1407-Sunday-01_merged_982758	", "	1989780-SE2014-hsep1401-Weekday-02_merged_989388	",
				"	1989830-SE2014-hsep1401-Weekday-02_merged_988429	", "	1989804-SE2014-hsep1401-Weekday-02_merged_983135	",
				"	1982593-SE2014-hsep1406-Saturday-01_merged_983638	", "	1984260-SE2014-hsep1407-Sunday-01_merged_989769	",
				"	1984253-SE2014-hsep1407-Sunday-01_merged_987936	", "	1989844-SE2014-hsep1401-Weekday-02_merged_986090	",
				"	1984255-SE2014-hsep1407-Sunday-01_merged_985409	", "	1989820-SE2014-hsep1401-Weekday-02_merged_988999	",
				"	1989813-SE2014-hsep1401-Weekday-02_merged_987678	", "	1989790-SE2014-hsep1401-Weekday-02_merged_984526	",
				"	1982607-SE2014-hsep1406-Saturday-01_merged_987632	", "	1982616-SE2014-hsep1406-Saturday-01_merged_989800	",
				"	1982612-SE2014-hsep1406-Saturday-01_merged_980546	", "	1989816-SE2014-hsep1401-Weekday-02_merged_987057	",
				"	1989815-SE2014-hsep1401-Weekday-02_merged_984006	", "	1989824-SE2014-hsep1401-Weekday-02_merged_982680	",
				"	1989788-SE2014-hsep1401-Weekday-02_merged_980629	", "	1982628-SE2014-hsep1406-Saturday-01_merged_983401	",
				"	1982610-SE2014-hsep1406-Saturday-01_merged_982827	", "	1989810-SE2014-hsep1401-Weekday-02_merged_984337	",
				"	1989837-SE2014-hsep1401-Weekday-02_merged_986116	", "	1984252-SE2014-hsep1407-Sunday-01_merged_980734	",
				"	1989830-SE2014-hsep1401-Weekday-02_merged_982777	", "	1989808-SE2014-hsep1401-Weekday-02_merged_982420	",
				"	1982601-SE2014-hsep1406-Saturday-01_merged_989624	", "	1989804-SE2014-hsep1401-Weekday-02_merged_988787	",
				"	1984261-SE2014-hsep1407-Sunday-01_merged_982730	", "	1989823-SE2014-hsep1401-Weekday-02_merged_987427	",
				"	1984256-SE2014-hsep1407-Sunday-01_merged_980997	", "	1982592-SE2014-hsep1406-Saturday-01_merged_985448	",
				"	1984240-SE2014-hsep1407-Sunday-01_merged_985444	", "	1982605-SE2014-hsep1406-Saturday-01_merged_988125	",
				"	1989806-SE2014-hsep1401-Weekday-02_merged_980312	", "	1982595-SE2014-hsep1406-Saturday-01_merged_991254	",
				"	1984263-SE2014-hsep1407-Sunday-01_merged_989691	", "	1989785-SE2014-hsep1401-Weekday-02_merged_990428	",
				"	1982623-SE2014-hsep1406-Saturday-01_merged_989712	", "	1989822-SE2014-hsep1401-Weekday-02_merged_980853	",
				"	1989786-SE2014-hsep1401-Weekday-02_merged_990674	", "	1989843-SE2014-hsep1401-Weekday-02_merged_990274	",
				"	1989792-SE2014-hsep1401-Weekday-02_merged_982387	", "	1982612-SE2014-hsep1406-Saturday-01_merged_986198	",
				"	1989834-SE2014-hsep1401-Weekday-02_merged_985429	", "	1989798-SE2014-hsep1401-Weekday-02_merged_983639	",
				"	1984245-SE2014-hsep1407-Sunday-01_merged_987016	", "	1989782-SE2014-hsep1401-Weekday-02_merged_983310	",
				"	1989828-SE2014-hsep1401-Weekday-02_merged_989511	", "	1989789-SE2014-hsep1401-Weekday-02_merged_989506	",
				"	1982626-SE2014-hsep1406-Saturday-01_merged_988595	", "	1989832-SE2014-hsep1401-Weekday-02_merged_989161	",
				"	1989805-SE2014-hsep1401-Weekday-02_merged_991049	", "	1989844-SE2014-hsep1401-Weekday-02_merged_980438	",
				"	1984246-SE2014-hsep1407-Sunday-01_merged_980708	", "	1984247-SE2014-hsep1407-Sunday-01_merged_984577	",
				"	1984233-SE2014-hsep1407-Sunday-01_merged_987171	", "	1989787-SE2014-hsep1401-Weekday-02_merged_989779	",
				"	1982625-SE2014-hsep1406-Saturday-01_merged_983147	", "	1989829-SE2014-hsep1401-Weekday-02_merged_988687	",
				"	1982597-SE2014-hsep1406-Saturday-01_merged_988865	", "	1989840-SE2014-hsep1401-Weekday-02_merged_980460	",
				"	1982606-SE2014-hsep1406-Saturday-01_merged_989475	", "	1984258-SE2014-hsep1407-Sunday-01_merged_982381	",
				"	1989796-SE2014-hsep1401-Weekday-02_merged_981759	", "	1982617-SE2014-hsep1406-Saturday-01_merged_990023	",
				"	1982605-SE2014-hsep1406-Saturday-01_merged_982473	", "	1982615-SE2014-hsep1406-Saturday-01_merged_985678	",
				"	1982599-SE2014-hsep1406-Saturday-01_merged_988593	", "	1989791-SE2014-hsep1401-Weekday-02_merged_983474	",
				"	1989838-SE2014-hsep1401-Weekday-02_merged_986925	", "	1989819-SE2014-hsep1401-Weekday-02_merged_989689	",
				"	1982622-SE2014-hsep1406-Saturday-01_merged_989372	", "	1984234-SE2014-hsep1407-Sunday-01_merged_982474	",
				"	1982594-SE2014-hsep1406-Saturday-01_merged_988662	", "	1989814-SE2014-hsep1401-Weekday-02_merged_985299	",
				"	1989840-SE2014-hsep1401-Weekday-02_merged_986112	", "	1984237-SE2014-hsep1407-Sunday-01_merged_985807	",
				"	1989799-SE2014-hsep1401-Weekday-02_merged_982563	", "	1984241-SE2014-hsep1407-Sunday-01_merged_983090	",
				"	1982595-SE2014-hsep1406-Saturday-01_merged_985602	", "	1982602-SE2014-hsep1406-Saturday-01_merged_985813	",
				"	1989789-SE2014-hsep1401-Weekday-02_merged_983854	", "	1989821-SE2014-hsep1401-Weekday-02_merged_988117	",
				"	1982610-SE2014-hsep1406-Saturday-01_merged_988479	", "	1982611-SE2014-hsep1406-Saturday-01_merged_981180	",
				"	1984233-SE2014-hsep1407-Sunday-01_merged_981519	", "	1984245-SE2014-hsep1407-Sunday-01_merged_981364	",
				"	1989846-SE2014-hsep1401-Weekday-02_merged_981245	", "	1989829-SE2014-hsep1401-Weekday-02_merged_983035	",
				"	1984262-SE2014-hsep1407-Sunday-01_merged_986930	", "	1984239-SE2014-hsep1407-Sunday-01_merged_982965	",
				"	1982621-SE2014-hsep1406-Saturday-01_merged_989818	", "	1989796-SE2014-hsep1401-Weekday-02_merged_987411	",
				"	1989781-SE2014-hsep1401-Weekday-02_merged_983754	", "	1989842-SE2014-hsep1401-Weekday-02_merged_980668	",
				"	1984238-SE2014-hsep1407-Sunday-01_merged_980309	", "	1982613-SE2014-hsep1406-Saturday-01_merged_988129	",
				"	1982602-SE2014-hsep1406-Saturday-01_merged_980161	", "	1984250-SE2014-hsep1407-Sunday-01_merged_991107	",
				"	1989838-SE2014-hsep1401-Weekday-02_merged_981273	", "	1982618-SE2014-hsep1406-Saturday-01_merged_982066	",
				"	1989841-SE2014-hsep1401-Weekday-02_merged_980898	", "	1984239-SE2014-hsep1407-Sunday-01_merged_988617	",
				"	1989806-SE2014-hsep1401-Weekday-02_merged_985964	", "	1989792-SE2014-hsep1401-Weekday-02_merged_988039	",
				"	1982594-SE2014-hsep1406-Saturday-01_merged_983010	", "	1982597-SE2014-hsep1406-Saturday-01_merged_983213	",
				"	1982603-SE2014-hsep1406-Saturday-01_merged_981575	", "	1982619-SE2014-hsep1406-Saturday-01_merged_991159	",
				"	1982600-SE2014-hsep1406-Saturday-01_merged_987046	", "	1989809-SE2014-hsep1401-Weekday-02_merged_988005	",
				"	1989784-SE2014-hsep1401-Weekday-02_merged_980092	", "	1984259-SE2014-hsep1407-Sunday-01_merged_988883	",
				"	1989793-SE2014-hsep1401-Weekday-02_merged_984220	", "	1982603-SE2014-hsep1406-Saturday-01_merged_987227	",
				"	1982620-SE2014-hsep1406-Saturday-01_merged_985384	", "	1984253-SE2014-hsep1407-Sunday-01_merged_982284	",
				"	1989823-SE2014-hsep1401-Weekday-02_merged_981775	", "	1984254-SE2014-hsep1407-Sunday-01_merged_988410	",
				"	1982623-SE2014-hsep1406-Saturday-01_merged_984060	", "	1990452-SE2014-hsep1401-Weekday-02_merged_984801	",
				"	1989785-SE2014-hsep1401-Weekday-02_merged_984776	", "	1982625-SE2014-hsep1406-Saturday-01_merged_988799	",
				"	1984260-SE2014-hsep1407-Sunday-01_merged_984117	", "	1989839-SE2014-hsep1401-Weekday-02_merged_985095	",
				"	1982626-SE2014-hsep1406-Saturday-01_merged_982943	", "	1984236-SE2014-hsep1407-Sunday-01_merged_987212	",
				"	1989825-SE2014-hsep1401-Weekday-02_merged_986095	", "	1984242-SE2014-hsep1407-Sunday-01_merged_989489	",
				"	1989845-SE2014-hsep1401-Weekday-02_merged_986021	", "	1984249-SE2014-hsep1407-Sunday-01_merged_988604	",
				"	1984259-SE2014-hsep1407-Sunday-01_merged_983231	", "	1989799-SE2014-hsep1401-Weekday-02_merged_988215	",
				"	1989803-SE2014-hsep1401-Weekday-02_merged_987110	", "	1989802-SE2014-hsep1401-Weekday-02_merged_987192	",
				"	1989820-SE2014-hsep1401-Weekday-02_merged_983347	", "	1982608-SE2014-hsep1406-Saturday-01_merged_990841	",
				"	1989797-SE2014-hsep1401-Weekday-02_merged_989863	", "	1984265-SE2014-hsep1407-Sunday-01_merged_986070	",
				"	1989819-SE2014-hsep1401-Weekday-02_merged_984037	", "	1989817-SE2014-hsep1401-Weekday-02_merged_987404	",
				"	1989782-SE2014-hsep1401-Weekday-02_merged_988962	", "	1984246-SE2014-hsep1407-Sunday-01_merged_986360	",
				"	1989845-SE2014-hsep1401-Weekday-02_merged_980369	", "	1989822-SE2014-hsep1401-Weekday-02_merged_986505	",
				"	1989783-SE2014-hsep1401-Weekday-02_merged_983921	", "	1989800-SE2014-hsep1401-Weekday-02_merged_989767	",
				"	1982627-SE2014-hsep1406-Saturday-01_merged_985536	", "	1984256-SE2014-hsep1407-Sunday-01_merged_986649	",
				"	1989827-SE2014-hsep1401-Weekday-02_merged_985680	", "	1982598-SE2014-hsep1406-Saturday-01_merged_987129	",
				"	1989826-SE2014-hsep1401-Weekday-02_merged_983751	", "	1989839-SE2014-hsep1401-Weekday-02_merged_990747	",
				"	1984249-SE2014-hsep1407-Sunday-01_merged_982952	", "	1982606-SE2014-hsep1406-Saturday-01_merged_983823	",
				"	1989831-SE2014-hsep1401-Weekday-02_merged_991222	", "	1982592-SE2014-hsep1406-Saturday-01_merged_991100	",
				"	1982622-SE2014-hsep1406-Saturday-01_merged_983720	", "	1982591-SE2014-hsep1406-Saturday-01_merged_984124	",
				"	1989793-SE2014-hsep1401-Weekday-02_merged_989872	", "	1989817-SE2014-hsep1401-Weekday-02_merged_981752	",
				"	1989784-SE2014-hsep1401-Weekday-02_merged_985744	", "	1984236-SE2014-hsep1407-Sunday-01_merged_981560	",
				"	1989816-SE2014-hsep1401-Weekday-02_merged_981405	", "	1989835-SE2014-hsep1401-Weekday-02_merged_986675	",
				"	1989781-SE2014-hsep1401-Weekday-02_merged_989406	", "	1984244-SE2014-hsep1407-Sunday-01_merged_982590	",
				"	1982598-SE2014-hsep1406-Saturday-01_merged_981477	", "	1984248-SE2014-hsep1407-Sunday-01_merged_985168	",
				"	1989821-SE2014-hsep1401-Weekday-02_merged_982465	", "	1984264-SE2014-hsep1407-Sunday-01_merged_985802	",
				"	1989795-SE2014-hsep1401-Weekday-02_merged_982651	", "	1982608-SE2014-hsep1406-Saturday-01_merged_985189	",
				"	1984265-SE2014-hsep1407-Sunday-01_merged_980418	" };

		String[] tripIDs = new String[tripIDArray.length];
		for (int i = 0; i < tripIDArray.length; i++) {
			tripIDs[i] = tripIDArray[i].trim();
		}

		return tripIDs;

	}
	
	private static String[] getStopIDs() {

		String[] tripIDArray = {"	16720_merged_980003	",
				"	15815	",
				"	15040	",
				"	58150	",
				"	14720	",
				"	14035	",
				"	13830	",
				"	13495	",
				"	13510	",
				"	13560	",
				"	13485	",
				"	13110	",
				"	12905	",
				"	12570	",
				"	12335	",
				"	27200	",
				"	12055	",
				"	11975	",
				"	11870	",
				"	55210	",
				"	11720	",
				"	11275	",
				"	11045	",
				"	10885	",
				"	27210	",
				"	10805	",
				"	10765	",
				"	10690	",
				"	10630	",
				"	10460	",
				"	10260	",
				"	9980	",
				"	9820	",
				"	9650	",
				"	9355	",
				"	58180	",
				"	9015	",
				"	8680	",
				"	8570	",
				"	8295	",
				"	8270	",
				"	8140	",
				"	8070	",
				"	7920	",
				"	7725	",
				"	55215	",
				"	7525	",
				"	7345	",
				"	7250	",
				"	7135	",
				"	7020	",
				"	6870	",
				"	6770	",
				"	6695	",
				"	58185	",
				"	6210	",
				"	5980	",
				"	5710	",
				"	5370	",
				"	5230	",
				"	5125	",
				"	4980	",
				"	4820	",
				"	4730	",
				"	4630	",
				"	4600	",
				"	16720_merged_980009	",
				"	10440	",
				"	10395	",
				"	10105	",
				"	9865	",
				"	9755	",
				"	9490	",
				"	9335	",
				"	9155	",
				"	9065	",
				"	8980	",
				"	8905	",
				"	8785	",
				"	8705	",
				"	8645	",
				"	8600	",
				"	8550	",
				"	8465	",
				"	8405	",
				"	8375	",
				"	8355	",
				"	8300	",
				"	7750	",
				"	7735	",
				"	7645	",
				"	7580	",
				"	7455	",
				"	7425	",
				"	7350	",
				"	7305	",
				"	7215	",
				"	7145	",
				"	7095	",
				"	7040	",
				"	6945	",
				"	6845	",
				"	6740	",
				"	6610	",
				"	6495	",
				"	6315	",
				"	59970	",
				"	58190	",
				"	55225	",
				"	5345	",
				"	5295	",
				"	58205	",
				"	5105	",
				"	4940	",
				"	4655	",
				"	4775	",
				"	4855	",
				"	4935	",
				"	5145	",
				"	5275	",
				"	5500	",
				"	5695	",
				"	5790	",
				"	5995	",
				"	58230	",
				"	6225	",
				"	59975	",
				"	6710	",
				"	6785	",
				"	6895	",
				"	7035	",
				"	7205	",
				"	7375	",
				"	7450	",
				"	7585	",
				"	7640	",
				"	7765	",
				"	7915	",
				"	8040	",
				"	8150	",
				"	8245	",
				"	8565	",
				"	58240	",
				"	8950	",
				"	58245	",
				"	9330	",
				"	9360	",
				"	9570	",
				"	9640	",
				"	9830	",
				"	9880	",
				"	10045	",
				"	10240	",
				"	10335	",
				"	10455	",
				"	10665	",
				"	10685	",
				"	10755	",
				"	10790	",
				"	10890	",
				"	11060	",
				"	11210	",
				"	11355	",
				"	11555	",
				"	11680	",
				"	12050	",
				"	12325	",
				"	12910	",
				"	13125	",
				"	13430	",
				"	13565	",
				"	13500	",
				"	58175	",
				"	13835	",
				"	14005	",
				"	14705	",
				"	15325	",
				"	15545	",
				"	15880	",
				"	16125	",
				"	16385	",
				"	5075	",
				"	5120	",
				"	5285	",
				"	5340	",
				"	55195	",
				"	50620	",
				"	58220	",
				"	59980	",
				"	58225	",
				"	6520	",
				"	6705	",
				"	6815	",
				"	6920	",
				"	7010	",
				"	7090	",
				"	7155	",
				"	7245	",
				"	7280	",
				"	7320	",
				"	7340	",
				"	7420	",
				"	7445	",
				"	59985	",
				"	7505	",
				"	7555	",
				"	7605	",
				"	7670	",
				"	7730	",
				"	7745	",
				"	8305	",
				"	8330	",
				"	8370	",
				"	8400	",
				"	8445	",
				"	8545	",
				"	8585	",
				"	8640	",
				"	8695	",
				"	8775	",
				"	8900	",
				"	8985	",
				"	9070	",
				"	9415	",
				"	9495	",
				"	59950	",
				"	58880	",
				"	10075	",
				"	10145	",
				"	10390	",
				"	12370	",
				"	12495	",
				"	12690	",
				"	12935	",
				"	13250	"
};

		String[] tripIDs = new String[tripIDArray.length];
		for (int i = 0; i < tripIDArray.length; i++) {
			tripIDs[i] = tripIDArray[i].trim();
		}

		return tripIDs;

	}

	private static Map<String, Map<Integer, String[]>> getBodyMap(Map<String, List<String>> fileDataMap) {

		Map<String, Map<Integer, String[]>> bodyMap = new HashMap<String, Map<Integer, String[]>>();
		Map<Integer, String[]> bodyArrayMap = new HashMap<Integer, String[]>();

		String key = "";
		String[] bodyArray = null;
		for (Entry<String, List<String>> fileData : fileDataMap.entrySet()) {

			key = fileData.getKey();
			List<String> dataList = fileData.getValue();

			for (int i = 1; i < dataList.size(); i++) {
				bodyArray = dataList.get(i).split(",");
				bodyArrayMap.put(i, bodyArray);
			}
			bodyMap.put(key, bodyArrayMap);
		}
		return bodyMap;
	}

	private static Map<String, String[]> getHeaderMap(Map<String, List<String>> fileDataMap) {

		Map<String, String[]> headerMap = new HashMap<String, String[]>();

		String key = "";
		String[] headerTitlesArray = null;

		for (Entry<String, List<String>> fileData : fileDataMap.entrySet()) {

			key = fileData.getKey();

			if (fileData.getValue().size() > 0) {
				headerTitlesArray = fileData.getValue().get(0).split(",");
				headerMap.put(key, headerTitlesArray);
			}

		}
		return headerMap;
	}

	private static List<String> getFilePathList(String folderPath) {

		List<String> filePathList = new ArrayList<String>();

		File folder = new File(folderPath);
		File[] filesInFolder = folder.listFiles();

		for (File file : filesInFolder) {
			String abStrin = file.getAbsolutePath();
			filePathList.add(abStrin);
		}
		return filePathList;
	}

	private static Map<String, List<String>> readTheFiles(List<String> filePathList) {

		Map<String, List<String>> fileDataMap = new HashMap<String, List<String>>();
		List<String> fileDataList = null;
		try {

			for (int i = 0; i < filePathList.size(); i++) {
				String filePath = filePathList.get(i);
				fileDataList = new ArrayList<String>();
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					fileDataList.add(line);
				}
				String[] filePathSplit = filePath.split("\\\\");
				String fileName = filePathSplit[filePathSplit.length - 1];
				fileDataMap.put(fileName, fileDataList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileDataMap;
	}

	private static Map<String, List<String>> wirteTheFiles(List<String> filePathList) {

		Map<String, List<String>> fileDataMap = new HashMap<String, List<String>>();
		List<String> fileDataList = null;
		try {

			for (int i = 0; i < filePathList.size(); i++) {
				String filePath = filePathList.get(i);
				fileDataList = new ArrayList<String>();
				BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
				String line = "";
				while ((line = bufferedReader.readLine()) != null) {
					fileDataList.add(line);
				}
				String[] filePathSplit = filePath.split("\\\\");
				String fileName = filePathSplit[filePathSplit.length - 1];
				fileDataMap.put(fileName, fileDataList);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileDataMap;
	}

	private static List<String> getOverlappingTitles(String[] titles) {
		List<String> overlappingTitleList = new ArrayList<>();
		Set<String> overlappingTitleSet = new HashSet<>();
		for (int i = 1; i < titles.length; i++) {
			String nextTitle = titles[i];
			if (!overlappingTitleSet.add(nextTitle)) {
				overlappingTitleList.add(nextTitle);
			}
		}
		return overlappingTitleList;
	}

}