package com.olsonzoo.fellowshipone

/**
 * Merges the Helpmate & NSpire data.
 *
 * Approach:
 * - For each person in NSpire, if they are a child or youth, see if they were already in the Helpmate data.
 *   If so, assume that the NSpire data is more up to date and accurate and lean towards using it over the Helpmate data
 *   when creating the F1 output.  This means if both NSpire & Helpmate have an email address and they are different,
 *   I'll assume the NSpire record is correct
 *
 * - If the person from NSpire is an adult, and they are in Helpmate, assume that the Helpmate data is more accurate
 *   when creating the F1 output.
 *
 * - If the person from NSpire is a child/youth and they are not in Helpmate, find their parents in NSpire
 *   (using the household ID which ties all people from the same household) and see if either of their parents are in
 *   the Helpmate data.  If they are, then add the child/youth as a member of that household when creating the F1 output.
 *
 * - If the person from NSpire is an adult and is not in Helpmate, add them (along with their kids) into the F1 output.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class HouseholdMerger {
    Map<String, Household> hmHouseholds
    Map<String, Household> nHouseholds

    public Map<String, Household> merge() {
        println "\nBeginning Merge operation...\n\n"
        Map<String, Household> households = [:]
        households.putAll(hmHouseholds)

        List<Individual> hmPeople = []
        hmHouseholds.values().each {hmHH ->
            hmPeople.addAll(hmHH.individuals.values())
        }

        nHouseholds.values().each {nspireHH ->
            println "\nMerging Nspire Household $nspireHH.fullName"

            if (nspireHH.individuals.size() == 0) {
                println "...skipping; no people in household $nspireHH.fullName"
            } else {
                Household hmHH = findHelpmateMatchingHousehold(nspireHH, hmPeople)
                if (hmHH) {
                    doMerge(nspireHH, hmHH)
                } else {
                    println "No match found for NSpire household $nspireHH.fullName; adding directly to output"
                    households.put(nspireHH.id, nspireHH)
                }
            }
        }
        println "merged size: ${households.size()}"
        return households
    }

    private void doMerge(Household nspireHH, Household helpmateHH) {
        println "Beginning merge of Nspire household '$nspireHH.fullName' and Helpmate household '$helpmateHH.fullName'"

        mergeHouseholdFields(nspireHH, helpmateHH)
        mergeHeads(nspireHH, helpmateHH)
        mergeSpouses(nspireHH, helpmateHH)
        mergeChildren(nspireHH, helpmateHH)
    }

    private def mergeHouseholdFields(Household nspireHH, Household helpmateHH) {

        // merge household fields (Helpmate wins)
        if (!helpmateHH.primaryAddress1 && nspireHH.primaryAddress1) {
            println "using nspire primaryAddress1=$nspireHH.primaryAddress1"
            helpmateHH.primaryAddress1 = nspireHH.primaryAddress1
        }
        if (!helpmateHH.primaryAddress2 && nspireHH.primaryAddress2) {
            println "using nspire primaryAddress2=$nspireHH.primaryAddress2"
            helpmateHH.primaryAddress2 = nspireHH.primaryAddress2
        }
        if (!helpmateHH.primaryCity && nspireHH.primaryCity) {
            println "using nspire primaryCity=$nspireHH.primaryCity"
            helpmateHH.primaryCity = nspireHH.primaryCity
        }
        if (!helpmateHH.primaryState && nspireHH.primaryState) {
            println "using nspire primaryState=$nspireHH.primaryState"
            helpmateHH.primaryState = nspireHH.primaryState
        }
        if (!helpmateHH.primaryPostalCode && nspireHH.primaryPostalCode) {
            println "using nspire primaryPostalCode=$nspireHH.primaryPostalCode"
            helpmateHH.primaryPostalCode = nspireHH.primaryPostalCode
        }
        if (!helpmateHH.primaryPhone1 && nspireHH.primaryPhone1) {
            println "using nspire primaryPhone1=$nspireHH.primaryPhone1"
            helpmateHH.primaryPhone1 = nspireHH.primaryPhone1
        }
        if (!helpmateHH.email && nspireHH.email) {
            println "using nspire email=$nspireHH.email"
            helpmateHH.email = nspireHH.email
        }
        nspireHH.additionalEmail.each {email ->
            if (!helpmateHH.additionalEmail.collect {it.toLowerCase()}.contains(email.toLowerCase()) && helpmateHH.email.toLowerCase() != email.toLowerCase()) {
                println "using nspire additional email $email; adding to helpmate additional emails"
                helpmateHH.additionalEmail << email
            }
        }
        if (!helpmateHH.website && nspireHH.website) {
            println "using nspire website=$nspireHH.website"
            helpmateHH.website = nspireHH.website
        }
        if (nspireHH.note1) {
            if (helpmateHH.note1) {
                println "using nspire note1=$nspireHH.note1 as note2"
                helpmateHH.note2 = nspireHH.note1
            } else {
                println "using nspire note1=$nspireHH.note1"
                helpmateHH.note1 = nspireHH.note1
            }

        }
    }

    private def mergeHeads(Household nspireHH, Household helpmateHH) {
        Individual nsHead = findHead(nspireHH)
        Individual hmHead = findHead(helpmateHH)
        Individual hmSpouse = findSpouse(helpmateHH)

        // if Helpmate is missing a head (and the Nspire head isn't already the Helpmate spouse), add them
        if (!hmHead && (!hmSpouse || (nsHead.fullName.toLowerCase() != hmSpouse.fullName.toLowerCase()))) {
            println "Helpmate household $helpmateHH.fullName had no HEAD, so adding Nspire HEAD $nsHead.fullName"
            helpmateHH.individuals.put(nsHead.id, nsHead)
        }
        // see if Nspire head matches Helpmate head
        else if (nsHead.fullName.toLowerCase() == hmHead.fullName.toLowerCase()) {
            //  if so, add any missing fields from Nspire (don't change Helpmate fields)
            println "HEAD-HEAD merge: Beginning merge of Nspire HEAD '$nsHead.fullName' and Helpmate HEAD '$hmHead.fullName'"
            mergeAdult(nsHead, hmHead)
        }
        //  if not, see if if Nspire head matches Helpmate spouse
        else if (hmSpouse && nsHead.fullName.toLowerCase() == hmSpouse.fullName.toLowerCase()) {
            //   if so, add any missing fields from Nspire (don't change Helpmate fields)
            println "HEAD-SPOUSE merge: Beginning merge of Nspire HEAD '$nsHead.fullName' and Helpmate SPOUSE '$hmSpouse.fullName'"
            mergeAdult(nsHead, hmSpouse)
        } else {
            //   if not, what to do?
            println "WARNING: couldn't find match for Nspire HEAD '$nsHead.fullName'"
        }
    }

    private def mergeSpouses(Household nspireHH, Household helpmateHH) {
        Individual nsSpouse = findSpouse(nspireHH)
        Individual hmSpouse = findSpouse(helpmateHH)
        Individual hmHead = findHead(helpmateHH)

        // no spouse in NSpire; bail
        if (!nsSpouse) {
            return
        }

        // if Helpmate is missing a spouse (and the Nspire spouse isn't already the Helpmate head), add them
        if (!hmSpouse && (!hmHead || (nsSpouse.fullName.toLowerCase() != hmHead.fullName.toLowerCase()))) {
            println "Helpmate household $helpmateHH.fullName had no SPOUSE, so adding Nspire SPOUSE $nsSpouse.fullName"
            helpmateHH.individuals.put(nsSpouse.id, nsSpouse)
        }
        // see if Nspire spouse matches Helpmate spouse
        else if (hmSpouse && nsSpouse.fullName.toLowerCase() == hmSpouse.fullName.toLowerCase()) {
            //  if so, add any missing fields from Nspire (don't change Helpmate fields)
            println "SPOUSE-SPOUSE merge: Beginning merge of Nspire SPOUSE '$nsSpouse.fullName' and Helpmate SPOUSE '$hmSpouse.fullName'"
            mergeAdult(nsSpouse, hmSpouse)
        }
        //  if not, see if if Nspire spouse matches Helpmate head
        else if (nsSpouse.fullName.toLowerCase() == hmHead.fullName.toLowerCase()) {
            //   if so, add any missing fields from Nspire (don't change Helpmate fields)
            println "SPOUSE-HEAD merge: Beginning merge of Nspire SPOUSE '$nsSpouse.fullName' and Helpmate HEAD '$hmHead.fullName'"
            mergeAdult(nsSpouse, hmHead)
        } else {
            //   if not, what to do?
            println "WARNING: couldn't find match for Nspire SPOUSE '$nsSpouse.fullName'"
        }
    }

    private def mergeChildren(Household nspireHH, Household helpmateHH) {
        Collection<Individual> nsChildren = findChildren(nspireHH)
        Collection<Individual> hmChildren = findChildren(helpmateHH)

        nsChildren.each { nsChild->
            println "Beginning merge of Nspire CHILD '$nsChild.fullName'"

            // for each Nspire child, find matching Helpmate child
            List<Individual> matchingHmChildren = hmChildren.findAll {it.fullName.toLowerCase() == nsChild.fullName.toLowerCase()} as List
            if (matchingHmChildren.size() > 1) {
                println "ERROR: more than one matching Helpmate CHILD found for Nspire child '$nsChild.fullName'"
            } else if (matchingHmChildren.size() < 1) {
                //  if not found, add Nspire child to household
                println "Nspire child '$nsChild.fullName' not found in household; adding"
                helpmateHH.individuals.put(nsChild.id, nsChild)
            } else {
                //  if found, Nspire child's fields override Helpmate child
                def hmChild = matchingHmChildren.first()
                println "CHILD-CHILD merge: Beginning merge of Nspire CHILD '$nsChild.fullName' and Helpmate CHILD '$hmChild.fullName'"
                mergeChild(nsChild, hmChild)
            }
        }
    }

    private def mergeAdult(Individual nsPerson, Individual hmPerson) {
        if (!hmPerson.middleName && nsPerson.middleName) {
            println "using nspire middleName=$nsPerson.middleName"
            hmPerson.middleName = nsPerson.middleName
        }
        if (!hmPerson.nickName && nsPerson.nickName && (nsPerson.nickName != nsPerson.firstName) /* only nicknames that are diff't from first names*/) {
            println "using nspire nickName=$nsPerson.nickName"
            hmPerson.nickName = nsPerson.nickName
        }
        if (!hmPerson.suffix && nsPerson.suffix) {
            println "using nspire suffix=$nsPerson.suffix"
            hmPerson.suffix = nsPerson.suffix
        }
        if (!hmPerson.gender && nsPerson.gender) {
            println "using nspire gender=$nsPerson.gender"
            hmPerson.gender = nsPerson.gender
        }
        if (!hmPerson.school && nsPerson.school) {
            println "using nspire school=$nsPerson.school"
            hmPerson.school = nsPerson.school
        }
        if (!hmPerson.grade && nsPerson.grade) {
            println "using nspire grade=$nsPerson.grade"
            hmPerson.grade = nsPerson.grade
        }
        if (!hmPerson.graduationYear && nsPerson.graduationYear) {
            println "using nspire graduationYear=$nsPerson.graduationYear"
            hmPerson.graduationYear = nsPerson.graduationYear
        }
        if (!hmPerson.emailAddress1 && nsPerson.emailAddress1) {
            println "using nspire emailAddress1=$nsPerson.emailAddress1"
            hmPerson.emailAddress1 = nsPerson.emailAddress1
        }
        if (!hmPerson.emailAddress2 && nsPerson.emailAddress2) {
            println "using nspire emailAddress2=$nsPerson.emailAddress2"
            hmPerson.emailAddress2 = nsPerson.emailAddress2
        }
        if (!hmPerson.cellPhone && nsPerson.cellPhone) {
            println "using nspire cellPhone=$nsPerson.cellPhone"
            hmPerson.cellPhone = nsPerson.cellPhone
        }
        if (!hmPerson.textMessage && nsPerson.textMessage) {
            println "using nspire textMessage=$nsPerson.textMessage"
            hmPerson.textMessage = nsPerson.textMessage
        }
        if (!hmPerson.birthDate && nsPerson.birthDate) {
            println "using nspire birthDate=$nsPerson.birthDate"
            hmPerson.birthDate = nsPerson.birthDate
        }
        if (!hmPerson.status && nsPerson.status) {
            println "using nspire status=$nsPerson.status"
            hmPerson.status = nsPerson.status
        }
        if (!hmPerson.statusGroup && nsPerson.statusGroup) {
            println "using nspire statusGroup=$nsPerson.statusGroup"
            hmPerson.statusGroup = nsPerson.statusGroup
        }
        if (!hmPerson.statusComment && nsPerson.statusComment) {
            println "using nspire statusComment=$nsPerson.statusComment"
            hmPerson.statusComment = nsPerson.statusComment
        }
        if (!hmPerson.subStatus && nsPerson.subStatus) {
            println "using nspire subStatus=$nsPerson.subStatus"
            hmPerson.subStatus = nsPerson.subStatus
        }
        if (!hmPerson.ministry && nsPerson.ministry) {
            println "using nspire ministry=$nsPerson.ministry"
            hmPerson.ministry = nsPerson.ministry
        }
        if (!hmPerson.website && nsPerson.website) {
            println "using nspire website=$nsPerson.website"
            hmPerson.website = nsPerson.website
        }
        if (!hmPerson.emergencyPhone && nsPerson.emergencyPhone) {
            println "using nspire emergencyPhone=$nsPerson.emergencyPhone"
            hmPerson.emergencyPhone = nsPerson.emergencyPhone
        }
        if (!hmPerson.emergencyPhoneComment && nsPerson.emergencyPhoneComment) {
            println "using nspire emergencyPhoneComment=$nsPerson.emergencyPhoneComment"
            hmPerson.emergencyPhoneComment = nsPerson.emergencyPhoneComment
        }
        if (!hmPerson.instantMessageId && nsPerson.instantMessageId) {
            println "using nspire instantMessageId=$nsPerson.instantMessageId"
            hmPerson.instantMessageId = nsPerson.instantMessageId
        }
        if (!hmPerson.currentChurch && nsPerson.currentChurch) {
            println "using nspire currentChurch=$nsPerson.currentChurch"
            hmPerson.currentChurch = nsPerson.currentChurch
        }
        if (!hmPerson.ethnicity && nsPerson.ethnicity) {
            println "using nspire ethnicity=$nsPerson.ethnicity"
            hmPerson.ethnicity = nsPerson.ethnicity
        }
        if (!hmPerson.hobbies && nsPerson.hobbies) {
            println "using nspire hobbies=$nsPerson.hobbies"
            hmPerson.hobbies = nsPerson.hobbies
        }
        if (!hmPerson.ministryTeams && nsPerson.ministryTeams) {
            println "using nspire ministryTeams=$nsPerson.ministryTeams"
            hmPerson.ministryTeams = nsPerson.ministryTeams
        }
        if (!hmPerson.generalNote && nsPerson.generalNote) {
            println "using nspire generalNote=$nsPerson.generalNote"
            hmPerson.generalNote = nsPerson.generalNote
        }
        if (!hmPerson.miscNote && nsPerson.miscNote) {
            println "using nspire miscNote=$nsPerson.miscNote"
            hmPerson.miscNote = nsPerson.miscNote
        }
        if (!hmPerson.counselingNote && nsPerson.counselingNote) {
            println "using nspire counselingNote=$nsPerson.counselingNote"
            hmPerson.counselingNote = nsPerson.counselingNote
        }
    }

    private def mergeChild(Individual nsChild, Individual hmChild) {
        if (nsChild.middleName && nsChild.middleName != hmChild.middleName) {
            print "using nspire middleName=$nsChild.middleName"
            if (hmChild.middleName) println " (replacing helpmate middleName=$hmChild.middleName)"
            hmChild.middleName = nsChild.middleName
        }
        if (nsChild.nickName && (nsChild.nickName != nsChild.firstName) /* only nicknames that are diff't from first names*/) {
            println "using nspire nickName=$nsChild.nickName"
            if (hmChild.nickName) println " (replacing helpmate nickName=$hmChild.nickName)"
            hmChild.nickName = nsChild.nickName
        }
        if (nsChild.suffix) {
            println "using nspire suffix=$nsChild.suffix"
            if (hmChild.suffix) println " (replacing helpmate suffix=$hmChild.suffix)"
            hmChild.suffix = nsChild.suffix
        }
        if (nsChild.gender && nsChild.gender != hmChild.gender) {
            println "using nspire gender=$nsChild.gender"
            if (hmChild.gender) println " (replacing helpmate gender=$hmChild.gender)"
            hmChild.gender = nsChild.gender
        }
        if (nsChild.school) {
            println "using nspire school=$nsChild.school"
            if (hmChild.school) println " (replacing helpmate school=$hmChild.school)"
            hmChild.school = nsChild.school
        }
        if (nsChild.grade) {
            println "using nspire grade=$nsChild.grade"
            if (hmChild.grade) println " (replacing helpmate grade=$hmChild.grade)"
            hmChild.grade = nsChild.grade
        }
        if (nsChild.graduationYear) {
            println "using nspire graduationYear=$nsChild.graduationYear"
            if (hmChild.graduationYear) println " (replacing helpmate graduationYear=$hmChild.graduationYear)"
            hmChild.graduationYear = nsChild.graduationYear
        }
        if (nsChild.emailAddress1 && nsChild.emailAddress1.toLowerCase() != hmChild.emailAddress1.toLowerCase()) {
            println "using nspire emailAddress1=$nsChild.emailAddress1"
            if (hmChild.emailAddress1) println " (replacing helpmate emailAddress1=$hmChild.emailAddress1)"
            hmChild.emailAddress1 = nsChild.emailAddress1
        }
        if (nsChild.emailAddress2 && nsChild.emailAddress2.toLowerCase() != hmChild.emailAddress2.toLowerCase()) {
            println "using nspire emailAddress2=$nsChild.emailAddress2"
            if (hmChild.emailAddress2) println " (replacing helpmate emailAddress2=$hmChild.emailAddress2)"
            hmChild.emailAddress2 = nsChild.emailAddress2
        }
        if (nsChild.cellPhone) {
            println "using nspire cellPhone=$nsChild.cellPhone"
            if (hmChild.cellPhone) println " (replacing helpmate cellPhone=$hmChild.cellPhone)"
            hmChild.cellPhone = nsChild.cellPhone
        }
        if (nsChild.textMessage) {
            println "using nspire textMessage=$nsChild.textMessage"
            if (hmChild.textMessage) println " (replacing helpmate textMessage=$hmChild.textMessage)"
            hmChild.textMessage = nsChild.textMessage
        }
        if (nsChild.birthDate && nsChild.birthDate != hmChild.birthDate) {
            println "using nspire birthDate=$nsChild.birthDate"
            if (hmChild.birthDate) println " (replacing helpmate birthDate=$hmChild.birthDate)"
            hmChild.birthDate = nsChild.birthDate
        }
        // nspire status and related fields are not as good as helpmate status, so only use nspire if hm is empty
        if (!hmChild.status && nsChild.status) {
            println "using nspire status=$nsChild.status"
            if (hmChild.status) println " (replacing helpmate status=$hmChild.status)"
            hmChild.status = nsChild.status
        }
        if (!hmChild.statusGroup && nsChild.statusGroup) {
            println "using nspire statusGroup=$nsChild.statusGroup"
            if (hmChild.statusGroup) println " (replacing helpmate statusGroup=$hmChild.statusGroup)"
            hmChild.statusGroup = nsChild.statusGroup
        }
        if (!hmChild.statusComment && nsChild.statusComment) {
            println "using nspire statusComment=$nsChild.statusComment"
            if (hmChild.statusComment) println " (replacing helpmate statusComment=$hmChild.statusComment)"
            hmChild.statusComment = nsChild.statusComment
        }
        if (!hmChild.subStatus && nsChild.subStatus) {
            println "using nspire subStatus=$nsChild.subStatus"
            if (hmChild.subStatus) println " (replacing helpmate subStatus=$hmChild.subStatus)"
            hmChild.subStatus = nsChild.subStatus
        }
        if (nsChild.ministry) {
            println "using nspire ministry=$nsChild.ministry"
            if (hmChild.ministry) println " (replacing helpmate ministry=$hmChild.ministry)"
            hmChild.ministry = nsChild.ministry
        }
        if (nsChild.website) {
            println "using nspire website=$nsChild.website"
            if (hmChild.website) println " (replacing helpmate website=$hmChild.website)"
            hmChild.website = nsChild.website
        }
        if (nsChild.emergencyPhone) {
            println "using nspire emergencyPhone=$nsChild.emergencyPhone"
            if (hmChild.emergencyPhone) println " (replacing helpmate emergencyPhone=$hmChild.emergencyPhone)"
            hmChild.emergencyPhone = nsChild.emergencyPhone
        }
        if (nsChild.emergencyPhoneComment) {
            println "using nspire emergencyPhoneComment=$nsChild.emergencyPhoneComment"
            if (hmChild.emergencyPhoneComment) println " (replacing helpmate emergencyPhoneComment=$hmChild.emergencyPhoneComment)"
            hmChild.emergencyPhoneComment = nsChild.emergencyPhoneComment
        }
        if (nsChild.instantMessageId) {
            println "using nspire instantMessageId=$nsChild.instantMessageId"
            if (hmChild.instantMessageId) println " (replacing helpmate instantMessageId=$hmChild.instantMessageId)"
            hmChild.instantMessageId = nsChild.instantMessageId
        }
        if (nsChild.currentChurch) {
            println "using nspire currentChurch=$nsChild.currentChurch"
            if (hmChild.currentChurch) println " (replacing helpmate currentChurch=$hmChild.currentChurch)"
            hmChild.currentChurch = nsChild.currentChurch
        }
        if (nsChild.ethnicity) {
            println "using nspire ethnicity=$nsChild.ethnicity"
            if (hmChild.ethnicity) println " (replacing helpmate ethnicity=$hmChild.ethnicity)"
            hmChild.ethnicity = nsChild.ethnicity
        }
        if (nsChild.hobbies) {
            println "using nspire hobbies=$nsChild.hobbies"
            if (hmChild.hobbies) println " (replacing helpmate hobbies=$hmChild.hobbies)"
            hmChild.hobbies = nsChild.hobbies
        }
        if (nsChild.ministryTeams) {
            println "using nspire ministryTeams=$nsChild.ministryTeams"
            if (hmChild.ministryTeams) println " (replacing helpmate ministryTeams=$hmChild.ministryTeams)"
            hmChild.ministryTeams = nsChild.ministryTeams
        }
        if (nsChild.generalNote) {
            println "using nspire generalNote=$nsChild.generalNote"
            if (hmChild.generalNote) println " (replacing helpmate generalNote=$hmChild.generalNote)"
            hmChild.generalNote = nsChild.generalNote
        }
        if (nsChild.miscNote) {
            println "using nspire miscNote=$nsChild.miscNote"
            if (hmChild.miscNote) println " (replacing helpmate miscNote=$hmChild.miscNote)"
            hmChild.miscNote = nsChild.miscNote
        }
        if (nsChild.counselingNote) {
            println "using nspire counselingNote=$nsChild.counselingNote"
            if (hmChild.counselingNote) println " (replacing helpmate counselingNote=$hmChild.counselingNote)"
            hmChild.counselingNote = nsChild.counselingNote
        }
    }

    private Household findHelpmateMatchingHousehold(Household nspireHH, List<Individual> hmPeople) {
        List<Household> householdMatches = []

        // Get NSpire household people
        Individual head = findHead(nspireHH)
        Individual spouse = findSpouse(nspireHH)
        Collection<Individual> children = findChildren(nspireHH)

        // Try to find matching Helpmate household using Head
        findMatching(hmPeople, Individual.HEAD, head, householdMatches)

        // too many matches found
        if (householdMatches.size() > 1) {
            println "Too many matches found 1, size = ${householdMatches.size()}"
            List<Household> spouseHouseholdMatches = []
            // Try to find matching Helpmate household using spouse
            findMatching(hmPeople, Individual.SPOUSE, spouse, spouseHouseholdMatches)
            if (spouseHouseholdMatches.size() == 1) {
                println "Found spouse match; removing other matches"
                householdMatches = []
                householdMatches << spouseHouseholdMatches.first()
            }
        }

        // no match found, try to find match using spouse
        if (householdMatches.isEmpty()) {
            findMatching(hmPeople, Individual.SPOUSE, spouse, householdMatches)
        }

        if (householdMatches.isEmpty()) {
            for (Individual child in children) {
                findMatching(hmPeople, Individual.CHILD, child, householdMatches)
                if (!householdMatches.isEmpty()) {
                    break // match found
                }
            }
        }

        if (householdMatches.size() > 1) {
            println "Still too many matches found; clearing result"
            householdMatches = []
        }
        if (householdMatches.isEmpty()) {
            println "No matches found for any household members"
            return null
        } else {
            return householdMatches.first()
        }
    }

    private Collection<Individual> findChildren(Household hh) {
        return hh.individuals.values().findAll {it.position == Individual.CHILD}
    }

    private Individual findSpouse(Household hh) {
        return hh.individuals.values().find {it.position == Individual.SPOUSE}
    }

    private Individual findHead(Household hh) {
        return hh.individuals.values().find {it.position == Individual.HEAD}
    }

    private def findMatching(List<Individual> hmPeople, String position, Individual candidate, List<Household> householdMatches) {
        
        if (!candidate) {
            return
        }

        List<Individual> matches = hmPeople.findAll {hmPerson ->
            hmPerson.fullName.toLowerCase() == candidate.fullName.toLowerCase()
        } as List<Individual>

        if (matches.size() == 1) {
            println "Helpmate match found for $position $candidate.fullName"
            householdMatches << hmHouseholds.get(matches[0].householdId)
        } else if (matches.size() < 1) {
            println "No Helpmate match for $position $candidate.fullName"
        } else {
            println "WARNING: more than one Helpmate match for $position $candidate.fullName"
            matches.each {
                householdMatches << hmHouseholds.get(it.householdId)
            }
        }
    }
}
