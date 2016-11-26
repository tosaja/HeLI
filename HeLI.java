/*
    HeLI, general purpose language identifier for digital text
	See: Jauhiainen, Lindén, Jauhiainen 2016: "HeLI, a Word-Based Backoff Method for Language Identification" In Proceedings of the 3rd Workshop on Language Technology for Closely Related Languages, Varieties and Dialects (VarDial)
    Copyright (C) 2016 Tommi Jauhiainen

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.*;
import java.util.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import java.lang.Math.*;

class HeLI {

// global table holding the language models for all the languages

	private static Table<String, String, Double> gramDictCap;
	private static Table<String, String, Double> gramDictLow;
	private static Table<String, String, Double> wordDictCap;
	private static Table<String, String, Double> wordDictLow;

// global variable containing all the languages known by the identifier

	private static List<String> languageList = new ArrayList<String>();

// These variables should be moved to a configuration file
// They set the number of units to read from each model file

	private static double usedlow1gram = 1000;
	private static double usedlow2gram = 1000;
	private static double usedlow3gram = 1000;
	private static double usedlow4gram = 1000;
	private static double usedlow5gram = 1000;
	private static double usedlow6gram = 1000;
	private static double usedlow7gram = 1000;
	private static double usedlow8gram = 1000;
	
	private static double usedcap1gram = 1000;
	private static double usedcap2gram = 1000;
	private static double usedcap3gram = 1000;
	private static double usedcap4gram = 1000;
	private static double usedcap5gram = 1000;
	private static double usedcap6gram = 1000;
	private static double usedcap7gram = 1000;
	private static double usedcap8gram = 1000;
	
	private static double usedcapwords = 1000;
	
	private static double usedlowwords = 1000;

// this is the penalty value for unseen tokens

	private static double gramsakko = 6.6;
	
// This is the maximum length of used character n-grams (setting them to 0 gives the same outcome, but the identifier still divides the words)
	
	private static int maksimingram = 8;

	public static void main(String[] args) {
		
// We read the file languagelist which includes list of the languages to be included in the repertoire of the language identifier. You can use "ls -la Models/ | egrep 'Model' | gawk '{print $9}' | sed 's/\..*//' | sort | uniq > languagelist" to include all the languages which have models in the Models directory.
		
		File file = new File("languagelist");

		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			while ((text = reader.readLine()) != null) {
				languageList.add(text);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}
		
		gramDictLow = HashBasedTable.create();
		gramDictCap = HashBasedTable.create();
		wordDictCap = HashBasedTable.create();
		wordDictLow = HashBasedTable.create();
		
		ListIterator gramiterator = languageList.listIterator();
		while(gramiterator.hasNext()) {
			Object element = gramiterator.next();
			String language = (String) element;

			loadIn(usedlow1gram, language, "LowGramModel1");
			loadIn(usedlow2gram, language, "LowGramModel2");
			loadIn(usedlow3gram, language, "LowGramModel3");
			loadIn(usedlow4gram, language, "LowGramModel4");
			loadIn(usedlow5gram, language, "LowGramModel5");
			loadIn(usedlow6gram, language, "LowGramModel6");
			loadIn(usedlow7gram, language, "LowGramModel7");
			loadIn(usedlow8gram, language, "LowGramModel8");

			loadIn(usedcap1gram, language, "CapGramModel1");
			loadIn(usedcap2gram, language, "CapGramModel2");
			loadIn(usedcap3gram, language, "CapGramModel3");
			loadIn(usedcap4gram, language, "CapGramModel4");
			loadIn(usedcap5gram, language, "CapGramModel5");
			loadIn(usedcap6gram, language, "CapGramModel6");
			loadIn(usedcap7gram, language, "CapGramModel7");
			loadIn(usedcap8gram, language, "CapGramModel8");

			loadIn(usedcapwords, language, "CapWordModel");
			loadIn(usedlowwords, language, "LowWordModel");
//			System.out.println(usedlowwords + language + "lowword");
		}

		BufferedReader testfilereader = null;

