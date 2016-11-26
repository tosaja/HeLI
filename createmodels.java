/*
    createmodels.java creates the language models used by the HeLI language identifier
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

import java.util.*;
import java.io.*;

class createmodels {

	private static BufferedWriter writer = null;

// main method goes through the directory "Training" and created language model files from each file ending with ".train".

	public static void main(String[] args) {
		
		File file = new File("Training");

		File[] files = file.listFiles();
		
		for (File file2 : files) {
			if (file2.getName().contains(".train")) {
				createmodel(file2);
			}
		}
	}

// createmodel method creates 18 files for unmodified and lowercased words and character n-grams from 1 to 8.

	private static void createmodel(File file) {
		int maxngram = 8;

		TreeMap<String,Integer> wordDictCap = new TreeMap<String,Integer>();
		TreeMap<String,Integer> wordDictLow = new TreeMap<String,Integer>();
		TreeMap<String,Integer> gramDictCap = new TreeMap<String,Integer>();
		TreeMap<String,Integer> gramDictLow = new TreeMap<String,Integer>();
		TreeMap<String,Integer> tempGramDict = new TreeMap<String,Integer>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			
			String line = "";
			
			int wordDictCapAmount = 0;
			int wordDictLowAmount = 0;
			int gramDictAmount = 0;

// the first replaceAll statement in the next while loop includes some characters we have encountered that are not recognized by the general regular expressions

			while ((line = reader.readLine()) != null) {
		
//
				line = line.replaceAll("[^\\p{L}\\p{M}′'’´ʹािीुूृेैोौंँः् া ি ী ু ূ ৃ ে ৈ ো ৌ।্্্я̄\\u07A6\\u07A7\\u07A8\\u07A9\\u07AA\\u07AB\\u07AC\\u07AD\\u07AE\\u07AF\\u07B0\\u0A81\\u0A82\\u0A83\\u0ABC\\u0ABD\\u0ABE\\u0ABF\\u0AC0\\u0AC1\\u0AC2\\u0AC3\\u0AC4\\u0AC5\\u0AC6\\u0AC7\\u0AC8\\u0AC9\\u0ACA\\u0ACB\\u0ACC\\u0ACD\\u0AD0\\u0AE0\\u0AE1\\u0AE2\\u0AE3\\u0AE4\\u0AE5\\u0AE6\\u0AE7\\u0AE8\\u0AE9\\u0AEA\\u0AEB\\u0AEC\\u0AED\\u0AEE\\u0AEF\\u0AF0\\u0AF1]", " ");

// We collapse all whitespaces and remove then from the end and the beginning

				line = line.replaceAll("  *", " ");
				line = line.replaceAll("^ ", "");
				line = line.replaceAll(" $", "");
				
				String[] words = line.split(" ");
			
				for (String word : words) {

// We populate the treeMap wordDictCap with words including capital letters

					if (wordDictCap.containsKey(word)) {
						wordDictCap.put(word, wordDictCap.get(word) + 1);
						wordDictCapAmount++;
					}
					else {
						wordDictCap.put(word, 1);
						wordDictCapAmount++;
					}

// We populate the treeMap wordDictLow with lowercased words
					
					if (wordDictLow.containsKey(word.toLowerCase())) {
						wordDictLow.put(word.toLowerCase(), wordDictLow.get(word.toLowerCase()) + 1);
						wordDictLowAmount++;
					}
					else {
						wordDictLow.put(word.toLowerCase(), 1);
						wordDictLowAmount++;
					}

// We insert a whitespace before and after the word. We populate the treeMaps gramDictCap and gramDictLow with character n-grams of sizes from 1 to maxngram.
					
					int t = maxngram;
					
					word = " " + word + " ";
					
					while (t > 0) {
						int pituus = word.length();
						int x = 0;
						if (pituus > (t-1)) {
							while (x < pituus - t + 1) {
								String gram = word.substring(x,x+t);
								if (gramDictCap.containsKey(gram)) {
									gramDictCap.put(gram, gramDictCap.get(gram) + 1);
								}
								else {
									gramDictCap.put(gram, 1);
								}

								if (gramDictLow.containsKey(gram.toLowerCase())) {
									gramDictLow.put(gram.toLowerCase(), gramDictLow.get(gram.toLowerCase()) + 1);
								}
								else {
									gramDictLow.put(gram.toLowerCase(), 1);
								}
								x = x + 1;
							}
						}
						t = t -1 ;
					}

				}
			}
			
			reader.close();

// We write the words with capital letters into a file
			File outfile = new File(file.getName().replaceAll(".train",".CapWordModel"));
			sorttaa(outfile.getName(), wordDictCap, wordDictCapAmount);

// We write the words with lowercased letters into a file
			outfile = new File(file.getName().replaceAll(".train",".LowWordModel"));
			sorttaa(outfile.getName(), wordDictLow, wordDictLowAmount);

// We write the ngrams with lowercased letters into 8 files
			int t = maxngram;
			while (t > 0) {
			
				gramDictAmount = 0;
				tempGramDict = new TreeMap<String,Integer>();
			
				Set gramdictset = gramDictLow.entrySet();
				Iterator gramdictseti = gramdictset.iterator();
				while (gramdictseti.hasNext()) {
					Map.Entry incominggram = (Map.Entry)gramdictseti.next();
					String gram = (String)incominggram.getKey();
					int amount = (int)incominggram.getValue();
					if (gram.length() == t) {
						tempGramDict.put(gram, amount);
						gramDictAmount = gramDictAmount + amount;
					}
				}
				
				outfile = new File(file.getName().replaceAll(".train",".LowGramModel"+t));
				sorttaa(outfile.getName(), tempGramDict, gramDictAmount);
				
				t = t -1 ;
			}

// We write the ngrams with capital letters into 8 files.
			t = maxngram;
			while (t > 0) {
			
				gramDictAmount = 0;
				tempGramDict = new TreeMap<String,Integer>();
			
				Set gramdictset = gramDictCap.entrySet();
				Iterator gramdictseti = gramdictset.iterator();
				while (gramdictseti.hasNext()) {
					Map.Entry incominggram = (Map.Entry)gramdictseti.next();
					String gram = (String)incominggram.getKey();
					int amount = (int)incominggram.getValue();
					if (gram.length() == t) {
						tempGramDict.put(gram, amount);
						gramDictAmount = gramDictAmount + amount;
					}
				}
				
				outfile = new File(file.getName().replaceAll(".train",".CapGramModel"+t));
				sorttaa(outfile.getName(), tempGramDict, gramDictAmount);
				
				t = t -1 ;
			}
			

			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void sorttaa(String filename, TreeMap<String,Integer> dict, int dictAmount) {
		try {
			File outfile = new File(filename.replaceAll("^","Models/"));

// We print out the file we are currently creating
			System.out.println(outfile.getName());
			
// We are always destroying the older files with the same name if such exist
			outfile.createNewFile();

			writer = new BufferedWriter(new FileWriter(outfile));

			writer.write(dictAmount + "\n");

// we go through the value sorted entrySet in reverse order and write each entry to a file.
			dict.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).forEachOrdered(s->kirjoita(s.getKey() + "\t" + s.getValue() + "\n"));
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void kirjoita(String s) {
		try {
			writer.write(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
