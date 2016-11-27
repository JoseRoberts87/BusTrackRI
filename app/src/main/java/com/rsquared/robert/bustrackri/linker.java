package com.rsquared.robert.bustrackri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Linker {

	public static void main(String[] args) {
		System.out.println("Starting Linker...");
		long startMain = System.currentTimeMillis();

		String folderPath = "./google_transit";
		List<String> filePathList = getFilePathList(folderPath);
		Map<String, List<String>> fileDataMap = readFiles(filePathList);
		Map<String, String[]> headerMap = getHeaderMap(fileDataMap);
		Map<String, Map<Integer, String[]>> bodyMap = getBodyMap(fileDataMap);

		String[] routeHeaderArray = headerMap.get("routes.txt");
		String[] tripsHeaderArray = headerMap.get("trips.txt");
		String[] stopsHeaderArray = headerMap.get("stops.txt");
		String[] stopTimesHeaderArray = headerMap.get("stop_times.txt");
		String[] shapesHeaderArray = headerMap.get("shapes.txt");

		// get route_id index in trips.txt
		int routeIdIndex = 0;

		for (int i = 0; i < tripsHeaderArray.length; i++) {
			if (tripsHeaderArray[i].equalsIgnoreCase("route_id")) {
				routeIdIndex = i;
			}
		}
		int routeIdToRetrieve = 3;

		// get trip_id index in stop_times.txt

		int tripIdIndex = 0;

		for (int i = 0; i < tripsHeaderArray.length; i++) {
			if (tripsHeaderArray[i].equalsIgnoreCase("trip_id")) {
				tripIdIndex = i;
			}
		}

		Map<Integer, String[]> routeBodyMaps = bodyMap.get("routes.txt");
		Map<Integer, String[]> shapesBodyMaps = bodyMap.get("shapes.txt");
		Map<Integer, String[]> stopsBodyMaps = bodyMap.get("stops.txt");
		Map<Integer, String[]> stopTimesBodyMaps = bodyMap.get("stop_times.txt");
		Map<Integer, String[]> tripsBodyMaps = bodyMap.get("trips.txt");

		List<Integer> routeIdInRoutesList = getIdLIst(routeIdIndex, routeBodyMaps);

		for (int x = 0; x < routeIdInRoutesList.size(); x++) {

			routeIdToRetrieve = routeIdInRoutesList.get(x);

			Map<Integer, String[]> routeMapByIdInRoutes = getRouteBodyForRouteId(routeIdIndex, routeIdToRetrieve, routeBodyMaps);
			Map<Integer, String[]> routeMapByIdInShapes = getRouteBodyForRouteId(routeIdIndex, routeIdToRetrieve, shapesBodyMaps);
			Map<Integer, String[]> routeMapByIdInStops = getRouteBodyForRouteId(routeIdIndex, routeIdToRetrieve, stopsBodyMaps);
			Map<Integer, String[]> routeMapByIdInStopTimes = getRouteBodyForRouteId(routeIdIndex, routeIdToRetrieve, stopTimesBodyMaps);
			Map<Integer, String[]> routeMapByIdInTrips = getRouteBodyForRouteId(routeIdIndex, routeIdToRetrieve, tripsBodyMaps);

			List<Integer> tripIdsInRouteList = getIdLIst(tripIdIndex, routeMapByIdInTrips);
			Set<Integer> tripIdsInRouteSet = new HashSet<Integer>(tripIdsInRouteList);
			final List<Integer> tripIdsInRouteSetList = new ArrayList<Integer>(tripIdsInRouteSet);

			// get trip_id index in stop_times.txt

			int tripIdIndexInStopTimes = 0;

			for (int i = 0; i < stopTimesHeaderArray.length; i++) {
				if (stopTimesHeaderArray[i].equalsIgnoreCase("trip_id")) {
					tripIdIndexInStopTimes = i;
				}
			}

			Map<Integer, String[]> tripIdMapByIdInStopTimes = null;
			// for (int i = 0; i < tripIdsInRouteSetList.size(); i++) {

			tripIdMapByIdInStopTimes = getBodyForId(tripIdIndexInStopTimes, tripIdsInRouteSetList, stopTimesBodyMaps);

			// }

			// get trip_id index in stop_times.txt

			int stopIdIndexInStopTime = 0;

			for (int i = 0; i < stopTimesHeaderArray.length; i++) {
				if (stopTimesHeaderArray[i].equalsIgnoreCase("stop_id")) {
					stopIdIndexInStopTime = i;
				}
			}

			List<Integer> stopIdsInStopTimesList = getIdLIst(stopIdIndexInStopTime, tripIdMapByIdInStopTimes);
			Set<Integer> stopIdsInStopTimesSet = new HashSet<Integer>(stopIdsInStopTimesList);
			List<Integer> stopIdsInStopTimesSetList = new ArrayList<Integer>(stopIdsInStopTimesSet);

			int stopIdIndexInStops = 0;

			for (int i = 0; i < stopsHeaderArray.length; i++) {
				if (stopsHeaderArray[i].equalsIgnoreCase("trip_id")) {
					stopIdIndexInStops = i;
				}
			}

			Map<Integer, String[]> stopIdMapByIdInStops = null;
			// for (int i = 0; i < stopIdsInStopTImesSetList.size(); i++) {

			stopIdMapByIdInStops = getBodyForId(stopIdIndexInStops, stopIdsInStopTimesSetList, stopsBodyMaps);

			Map<Integer, String[]> stopMapByIdInStopTimes = appendStopsToStopTimes(stopIdMapByIdInStops, tripIdMapByIdInStopTimes);
			
			Map<Integer, String[]> stopMapByIdInStopTimesAndTrips = appendStopsToStopTimes(routeMapByIdInTrips, stopMapByIdInStopTimes);


			// writeFile("./doc/stopIdMapByIdInStops.txt",
			// stopIdMapByIdInStops);
			// writeFile("./doc/tripIdMapByIdInStopTimes.txt",
			// tripIdMapByIdInStopTimes);

			List<String[]> listOfStringArray = new ArrayList<String[]>();

			for (Entry<Integer, String[]> stopMapByIdInStopTime : stopMapByIdInStopTimesAndTrips.entrySet()) {

				listOfStringArray.add(stopMapByIdInStopTime.getValue());

			}
			
			Collections.sort(listOfStringArray, new Comparator<String[]>() {

				public int compare(String[] strings, String[] otherString) {

					int compared1 = (otherString[0].compareTo(strings[0]));
					return compared1;
				}

			});

			List<String[]> sortedListOfStringArray = superSort(listOfStringArray, tripIdsInRouteSetList, 0, 1);

			String[] headerStopMapByIdInStopTimes = addArrays(stopTimesHeaderArray, stopsHeaderArray);

			writeFileList("./results/route_" + String.valueOf(routeIdToRetrieve) + ".txt", headerStopMapByIdInStopTimes, sortedListOfStringArray);

			// writeFile("./doc/routeIdInRoutesList" +
			// String.valueOf(routeIdToRetrieve) + ".txt", routeIdInRoutesList);

			// }
		}

		// String[] overlappingHeaderFields = getOverlappingFields(headerMap);

		long endMain = System.currentTimeMillis();
		long totalMain = (endMain - startMain);
		System.out.println("startMain: " + startMain);
		System.out.println("endMain: " + endMain);
		System.out.println("totalMain: " + totalMain);
		System.out.println("totalMain in milli Seconds: " + totalMain);

	}

	private static List<String[]> superSort(List<String[]> listOfStringArrayList, final List<Integer> tripIdsInRouteSetList, int i, int j) {

		List<String[]> sortedListOfStringArray = new ArrayList<String[]>();

		for (Integer tripId : tripIdsInRouteSetList) {

			List<String[]> listOfStringArrayListToSort = new ArrayList<String[]>();
			
			String tripIdString = String.valueOf(tripId).trim();

			for (String[] listOfStringArray : listOfStringArrayList) {

				if (listOfStringArray[0].trim().equalsIgnoreCase(tripIdString)) {

					listOfStringArrayListToSort.add(listOfStringArray);

				}

			}
			
			
			Collections.sort(listOfStringArrayListToSort, new Comparator<String[]>() {

				public int compare(String[] strings, String[] otherString) {

					int compared1 = (strings[1].compareTo(otherString[1]));
					return compared1;
				}

			});
			
			sortedListOfStringArray.addAll(listOfStringArrayListToSort);
		}

/*		Collections.sort(listOfStringArrayList, new Comparator<String[]>() {

			@Override
			public int compare(String[] strings, String[] otherString) {
				for (int i = 0; i < tripIdsInRouteSetList.size(); i++) {
					final String tripId = String.valueOf(tripIdsInRouteSetList.get(i));

					if (strings[0].equalsIgnoreCase(tripId)) {

						int compared = (String.valueOf(strings[1]).compareTo(String.valueOf(otherString[1])));
						return compared;
					}
				}

				return 0;
			}

		});*/

		// TODO Auto-generated method stub
		return sortedListOfStringArray;
	}

	private static Map<Integer, String[]> appendStopsToStopTimes(Map<Integer, String[]> stopIdMapByIdInStops, Map<Integer, String[]> tripIdMapByIdInStopTimes) {

		int stopsIndexInStops = 0;
		int stopIndexInStopTimes = 3;
		// int tripIndexInStopTimes = 0;

		Map<Integer, String[]> stopMapByIdInStopTimes = tripIdMapByIdInStopTimes;

		for (Entry<Integer, String[]> tripIdMapByIdInStopTime : tripIdMapByIdInStopTimes.entrySet()) {

			int stopTimesKey = tripIdMapByIdInStopTime.getKey();
			String[] stopTimesArray = tripIdMapByIdInStopTime.getValue();

			for (Entry<Integer, String[]> stopIdMapByIdInStop : stopIdMapByIdInStops.entrySet()) {

				int stopsKey = stopIdMapByIdInStop.getKey();
				String[] stopsArray = stopIdMapByIdInStop.getValue();

				String stopIdInStopTimes = stopTimesArray[stopIndexInStopTimes].trim();
				String stopsIdInStop = stopsArray[stopsIndexInStops].trim();

				if (stopIdInStopTimes.equals(stopsIdInStop)) {
					String[] stopsAndStopTimesArray = addArrays(stopTimesArray, stopsArray);
					tripIdMapByIdInStopTime.setValue(stopsAndStopTimesArray);
					break;
				}
			}

		}

		return stopMapByIdInStopTimes;
	}

	private static String[] addArrays(String[] firstArray, String[] secondArray) {

		/*
		 * List<String> firstList = Arrays.asList(firstArray); List<String>
		 * secondList = Arrays.asList(secondArray);
		 * 
		 * List<String> addedList = new ArrayList<String>();
		 * 
		 * addedList.addAll(firstList); addedList.addAll(secondList);
		 * 
		 * addedArray = (String[]) addedList.toArray();
		 */

		int firstArrayLength = firstArray.length;
		int secondArrayLength = secondArray.length;

		String[] addedArray = new String[firstArrayLength + secondArrayLength];

		for (int i = 0; i < firstArray.length; i++) {
			addedArray[i] = firstArray[i];
		}

		for (int j = 0; j < secondArray.length; j++) {
			addedArray[firstArrayLength + j] = secondArray[j];
		}

		return addedArray;

	}

	/*
	 * private static String[] getOverlappingFields(Map<String, String[]>
	 * headerMap) { String[] overlappingFields = null; for(Entry<String,
	 * String[]> fields: headerMap.entrySet()){ fields.getValue(); } return
	 * null; }
	 */

	private static Map<Integer, String[]> getRouteBodyForRouteId(int routeIdIndex, int routeIdToRetrieve, Map<Integer, String[]> routeBodyMaps) {

		Map<Integer, String[]> routeMapById = new HashMap<Integer, String[]>();

		for (Entry<Integer, String[]> routeBodyMap : routeBodyMaps.entrySet()) {

			int key = routeBodyMap.getKey();
			String[] routeBodyArray = routeBodyMap.getValue();
			String routeBody = routeBodyArray[routeIdIndex].trim();
			if (routeBody.equalsIgnoreCase(String.valueOf(routeIdToRetrieve))) {
				routeMapById.put(key, routeBodyArray);
			}

		}
		return routeMapById;

	}

	private static Map<Integer, String[]> getBodyForId(int routeIdIndex, List<Integer> stopIdsInStopTimesSetList, Map<Integer, String[]> routeBodyMaps) {

		Map<Integer, String[]> routeMapById = new HashMap<Integer, String[]>();

		for (int i = 0; i < stopIdsInStopTimesSetList.size(); i++) {
			int stopIdsInStopTimes = stopIdsInStopTimesSetList.get(i);
			for (Entry<Integer, String[]> routeBodyMap : routeBodyMaps.entrySet()) {

				int key = routeBodyMap.getKey();
				String[] routeBodyArray = routeBodyMap.getValue();
				String routeBody = routeBodyArray[routeIdIndex].trim();
				if (routeBody.equalsIgnoreCase(String.valueOf(stopIdsInStopTimes))) {
					routeMapById.put(key, routeBodyArray);
				}

			}
		}
		return routeMapById;

	}

	private static List<Integer> getIdLIst(Integer index, Map<Integer, String[]> routeMapByIdInRoutes) {
		String[] eva = null;

		List<Integer> idList = new ArrayList<Integer>();
		try {

			for (Entry<Integer, String[]> routeMapByIdInRoute : routeMapByIdInRoutes.entrySet()) {

				int key = routeMapByIdInRoute.getKey();
				String[] tripBodyArray = routeMapByIdInRoute.getValue();
				String tripBody = tripBodyArray[index].trim();
				eva = tripBodyArray;
				idList.add(Integer.valueOf(tripBody));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(eva);
		}

		return idList;
	}

	private static List<Integer> getRouteIdLIst() {

		return null;
	}

	private static Map<String, Map<Integer, String[]>> getBodyMap(Map<String, List<String>> fileDataMap) {

		Map<String, Map<Integer, String[]>> bodyMap = new HashMap<String, Map<Integer, String[]>>();

		String key = "";
		String[] bodyArray = null;
		for (Entry<String, List<String>> fileData : fileDataMap.entrySet()) {
			Map<Integer, String[]> bodyArrayMap = new HashMap<Integer, String[]>();
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

	private static Map<String, List<String>> readFiles(List<String> filePathList) {

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

	private static void writeFile(String filePath, String[] headersArray, Map<Integer, String[]> stopIdMapByIdInStops) {

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));

			for (Entry<Integer, String[]> stopIdMapByIdInStop : stopIdMapByIdInStops.entrySet()) {
				String[] stopIdMapByIdInStopArray = stopIdMapByIdInStop.getValue();
				for (int i = 0; i < stopIdMapByIdInStopArray.length; i++) {
					bufferedWriter.write(stopIdMapByIdInStopArray[i] + ", ");
				}
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeFile(String filePath, Map<Integer, String[]> stopIdMapByIdInStops) {

		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));

			for (Entry<Integer, String[]> stopIdMapByIdInStop : stopIdMapByIdInStops.entrySet()) {
				String[] stopIdMapByIdInStopArray = stopIdMapByIdInStop.getValue();
				for (int i = 0; i < stopIdMapByIdInStopArray.length; i++) {
					bufferedWriter.write(stopIdMapByIdInStopArray[i] + ", ");
				}
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeFile(String filePath, List<Integer> routeIdInRoutesList) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
			for (int i = 0; i < routeIdInRoutesList.size(); i++) {
				bufferedWriter.write(routeIdInRoutesList.get(i) + ", ");
			}
			bufferedWriter.write("\n");
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void writeFileList(String filePath, String[] headers, List<String[]> routeIdInRoutesList) {
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
			for (int i = 0; i < headers.length; i++) {
				bufferedWriter.write(headers[i] + ", ");
			}
			bufferedWriter.write("\n");
			for (int i = 0; i < routeIdInRoutesList.size(); i++) {
				for (int j = 0; j < routeIdInRoutesList.get(i).length; j++) {
					bufferedWriter.write(routeIdInRoutesList.get(i)[j] + ", ");

				}
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
