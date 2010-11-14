package com.olsonzoo.fellowshipone

import org.codehaus.groovy.scriptom.util.office.ExcelHelper

/**
 * Writes out the data to the Fellowship One excel file.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class FellowshipOneWriter {
    Map<String, Household> households
    private static final String JEFF = 'jolson'
    private static final String GENERAL_NOTES = 'General Notes'

    def write(limit) {
        final outputFile = new File('../output/FT Standard Format.xls').canonicalFile

        def helper = new ExcelHelper()
        helper.create(new File("../FT Standard Format.xls"), outputFile) {workbook ->

            def list = households.values() as List<Household>
            sortHouseholds(list)

            if (limit) {
                list = list[0..limit - 1] as List<Household>
            }
            writePeople(workbook, list)
            writeAddresses(workbook, list)
            writeCommunications(workbook, list)
            writeNotes(workbook, list)
            writeAttributes(workbook, list)
        }

    }

    private def writePeople(workbook, List<Household> list) {
        def personSheet = workbook.Sheets.Item[2]
        def row = 2
        list.eachWithIndex {hh, i ->
            println "P$i: ${hh.fullName}"
            hh.individuals.values().each {person ->
                personSheet.Cells.Item[row, 1] = hh.id
                personSheet.Cells.Item[row, 2] = person.id
                personSheet.Cells.Item[row, 3] = person.position
                personSheet.Cells.Item[row, 4] = person.title
                personSheet.Cells.Item[row, 5] = person.lastName
                personSheet.Cells.Item[row, 6] = person.firstName
                personSheet.Cells.Item[row, 7] = person.nickName
                personSheet.Cells.Item[row, 8] = person.middleName
                personSheet.Cells.Item[row, 9] = person.suffix
                personSheet.Cells.Item[row, 11] = person.statusGroup
                personSheet.Cells.Item[row, 12] = person.status
                personSheet.Cells.Item[row, 13] = person.subStatus
                personSheet.Cells.Item[row, 15] = person.statusComment
                personSheet.Cells.Item[row, 16] = person.envelopeNumber
                personSheet.Cells.Item[row, 19] = person.gender
                personSheet.Cells.Item[row, 20] = person.birthDate
                personSheet.Cells.Item[row, 21] = person.maritalStatus
                personSheet.Cells.Item[row, 23] = person.occupation
                personSheet.Cells.Item[row, 24] = person.employer
                personSheet.Cells.Item[row, 27] = person.school

                row++
            }
        }
    }

    private def writeAddresses(workbook, List<Household> list) {
        def addressSheet = workbook.Sheets.Item[3]
        def row = 2
        list.eachWithIndex {hh, i ->
            println "A$i: ${hh.fullName}"

            // Note: we only bring over addresses where there is an address line 1 or 2
            if (hh.primaryAddress1 || hh.primaryAddress2) {
                addressSheet.Cells.Item[row, 1] = hh.id
                addressSheet.Cells.Item[row, 3] = 'Primary'
                addressSheet.Cells.Item[row, 4] = hh.primaryAddress1
                addressSheet.Cells.Item[row, 5] = hh.primaryAddress2
                addressSheet.Cells.Item[row, 7] = hh.primaryCity
                addressSheet.Cells.Item[row, 8] = hh.primaryState
                addressSheet.Cells.Item[row, 9] = hh.primaryPostalCode
                addressSheet.Cells.Item[row, 10] = hh.primaryCountry
                addressSheet.Cells.Item[row, 11] = hh.primaryDirections
                row++
            }
            if (hh.secondaryAddress1 || hh.secondaryAddress2) {
                addressSheet.Cells.Item[row, 1] = hh.id
                addressSheet.Cells.Item[row, 3] = 'Secondary'
                addressSheet.Cells.Item[row, 4] = hh.secondaryAddress1
                addressSheet.Cells.Item[row, 5] = hh.secondaryAddress2
                addressSheet.Cells.Item[row, 7] = hh.secondaryCity
                addressSheet.Cells.Item[row, 8] = hh.secondaryState
                addressSheet.Cells.Item[row, 9] = hh.secondaryPostalCode
                addressSheet.Cells.Item[row, 10] = hh.secondaryCountry
                addressSheet.Cells.Item[row, 11] = hh.secondaryDirections
                row++
            }

        }
    }

    private def writeCommunications(workbook, List<Household> list) {
        def commSheet = workbook.Sheets.Item[4]
        def row = 2
        list.eachWithIndex {hh, i ->
            println "C$i: ${hh.fullName}"

            if (hh.primaryPhone1) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Home Phone'
                commSheet.Cells.Item[row, 4] = hh.primaryPhone1
                commSheet.Cells.Item[row, 5] = hh.primaryPhone1unlisted
                row++
            }
            if (hh.primaryPhone2) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Home Phone' // verified with F1 that it's ok to have 2 Home Phones
                commSheet.Cells.Item[row, 4] = hh.primaryPhone2
                commSheet.Cells.Item[row, 5] = hh.primaryPhone2unlisted
                commSheet.Cells.Item[row, 6] = 'Primary Phone 2'
                row++
            }
            if (hh.primaryFax) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Fax'
                commSheet.Cells.Item[row, 4] = hh.primaryFax
                row++
            }
            if (hh.secondaryPhone1) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Previous Phone'
                commSheet.Cells.Item[row, 4] = hh.secondaryPhone1
                commSheet.Cells.Item[row, 5] = hh.secondaryPhone1unlisted
                commSheet.Cells.Item[row, 6] = 'Secondary Phone'
                row++
            }
            if (hh.secondaryPhone2) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Previous Phone'
                commSheet.Cells.Item[row, 4] = hh.secondaryPhone2
                commSheet.Cells.Item[row, 5] = hh.secondaryPhone2unlisted
                commSheet.Cells.Item[row, 6] = 'Secondary Phone'
                row++
            }
            if (hh.secondaryFax) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Fax'
                commSheet.Cells.Item[row, 4] = hh.secondaryFax
                commSheet.Cells.Item[row, 6] = 'Secondary Fax'
                row++
            }
            if (hh.email) {
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Email'
                commSheet.Cells.Item[row, 4] = hh.email
                commSheet.Cells.Item[row, 5] = hh.emailUnlisted
                row++
            }
            hh.additionalEmail.each {email ->
                commSheet.Cells.Item[row, 1] = hh.id
                commSheet.Cells.Item[row, 3] = 'Alternate Email'
                commSheet.Cells.Item[row, 4] = email
                // should do unlisted, but turned out we didn't have any of these in the additional category
                row++
            }


            hh.individuals.values().each {person ->
                if (person.cellPhone) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Mobile'
                    commSheet.Cells.Item[row, 4] = person.cellPhone
                    commSheet.Cells.Item[row, 5] = person.cellPhoneUnlisted
                    if (person.textMessage) {
                        commSheet.Cells.Item[row, 6] = "Text message address: $person.textMessage"
                    }
                    row++
                }
                if (person.pager) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Pager'
                    commSheet.Cells.Item[row, 4] = person.pager
                    commSheet.Cells.Item[row, 5] = person.pagerUnlisted
                    row++
                }
                if (person.altPhone) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Vacation Phone'
                    commSheet.Cells.Item[row, 4] = person.altPhone
                    commSheet.Cells.Item[row, 5] = person.altPhoneUnlisted
                    commSheet.Cells.Item[row, 6] = 'Alt Phone'
                    row++
                }
                if (person.workPhone) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Work Phone'
                    commSheet.Cells.Item[row, 4] = person.workPhone
                    commSheet.Cells.Item[row, 5] = person.workPhoneUnlisted
                    row++
                }
                if (person.workFax) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Fax'
                    commSheet.Cells.Item[row, 4] = person.workFax
                    commSheet.Cells.Item[row, 6] = 'Work Fax'
                    row++
                }
                if (person.emailAddress1) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Email'
                    commSheet.Cells.Item[row, 4] = person.emailAddress1
                    commSheet.Cells.Item[row, 5] = person.emailAddress1Unlisted
                    row++
                }
                if (person.emailAddress2) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Alternate Email'
                    commSheet.Cells.Item[row, 4] = person.emailAddress2
                    commSheet.Cells.Item[row, 5] = person.emailAddress2Unlisted
                    row++
                }
                if (person.website) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Web Address'
                    commSheet.Cells.Item[row, 4] = person.website
                    row++
                }
                if (person.emergencyPhone) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'Emergency Phone'
                    commSheet.Cells.Item[row, 4] = person.emergencyPhone
                    commSheet.Cells.Item[row, 6] = person.emergencyPhoneComment
                    row++
                }
                if (person.instantMessageId) {
                    commSheet.Cells.Item[row, 1] = hh.id
                    commSheet.Cells.Item[row, 2] = person.id
                    commSheet.Cells.Item[row, 3] = 'IM Address'
                    commSheet.Cells.Item[row, 4] = person.instantMessageId
                    row++
                }
            }
        }
    }

    private def writeNotes(workbook, List<Household> list) {
        def notesSheet = workbook.Sheets.Item[5]
        def row = 2
        list.eachWithIndex {hh, i ->
            println "N$i: ${hh.fullName}"

            if (hh.note1) {
                notesSheet.Cells.Item[row, 1] = hh.id
                notesSheet.Cells.Item[row, 3] = GENERAL_NOTES
                notesSheet.Cells.Item[row, 4] = JEFF
                notesSheet.Cells.Item[row, 6] = hh.note1
                row++
            }
            if (hh.note2) {
                notesSheet.Cells.Item[row, 1] = hh.id
                notesSheet.Cells.Item[row, 3] = GENERAL_NOTES
                notesSheet.Cells.Item[row, 4] = JEFF
                notesSheet.Cells.Item[row, 6] = hh.note2
                row++
            }
            if (hh.missionOrg) {
                notesSheet.Cells.Item[row, 1] = hh.id
                notesSheet.Cells.Item[row, 3] = 'Missions Organization'
                notesSheet.Cells.Item[row, 4] = JEFF
                notesSheet.Cells.Item[row, 6] = hh.missionOrg
                row++
            }

            hh.addressNotes.each {note ->
                notesSheet.Cells.Item[row, 1] = hh.id
                notesSheet.Cells.Item[row, 3] = 'Address Notes'
                notesSheet.Cells.Item[row, 4] = JEFF
                notesSheet.Cells.Item[row, 6] = note
                row++
            }

            hh.individuals.values().each {person ->

                if (person.generalNote) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = GENERAL_NOTES
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.generalNote
                    row++
                }
                if (person.maritalStatusNote) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Marital Status'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.maritalStatusNote
                    row++
                }
                if (person.miscNote) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = GENERAL_NOTES
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.miscNote
                    row++
                }
                if (person.counselingNote) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Counseling Notes'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.counselingNote
                    row++
                }
                if (person.currentChurch) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Current Church'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.currentChurch
                    row++
                }
                if (person.ethnicity) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Ethnicity'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.ethnicity
                    row++
                }
                if (person.hobbies) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Hobbies'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.hobbies
                    row++
                }
                if (person.ministryTeams) {
                    notesSheet.Cells.Item[row, 1] = hh.id
                    notesSheet.Cells.Item[row, 2] = person.id
                    notesSheet.Cells.Item[row, 3] = 'Ministries'
                    notesSheet.Cells.Item[row, 4] = JEFF
                    notesSheet.Cells.Item[row, 6] = person.ministryTeams
                    row++
                }
            }
        }
    }

    private def writeAttributes(workbook, List<Household> list) {
        def attribSheet = workbook.Sheets.Item[6]
        def row = 2
        list.eachWithIndex {hh, i ->
            println "T$i: ${hh.fullName}"

            hh.individuals.values().each {person ->

                if (person.anniversaryDate) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Milestones'
                    attribSheet.Cells.Item[row, 3] = 'Anniversary'
                    attribSheet.Cells.Item[row, 4] = person.anniversaryDate
                    attribSheet.Cells.Item[row, 6] = person.anniversaryComment
                    row++
                }
                if (person.deceasedDate) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Milestones'
                    attribSheet.Cells.Item[row, 3] = 'Deceased'
                    attribSheet.Cells.Item[row, 4] = person.deceasedDate
                    attribSheet.Cells.Item[row, 6] = person.deceasedComment
                    row++
                }
                if (person.baptismDate) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Milestones'
                    attribSheet.Cells.Item[row, 3] = 'Baptism'
                    attribSheet.Cells.Item[row, 4] = person.baptismDate
                    attribSheet.Cells.Item[row, 6] = person.baptismComment
                    row++
                }
                if (person.confirmationDate) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Milestones'
                    attribSheet.Cells.Item[row, 3] = 'Confirmation'
                    attribSheet.Cells.Item[row, 4] = person.confirmationDate
                    attribSheet.Cells.Item[row, 6] = person.confirmationComment
                    row++
                }
                if (person.membershipDate) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Milestones'
                    attribSheet.Cells.Item[row, 3] = 'Membership'
                    attribSheet.Cells.Item[row, 4] = person.membershipDate
                    attribSheet.Cells.Item[row, 6] = person.membershipComment
                    row++
                }
                if (person.graduationYear) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Graduation Year'
                    attribSheet.Cells.Item[row, 3] = person.graduationYear
                    // note: not sure if this is worth putting in
                    //attribSheet.Cells.Item[row, 6] = person.grade
                    row++
                }
                if (person.ministry) {
                    attribSheet.Cells.Item[row, 1] = person.id
                    attribSheet.Cells.Item[row, 2] = 'Ministry'
                    attribSheet.Cells.Item[row, 3] = person.ministry
                    row++
                }

            }
        }
    }

    private List<Household> sortHouseholds(List<Household> list) {
        return list.sort {a, b ->
            a.lastName == b.lastName ?
                (a.formalGreeting < b.formalGreeting ? -1 : 1) :
                (a.lastName < b.lastName ? -1 : 1)
        }
    }


}
