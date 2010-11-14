package com.olsonzoo.fellowshipone

import au.com.bytecode.opencsv.CSVReader
import com.google.common.collect.Maps

/**
 * Parses the Church Helpmate CSV files.
 *
 * Note: the Helpmate files were created by adding the following macro to the 'CustomFrontEnd.mdb' file:
 *
 * @{code
 * Public Sub ExportAllTables_to_CSV()
 *   Dim obj As AccessObject, dbs As Object
 *   Set dbs = Application.CurrentData
 *
 *   For Each obj In dbs.AllTables
 *    If Left(obj.Name, 4) <> "MSys" Then
 *     Dim strFolder As String
 *     strFolder = "c:\temp\"
 *     DoCmd.TransferText acExportDelim, , obj.Name, strFolder & obj.Name & ".csv", True
 *    End If
 *   Next obj
 * End Sub
 * }
 *
 * This dumps out .csv files for each table to the c:\temp directory.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class HelpmateParser {
    def inputDir = "../ChurchHelpmate/"
    def householdCSV = "tblHOUSE.csv"
    def individualCSV = "tblIND.csv"
    def schoolCSV = "tblSCHOOL.csv"

    private Map<String, HelpmateSchool> schools = Maps.newHashMapWithExpectedSize(10)

    /**
     * Parses the households and individuals and populates a map.
     */
    Map<String, Household> parseHouseholds() {
        println "\nBeginning parse of Helpmate data..."
        CSVReader reader = new CSVReader(new FileReader(inputDir + householdCSV));
        List<String[]> inputHouseholds = reader.readAll();

        Map<String, Household> households = Maps.newHashMapWithExpectedSize(2000)
        for (int i = 1 /*skip title row*/; i < inputHouseholds.size(); i++) {
            String[] input = inputHouseholds.get(i);
            Household hh = new Household()
            hh.id = input[0]
            hh.lastName = input[3]
            hh.fullName = input[4].trim()
            hh.formalGreeting = input[6]
            hh.mainContactId = input[16]
            hh.status = input[21]
            hh.envelopeNumber = input[22].trim()
            hh.email = input[43]
            hh.emailUnlisted = input[44]
            hh.website = input[45]
            hh.note1 = input[46]
            hh.missionOrg = input[64]
            hh.activeAddress = input[81]
            hh.primaryAddress1 = input[83]
            hh.primaryAddress2 = input[84]
            hh.primaryCity = input[85]
            hh.primaryState = input[86]
            hh.primaryPostalCode = input[87]
            hh.primaryPhone1 = input[91]
            hh.primaryPhone1unlisted = input[92]
            hh.primaryPhone2 = input[93]
            hh.primaryPhone2unlisted = input[94]
            hh.primaryFax = input[95]
            hh.primaryCountry = input[96]
            hh.primaryDirections = input[102]
            hh.secondaryAddress1 = input[103]
            hh.secondaryAddress2 = input[104]
            hh.secondaryCity = input[105]
            hh.secondaryState = input[106]
            hh.secondaryPostalCode = input[107]
            hh.secondaryPhone1 = input[111]
            hh.secondaryPhone1unlisted = input[112]
            hh.secondaryPhone2 = input[113]
            hh.secondaryPhone2unlisted = input[114]
            hh.secondaryFax = input[115]
            hh.secondaryCountry = input[118]
            hh.secondaryDirections = input[124]
            hh.guid = input[141]

            households.put(hh.id, hh)
        }

        parseSchools()
        parseIndividuals(households)

        return households
    }

    private void parseSchools() {
        CSVReader reader = new CSVReader(new FileReader(inputDir + schoolCSV));
        List<String[]> inputSchools = reader.readAll();

        for (int i = 1 /*skip title row*/; i < inputSchools.size(); i++) {
            String[] input = inputSchools.get(i);
            HelpmateSchool school = new HelpmateSchool()
            school.id = input[0]
            school.name = input[1]
            schools.put(school.id, school)
        }
    }

    private void parseIndividuals(Map<String, Household> households) {
        CSVReader reader = new CSVReader(new FileReader(inputDir + individualCSV));
        List<String[]> inputIndividuals = reader.readAll();

        for (int i = 1 /*skip title row*/; i < inputIndividuals.size(); i++) {
            String[] input = inputIndividuals.get(i);
            Individual individual = new Individual()
            individual.id = input[0]
            individual.householdId = input[1]
            def school = schools.get(input[3])
            if (school != null) {
                individual.school = school.name
                println "school: ${school.name}"
            }
            individual.contribTypeId = input[4]
            individual.firstName = input[5]
            individual.nickName = input[6]
            individual.middleName = input[10]
            individual.lastName = input[11]
            individual.suffix = input[14]
            individual.title = input[15]
            individual.envelopeNumber = input[24].trim()
            individual.status = input[25]
            individual.gender = input[26]
            individual.maritalStatus = input[27]
            individual.mainContact = input[28]
            individual.guardian = input[29]
            individual.occupation = input[30]
            individual.birthDate = input[31]
            individual.anniversaryDate = input[33] // See http://fellowshipone.custhelp.com/cgi-bin/fellowshipone.cfg/php/enduser/std_adp.php?p_faqid=578
            individual.anniversaryComment = input[34] // put in notes
            individual.deceased = input[35]
            individual.deceasedDate = input[36]
            individual.deceasedComment = input[37] // put in notes
            individual.baptismDate = input[39]
            individual.baptismComment = input[40]
            individual.confirmationDate = input[42]
            individual.confirmationComment = input[43]
            individual.membershipDate = input[48]
            individual.membershipComment = input[49]
            individual.cellPhone = input[68]
            individual.cellPhoneUnlisted = input[69]
            individual.pager = input[70]
            individual.pagerUnlisted = input[71]
            individual.altPhone = input[72]
            individual.altPhoneUnlisted = input[73]
            individual.emailAddress1 = input[74]
            individual.emailAddress1Unlisted = input[75]
            individual.emailAddress2 = input[76]
            individual.emailAddress2Unlisted = input[77]
            individual.website = input[78]
            individual.employer = input[79]
            individual.workPhone = input[80]
            individual.workPhoneUnlisted = input[81]
            individual.workFax = input[83]
            individual.generalNote = input[88]
            individual.relationship = input[106]
            individual.guid = input[134]

            // add individual to household
            def household = households[individual.householdId]
            if (household != null) {
                household.individuals[individual.id] = individual
            } else {
                println "No household found for individual id=${individual.id}; name=$firstName $lastName"
            }
        }

    }
}