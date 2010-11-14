package com.olsonzoo.fellowshipone

/**
 * Class for final validation after merge is complete.
 * 
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
class FinalValidator extends ValidatorBase {
    void validateAndCorrect(Map<String, Household> households) {
        println "\nBeginning Final Validation\n"
        households.values().each {hh ->
            println "\nValidating ${hh.fullName}..."
            fixEmail(hh)
            List<Individual> heads = hh.individuals.values().findAll {it.position == Individual.HEAD} as List
            if (hh.individuals.isEmpty()) println "WARNING: no members for merged household $hh.fullName"
            if (heads.size() > 1) {
                println "ERROR: too many heads found for merged household $hh.fullName"
            } else if (heads.size() < 1) {
                if (hh.individuals.isEmpty()) {
                    println "WARNING: no head found for merged household $hh.fullName, but household is empty"
                } else {
                    println "ERROR: no head found for merged household $hh.fullName"
                }
                println ""
            }
        }
    }

    String getDateFormat() {
        return ""
    }
}
