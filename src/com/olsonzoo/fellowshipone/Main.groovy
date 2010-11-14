package com.olsonzoo.fellowshipone

/**
 * Imports Church Helpmate data from CSV files into Fellowship One XLS spreadsheet.
 *
 * @author Jeff Olson (jeff@olsonzoo.com)
 */
def startTime = System.currentTimeMillis()
println "Starting conversion at ${new Date(startTime)}"
def limit = ''
if (args) {
    limit = args[0] as Integer
}

def hmParser = new HelpmateParser()
Map<String, Household> hmHouseholds = hmParser.parseHouseholds()

def hmValidator = new HelpmateValidator()
hmValidator.validateAndCorrect(hmHouseholds)

def nParser = new NspireParser()
HashMap<String, Household> nHouseholds = nParser.parseHouseholds()

def nValidator = new NspireValidator()
nValidator.validateAndCorrect(nHouseholds)

def merger = new HouseholdMerger(hmHouseholds: hmHouseholds, nHouseholds: nHouseholds)
Map mergedHouseholds = merger.merge()

def fValidator = new FinalValidator()
fValidator.validateAndCorrect(mergedHouseholds)

def writer = new FellowshipOneWriter(households: mergedHouseholds)
writer.write(limit)

def endTime = System.currentTimeMillis()
def duration = (endTime - startTime) / 1000 / 60
println "\nTotal time: $duration minutes"

// Known issues
// - NSpire household website field has some some phone numbers
