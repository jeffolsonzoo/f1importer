package com.olsonzoo.fellowshipone

/**
 * Validates and performs cleanup on NSpire households.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */

class NspireValidator extends ValidatorBase {
    private static final String NSPIRE_DATE_FORMAT = 'MM/dd/yyyy'
    private static final String UNKNOWN = "Unknown"
    private int dummyPersonId = 10000;

    public void validateAndCorrect(Map<String, Household> households) {
        println "\n\nBeginning NSpire Validation\n"
        int headCount = 0
        households.values().each {hh ->
            println "------------\nValidating NSpire household ${hh.fullName}..."
            boolean headFound = false
            int headCountForHH = 0
            if (hh.individuals.size() == 0) println "No household members!"
            hh.individuals.values().each {person ->
                println "\nValidating ${person.fullName}"
                updateStatus(person)
                person.position = getPosition(hh, person)
                person.birthDate = fixDate(person.birthDate)
                person.school = fixSchool(person.school)
                person.ministry = fixMinistry(person.ministry)
                if (person.position == Individual.HEAD) {
                    headCount++
                    headCountForHH++
                    headFound = true
                }
                fixPhoneNumbers(hh, person)
                determineGraduationYear(person)
                println ""
            }
            fixEmail(hh)

            if (!headFound) {
                handleHeadNotFound(hh)
            }
            if (headCountForHH > 1) println "ERROR: too many heads (${headCountForHH}) for ${hh.fullName}"

        }
        println "\nhhCount=${households.values().size()}"
        println "headCount=$headCount"
        println "All NSpire households after validation:"
        
        households.values().each {hh ->
            println "\nHousehold: ${hh.fullName}"

            int hc = 0
            hh.individuals.values().each {person ->
                if (person.position == Individual.HEAD) {
                    hc++
                }
                println "\tPerson: ${person.fullName}\t\t${person.position}"
            }
            if (hc == 0 && hh.individuals.size() > 0) println "No head!"
            if (hc > 1) println "Too many heads!"
        }

    }

    private String fixSchool(String school) {
        if (school?.toLowerCase() == 'none') {
            return ""
        } else {
            return school
        }
    }

    private String fixMinistry(String ministry) {
        if (ministry?.toLowerCase() == 'unassigned') {
            return ""
        } else {
            return ministry
        }
    }

    // see http://fellowshipone.custhelp.com/cgi-bin/fellowshipone.cfg/php/enduser/std_adp.php?p_faqid=233
    private void determineGraduationYear(Individual person) {
        if (person.grade && person.position == Individual.CHILD) {
            println "Determining graduation year for grade=$person.grade"
            /* Trying to figure out all the age/grade stuff for pre-elementary ages
           1/1/10 - 0 yr 5 mo - 5 years old at 1/1/15, start K 9/1/15, grad K 6/1/16, grad 12 6/1/28
           1/1/09 - 1 yr 5 mo - 5 years old at 1/1/14, start K 9/1/14, grad K 6/1/15, grad 12 6/1/27
           1/1/08 - 2 yr 5 mo - 5 years old at 1/1/13, start K 9/1/13, grad K 6/1/14, grad 12 6/1/26
           1/1/07 - 3 yr 5 mo - 5 years old at 1/1/12, start K 9/1/12, grad K 6/1/13, grad 12 6/1/25
           1/1/06 - 4 yr 5 mo - 5 years old at 1/1/11, start K 9/1/11, grad K 6/1/12, grad 12 6/1/24
           1/1/05 - 5 yr 5 mo - 5 years old at 1/1/10, start K 9/1/10, grad K 6/1/11, grad 12 6/1/23
           1/1/04 - 6 yr 5 mo - 5 years old at 1/1/09, start K 9/1/09, grad K 6/1/10, grad 12 6/1/22 - in K now
            */
            // note: this will work only if run between May & August 2010.  If run later than that, will need to modify
            switch (person.grade) {
                case 'Birth to 12 Months':
                    person.graduationYear = '2028 - Birth to 12 Months'
                    break
                case '12 to 24 Months':
                    person.graduationYear = '2027 - 12 to 24 Months'
                    break
                case '2 to 3 Years':
                    person.graduationYear = '2026 - 2 to 3 Years'
                    break
                case '3 to 4 Years':
                    person.graduationYear = '2025 - 3 to 4 Years'
                    break
                case 'Pre-kindergarten':
                    // determine if they are going into Kindergarten next year or not
                    if (person.birthDate) {
                        println "birthdate for pre-k child: $person.birthDate"
                        def parsedBD = new Date().parse(ValidatorBase.F1_DATE_FORMAT, person.birthDate)
                        def cutoffFor2023 = new Date().parse(ValidatorBase.F1_DATE_FORMAT, "2005-09-01")
                        if (parsedBD >= cutoffFor2023) {
                            person.graduationYear = '2024 - 4 to 5 Years'
                        } else {
                            person.graduationYear = '2023 - Pre-kindergarten'
                        }
                    } else {
                        println "WARNING: no birthdate for pre-k child; have to guess on grad year"
                        person.graduationYear = '2023 - Pre-kindergarten'
                    }
                    break
                case 'Kindergarten':
                    person.graduationYear = '2022 - Kindergarten'
                    break
                case 'First Grade':
                    person.graduationYear = '2021 - 1st Grade'
                    break
                case 'Second Grade':
                    person.graduationYear = '2020 - 2nd Grade'
                    break
                case 'Third Grade':
                    person.graduationYear = '2019 - 3rd Grade'
                    break
                case 'Fourth Grade':
                    person.graduationYear = '2018 - 4th Grade'
                    break
                case 'Fifth Grade':
                    person.graduationYear = '2017 - 5th Grade'
                    break
                case 'Sixth Grade':
                    person.graduationYear = '2016 - 7th Grade in Fall'
                    break
                case 'Seventh Grade':
                    person.graduationYear = '2015 - 7th Grade'
                    break
                case 'Eighth Grade':
                    person.graduationYear = '2014 - 8th Grade'
                    break
                case 'Ninth Grade':
                    person.graduationYear = '2013 - 9th Grade'
                    break
                case 'Tenth Grade':
                    person.graduationYear = '2012 - 10th Grade'
                    break
                case 'Eleventh Grade':
                    person.graduationYear = '2011 - 11th Grade'
                    break
                case 'Twelfth Grade':
                    person.graduationYear = '2010 - 12th Grade'
                    break
                case 'Post High School':
                case 'College Freshman':
                case 'College Junior':
                case 'College Senior':
                case 'College Sophomore':
                case 'Post College':
                default:
                    person.graduationYear = ''
                    break
            }
            println "Graduation year = $person.graduationYear"
        }
    }

