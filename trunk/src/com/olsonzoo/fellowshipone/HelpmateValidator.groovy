package com.olsonzoo.fellowshipone

import org.codehaus.groovy.runtime.TimeCategory

/**
 * Validates and performs cleanup on Church Helpmate households.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class HelpmateValidator extends ValidatorBase {
    final def HELPMATE_DATE_FORMAT = 'MM/dd/yyyy H:mm'

    public void validateAndCorrect(Map<String, Household> households) {
        println "\nBeginning Helpmate Validation\n"
        int headCount = 0
        households.values().each {hh ->
            println "\nValidating Helpmate household ${hh.fullName}..."
            boolean headFound = false
            int headCountForHH = 0
            hh.individuals.values().each {person ->
                updateStatus(person)
                person.position = getPosition(hh, person)
                person.envelopeNumber = getEnvelopeNumber(hh, person)
                person.gender = (person.gender == 'Unknown' ? '' : person.gender)
                fixDates(person)
                person.maritalStatus = getMaritalStatus(person)
                if (person.position == Individual.HEAD) {
                    headCount++
                    headCountForHH++
                    headFound = true
                }
            }

            swapAddresses(hh)
            fixAddresses(hh)
            fixPhoneNumbers(hh)
            fixEmail(hh)
            fixUnlisted(hh)

            if (!headFound) println "ERROR: no head for ${hh.fullName}"
            if (headCountForHH > 1) println "ERROR: too many heads (${headCountForHH}) for ${hh.fullName}"
            if (hh.individuals.size() == 0) {
                println "No household members!"
            }
        }
        println "headCount=$headCount"
        println "hhCount=${households.values().size()}"
        println "removing all empty households"
        Collection<Household> emptyHouseholds = households.values().findAll {it.individuals.size() == 0}
        emptyHouseholds.each {households.remove(it.id)}
        println "hhCount=${households.values().size()}"
    }

    private String getEnvelopeNumber(Household hh, Individual person) {
        if (!person.envelopeNumber && person.position == Individual.HEAD && hh.envelopeNumber) {
            println "Assigning envelope #${hh.envelopeNumber} to ${person.fullName} who is head of household ${hh.fullName}"
            return hh.envelopeNumber
        } else if (person.envelopeNumber) {
            println "${person.fullName} already has envelope #${person.envelopeNumber}"
            return person.envelopeNumber
        } else {
            println "No envelope for ${person.fullName} in ${hh.fullName}"
            return ""
        }
    }

    private def fixDates(Individual person) {
        // birth
        if (person.birthDate) println "person.birthDate: ${person.birthDate}"
        person.birthDate = fixDate(person.birthDate)

        // anniversary
        if (!person.anniversaryDate && person.anniversaryComment) {
            person.anniversaryDate = guessDateFromComment(person.anniversaryComment)
        }
        person.anniversaryDate = fixDate(person.anniversaryDate)
        if (person.anniversaryDate) println "person.anniversaryDate: ${person.anniversaryDate}"
        if (person.anniversaryComment) println "person.anniversaryComment: ${person.anniversaryComment}"

        // baptism
        if (!person.baptismDate && person.baptismComment) {
            person.baptismDate = guessDateFromComment(person.baptismComment)
        }
        person.baptismDate = fixDate(person.baptismDate)
        if (person.baptismDate) println "person.baptismDate: ${person.baptismDate}"
        if (person.baptismComment) println "person.baptismComment: ${person.baptismComment}"

        // confirmation
        if (!person.confirmationDate && person.confirmationComment) {
            person.confirmationDate = guessDateFromComment(person.confirmationComment)
        }
        person.confirmationDate = fixDate(person.confirmationDate)
        if (person.confirmationDate) println "person.confirmationDate: ${person.confirmationDate}"
        if (person.confirmationComment) println "person.confirmationComment: ${person.confirmationComment}"

        // membership
        if (!person.membershipDate && person.membershipComment) {
            person.membershipDate = guessDateFromComment(person.membershipComment)
        }
        person.membershipDate = fixDate(person.membershipDate)
        if (person.membershipDate) println "person.membershipDate: ${person.membershipDate}"
        if (person.membershipComment) println "person.membershipComment: ${person.membershipComment}"
    }

    private String guessDateFromComment(String comment) {
        def m
        def time = "0:00"
        comment = comment.trim()

        // "02/26/1995 dedicated"
        if ((m = (comment =~ /(\d\d?)\/(\d\d?)\/(\d{4})/))) {
            return "${m.group(1).padLeft(2, "0")}/${m.group(2).padLeft(2, "0")}/${m.group(3)} $time"
        }
        //  "dedicated 5/31/98"
        if ((m = (comment =~ /(\d\d?)\/(\d\d?)\/(\d{2})/))) {
            return "${m.group(1).padLeft(2, "0")}/${m.group(2).padLeft(2, "0")}/19${m.group(3)} $time"
        }
        // ??/??/1977 -> 1/1/1977
        // "??/??/1941 13 or 14 years old" -> 1/1/1941
        if ((m = (comment =~ /\?{2}\/\?{2}\/(\d{4})/))) {
            return "01/01/${m.group(1)} $time"
        }
        // 04/??/1928 -> 4/1/1928
        if ((m = (comment =~ /(\d\d?)\/\?{2}\/(\d{4})/))) {
            return "${m.group(1).padLeft(2, "0")}/01/${m.group(2)} $time"
        }
        // 4/48 -> 4/1/1948
        // 6/36 -> 6/1/1936
        if ((m = (comment =~ /(\d\d?)\/(\d\d)/))) {
            return "${m.group(1).padLeft(2, "0")}/01/19${m.group(2)} $time"
        }
        // 1942 -> 1/1/1942
        // "Profession of faith Spring of 1967" -> 1/1/1967
        // 1956-1964 -> 1/1/1956
        // "1985--exact date unknown." -> 1/1/1985
        if ((m = (comment =~ /(\d{4})/))) {
            return "01/01/${m.group(1)} $time"
        }

        // default...punt
        return "01/01/9999 $time"
    }

    /** Swaps primary & secondary addresses & phone numbers if secondary is active.      */
    private def swapAddresses(Household hh) {
        if (hh.activeAddress == '2') {
            println "Swapping addresses for ${hh.fullName}"
            swapVariables(hh, "Address1")
            swapVariables(hh, "Address2")
            swapVariables(hh, "City")
            swapVariables(hh, "State")
            swapVariables(hh, "PostalCode")
            swapVariables(hh, "Country")
            swapVariables(hh, "Phone1")
            swapVariables(hh, "Phone1unlisted")
            swapVariables(hh, "Phone2")
            swapVariables(hh, "Phone2unlisted")
        }
    }

    private def fixAddresses(Household hh) {
        // MPLS
        hh.primaryCity = hh.primaryCity.replace('MPLS', "Minneapolis")
        hh.secondaryCity = hh.secondaryCity.replace('MPLS', "Minneapolis")

        hh.primaryCountry = fixCountry(hh.primaryCountry)
        hh.secondaryCountry = fixCountry(hh.secondaryCountry)
        if (hh.primaryCountry) println "hh.primaryCountry: ${hh.primaryCountry}"
        if (hh.secondaryCountry) println "hh.secondaryCountry: ${hh.secondaryCountry}"

        // special fix for one address:
        if (hh.primaryState == 'FUKUOKA-KEN') {
            hh.primaryCity += ', FUKUOKA-KEN'
            hh.primaryState = null
            println "primaryCity: ${hh.primaryCity}"
            println "primaryState: ${hh.primaryState}"
        }

        // if no address line 1, we'll skip outputting it, but we want to save the address comment (from directions field in Helpmate) if exists
        if (!hh.primaryAddress1 && hh.primaryDirections) {
            hh.addressNotes << hh.primaryDirections
        }
        if (!hh.secondaryAddress1 && hh.secondaryDirections) {
            hh.addressNotes << hh.secondaryDirections
        }
    }

    // Format countries: if not in list return original.  There are more of course, but these are all we need for our data.
    private String fixCountry(String country) {
        switch (country.trim().toUpperCase()) {
            case 'CAMEROON':
                return 'CM - Cameroon'
            case 'CANADA':
                return 'CA - Canada'
            case 'CHINA':
                return 'CN - China'
            case 'COSTA RICA':
                return 'CR - Costa Rica'
            case 'FRANCE':
                return 'FR - France'
            case 'INDONESIA':
                return 'ID - Indonesia'
            case 'JAPAN':
                return 'JP - Japan'
            case 'PHILIPPINES':
                return 'PH - Philippines'
            case 'SINGAPORE':
                return 'SG - Singapore'
            default:
                return country
        }
    }

    private def swapVariables(hh, field) {
        // this is ugly but I couldn't get a more sane version working
        def temp = hh["secondary$field"]
        hh["secondary$field"] = hh["primary$field"]
        hh["primary$field"] = temp
    }

    private def fixPhoneNumbers(Household hh) {
        // See if indiv phone matches hh; if so, remove from hh
        hh.individuals.values().each {person ->
            [person.cellPhone, person.altPhone, person.workPhone].each {personNum ->
                [hh.primaryPhone1, hh.primaryPhone2, hh.secondaryPhone1, hh.secondaryPhone2].each {hhNum ->
                    def p = stripPhoneNumber(personNum)
                    def h = stripPhoneNumber(hhNum)
                    if (personNum && hhNum && p == h) {
                        println "Clearing phone number ${hhNum} for ${hh.fullName}; matches number for ${person.firstName}"
                        // this is a bit of a hack...is there a better way in Groovy to tell which field was matched?
                        if (hh.primaryPhone1 == hhNum) hh.primaryPhone1 = ''
                        if (hh.primaryPhone2 == hhNum) hh.primaryPhone2 = ''
                        if (hh.secondaryPhone1 == hhNum) hh.secondaryPhone1 = ''
                        if (hh.secondaryPhone2 == hhNum) hh.secondaryPhone2 = ''
                    }
                }
            }
        }
    }

    private def fixUnlisted(Household hh) {
        hh.primaryPhone1unlisted == '1' ? (hh.primaryPhone1unlisted = 'Yes') : (hh.primaryPhone1unlisted = '')
        hh.primaryPhone2unlisted == '1' ? (hh.primaryPhone2unlisted = 'Yes') : (hh.primaryPhone2unlisted = '')
        hh.secondaryPhone1unlisted == '1' ? (hh.secondaryPhone1unlisted = 'Yes') : (hh.secondaryPhone1unlisted = '')
        hh.secondaryPhone2unlisted == '1' ? (hh.secondaryPhone2unlisted = 'Yes') : (hh.secondaryPhone2unlisted = '')
        hh.emailUnlisted == '1' ? (hh.emailUnlisted = 'Yes') : (hh.emailUnlisted = '')

        hh.individuals.values().each {person ->
            person.cellPhoneUnlisted == '1' ? (person.cellPhoneUnlisted = 'Yes') : (person.cellPhoneUnlisted = '')
            person.pagerUnlisted == '1' ? (person.pagerUnlisted = 'Yes') : (person.pagerUnlisted = '')
            person.altPhoneUnlisted == '1' ? (person.altPhoneUnlisted = 'Yes') : (person.altPhoneUnlisted = '')
            person.workPhoneUnlisted == '1' ? (person.workPhoneUnlisted = 'Yes') : (person.workPhoneUnlisted = '')
            person.emailAddress1Unlisted == '1' ? (person.emailAddress1Unlisted = 'Yes') : (person.emailAddress1Unlisted = '')
            person.emailAddress2Unlisted == '1' ? (person.emailAddress2Unlisted = 'Yes') : (person.emailAddress2Unlisted = '')
        }
    }


    private String stripPhoneNumber(String number) {
        if (!number) {
            return ""
        }
        return number.replaceAll("\\D", "").trim(); // removes non-numeric for comparison
    }

    private void updateStatus(Individual person) {
        switch (person.status.toLowerCase()) {
            case 'child of member':
                person.statusGroup = Individual.ATTENDEE
                person.status = 'Child of Member'
                break;
            case 'deceased':
                person.statusGroup = 'Deceased'
                person.status = 'Deceased'
                break;
            case 'historical':
                person.statusGroup = 'Inactive'
                person.status = 'Attendee' // Note: these might be Inactive Members, but no way to tell
                break;
            case 'inactive':
                person.statusGroup = 'Inactive'
                person.status = 'Attendee'
                break;
            case 'living love church':
                person.statusGroup = 'Inactive'
                person.status = 'Attendee'
                person.statusComment = 'Living Love Church'
                break;
            case 'member':
                person.statusGroup = 'Member'
                person.status = 'Member'
                break;
            case 'member elsewhere':
                person.statusGroup = 'Inactive'
                person.status = 'Attendee' // Note: these might be either inactive members or dropped, but I don't know which
                person.statusComment = 'Member Elsewhere'
                break;
            case 'member/shut-in':
                person.statusGroup = 'Member'
                person.status = 'Member'
                person.subStatus = 'Shut-in'
                break;
            case 'missionary':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Missionary'
                break;
            case 'missionary/ newsletter':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Missionary'
                person.statusComment = 'Missionary/Newsletter'
                break;
            case 'newsletter':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Newsletter'
                break;
            case 'regular':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Regular'
                break;
            case 'regular/ youth group':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Regular'
                person.statusComment = 'Regular/Youth Group'
                break;
            case 'unknown':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.statusComment = 'Unknown'
                break;
            case 'vbs':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.statusComment = 'VBS'
                break;
            case 'vbs historical':
                person.statusGroup = 'Inactive'
                person.status = 'Attendee'
                person.statusComment = 'VBS Historical'
                break;
            case 'visitor':
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.subStatus = 'Visitor'
                break;
            default:
                println "Unknown status for ${person.fullName}: ${person.status}"
                person.statusGroup = 'Attendee'
                person.status = 'Attendee'
                person.statusComment = 'Unknown'
                break;
        }
    }

    private String getPosition(Household hh, Individual person) {
        String position
        println "hh.individuals.size(): ${hh.individuals.size()}" 
        if (hh.mainContactId == person.id || hh.individuals.size() == 1) {
            position = Individual.HEAD
        } else if (person.guardian == '1') {
            position = Individual.SPOUSE
        } else {
            position = Individual.CHILD
        }
        return position
    }

    private String getMaritalStatus(Individual person) {
        switch (person.maritalStatus) {
            case 'Separated':
                person.maritalStatusNote = 'Separated'
                return 'Married'
            case 'Single':
                // check age
                if (isMinor(person)) {
                    return 'Chld/Yth'
                } else {
                    return 'Single'
                }
            case 'Unknown':
                return ''
            case 'Widow/er':
            case 'Widowed':
                if (person.gender == 'Male') {
                    return 'Widower'
                } else {
                    return 'Widowed'
                }
            default:
                return person.maritalStatus
        }
    }

    public String getDateFormat() {
        return HELPMATE_DATE_FORMAT
    }
    
    private boolean isMinor(Individual person) {
        if (person.birthDate) {
            Date now = new Date()
            Date birthDate = new Date().parse(F1_DATE_FORMAT, person.birthDate as String)
            //noinspection GroovyAssignabilityCheck
            use(TimeCategory) {
                return (((birthDate + 18.years) as Date) > now)
            }
        } else {
            return person.position == 'Child of Member'
        }
    }

}
