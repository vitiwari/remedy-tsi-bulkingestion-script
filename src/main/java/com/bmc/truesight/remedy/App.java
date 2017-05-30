package com.bmc.truesight.remedy;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bmc.arsys.api.OutputInteger;
import com.bmc.truesight.remedy.beans.Configuration;
import com.bmc.truesight.remedy.beans.Payload;
import com.bmc.truesight.remedy.exception.ParsingException;
import com.bmc.truesight.remedy.exception.ValidationException;
import com.bmc.truesight.remedy.util.ConfigParser;
import com.bmc.truesight.remedy.util.ConfigValidator;
import com.bmc.truesight.remedy.util.Constants;
import com.bmc.truesight.remedy.util.RemedyReader;
import com.bmc.truesight.remedy.util.TsiHttpClient;

/**
 * Main Application Entry This application reads the files incidentTemplate.json
 * & changeTemplate.json It parses the files and validates for the syntactical
 * and semantical correctness It Reads the incident/Change tickets from the
 * Remedy Server based on the configuration provided and then sends as an events
 * to the Truesight Intelligence server
 *
 * @author vitiwari
 */
public class App {

    private final static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        boolean readIncidents = false;
        boolean readChange = false;
        if (args.length == 0) {
            System.out.println("Do you want to ingest Remedy Incidents tickets as events? (y/n)");
            Scanner scanner = new Scanner(System.in);
            String input1 = scanner.next();
            if (input1.equalsIgnoreCase("y")) {
                readIncidents = true;
            }
            System.out.println("Do you want to ingest Remedy Change tickets as events also? (y/n)");
            String input2 = scanner.next();
            if (input2.equalsIgnoreCase("y")) {
                readChange = true;
            }
        } else {
            for (String module : args) {
                if (module.equalsIgnoreCase("incident") || module.equalsIgnoreCase("incidents")) {
                    readIncidents = true;
                }
                if (module.equalsIgnoreCase("change") || module.equalsIgnoreCase("changes")) {
                    readChange = true;
                }
            }
        }
        if (readIncidents) {
            readAndIngestIncidents();
        }
        if (readChange) {
            readAndIngestChanges();
        }
    }

    private static void readAndIngestIncidents() {

        String path = null;
        boolean hasLoggedIntoRemedy = false;
        try {
            path = new java.io.File(".").getCanonicalPath();
            path += "\\incidentTemplate.json";
        } catch (IOException e2) {
            log.error("The file path couldnot be found ");
        }
        // PARSING THE CONFIGURATION FILE
        ConfigParser incidentParser = new ConfigParser(path);
        try {
            incidentParser.readParseConfigFile();
        } catch (ParsingException ex) {
            log.error(ex.getMessage());
            System.exit(0);
        }
        log.info("{} file reading and parsing succesfull", path);
        // VALIDATION OF THE CONFIGURATION
        ConfigValidator incidentValidator = new ConfigValidator();
        try {
            incidentValidator.validate(incidentParser);
        } catch (ValidationException ex) {
            log.error(ex.getMessage());
            System.exit(0);
        }
        log.info("{} configuration file validation succesfull", path);

        Configuration config = incidentParser.getConfiguration();
        RemedyReader incidentReader = new RemedyReader(incidentParser, Constants.HELP_DESK_FORM);
        TsiHttpClient client = new TsiHttpClient(config);
        try {
            // Start Login
            hasLoggedIntoRemedy = incidentReader.login();

            int chunkSize = config.getChunkSize();
            int startFrom = 1;
            int iteration = 1;
            OutputInteger nMatches = new OutputInteger();
            boolean readNext = true;
            log.info("Started reading {} remedy incidents starting from index {} , [Start Date: {}, End Date: {}]", new Object[]{chunkSize, startFrom, config.getStartDateTime(), config.getEndDateTime()});
            while (readNext) {
                List<Payload> eventList = incidentReader.readRemedyTickets(startFrom, chunkSize, nMatches);
                log.info("[iteration : {}]  Recieved {} remedy incidents", new Object[]{iteration, eventList.size()});

                if (nMatches.longValue() <= (startFrom + chunkSize)) {
                    readNext = false;
                }
                iteration++;
                startFrom = startFrom + chunkSize;
                client.pushBulkEventsToTSI(eventList);
            }
        } catch (Exception ex) {
            log.error("Error {}", ex.getMessage());
        } finally {
            if (hasLoggedIntoRemedy) {
                incidentReader.logout();
            }
        }

    }

    private static void readAndIngestChanges() {
        String path = null;
        boolean hasLoggedIntoRemedy = false;
        try {
            path = new java.io.File(".").getCanonicalPath();
            path += "\\changeTemplate.json";
        } catch (IOException e2) {
            log.error("The file path couldnot be found ");
        }
        // PARSING THE CONFIGURATION FILE
        ConfigParser changeParser = new ConfigParser(path);
        try {
            changeParser.readParseConfigFile();
        } catch (ParsingException ex) {
            log.error(ex.getMessage());
            System.exit(0);
        }
        log.info("{} file reading and parsing succesfull", path);
        // VALIDATION OF THE CONFIGURATION
        ConfigValidator changeValidator = new ConfigValidator();
        try {
            changeValidator.validate(changeParser);
        } catch (ValidationException ex) {
            log.error(ex.getMessage());
            System.exit(0);
        }
        log.info("{} configuration file validation succesfull", path);

        Configuration config = changeParser.getConfiguration();
        RemedyReader changeReader = new RemedyReader(changeParser, Constants.CHANGE_FORM);
        TsiHttpClient client = new TsiHttpClient(config);
        try {

            // Start Login
            hasLoggedIntoRemedy = changeReader.login();

            int chunkSize = config.getChunkSize();
            int startFrom = 1;
            int iteration = 1;
            OutputInteger nMatches = new OutputInteger();
            boolean readNext = true;
            log.info("Started reading {} remedy Change tickets starting from index {} , [Start Date: {}, End Date: {}]", new Object[]{chunkSize, startFrom, config.getStartDateTime(), config.getEndDateTime()});
            while (readNext) {
                List<Payload> eventList = changeReader.readRemedyTickets(startFrom, chunkSize, nMatches);
                log.info("[iteration : {}]  Recieved {} remedy Changes", new Object[]{iteration, eventList.size()});

                if (nMatches.longValue() <= (startFrom + chunkSize)) {
                    readNext = false;
                }
                iteration++;
                startFrom = startFrom + chunkSize;

                // Send the events to TSI
                client.pushBulkEventsToTSI(eventList);
            }
        } catch (Exception ex) {
            log.error("Error {}", ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (hasLoggedIntoRemedy) {
                changeReader.logout();
            }
        }
    }
}