    private void fixPhoneNumbers(Household hh, Individual person) {
        if (person.emergencyPhone) {
            println "emergencyPhone before: $person.emergencyPhone"
            // emergency phone number only split into comment if follows pattern of "Name: 999-999-9999" where number can be any form of phone #
            if (person.emergencyPhone ==~ /(\D+?):?\s*([0-9(-]+)/) {
                def m
                if ((m = (person.emergencyPhone =~ /(\D+?):?\s*([0-9(].*)/))) {
                    person.emergencyPhoneComment = "${m.group(1)}"
                    person.emergencyPhone = "${m.group(2)}"
                }
            }
            println "emergencyPhone after : $person.emergencyPhone"
            println "emergencyPhoneComment: $person.emergencyPhoneComment"
        }
    }

    private def handleHeadNotFound(Household hh) {
        println "no head for ${hh.fullName}"
        if (hh.individuals.size() > 0) {
            Individual spouse = hh.individuals.values().find {it.position == Individual.SPOUSE}
            if (spouse) {
                println "Changing ${spouse.fullName} from SPOUSE to HEAD"
                spouse.position = Individual.HEAD
            } else {
                println "...creating dummy parents"
                createDummyParents(hh)
            }
        } else {
            println "...but no household members, so skipping"
        }
    }

    private def createDummyParents(Household hh) {

        String lastName = ""
        def firstNames = []
        def m
        if ((m = (hh.fullName =~ /([^,]+)(,\s*)?(.*)/))) {
            lastName = "${m.group(1).trim()}"
            String firstName = "${m.group(3).trim()}"
            if (firstName.contains("&")) {
                firstName.split("&").each {
                    firstNames << it.trim()
                }
            } else if (firstName.contains(" and ")) {
                firstName.split(" and ").each {
                    firstNames << it.trim()
                }
            } else if (firstName.isEmpty()) {
                firstNames << UNKNOWN
            } else {
                firstNames << firstName
            }
        }
        println "ln=$lastName"
        firstNames.eachWithIndex {name, i ->
            println "fn$i=$name"
        }

        Individual existing = hh.individuals.values().find {it.firstName == firstNames[0]}
        if (existing) {
            println "${firstNames[0]} already exists, with position=${existing.position}!  Creating dummy parent with first name = $UNKNOWN"
            firstNames[0] = UNKNOWN
        }

        if (lastName && firstNames[0]) {
            createDummyParent(firstNames[0], lastName, Individual.HEAD, hh)
        }
        if (lastName && firstNames[1]) {
            createDummyParent(firstNames[1], lastName, Individual.SPOUSE, hh)
        }
    }

    private def createDummyParent(String firstName, String lastName, String position, Household hh) {
        Individual person = new Individual()
        person.householdId = hh.id
        person.id = getNextDummyPersonId()
        println "Dummy person id=${person.id}"
        person.firstName = firstName
        person.lastName = lastName
        person.position = position
        person.statusGroup = Individual.ATTENDEE
        person.status = Individual.ATTENDEE
        hh.individuals.put(person.id, person)
    }

    private String getPosition(Household hh, Individual person) {
        String position
        println "hh.individuals.size(): ${hh.individuals.size()}"
        if (person.position == 'Parent/Adult' && (person.gender == 'Male' || hh.individuals.size() == 1)) {
            position = Individual.HEAD
        } else if (person.position == 'Parent/Adult') {
            position = Individual.SPOUSE
        } else if (person.position.toLowerCase().startsWith('pca')) {
            position = Individual.HEAD
        } else {
            position = Individual.CHILD
        }
        println "position=$position"
        return position
    }

    public String getDateFormat() {
        return NSPIRE_DATE_FORMAT
    }

    // this is a hack because we could theoretically collide with existing person IDs from both Helpmate or Nspire, but it will work for our data

    private int getNextDummyPersonId() {
        return dummyPersonId++;
    }

    private void updateStatus(Individual person) {
        switch (person.status.toLowerCase()) {
            case 'member':
                person.status = Individual.ATTENDEE
                person.statusComment = 'NSpire Member'
                break;
            case 'non-member':
                person.status = Individual.ATTENDEE
                person.statusComment = 'NSpire Non-Member'
                break;
            case 'visitor':
                person.status = Individual.ATTENDEE
                person.statusComment = 'NSpire Visitor'
                break;
            default:
                println "Unknown status for ${person.fullName}: ${person.status}"
                person.status = Individual.ATTENDEE
                person.statusComment = 'Unknown'
                break;
        }
        if (person.active.toLowerCase() == 'inactive') {
            person.statusGroup = 'Inactive'
        } else {
            person.statusGroup = Individual.ATTENDEE
        }
    }
}