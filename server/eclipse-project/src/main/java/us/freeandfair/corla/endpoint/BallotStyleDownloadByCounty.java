/*
 * Free & Fair Colorado RLA System
 * 
 * @title ColoradoRLA
 * @created Jul 27, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.BallotStyle;
import us.freeandfair.corla.model.CastVoteRecord;
import us.freeandfair.corla.model.CastVoteRecord.RecordType;

/**
 * The ballot style by county download endpoint.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class BallotStyleDownloadByCounty implements Endpoint {
  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.GET;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/ballot-style/county/:counties";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpoint(final Request the_request, final Response the_response) {
    final String[] counties = the_request.params(":counties").split(",");
    final Set<String> county_set = new HashSet<String>(Arrays.asList(counties));
    final Collection<CastVoteRecord> cvr_set = 
        CastVoteRecord.getMatching(county_set, RecordType.UPLOADED);
    final Set<BallotStyle> bs_set = new HashSet<BallotStyle>();
    for (final CastVoteRecord cvr : cvr_set) {
      bs_set.add(cvr.ballotStyle());
    }
    return Main.GSON.toJson(bs_set);
  }
}