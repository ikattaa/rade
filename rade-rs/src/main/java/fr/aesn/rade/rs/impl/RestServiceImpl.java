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
package fr.aesn.rade.rs.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.persist.model.CirconscriptionBassin;
import fr.aesn.rade.persist.model.Commune;
import fr.aesn.rade.persist.model.Delegation;
import fr.aesn.rade.persist.model.Departement;
import fr.aesn.rade.persist.model.Region;
import fr.aesn.rade.rs.RestRequestException;
import fr.aesn.rade.rs.RestService;
import fr.aesn.rade.rs.dto.CirconscriptionBassinListDto;
import fr.aesn.rade.rs.dto.CommuneListDto;
import fr.aesn.rade.rs.dto.DelegationListDto;
import fr.aesn.rade.rs.dto.DepartementListDto;
import fr.aesn.rade.rs.dto.HateoasCirconscriptionBassinDto;
import fr.aesn.rade.rs.dto.HateoasCommuneDto;
import fr.aesn.rade.rs.dto.HateoasDelegationDto;
import fr.aesn.rade.rs.dto.HateoasDepartementDto;
import fr.aesn.rade.rs.dto.HateoasRegionDto;
import fr.aesn.rade.rs.dto.RegionListDto;
import fr.aesn.rade.service.BassinService;
import fr.aesn.rade.service.CommuneService;
import fr.aesn.rade.service.DelegationService;
import fr.aesn.rade.service.DepartementService;
import fr.aesn.rade.service.RegionService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * REST WebService Implementation for the Rade Project.
 * Exposes Region, Departement, Commune, Bassin and Delegation details through
 * a simple REST interface.
 * 
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
public class RestServiceImpl
  implements RestService {
  /** Region Service. */
  @Setter
  private RegionService regionService;
  /** Departement Service. */
  @Setter
  private DepartementService departementService;
  /** Commune Service. */
  @Setter
  private CommuneService communeService;
  /** Delegation Service. */
  @Setter
  private DelegationService delegationService;
  /** Bassin Service. */
  @Setter
  private BassinService bassinService;

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
                               @QueryParam("date") final String rawDate) {
    log.info("Executing operation getAllRegion with code {} and date {}",
             rawCodes, rawDate);
    List<Region> regions = null;
    try {
      Date date = decodeDate(rawDate);
      if (rawCodes != null && !rawCodes.isEmpty()) {
        String code = null;
        Region region = null;
        regions = new ArrayList<>(rawCodes.size());
        for (String rawCode : rawCodes) {
          code = decodeString(rawCode);
          region = regionService.getRegionByCode(code, date);
          if (region != null) {
              regions.add(region);
          }
        }
        log.debug("found {} regions for the given codes", regions.size());
      } else {
        regions = regionService.getAllRegion(date);
        log.debug("found {} regions", regions.size());
      }
      // send response
      if (regions == null || regions.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(RegionListDto.fromEntityList(regions))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

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
                            @QueryParam("date") final String rawDate) {
    log.info("Executing operation getRegion with code {} and date {}", rawCode, rawDate);
    try {
      String code = decodeString(rawCode);
      Date date = decodeDate(rawDate);
      Region region = regionService.getRegionByCode(code, date);
      if (region == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(HateoasRegionDto.fromEntity(region, getRestBasePath(req, REST_PATH_REGION)))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

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
                                    @QueryParam("date") final String rawDate) {
    log.info("Executing operation getAllDepartement with code {} and date {}",
             rawCodes, rawDate);
    List<Departement> depts = null;
    try {
      Date date = decodeDate(rawDate);
      if (rawCodes != null && !rawCodes.isEmpty()) {
        String code = null;
        Departement dept = null;
        depts = new ArrayList<>(rawCodes.size());
        for (String rawCode : rawCodes) {
          code = decodeString(rawCode);
          dept = departementService.getDepartementByCode(code, date);
          if (dept != null) {
            depts.add(dept);
          }
        }
        log.debug("found {} departements for the given codes", depts.size());
      } else {
        depts = departementService.getAllDepartement(date);
        log.debug("found {} departements", depts.size());
      }
      // send response
      if (depts == null || depts.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(DepartementListDto.fromEntityList(depts))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

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
                                 @QueryParam("date") final String rawDate) {
    log.info("Executing operation getDepartement with code {} and date {}", rawCode, rawDate);
    try {
      String code = decodeString(rawCode);
      Date date = decodeDate(rawDate);
      Departement dept = departementService.getDepartementByCode(code, date);
      if (dept == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(HateoasDepartementDto.fromEntity(dept, getRestBasePath(req, REST_PATH_DEPARTEMENT)))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

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
  //@Path(REST_PATH_COMMUNE)
  @Path(REST_PATH_COMMUNE + "{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllCommune(@Context final HttpServletRequest req,
                                @QueryParam("code") final List<String> rawCodes,
                                @QueryParam("dept") final String rawDept,
                                @QueryParam("namelike") final String rawNameLike,
                                @QueryParam("date") final String rawDate) {
    log.info("Executing operation getAllCommune with code {}, dept {}, name like {} and date {}",
             rawCodes, rawDept, rawNameLike, rawDate);
    List<Commune> communes = null;
    try {
      Date date = decodeDate(rawDate);
      // if Query Parameter "code" is present, ignore "dept" and "namelike"
      if (rawCodes != null && !rawCodes.isEmpty()) {
        String code = null;
        Commune commune = null;
        communes = new ArrayList<>(rawCodes.size());
        for (String rawCode : rawCodes) {
          code = decodeString(rawCode);
          commune = communeService.getCommuneByCode(code, date);
          if (commune != null) {
            communes.add(commune);
          }
        }
        log.debug("found {} communes for the given codes", communes.size());
      }
      // if Query Parameter "code" is NOT present, use "dept" and "namelike"
      else {
        String dept = decodeString(rawDept);
        String nameLike = decodeString(rawNameLike);
        communes = communeService.getAllCommune(dept, nameLike, date);
        log.debug("found {} communes for the given criteria", communes.size());
      }
      // send response
      if (communes == null || communes.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(CommuneListDto.fromEntityList(communes))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }
  

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
                             @QueryParam("date") final String rawDate) {
    log.info("Executing operation getCommune with code {} and date {}", rawCode, rawDate);
    try {
      String code = decodeString(rawCode);
      Date date = decodeDate(rawDate);
      Commune commune = communeService.getCommuneByCode(code, date);
      if (commune == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(HateoasCommuneDto.fromEntity(commune, getRestBasePath(req, REST_PATH_COMMUNE)))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

  /**
   * Get all Delegation.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @return list of all Delegation.
   */
  @GET
  @Path(REST_PATH_DELEGATION)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllDelegation(@Context final HttpServletRequest req) {
  log.info("Executing operation getAllDelegation");
    List<Delegation> delegations = delegationService.getAllDelegation();
    if (delegations.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND)
                     .build();
    } else {
      return Response.ok(DelegationListDto.fromEntityList(delegations))
                     .build();
    }
  }

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
                                @PathParam("code") final String rawCode) {
    log.info("Executing operation getDelegation with code {}", rawCode);
    try {
      String code = decodeString(rawCode);
      Delegation delegation = delegationService.getDelegationById(code);
      if (delegation == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(HateoasDelegationDto.fromEntity(delegation, getRestBasePath(req, REST_PATH_DELEGATION)))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

  /**
   * Get all CirconscriptionBassin.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @return list of all CirconscriptionBassin.
   */
  @GET
  @Path(REST_PATH_CIRCONSCRIPTION_BASSIN)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllBassin(@Context final HttpServletRequest req) {
  log.info("Executing operation getAllBassin");
    List<CirconscriptionBassin> bassins = bassinService.getAllBassin();
    if (bassins.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND)
                     .build();
    } else {
      return Response.ok(CirconscriptionBassinListDto.fromEntityList(bassins))
                     .build();
    }
  }

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
                            @PathParam("code") final String rawCode) {
    log.info("Executing operation getBassin with code {}", rawCode);
    try {
      String code = decodeString(rawCode);
      CirconscriptionBassin bassin = bassinService.getBassinByCode(code);
      if (bassin == null) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(HateoasCirconscriptionBassinDto.fromEntity(bassin, getRestBasePath(req, REST_PATH_CIRCONSCRIPTION_BASSIN)))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }

  /**
   * Check and decode the given String.
   * @param rawString the String to check and decode.
   * @return the decoded String.
   * @throws RestRequestException if the String could not be decoded.
   */
  private static final String decodeString(final String rawString)
    throws RestRequestException {
    if (rawString == null) {
      return "";
    }
    try {
      return URLDecoder.decode(rawString, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RestRequestException("Could not decode " + rawString, e);
    }
  }

  /**
   * Check and parse the given date String.
   * @param rawDate the date String to check and parse.
   * @return the parsed date String.
   * @throws RestRequestException if the date String could not be parsed.
   */
  private static final Date decodeDate(final String rawDate)
    throws RestRequestException {
    if (rawDate == null) {
      return new Date();
    } else {
      try {
        return DateConversionUtils.urlStringToDate(rawDate);
      } catch (ParseException e) {
        throw new RestRequestException("Could not parse date " + rawDate, e);
      }
    }
  }
  
private static final boolean decodeBoolean(final String rawgenealogie)
		    throws RestRequestException {		   
		      try {
		        Boolean genealogie= Boolean.parseBoolean(rawgenealogie);
		        if(genealogie) {
		        	return true;
		        }
		        return false;		        
		      } catch (Exception e) {
		        throw new RestRequestException("Could not parse genealogie " + rawgenealogie, e);
		      }
		    
		  }

  /**
   * Extracts the REST Services base path from the given HTTP Request and the
   * Service Path.
   * @param req HTTP Request (for determining base path of Rest Service).
   * @param servicePath the path of REST query used by the request.
   * @return the REST Services base path.
   */
  private static final String getRestBasePath(final HttpServletRequest req,
                                              final String servicePath) {
    String url = req.getRequestURL().toString();
    int index = url.indexOf(servicePath);
    if (index < 0) {
      log.error("Request ({}) not from declared service: {}", url, servicePath);
      return url;
    }
    return url.substring(0, index);
  }
 @GET
 @Path(REST_PATH_COMMUNE)
 @Produces(MediaType.APPLICATION_JSON)
public Response getAllCommunes(@Context final HttpServletRequest req,
        @QueryParam("code") final List<String> rawCodes,
        @QueryParam("dept") final String rawDept,
        @QueryParam("namelike") final String rawNameLike,
        @QueryParam("date") final String rawDate,
        @QueryParam("genealogie") final String rawgenealogie ) {
	// TODO Auto-generated method stub
	    log.info("Executing operation getAllCommunes with code {}, dept {}, name like {} , date {} and genealogie {}",
	             rawCodes, rawDept, rawNameLike, rawDate,rawgenealogie);
	 List<Commune> communes = null;
	    try {
	        Date date = decodeDate(rawDate);
	        boolean genealogie = decodeBoolean(rawgenealogie);
	        // if Query Parameter "code" is present, ignore "dept" and "namelike"
	        if (rawCodes != null && !rawCodes.isEmpty()) {
	          String code = null;
	          Commune commune = null;
	          
	          communes = new ArrayList<>(rawCodes.size());
	          for (String rawCode : rawCodes) {
	            code = decodeString(rawCode);
	            List<Commune> communesEnfants=null;
	            if(genealogie) {
	            	communesEnfants= communeService.getAllCommuneEnfantActiveByCodeInactiveParent(code,date);
	            }else {
		            commune = communeService.getCommuneActiveByCode(code, date);
	            }
	            if (communesEnfants != null) {
	              communes.addAll(communesEnfants);
	            }
	            if (commune != null ) {
	              communes.add(commune);
	            }
	          }
	          log.debug("found {} communes for the given codes", communes.size());
	        }
	        // if Query Parameter "code" is NOT present, use "dept" and "namelike"
	        else {
	          String dept = decodeString(rawDept);
	          String nameLike = decodeString(rawNameLike);
	          communes = communeService.getAllCommune(dept, nameLike, date);
	          log.debug("found {} communes for the given criteria", communes.size());
	        }
	        // send response
	        if (communes == null || communes.isEmpty()) {
	          return Response.status(Response.Status.NOT_FOUND)
	                         .build();
	        } else {
	        	
	          return Response.ok(CommuneListDto.fromEntityList(communes))
	                         .build();
	        }
	        
	    }catch (RestRequestException e) {
     log.error(e.getMessage(), e);
     return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
   }
 }


/**
   @GET
  @Path(REST_PATH_COMMUNE)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllCommune(@Context final HttpServletRequest req,
                                @QueryParam("code") final List<String> rawCodes,
                                @QueryParam("dept") final String rawDept,
                                @QueryParam("namelike") final String rawNameLike,
                                @QueryParam("date") final String rawDate) {
    log.info("Executing operation getAllCommune with code {}, dept {}, name like {} and date {}",
             rawCodes, rawDept, rawNameLike, rawDate);
    List<Commune> communes = null;
    try {
      Date date = decodeDate(rawDate);
      // if Query Parameter "code" is present, ignore "dept" and "namelike"
      if (rawCodes != null && !rawCodes.isEmpty()) {
        String code = null;
        Commune commune = null;
        communes = new ArrayList<>(rawCodes.size());
        for (String rawCode : rawCodes) {
          code = decodeString(rawCode);
          commune = communeService.getCommuneByCode(code, date);
          if (commune != null) {
            communes.add(commune);
          }
        }
        log.debug("found {} communes for the given codes", communes.size());
      }
      // if Query Parameter "code" is NOT present, use "dept" and "namelike"
      else {
        String dept = decodeString(rawDept);
        String nameLike = decodeString(rawNameLike);
        communes = communeService.getAllCommune(dept, nameLike, date);
        log.debug("found {} communes for the given criteria", communes.size());
      }
      // send response
      if (communes == null || communes.isEmpty()) {
        return Response.status(Response.Status.NOT_FOUND)
                       .build();
      } else {
        return Response.ok(CommuneListDto.fromEntityList(communes))
                       .build();
      }
    } catch (RestRequestException e) {
      log.error(e.getMessage(), e);
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity(e.getMessage())
                     .build();
    }
  }
 */


}
