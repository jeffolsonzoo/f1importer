package com.olsonzoo.fellowshipone

/**
 * A Church Helpmate household.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class Household {
    String id // done
    String lastName // N/A
    String fullName // N/A
    String formalGreeting // N/A
    String mainContactId // done
    String status // N/A
    String envelopeNumber // done
    String email // done
    String emailUnlisted // done
    List<String> additionalEmail = [] // done
    String website // done
    String note1 // done
    List<String> addressNotes = []
    String missionOrg // done
    String activeAddress // done
    String primaryAddress1 // done
    String primaryAddress2 // done
    String primaryCity // done
    String primaryState // done
    String primaryPostalCode // done
    String primaryPhone1 // done
    String primaryPhone1unlisted // done
    String primaryPhone2 // done
    String primaryPhone2unlisted // done
    String primaryFax // done
    String primaryCountry // done
    String primaryDirections // done
    String secondaryAddress1 // done
    String secondaryAddress2 // done
    String secondaryCity // done
    String secondaryState // done
    String secondaryPostalCode // done
    String secondaryPhone1 // done
    String secondaryPhone1unlisted // done
    String secondaryPhone2 // done
    String secondaryPhone2unlisted // done
    String secondaryFax // done
    String secondaryCountry // done
    String secondaryDirections // done
    String guid // N/A
    String note2 // from NSpire, if needed // done

    Map<String, Individual> individuals = [:] // done
}
