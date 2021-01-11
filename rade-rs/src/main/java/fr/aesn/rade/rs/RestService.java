/*  This file is part of the Rade project (https://github.com/mgimpel/rade).
 *  Copyright (C) 2018 Marc Gimpel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* $Id$ */
package fr.aesn.rade.rs;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST WebService Interface for the Rade Project.
 * Exposes Region, Departement, Commune, Bassin and Delegation details through
 * a simple REST interface.
 * 
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
public interface RestService {
  /** Relative path of the REST Service for Audit. */
  public static final String REST_PATH_AUDIT                  = "audit/";
  /** Relative path of the REST Service for Region. */
  public static final String REST_PATH_REGION                 = "region/";
  /** Relative path of the REST Service for Departement. */
  public static final String REST_PATH_DEPARTEMENT            = "departement/";
  /** Relative path of the REST Service for Commune. */
  public static final String REST_PATH_COMMUNE                = "commune/";
  /** Relative path of the REST Service for Delegation. */
  public static final String REST_PATH_DELEGATION             = "delegation/";
  /** Relative path of the REST Service for Circonscription Bassin. */
  public static final String REST_PATH_CIRCONSCRIPTION_BASSIN = "bassin/";

  /**
   * Get all Region.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCodes list of INSEE code of the Region.
   * @param rawDate the date at which the returned data is valid.
   * @return list of all Region.
   */
  @GET
  @Path(REST_PATH_REGION)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllRegion(@Context final HttpServletRequest req,
                               @QueryParam("code") final List<String> rawCodes,
                               @QueryParam("date") final String rawDate);

  /**
   * Get the Region with the given INSEE code.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCode the INSEE code of the Region.
   * @param rawDate the date at which the returned data is valid.
   * @return the Region with the given INSEE code.
   */
  @GET
  @Path(REST_PATH_REGION + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRegion(@Context final HttpServletRequest req,
                            @PathParam("code") final String rawCode,
                            @QueryParam("date") final String rawDate);

  /**
   * Get all Departement.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCodes list of INSEE code of the Departement.
   * @param rawDate the date at which the returned data is valid.
   * @return list of all Departement.
   */
  @GET
  @Path(REST_PATH_DEPARTEMENT)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllDepartement(@Context final HttpServletRequest req,
                                    @QueryParam("code") final List<String> rawCodes,
                                    @QueryParam("date") final String rawDate);

  /**
   * Get the Departement with the given INSEE code.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCode the INSEE code of the Departement.
   * @param rawDate the date at which the returned data is valid.
   * @return the Departement with the given INSEE code.
   */
  @GET
  @Path(REST_PATH_DEPARTEMENT + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDepartement(@Context final HttpServletRequest req,
                                 @PathParam("code") final String rawCode,
                                 @QueryParam("date") final String rawDate);

  /**
   * Get all Commune matching the request parameters.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCodes list of INSEE code of the Commune
   * (if no codes are given dept and namelike are used). 
   * @param rawDept the Departement INSEE code of the Commune
   * (not used if codes are given).
   * @param rawNameLike a part of the Commune enrich name
   * (not used if codes are given).
   * @param rawDate the date at which the returned data is valid.
   * @return list of all Commune matching the request parameters.
   */
  @GET
  @Path(REST_PATH_COMMUNE)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllCommune(@Context final HttpServletRequest req,
                                @QueryParam("code") final List<String> rawCodes,
                                @QueryParam("dept") final String rawDept,
                                @QueryParam("namelike") final String rawNameLike,
                                @QueryParam("date") final String rawDate);

  
  // modification du web service 
  /**
   * Get all Commune matching the request parameters.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCodes list of INSEE code of the Commune
   * (if no codes are given dept and namelike are used). 
   * @param rawDept the Departement INSEE code of the Commune
   * (not used if codes are given).
   * @param rawNameLike a part of the Commune enrich name
   * (not used if codes are given).
   * @param rawDate the date at which the returned data is valid.
   * @return list of all Commune matching the request parameters.
   */
  @GET
  @Path(REST_PATH_COMMUNE)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllCommunes(@Context final HttpServletRequest req,
                                @QueryParam("code") final List<String> rawCodes,
                                @QueryParam("dept") final String rawDept,
                                @QueryParam("namelike") final String rawNameLike,
                                @QueryParam("date") final String rawDate,
                                @QueryParam("genealogie") final String rawgenealogie);
  
  
  
  /**
   * Get the Commune with the given INSEE code.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCode the INSEE code of the Commune.
   * @param rawDate the date at which the returned data is valid.
   * @return the Commune with the given INSEE code.
   */
  @GET
  @Path(REST_PATH_COMMUNE + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCommune(@Context final HttpServletRequest req,
                             @PathParam("code") final String rawCode,
                             @QueryParam("date") final String rawDate);

  /**
   * Get all Delegation.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @return list of all Delegation.
   */
  @GET
  @Path(REST_PATH_DELEGATION)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllDelegation(@Context final HttpServletRequest req);

  /**
   * Get the Delegation with the given code.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCode the code of the Delegation.
   * @return the Delegation with the given code.
   */
  @GET
  @Path(REST_PATH_DELEGATION + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getDelegation(@Context final HttpServletRequest req,
                                @PathParam("code") final String rawCode);

  /**
   * Get all CirconscriptionBassin.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @return list of all CirconscriptionBassin.
   */
  @GET
  @Path(REST_PATH_CIRCONSCRIPTION_BASSIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllBassin(@Context final HttpServletRequest req);

  /**
   * Get the CirconscriptionBassin with the given code.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param rawCode the code of the CirconscriptionBassin.
   * @return the CirconscriptionBassin with the given code.
   */
  @GET
  @Path(REST_PATH_CIRCONSCRIPTION_BASSIN + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBassin(@Context final HttpServletRequest req,
                            @PathParam("code") final String rawCode);
}
