package com.olsonzoo.fellowshipone

import au.com.bytecode.opencsv.CSVReader
import com.google.common.collect.Maps

/**
 * Parses the NSpire CSV files.
 *
 * I created the .csv files by creating custom reports from the "All Households" and "All Individuals"
 * reports in NSpire (with both Active and Inactive included).  I picked out the fields I thought would be relevant.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class NspireParser {
    def inputDir = "../NSpire/"
    def householdCSV = "hh.csv"
    def individualCSV = "indiv.csv"

    /**
     * Parses the households and individuals and populates a map.
     */
    Map<String, Household> parseHouseholds() {
        println "\nBeginning parse of Nspire data..."
        CSVReader reader = new CSVReader(new FileReader(inputDir + householdCSV));
        List<String[]> inputHouseholds = reader.readAll();

        Map<String, Household> households = Maps.newHashMapWithExpectedSize(1000)
        for (int i = 1 /*skip title row*/; i < inputHouseholds.size(); i++) {
            String[] input = inputHouseholds.get(i);
            Household hh = new Household()
            hh.id = input[0]
            hh.fullName = input[1]
            hh.primaryAddress1 = input[2]
            hh.primaryAddress2 = input[3]
            hh.primaryCity = input[4]
            hh.primaryState = input[5]
            hh.primaryPostalCode = input[6]
            hh.primaryPhone1 = input[7] // default phone
            hh.primaryPhone1unlisted = input[8]
            hh.email = input[11]
            hh.website = input[12]
            hh.note1 = input[13]
            households.put(hh.id, hh)
        }

        parseIndividuals(households)

        return households
    }

    private void parseIndividuals(Map<String, Household> households) {
        CSVReader reader = new CSVReader(new FileReader(inputDir + individualCSV));
        List<String[]> inputIndividuals = reader.readAll();

        for (int i = 1 /*skip title row*/; i < inputIndividuals.size(); i++) {
            String[] input = inputIndividuals.get(i);
            Individual individual = new Individual()
            individual.householdId = input[0]
            individual.id = input[1]
            individual.lastName = input[2]
            individual.firstName = input[3]
            individual.middleName = input[4]
            individual.nickName = input[5]
            individual.suffix = input[6]
            individual.gender = input[7]
            individual.position = input[8]
            individual.school = input[9]
            individual.grade = input[10]
            individual.emailAddress1 = input[14]
            individual.cellPhone = input[16]
            individual.textMessage = input[17]
            individual.birthDate = input[19]
            individual.status = input[25]
            individual.active = input[26]
            individual.ministry = input[27]
            individual.website = input[32]
            individual.emergencyPhone = input[33]
            individual.instantMessageId = input[35]
            individual.currentChurch = input[36]
            individual.ethnicity = input[37]
            individual.hobbies = input[45]
            individual.ministryTeams = input[46]
            individual.generalNote = input[48]
            individual.miscNote = input[50]
            individual.counselingNote = input[51]

            // add individual to household
            def household = households[individual.householdId]
            if (household != null) {
                household.individuals[individual.id] = individual
            } else {
                println "No household found for individual id=${individual.id}; name=${individual.fullName}"
            }
        }

    }
}