/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 25, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Joey Dodds <jdodds@galois.com>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.csv;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.BallotStyle;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.Choice;
import us.freeandfair.corla.model.Contest;

/**
 * @description <description>
 * @explanation <explanation>
 * @bon OPTIONAL_BON_TYPENAME
 */
public class DominionCVRExportParser implements CVRExportParser {
  /**
   * The column containing the tabulator number in a Dominion export file.
   */
  private static final int TABULATOR_NUMBER_COLUMN = 1;
  
  /**
   * The column containing the batch ID in a Dominion export file.
   */
  private static final int BATCH_ID_COLUMN = 2;
  
  /**
   * The column containing the record ID in a Dominion export file.
   */
  private static final int RECORD_ID_COLUMN = 3;
  
  /**
   * The column containing the imprinted ID in a Dominion export file.
   */
  private static final int IMPRINTED_ID_COLUMN = 4;
  
  /**
   * The column containing the ballot type in a Dominion export file.
   */
  private static final int BALLOT_TYPE_COLUMN = 7;

  /**
   * The first column of contest names/choices in a Dominion export file.
   */
  private static final int FIRST_CHOICE_COLUMN = 8;
  
  /**
   * A flag indicating whether parse() has been run or not.
   */
  private boolean my_parse_status;
  
  /**
   * A flag indicating whether or not a parse was successful.
   */
  private boolean my_parse_success;
  
  /**
   * The parser to be used.
   */
  private final CSVParser my_parser;
  
  /**
   * The list of CVRs parsed from the supplied data export.
   */
  private final List<CastVoteRecord> my_cvrs = new ArrayList<CastVoteRecord>();
  
  /**
   * The list of contests parsed from the supplied data export.
   */
  private final List<Contest> my_contests = new ArrayList<Contest>();
  
  /**
   * The ballot styles inferred from the supplied data export.
   */
  private final Map<String, BallotStyle> my_ballot_styles = 
      new HashMap<String, BallotStyle>();
  
  /**
   * The ID of the county whose CVRs we are parsing.
   */
  private final int my_county_id;
  
  /**
   * Construct a new Dominion CVR export parser using the specified Reader,
   * for CVRs provided by the specified county.
   * 
   * @param the_reader The reader from which to read the CSV to parse.
   * @param the_county_id The ID of the county whose CVRs are to be parsed.
   * @exception IOException if an error occurs while constructing the parser.
   */
  public DominionCVRExportParser(final Reader the_reader, final int the_county_id) 
      throws IOException {
    my_parser = new CSVParser(the_reader, CSVFormat.DEFAULT);
    my_county_id = the_county_id;
  }
  
  /**
   * Construct a new Dominion CVR export parser to parse the specified
   * CSV string, for CVRs provided by the specified county.
   * 
   * @param the_string The CSV string to parse.
   * @param the_county_id The ID of the county whose CVRs are to be parsed.
   * @exception IOException if an error occurs while constructing the parser.
   */
  public DominionCVRExportParser(final String the_string, final int the_county_id)
      throws IOException {
    my_parser = CSVParser.parse(the_string, CSVFormat.DEFAULT);
    my_county_id = the_county_id;
  }
  
  /**
   * Strip the '="..."' from a column.
   * 
   * @param the_value The value to strip.
   * @return the stripped value, as a String, or the original String if it 
   * does not have the '="..."' form.
   */
  private String stripEqualQuotes(final String the_value) {
    String result = the_value;
    if (the_value.startsWith("=\"") && the_value.endsWith("\"")) {
      result = the_value.substring(0, the_value.length() - 1).replaceFirst("=\"", "");
    }
    return result;
  }
  