//		The file to be tested is put into the Test directory. No empty lines on the file.
//		File testfile = new File("./Test/test.txt");

		File testfile = new File("./Test/test.txt");

		try {
			testfilereader = new BufferedReader(new FileReader(testfile));
			String testline = "";
			
			while ((testline = testfilereader.readLine()) != null) {
				
				if (testline.length() < 2) {
					break;
				}
				
//				String mysterytext = testline;
				
				String mysterytext = testline.split("\t")[0];
				
				mysterytext = " " + mysterytext + " ";
				
				String identifiedLanguage = identifyText(mysterytext);

				System.out.println(testline + "\t" + identifiedLanguage);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (testfilereader != null) {
					testfilereader.close();
				}
			} catch (IOException e) {
			}
		}
	}
	
	private static void loadIn(double cutoff, String language, String tokentype) {
		Table<String, String, Double> tempDict;
		
		tempDict = HashBasedTable.create();
	
		String seuraava = null;
		String pituustiedosto = null;

		seuraava = "./Models/" + language + "." + tokentype;

		double grampituus = 0;
		double langamount = 0;

		File file = new File(seuraava);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			String text = null;
			
			text = reader.readLine();
			grampituus = Double.parseDouble(text);
			
			int laskuri = 0;
			while ((text = reader.readLine()) != null) {
				String[] line = text.split("\t");
				String gram = line[0];
				long amount = Long.parseLong(line[1]);

				if (laskuri < cutoff) {
					tempDict.put(gram, language, (double) amount);
					langamount = langamount + (double) amount;
				}
				else {
					break;
				}
				laskuri++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}				

		for (Cell<String, String, Double> cell: tempDict.cellSet()){
			double probability = -Math.log10(cell.getValue() / langamount);
			if (tokentype.equals("LowWordModel")) {
				wordDictLow.put(cell.getRowKey(), language, probability);
			}
			else if (tokentype.equals("CapWordModel")) {
				wordDictCap.put(cell.getRowKey(), language, probability);
			}
			else if (tokentype.contains("LowGram")) {
				gramDictLow.put(cell.getRowKey(), language, probability);
			}
			else if (tokentype.contains("CapGram")) {
				gramDictCap.put(cell.getRowKey(), language, probability);
			}
		}
	}
	
