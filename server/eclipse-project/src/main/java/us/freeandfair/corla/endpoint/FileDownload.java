/*
 * Free & Fair Colorado RLA System
 * @title ColoradoRLA
 * @created Jul 27, 2017
 * @copyright 2017 Free & Fair
 * @license GNU General Public License 3.0
 * @author Daniel M. Zimmerman <dmz@freeandfair.us>
 * @description A system to assist in conducting statewide risk-limiting audits.
 */

package us.freeandfair.corla.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import org.apache.cxf.attachment.Rfc5987Util;

import com.google.gson.JsonParseException;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.Main;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.UploadedFile;
import us.freeandfair.corla.util.FileHelper;
import us.freeandfair.corla.util.SparkHelper;

/**
 * The file download endpoint.
 * 
 * @author Daniel M. Zimmerman
 * @version 0.0.1
 */
@SuppressWarnings({"PMD.AtLeastOneConstructor", "PMD.ExcessiveImports"})
public class FileDownload extends AbstractEndpoint {
  /**
   * The download buffer size, in bytes.
   */
  private static final int BUFFER_SIZE = 1048576; // 1 MB

  /**
   * The maximum download size, in bytes.
   */
  private static final int MAX_DOWNLOAD_SIZE = 1073741824; // 1 GB

  /**
   * {@inheritDoc}
   */
  @Override
  public EndpointType endpointType() {
    return EndpointType.POST;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/download-file";
  }

  /**
   * This endpoint requires either authorization, but only allows downloads
   * by the county that made the upload, or by the state.
   * 
   * @return EITHER
   */
  @Override
  public AuthorizationType requiredAuthorization() {
    return AuthorizationType.EITHER;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String endpoint(final Request the_request, final Response the_response) {
    // we know we have either state or county authentication; this will be null
    // for state authentication
    final County county = Main.authentication().authenticatedCounty(the_request);

    try {
      final UploadedFile file =
          Main.GSON.fromJson(the_request.body(), UploadedFile.class);
      if (file == null) {
        badDataContents(the_response, "nonexistent file requested");
      } else if (county == null || county.id().equals(file.countyID())) {
        the_response.type("text/csv");
        try {
          the_response.raw().setHeader("Content-Disposition", "attachment; filename=\"" + 
                                       Rfc5987Util.encode(file.filename(), "UTF-8") + "\"");
        } catch (final UnsupportedEncodingException e) {
          serverError(the_response, "UTF-8 is unsupported (this should never happen)");
        }
        
        try (OutputStream os = SparkHelper.getRaw(the_response).getOutputStream()) {
          final int total =
              FileHelper.bufferedCopy(file.file().getBinaryStream(), os, 
                                      BUFFER_SIZE, MAX_DOWNLOAD_SIZE);
          Main.LOGGER.debug("sent file " + file.filename() + " of size " + total);
          ok(the_response);
        } catch (final SQLException | IOException e) {
          serverError(the_response, "Unable to stream response");
        }
      } else {
        unauthorized(the_response, "county " + county.id() + " attempted to download " + 
                                   "file " + file.filename() + " uploaded by county " + 
                                   file.countyID());
      }
    } catch (final JsonParseException e) {
      badDataContents(the_response, "malformed request: " + e.getMessage());
    }
    
    return my_endpoint_result.get();
  }
}