  /**
   * Parse the supplied data export. If it has already been parsed, this
   * method returns immediately.
   * 
   * @return true if the parse was successful, false otherwise
   */
  @Override
  public synchronized boolean parse() {
    if (my_parse_status) {
      // no need to parse if we've already parsed
      return my_parse_success;
    }
    
    boolean result = true; // presume the parse will succeed
    final Iterator<CSVRecord> records = my_parser.iterator();
    final long timestamp = System.currentTimeMillis();
    
    try {
      // we expect the first line to be the election name, which we currently discard
      records.next();
      
      // we expect the second line to be a list of contest names, each appearing once 
      // for each choice in the contest
      final CSVRecord contest_line = records.next();
      
      // find all the contest names, how many choices each has, 
      // and how many choices can be made in each
      final List<String> contest_names = new ArrayList<String>();
      final Map<String, Integer> contest_max_selections = new HashMap<String, Integer>();
      final Map<String, Integer> contest_choice_counts = new HashMap<String, Integer>();
      
      int index = FIRST_CHOICE_COLUMN;
      do {
        final String c = contest_line.get(index);
        int count = 0;
        while (index < contest_line.size() && 
               c.equals(contest_line.get(index))) {
          index = index + 1;
          count = count + 1;
        }
        // get the "(Vote For=" number from the contest name and clean up the name
        final String cn = c.substring(0, c.indexOf("(Vote For="));
        final String vf = c.replace(cn, "").replace("(Vote For=", "").replace(")", "");
        int ms = 1; // this is our default maximum selections
        try {
          ms = Integer.valueOf(vf);
        } catch (final NumberFormatException e) {
          // ignored
        }
        contest_names.add(cn);
        contest_choice_counts.put(cn, count);
        contest_max_selections.put(cn, ms);
      } while (index < contest_line.size());

      // we expect the third line to be a list of contest choices
      final CSVRecord contest_choices_line = records.next();
      
      // we expect the fourth line to be a list of contest choice "explanations" 
      // (such as political party affiliations)
      final CSVRecord contest_choice_explanations_line = records.next();
      
      index = FIRST_CHOICE_COLUMN;
      for (final String cn : contest_names) {
        final List<Choice> choices = new ArrayList<Choice>();
        final int end = index + contest_choice_counts.get(cn); 
        while (index < end) {
          final String ch = contest_choices_line.get(index);
          final String ex = contest_choice_explanations_line.get(index);
          choices.add(new Choice(ch, ex));
          index = index + 1;
        }
        // now that we have all the choices, we can create a Contest object for 
        // this contest (note the empty contest description at the moment, below, 
        // as that's not in the CVR files and may not actually be used)
        my_contests.add(new Contest(cn, "", choices, contest_max_selections.get(cn)));
      }
     
      // subsequent lines contain cast vote records
      while (records.hasNext()) {
        final CSVRecord cvr_line = records.next();
        try {
          final int tabulator_number = 
              Integer.valueOf(stripEqualQuotes(cvr_line.get(TABULATOR_NUMBER_COLUMN)));
          final int batch_id = 
              Integer.valueOf(stripEqualQuotes(cvr_line.get(BATCH_ID_COLUMN)));
          final int record_id = 
              Integer.valueOf(stripEqualQuotes(cvr_line.get(RECORD_ID_COLUMN)));
          final String imprinted_id = 
              stripEqualQuotes(cvr_line.get(IMPRINTED_ID_COLUMN));
          final String ballot_style_name = 
              stripEqualQuotes(cvr_line.get(BALLOT_TYPE_COLUMN));
          final List<Contest> contests = new ArrayList<Contest>();
          final Map<Contest, Set<Choice>> votes = new HashMap<Contest, Set<Choice>>();
          
          // for each contest, see if choices exist on the CVR; "0" or "1" are
          // votes or absences of votes; "" means that the contest is not in this style
          index = FIRST_CHOICE_COLUMN;
          for (final Contest co : my_contests) {
            boolean present = false;
            final Set<Choice> choices = new HashSet<Choice>();
            for (final Choice ch : co.choices()) {
              final String mark = cvr_line.get(index);
              present |= !mark.isEmpty();
              if (!mark.isEmpty() && Integer.valueOf(mark) != 0) {
                choices.add(ch);
              }
              index = index + 1;
            }
            // if this contest was on the ballot, add it to the votes
            if (present) {
              contests.add(co);
              votes.put(co, choices);
            }
          }
          
          // we should now have the votes for each contest; if the ballot style
          // doesn't exist for this ballot style name, create it now
          
          if (!my_ballot_styles.containsKey(ballot_style_name)) {
            my_ballot_styles.put(ballot_style_name, 
                                 new BallotStyle(ballot_style_name, contests));
          }
          
          final CastVoteRecord cvr = 
              new CastVoteRecord(false, timestamp, my_county_id, tabulator_number,
                                 batch_id, record_id, imprinted_id, 
                                 my_ballot_styles.get(ballot_style_name), votes);
          my_cvrs.add(cvr);
        } catch (final NumberFormatException e) {
          // we don't record the CVR since we couldn't get its number
          Main.LOGGER.error("Could not parse malformed CVR record (" + cvr_line + ")");
          result = false;
        }
      }
    } catch (final NoSuchElementException e) {
      Main.LOGGER.error("Could not parse CVR file because it had a malformed header");
      result = false;
    }
    
    // TODO if we had any kind of parse error, do we scrap the whole import? 
    my_parse_success = result;
    my_parse_status = true;
    return result;
  }

  /**
   * @return the CVRs parsed from the supplied data export.
   */
  @Override
  public synchronized List<CastVoteRecord> cvrs() {
    return Collections.unmodifiableList(my_cvrs);
  }

  /**
   * @return the contests inferred from the supplied data export.
   */
  @Override
  public synchronized List<Contest> contests() {
    return Collections.unmodifiableList(my_contests);
  }

  /**
   * @return the ballot styles inferred from the supplied data export.
   */
  @Override
  public synchronized Set<BallotStyle> ballotStyles() {
    return new HashSet<BallotStyle>(my_ballot_styles.values());
  }
  
  /**
   * 
   * <description>
   * <explanation>
   * @param
   */
  //@ behavior
  //@   requires P;
  //@   ensures Q;
  /*@ pure @
   */
  public static void main(final String... the_args) throws IOException {
    final Reader r = new FileReader("/Unsorted/cvrs.csv");
    final DominionCVRExportParser thing = new DominionCVRExportParser(r, 0);
    System.err.println(thing.parse());
    System.err.println(thing.cvrs());
    System.err.println(thing.contests());
    System.err.println(thing.ballotStyles());
  }

}
