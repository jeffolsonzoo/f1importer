package com.olsonzoo.fellowshipone
import java.text.ParseException

/**
 * Base class for validators.
 * 
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
abstract class ValidatorBase {
    public static final String F1_DATE_FORMAT = 'yyyy-MM-dd'

    abstract void validateAndCorrect(Map<String, Household> households)
    
    abstract String getDateFormat()

    protected String fixDate(String input) {
        def output = ''
        if (input) {
            println "Input date: $input"
            Date parsedDate = null;
            try {
                parsedDate = new Date().parse(getDateFormat(), input)
            } catch (ParseException e) {
                println "ERROR: parsing date '${input}': ${e.message}"
            }
            if (parsedDate) {
                output = parsedDate.format(F1_DATE_FORMAT)
            }
            println "Output date: $output"
        }
        return output
    }

    protected def fixEmail(Household hh) {
        if (hh.email.count("@") > 1) {
            println "hh ${hh.fullName} has problem email: ${hh.email}; splitting into to additionalEmail"
            if (hh.emailUnlisted == '1') {
                println "ERROR: need to handle emailUnlisted after split"
            }
            def emails = hh.email.split() as List<String>
            // remove trailing comma or semicolon
            emails = emails.collect { it.replaceAll(/[,;]$/, "") }
            hh.email = emails[0]
            emails.remove(0)
            emails.each {email ->
                hh.additionalEmail << email
            }
        }
        if (hh.website) println "\thh.website: ${hh.website}"
        if (hh.website?.contains("@")) {
            println "hh ${hh.fullName} has problem website: ${hh.website}; moving to additionalEmail"
            hh.additionalEmail << hh.website
            hh.website = null
        }

        hh.individuals.values().each {person ->
            if (person.website) println "\tperson.website: ${person.website}"
            if (person.website?.contains("@")) {
                println "person ${person.fullName} has problem website: ${person.website}; moving to hh's additionalEmail"
                hh.additionalEmail << person.website
                person.website = null
            }

            if (person.emailAddress1?.count("@") > 1) {
                println "${person.fullName} has problem email in emailAddress1: ${person.emailAddress1}; will attempt split into emailAddress2"
                if (person.emailAddress2) {
                    println "WARNING: ${person.fullName} already has emailAddress2; can't split emailAddress1 into 2"
                } else {
                    def emails = person.emailAddress1.split() as List<String>
                    // remove trailing comma or semicolon
                    emails = emails.collect { it.replaceAll(/[,;]$/, "") }
                    if (emails.size() > 2) {
                        println "ERROR: more than two email addresses in person.emailAddress1 for $person.fullName"
                    }
                    person.emailAddress1 = emails[0]
                    person.emailAddress2 = emails[1]
                }
            }
        }

        if (hh.email) println "\thh.email: ${hh.email}"
        hh.additionalEmail.each {println "\thh.additionalEmail: $it"}
        hh.individuals.values().each {person ->
            if (person.emailAddress1) println "\t${person.fullName} p.email1 ${person.emailAddress1}"
            if (person.emailAddress2) println "\t${person.fullName} p.email2 ${person.emailAddress2}"
        }

        if (hh.individuals.size() == 1 || bothHeadAndSpouseHaveEmail(hh)) {
            hh.individuals.values().each {person ->
                [person.emailAddress1, person.emailAddress2].findAll {it /* exists */}.each {personalEmail ->
                    if (personalEmail == hh.email) {
                        println "Removing email address ${hh.email} from household; matches ${person.firstName}'s email"
                        hh.email = ''
                    }
                    int size = hh.additionalEmail.size()
                    hh.additionalEmail = hh.additionalEmail.findAll {!(personalEmail == it)} as List<String>
                    if (size > hh.additionalEmail.size()) {
                        println "Removing email address (additional) ${personalEmail} from household; matched ${person.firstName}'s email"
                    }
                }
            }
        }

        if (!hh.email && hh.additionalEmail) {
            println "Shifting additional email ${hh.additionalEmail[0]} into main email"
            hh.email = hh.additionalEmail[0]
            hh.additionalEmail.remove(0)
        }

        println "\t----------------------"
        if (hh.email) println "\thh.email: ${hh.email}"
        hh.additionalEmail.each {println "\thh.additionalEmail: $it"}
        hh.individuals.values().each {person ->
            if (person.emailAddress1) println "\t${person.fullName} p.email1 ${person.emailAddress1}"
            if (person.emailAddress2) println "\t${person.fullName} p.email2 ${person.emailAddress2}"
        }

    }

    protected boolean bothHeadAndSpouseHaveEmail(Household hh) {
        if (hh.individuals.size() > 1) {
            if (hh.individuals.values().find { person -> person.position == Individual.HEAD && (person.emailAddress1 || person.emailAddress2)}) {
                if (hh.individuals.values().find { person -> person.position == Individual.SPOUSE && (person.emailAddress1 || person.emailAddress2)}) {
                    return true;
                }
            }
        }
        return false;
    }


}