// identifyText

	private static String identifyText(String mysteryText) {
		
//koko seuraava replace on turha haravointikäytössä
		mysteryText = mysteryText.replaceAll("[^\\p{L}\\p{M}′'’´ʹािीुूृेैोौंँः् া ি ী ু ূ ৃ ে ৈ ো ৌ।্্্я̄\\u07A6\\u07A7\\u07A8\\u07A9\\u07AA\\u07AB\\u07AC\\u07AD\\u07AE\\u07AF\\u07B0\\u0A81\\u0A82\\u0A83\\u0ABC\\u0ABD\\u0ABE\\u0ABF\\u0AC0\\u0AC1\\u0AC2\\u0AC3\\u0AC4\\u0AC5\\u0AC6\\u0AC7\\u0AC8\\u0AC9\\u0ACA\\u0ACB\\u0ACC\\u0ACD\\u0AD0\\u0AE0\\u0AE1\\u0AE2\\u0AE3\\u0AE4\\u0AE5\\u0AE6\\u0AE7\\u0AE8\\u0AE9\\u0AEA\\u0AEB\\u0AEC\\u0AED\\u0AEE\\u0AEF\\u0AF0\\u0AF1]", " ");

		mysteryText = mysteryText.replaceAll("  *", " ");
		
		Boolean ensimmainensana = false;
		
		if (mysteryText.substring(0,1).equals(" ")) {
			mysteryText = mysteryText.replaceAll("^ ", "");
			ensimmainensana = true;
		}
		
		int strLength = mysteryText.length();
		
		if (strLength == 0) {
			return("xx");
		}

		Boolean viimeinensana = false;
		
		if (mysteryText.substring(strLength - 1, strLength).equals(" ")) {
			mysteryText = mysteryText.replaceAll(" $", "");
			viimeinensana = true;
		}

		String[] sanat = mysteryText.split(" ");

		List<String> sanalista = new ArrayList<String>();
		
		double sanamaara = 0;
		
		Map<String, Double> languagepisteet = new HashMap();
		
		ListIterator pisteiterator = languageList.listIterator();
		while(pisteiterator.hasNext()) {
			Object element = pisteiterator.next();
			String pistelanguage = (String) element;
			languagepisteet.put(pistelanguage, 0.0);
		}
		
		sanamaara = 0;
		double monesko = 0;

		String mysterytextmodified = "";

		for (String ss : sanat) {
			if (ss.length() > 0) {
				monesko = monesko + 1;
				sanamaara = sanamaara +1;
				
				if (monesko == 1) {
					mysterytextmodified = ss;
				}
				else {
					mysterytextmodified = mysterytextmodified + " " + ss;
				}
			}
		}

		String[] sanat2 = mysterytextmodified.split(" ");

		monesko = 0;
		
		for (String sana : sanat2) {
		
			Boolean olisana = false;
			
			Map<String, Double> sanapisteet = new HashMap();
			
			monesko = monesko + 1;

			if (usedcapwords > 0 && !olisana) {
				String sanamod = sana.replaceAll("[^\\p{L}\\p{M}′'’ ]", "");
				if (wordDictCap.containsRow(sanamod)) {
					olisana = true;
					pisteiterator = languageList.listIterator();
					while(pisteiterator.hasNext()) {
						Object element = pisteiterator.next();
						String pistelanguage = (String) element;
						if (wordDictCap.contains(sanamod,pistelanguage)) {
							sanapisteet.put(pistelanguage, wordDictCap.get(sanamod,pistelanguage));
						}
						else {
							sanapisteet.put(pistelanguage, gramsakko);
						}
					}
				}
			}

			if (usedlowwords > 0 && !olisana) {
				String sanamod = sana.replaceAll("[^\\p{L}\\p{M}′'’ ]", "");
				sanamod = sanamod.toLowerCase();
				if (wordDictLow.containsRow(sanamod)) {
					olisana = true;
					pisteiterator = languageList.listIterator();
					while(pisteiterator.hasNext()) {
						Object element = pisteiterator.next();
						String pistelanguage = (String) element;
						if (wordDictLow.contains(sanamod,pistelanguage)) {
							sanapisteet.put(pistelanguage, wordDictLow.get(sanamod,pistelanguage));
						}
						else {
							sanapisteet.put(pistelanguage, gramsakko);
						}
					}
				}
			}

			if (!olisana) {
				pisteiterator = languageList.listIterator();
				while(pisteiterator.hasNext()) {
					Object element = pisteiterator.next();
					String pistelanguage = (String) element;
					sanapisteet.put(pistelanguage, 0.0);
				}
			}
			sana = " " + sana + " ";


			int t = maksimingram;
			while (t > 0) {
				if (olisana) {
					break;
				}
				else {
					int pituus = sana.length();
					int x = 0;
					int grammaara = 0;
					if (pituus > (t-1)) {
						while (x < pituus - t + 1) {
							String gram = sana.substring(x,x+t);
							if (gramDictCap.containsRow(gram)) {
								grammaara = grammaara + 1;
								olisana = true;
								
								pisteiterator = languageList.listIterator();
								while(pisteiterator.hasNext()) {
									Object element = pisteiterator.next();
									String pistelanguage = (String) element;
									if (gramDictCap.contains(gram,pistelanguage)) {
										sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)+gramDictCap.get(gram,pistelanguage)));
									}
									else {
										sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)+gramsakko));
									}
								}
							}
							x = x + 1;
						}
					}
					if (olisana) {
						pisteiterator = languageList.listIterator();
						while(pisteiterator.hasNext()) {
							Object element = pisteiterator.next();
							String pistelanguage = (String) element;
							sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)/grammaara));
						}
					}
				}
				t = t -1 ;
			}

			if (!olisana) {
				pisteiterator = languageList.listIterator();
				while(pisteiterator.hasNext()) {
					Object element = pisteiterator.next();
					String pistelanguage = (String) element;
					sanapisteet.put(pistelanguage, 0.0);
				}
			}
			
			t = maksimingram;
			while (t > 0) {
				if (olisana) {
					break;
				}
				else {
					int pituus = sana.length();
					String sanamod = sana.toLowerCase();
					int x = 0;
					int grammaara = 0;
					if (pituus > (t-1)) {
						while (x < pituus - t + 1) {
							String gram = sanamod.substring(x,x+t);
							if (gramDictLow.containsRow(gram)) {
								grammaara = grammaara + 1;
								olisana = true;
								
								pisteiterator = languageList.listIterator();
								while(pisteiterator.hasNext()) {
									Object element = pisteiterator.next();
									String pistelanguage = (String) element;
									if (gramDictLow.contains(gram,pistelanguage)) {
										sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)+gramDictLow.get(gram,pistelanguage)));
									}
									else {
										sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)+gramsakko));
									}
								}
							}
							x = x + 1;
						}
					}
					if (olisana) {
						pisteiterator = languageList.listIterator();
						while(pisteiterator.hasNext()) {
							Object element = pisteiterator.next();
							String pistelanguage = (String) element;
							sanapisteet.put(pistelanguage, (sanapisteet.get(pistelanguage)/grammaara));
						}
					}
				}
				t = t -1 ;
			}			
			
			
			pisteiterator = languageList.listIterator();
			while(pisteiterator.hasNext()) {
				Object element = pisteiterator.next();
				String pistelanguage = (String) element;
				languagepisteet.put(pistelanguage, (languagepisteet.get(pistelanguage) + sanapisteet.get(pistelanguage)));
			}

		}
		
		String voittaja = "xx";
		languagepisteet.put(voittaja, 7.0);
		
		Double pienin = gramsakko + 1;
		
		pisteiterator = languageList.listIterator();
		while(pisteiterator.hasNext()) {
			Object element = pisteiterator.next();
			String pistelanguage = (String) element;
			
			languagepisteet.put(pistelanguage, (languagepisteet.get(pistelanguage)/sanamaara));
			if (languagepisteet.get(element) < pienin) {
				pienin = languagepisteet.get(element);
				voittaja = pistelanguage;
			}
		}

		return (voittaja);
	}
}
