/*
 * Copyright (c) 2013 LDBC
 * Linked Data Benchmark Council (http://ldbc.eu)
 *
 * This file is part of ldbc_socialnet_dbgen.
 *
 * ldbc_socialnet_dbgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ldbc_socialnet_dbgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ldbc_socialnet_dbgen.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2011 OpenLink Software <bdsmt@openlinksw.com>
 * All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation;  only Version 2 of the License dated
 * June 1991.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ldbc.socialnet.dbgen.dictionary;

import ldbc.socialnet.dbgen.util.RandomGeneratorFarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * This class reads the file containing the names and countries for the companies used in the ldbc socialnet generation and 
 * provides access methods to get such data.
 */
public class CompanyDictionary {
    
    private static final String SEPARATOR = "  ";
    
	String fileName;

    TreeMap<Long, String>   companyName;
	TreeMap<Long, Integer> companyLocation;
	TreeMap<Integer, Vector<Long>> companiesByLocations;
	LocationDictionary locationDic;
	
    double probUnCorrelatedCompany;
	
    /**
     * Constructor.
     * 
     * @param fileName: The file with the company data.
     * @param locationDic: The location dictionary.
     * @param probUnCorrelatedCompany: Probability of selecting a country unrelated company.
     */
	public CompanyDictionary(String fileName, LocationDictionary locationDic, 
	         double probUnCorrelatedCompany) {
	    
		this.fileName = fileName;
		this.locationDic = locationDic;
		this.probUnCorrelatedCompany = probUnCorrelatedCompany;
	}

	/**
     * Initializes the dictionary extracting the data from the file.
     */
	public void init() {
        companyName = new TreeMap<Long,String>();
	    companyLocation = new TreeMap<Long, Integer>();
	    companiesByLocations = new TreeMap<Integer, Vector<Long>>();
	    for (Integer id : locationDic.getCountries()){
	        companiesByLocations.put(id, new Vector<Long>());
	    }
	    try {
            BufferedReader dictionary = new BufferedReader(
                    new InputStreamReader(getClass( ).getResourceAsStream(fileName), "UTF-8"));
            String line;  
            int previousId = -2;
            int currentId = -1; 
            long totalNumCompanies = 0;
            while ((line = dictionary.readLine()) != null) {
                String data[] = line.split(SEPARATOR);
                String locationName = data[0];
                String companyName  = data[1].trim();
                if (locationDic.getCountryId(locationName) != LocationDictionary.INVALID_LOCATION) {
                    currentId  = locationDic.getCountryId(locationName);
                    companiesByLocations.get(currentId).add(totalNumCompanies);
                    companyLocation.put(totalNumCompanies, currentId);
                    this.companyName.put(totalNumCompanies, companyName);
                    totalNumCompanies++;
                }
            }
            dictionary.close();
            System.out.println("Done ... " + totalNumCompanies + " companies were extracted");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	/**
	 * Gets the company country id.
	 */
	public int getCountry(Long company) {
	    return companyLocation.get(company);
	}
	
	/**
	 * Gets a random company of the input country. In case the given country doesn't any company
	 * a random one will be selected.
	 * @param countryId: A country id.
	 */
	public long getRandomCompany(RandomGeneratorFarm randomFarm, int countryId) {
		int locId = countryId;
		Vector<Integer> countries = locationDic.getCountries();
		if (randomFarm.get(RandomGeneratorFarm.Aspect.UNCORRELATED_COMPANY).nextDouble() <= probUnCorrelatedCompany) {
		    locId = countries.get(randomFarm.get(RandomGeneratorFarm.Aspect.UNCORRELATED_COMPANY_LOCATION).nextInt(countries.size()));
		}
		// In case the country doesn't have any company select another country.
		while (companiesByLocations.get(locId).size() == 0){
		    locId = countries.get(randomFarm.get(RandomGeneratorFarm.Aspect.UNCORRELATED_COMPANY_LOCATION).nextInt(countries.size()));
        }
		int randomCompanyIdx = randomFarm.get(RandomGeneratorFarm.Aspect.COMPANY).nextInt(companiesByLocations.get(locId).size());
		return companiesByLocations.get(locId).get(randomCompanyIdx);
	}

    public Set<Long> getCompanies() {
        return companyLocation.keySet();
    }

    public int getNumCompanies() {
        return companyName.size();
    }

    public String getCompanyName( long id ) {
        return companyName.get(id);
    }
}
