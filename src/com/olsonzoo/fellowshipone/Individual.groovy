package com.olsonzoo.fellowshipone

/**
 * Represents an individual in Church Helpmate or Nspire as well as the new fields for insertion into Fellowship One.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class Individual {
    public static final String HEAD = 'Head'
    public static final String SPOUSE = 'Spouse'
    public static final String CHILD = 'Child'
    public static final String ATTENDEE = 'Attendee'

    /** Church Helpmate fields   */
    String id // done
    String householdId // done
    String school // done
    String contribTypeId // n/a
    String firstName // done
    String nickName // done
    String middleName // done
    String lastName // done
    String suffix // done
    String title // done
    String envelopeNumber // done
    String status // done
    String gender // done
    String maritalStatus // done
    String maritalStatusNote // done
    String mainContact // done
    String guardian // done
    String occupation // done
    String birthDate // done
    String anniversaryDate // done
    String anniversaryComment // done
    String deceased // done
    String deceasedDate // done
    String deceasedComment // done
    String baptismDate // done
    String baptismComment // done
    String confirmationDate // done
    String confirmationComment // done
    String membershipDate // done
    String membershipComment // done
    String cellPhone // done
    String cellPhoneUnlisted // done
    String pager // done
    String pagerUnlisted // done
    String altPhone // done
    String altPhoneUnlisted // done
    String emailAddress1 // done
    String emailAddress1Unlisted // done
    String emailAddress2 // done
    String emailAddress2Unlisted // done
    String website // done
    String employer // done
    String workPhone // done
    String workPhoneUnlisted // done
    String workFax // done
    String generalNote // done
    String relationship // done
    String guid // N/A

    // fields from NSpire
    String grade // done
    String graduationYear // done
    String textMessage // done
    String active // done
    String ministry // done
    String emergencyPhone // done
    String emergencyPhoneComment // done
    String instantMessageId // done
    String currentChurch // done
    String ethnicity // done
    String hobbies // done
    String ministryTeams // done
    String miscNote // done
    String counselingNote // done

    /** Fellowship One specific fields.   */
    /** generated position from household primary contact id.    */
    String position
    String statusGroup
    String subStatus
    String statusComment

    def getFullName() {
        return firstName + " " + lastName
    }
}
