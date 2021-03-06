package com.sakha.s20.ai.ModelGenerator;



import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class ModelGenerator {
	static Map<String, Object> final_dict = new HashMap<String, Object>();
	

	public static Map<String, List<String>> trigram_model(List<String[]> CORPUS_list) {
		Map<String, List<String>> Dictionary = new DefaultDict<String, List<String>>(ArrayList.class);
		for (String[] sentence : CORPUS_list) {
			String previous_word_1 = "";
			String previous_word_2 = "";
			int j = 0, k = -1;
			for (int i = 0; i < sentence.length - 1; i++) {
				if (previous_word_1 != "" && previous_word_2 != "") {

					Dictionary.get(previous_word_2 + " " + previous_word_1).add(sentence[i + 1]);
				}
				j++;
				k++;
				previous_word_1 = sentence[j];
				previous_word_2 = sentence[k];
			}
		}
		return Dictionary;
	}

	public static Map<String, List<String>> bigram_model(List<String[]> CORPUS_list) {
		Map<String, List<String>> Dictionary = new DefaultDict<String, List<String>>(ArrayList.class);

		for (String[] sentence : CORPUS_list) {
			String previous_word = "";
			for (String currentword : sentence) {
				if (previous_word != "") {
					Dictionary.get(previous_word).add(currentword);
				}
				previous_word = currentword;
			}
		}
		return Dictionary;
	}

	public static Map<String, Object> probabilities(Map<String, List<String>> Dict) {
		Iterator<Map.Entry<String, List<String>>> itr = Dict.entrySet().iterator();
		while (itr.hasNext()) {
			Map<String,Object> tempDict = new HashMap<String, Object>();
			LinkedHashMap<String, Double> ProbabilitiesDict = new LinkedHashMap<String, Double>();
			Entry<String, List<String>> entry = itr.next();
			List<String> NextWords = entry.getValue();
			Set<String> UniqueWords = NextWords.stream().collect(Collectors.toSet());
			Set<String> UniqueWords1 = new LinkedHashSet<String>(UniqueWords);
			UniqueWords1.remove(NextWords.get(NextWords.size() - 1));
			UniqueWords1.add(NextWords.get(NextWords.size() - 1));
			int TotalWords = NextWords.size(), UniqueTotalWords = UniqueWords1.size(), Counter = 1;
			for (String word : UniqueWords1) {
				double occurrences = Collections.frequency(NextWords, word);
				double prob = occurrences / TotalWords;
				if (Counter < UniqueTotalWords) {
					ProbabilitiesDict.put(word, prob);
				} else if (Counter == UniqueTotalWords) {

					ProbabilitiesDict = ProbabilitiesDict.entrySet().stream()
							.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
							.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
					ProbabilitiesDict.put(word, prob);
				}
				Counter++;
			}
			tempDict.put("Total", TotalWords);
			tempDict.put("Words", ProbabilitiesDict);
			final_dict.put(entry.getKey(), tempDict);
		}
		return final_dict;
	}

	public static void model_generator(String txtFilePath, String jsonFilePath) {
		Map<String, List<String>> bigramDict = new HashMap<String, List<String>>();
		Map<String, List<String>> trigramDict = new HashMap<String, List<String>>();
		Map<String, Object> globalMap = new HashMap<String, Object>();
		ArrayList<String[]> arr = new ArrayList<String[]>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			globalMap = mapper.readValue(new File(jsonFilePath + "Model.json"),
					new TypeReference<Map<String, Object>>() {
					});
		} catch (JsonParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JsonMappingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try (BufferedReader br = new BufferedReader(new FileReader(txtFilePath))) {
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				arr.add(sCurrentLine.toLowerCase().split(" +"));

			}
			bigramDict = bigram_model(arr);
			trigramDict = trigram_model(arr);
			probabilities(bigramDict);
			probabilities(trigramDict);
			final_dict =  GlobalUpdation.GlobalUpdator(globalMap, final_dict); 

			mapper.writeValue(new File(jsonFilePath + "Model.json"), final_dict);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String textFilePath = args[0];
		String jsonFilePath = args[1];
//		String textFilePath = "/home/anup/SakhaGlobal/Next_word_prediction/Random/Next_Word_Predictor/input1.txt";
//		String jsonFilePath = "/home/anup/SakhaGlobal/S20/global/";
		model_generator(textFilePath, jsonFilePath);
//		bigram_generator(textFilePath,jsonFilePath);
	}
}
