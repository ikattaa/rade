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
package fr.aesn.rade.webapp.mvc.admin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ForkJoinPool;

import javax.servlet.http.HttpServletResponse;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fr.aesn.rade.common.util.DateConversionUtils;
import fr.aesn.rade.webapp.RadeUiException;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC Controller for Rade Batch processing.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
@Controller
@RequestMapping(BatchController.REQUEST_MAPPING)
public class BatchController {
  /** RequestMapping for this Controller. */
  public static final String REQUEST_MAPPING = "/batch";
  /** I18n message source. */
  @Autowired
  private MessageSource messageSource;
  /** Spring Batch Job Launcher. */
  @Autowired
  private JobLauncher jobLauncher;
  /** Batch Info Job. */
  @Autowired
  @Qualifier("webInfoJob")
  private Job infoJob;
  /** Batch Import Commune INSEE Job. */
  @Autowired
  @Qualifier("webImportCommuneInseeJob")
  private Job importCommuneInseeJob;
  /** Batch Import Historique INSEE Job. */
  @Autowired
  @Qualifier("importCommuneInseeHistoryJob")
  private Job importHistoriqueInseeJob;
  /** Batch Import Communes Sandre Job. */
  @Autowired
  @Qualifier("webImportCommuneSandreJob")
  private Job importSandreJob;
  /** Batch Import Delegation Job. */
  @Autowired
  @Qualifier("webImportDelegationJob")
  private Job importDelegationJob;
  @Autowired
  @Qualifier("exportDelegationJob")
  private Job exportDelegationJob;

  /**
   * Main Batch Page mapping.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping()
  public String home(final Model model) {
    model.addAttribute("titre", "Batch");
    return "admin/batch";
  }

  /**
   * Info Batch Upload mapping.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping("/info")
  public String infoGet(final Locale locale,
                        final Model model) {
    log.debug("Requesting /batch/info");
    model.addAttribute("titre", messageSource.getMessage("batchrequest.title.info", null, locale));
    model.addAttribute("postpath", "/batch/info");
    return "admin/batchrequest";
  }

  /**
   * Upload file and run Info batch.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param file the HTTP submitted file. 
   * @return View for the page.
   * @throws IOException if there was a problem recovering and saving file.
   */
  @PostMapping("/info")
  public String infoPost(final Locale locale,
                         final Model model,
                         @RequestParam("file") final MultipartFile file)
    throws IOException {
    log.debug("Posting to /batch/info");
    model.addAttribute("titre", messageSource.getMessage("batchresult.title.info", null, locale));
    Path tmpFile = storeTempFile(file);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", tmpFile.toUri().toString());
    jobBuilder.addString("auditAuteur", "WebBatch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import " + file.getOriginalFilename());
    model.addAttribute("file", file);
    model.addAttribute("uri", tmpFile.toUri());
    JobExecution exec = runSynchronousJobWithLog(infoJob, jobBuilder.toJobParameters());
    if (exec != null) {
      model.addAttribute("params", exec.getJobParameters().getParameters());
      model.addAttribute("status", exec.getExitStatus());
    }
    return "admin/batchresult";
  }

  /**
   * INSEE History Batch Upload mapping.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping("/communeinseeimport")
  public String importCommuneInseeGet(final Locale locale,
                                      final Model model) {
    log.debug("Requesting /batch/communeinseeimport");
    model.addAttribute("titre", messageSource.getMessage("batchrequest.title.communeinsee", null, locale));
    model.addAttribute("postpath", "/batch/communeinseeimport");
    return "admin/batchrequest";
  }

  /**
   * Upload file and run INSEE History import batch.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param file the HTTP submitted file. 
   * @return View for the page.
   * @throws IOException if there was a problem recovering and saving file.
   */
  @PostMapping("/communeinseeimport")
  public String importCommuneInseePost(final Locale locale,
                                       final Model model,
                                       @RequestParam("file") final MultipartFile file)
    throws IOException {
    log.debug("Posting to /batch/communeinseeimport");
    model.addAttribute("titre", messageSource.getMessage("batchresult.title.communeinsee", null, locale));
    Path tmpFile = storeTempFile(file);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", tmpFile.toUri().toString());
    jobBuilder.addString("auditAuteur", "WebBatch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import " + file.getOriginalFilename());
    jobBuilder.addDate("debutValidite", Date.from(LocalDate.of(1999, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    model.addAttribute("file", file);
    model.addAttribute("uri", tmpFile.toUri());
    runAsynchronousJob(importCommuneInseeJob, jobBuilder.toJobParameters());
    model.addAttribute("message", "For more details see <a href=\"../actuator/logfile\" target=\"_blank\">log file</a>");
    return "admin/batchresult";
  }

  /**
   * INSEE History Batch Upload mapping.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping("/historiqueinseeimport")
  public String importHistoriqueInseeGet(final Locale locale,
                                         final Model model) {
    log.debug("Requesting /batch/historiqueinseeimport");
    model.addAttribute("titre", messageSource.getMessage("batchrequest.title.historiqueinsee", null, locale));
    model.addAttribute("postpath", "/batch/historiqueinseeimport");
    return "admin/batchrequest";
  }

  /**
   * Upload file and run INSEE History import batch.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param file the HTTP submitted file. 
   * @param dateParam the HTTP submitted date. 
   * @return View for the page.
   * @throws IOException if there was a problem recovering and saving file.
   */
  @PostMapping("/historiqueinseeimport")
  public String importHistoriqueInseePost(final Locale locale,
                                          final Model model,
                                          @RequestParam("file") final MultipartFile file,@RequestParam(value = "debutValidite", required = false) final String dateParam)
    throws IOException {
	Date defaultDate = Date.from(LocalDate.of(1999, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date dateDebut=DateConversionUtils.urlStringToDate(dateParam,defaultDate);
    log.debug("Posting to /batch/historiqueinseeimport");
    model.addAttribute("titre", messageSource.getMessage("batchresult.title.historiqueinsee", null, locale));
    Path tmpFile = storeTempFile(file);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", tmpFile.toUri().toString());
    jobBuilder.addString("auditAuteur", "WebBatch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import " + file.getOriginalFilename());
    if(dateDebut!=null){
    jobBuilder.addDate("debutValidite", dateDebut);
    }else {
    jobBuilder.addDate("debutValidite", Date.from(LocalDate.of(1999, 1, 2).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    model.addAttribute("file", file);
    model.addAttribute("uri", tmpFile.toUri());
    model.addAttribute("debutValidite",dateDebut);
    runAsynchronousJob(importHistoriqueInseeJob, jobBuilder.toJobParameters());
    model.addAttribute("message", "For more details see <a href=\"../actuator/logfile\" target=\"_blank\">log file</a>");
    return "admin/batchresult";
  }

  /**
   * Sandre Batch Upload mapping.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping("/sandreimport")
  public String importSandreGet(final Locale locale,
                                final Model model) {
    log.debug("Requesting /batch/sandreimport");
    model.addAttribute("titre", messageSource.getMessage("batchrequest.title.sandre", null, locale));
    model.addAttribute("postpath", "/batch/sandreimport");
    return "admin/batchrequest";
  }

  /**
   * Upload file and run Sandre import batch.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param file the HTTP submitted file. 
   * @return View for the page.
   * @throws IOException if there was a problem recovering and saving file.
   */
  @PostMapping("/sandreimport")
  public String importSandrePost(final Locale locale,
                                 final Model model,
                                 @RequestParam("file") final MultipartFile file)
    throws IOException {
    log.debug("Posting to /batch/sandreimport");
    model.addAttribute("titre", messageSource.getMessage("batchresult.title.sandre", null, locale));
    Path tmpFile = storeTempFile(file);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", tmpFile.toUri().toString());
    jobBuilder.addString("auditAuteur", "WebBatch");
    jobBuilder.addDate("auditDate", new Date());
    jobBuilder.addString("auditNote", "Import " + file.getOriginalFilename());
    model.addAttribute("file", file);
    model.addAttribute("uri", tmpFile.toUri());
    runAsynchronousJob(importSandreJob, jobBuilder.toJobParameters());
    model.addAttribute("message", "For more details see <a href=\"../actuator/logfile\" target=\"_blank\">log file</a>");
    return "admin/batchresult";
  }

  /**
   * Delegation Batch Upload mapping.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @return View for the Batch Upload page
   */
  @GetMapping("/delegationimport")
  public String importDelegationGet(final Locale locale,
                                    final Model model) {
    log.debug("Requesting /batch/delegationimport");
    model.addAttribute("titre", messageSource.getMessage("batchrequest.title.delegation", null, locale));
    model.addAttribute("postpath", "/batch/delegationimport");
    return "admin/batchrequest";
  }

  /**
   * Upload file and run Delegation import batch.
   * @param locale locale in which to do the lookup.
   * @param model MVC model passed to JSP.
   * @param file the HTTP submitted file. 
   * @return View for the page.
   * @throws IOException if there was a problem recovering and saving file.
   */
  @PostMapping("/delegationimport")
  public String importDelegationPost(final Locale locale,
                                     final Model model,
                                     @RequestParam("file") final MultipartFile file)
    throws IOException {
    log.debug("Posting to /batch/delegationimport");
    model.addAttribute("titre", messageSource.getMessage("batchresult.title.delegation", null, locale));
    Path tmpFile = storeTempFile(file);
    JobParametersBuilder jobBuilder = new JobParametersBuilder();
    jobBuilder.addString("inputFile", tmpFile.toUri().toString());
    model.addAttribute("file", file);
    model.addAttribute("uri", tmpFile.toUri());
    JobExecution exec = runSynchronousJobWithLog(importDelegationJob, jobBuilder.toJobParameters());
    if (exec != null) {
      model.addAttribute("params", exec.getJobParameters().getParameters());
      model.addAttribute("status", exec.getExitStatus());
    }
    model.addAttribute("message", "For more details see <a href=\"../actuator/logfile\" target=\"_blank\">log file</a>");
    return "admin/batchresult";
  }

  /**
   * Send the Delegation list generated by the Export Batch job.
   * @param response
   * @throws IOException
   */
  @GetMapping("/delegationexport")
  public void exportDelegationGet(final HttpServletResponse response) throws RadeUiException {
    log.debug("Requesting /batch/delegationexport");
    Path tmpFile = null;
    try {
      tmpFile = createTempFile("batch", "delegations.csv");
      JobParametersBuilder jobBuilder = new JobParametersBuilder();
      jobBuilder.addString("outputFile", tmpFile.toUri().toString());
      JobExecution exec = runSynchronousJobWithException(exportDelegationJob,
                                                         jobBuilder.toJobParameters());
      if (!ExitStatus.COMPLETED.equals(exec.getExitStatus())) {
        throw new RadeUiException("Batch failed: " + exec.getExitStatus());
      }
      log.info("Export Delegation batch exit status: {}", exec.getExitStatus());
      response.setContentType("text/csv");
      /* "Content-Disposition : attachment" will directly download;
          may provide save as popup, based on your browser setting.
         "Content-Disposition : inline" will show viewable types (images/text/pdf/...) in the browser
          while others(e.g zip) will be directly downloaded. */
      response.setHeader("Content-Disposition", "attachment; filename=\"delegations.csv\"");
      response.setHeader("Content-type", "text/csv; charset=windows-1252");
      response.setContentLength((int)Files.size(tmpFile));
      try (OutputStream out = response.getOutputStream()) {
        Files.copy(tmpFile, out);
      }
    } catch (IOException | JobParametersInvalidException
                         | JobExecutionAlreadyRunningException
                         | JobInstanceAlreadyCompleteException
                         | JobRestartException e) {
      log.warn("Exception while running batch", e);
      throw new RadeUiException("Exception while running batch", e);
    } finally {
      if (tmpFile != null && Files.exists(tmpFile)) {
        try {
          Files.delete(tmpFile);
        } catch (IOException e) {
          log.warn("Could not delete temporary batch file {}", tmpFile, e);
        }
      }
    }
  }

  /**
   * Run the given Spring Batch Job with the given Parameters in the current
   * Thread.
   * @param job Spring Batch Job
   * @param params Spring Batch Job Parameters
   * @return Spring Batch Job Execution details
   */
  private JobExecution runSynchronousJobWithLog(final Job job,
                                                final JobParameters params) {
    JobExecution exec = null;
    try {
      exec = jobLauncher.run(job, params);
      log.info("Job ({}) completed: {}", job.getName(), exec);
    } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
      log.warn("Exception running Job ({})", job.getName(), e);
    }
    return exec;
  }

  /**
   * Run the given Spring Batch Job with the given Parameters in the current
   * Thread.
   * @param job Spring Batch Job
   * @param params Spring Batch Job Parameters
   * @return Spring Batch Job Execution details
   */
  private JobExecution runSynchronousJobWithException(final Job job,
                                                      final JobParameters params)
    throws JobExecutionAlreadyRunningException,
           JobRestartException,
           JobInstanceAlreadyCompleteException,
           JobParametersInvalidException {
    JobExecution exec = jobLauncher.run(job, params);
    log.info("Job ({}) completed: {}", job.getName(), exec);
    return exec;
  }

  /**
   * Run the given Spring Batch Job with the given Parameters in a new Thread
   * so that the page request may return.
   * @param job Spring Batch Job
   * @param params Spring Batch Job Parameters
   */
  private void runAsynchronousJob(final Job job,
                                  final JobParameters params) {
    ForkJoinPool.commonPool().submit(() -> runSynchronousJobWithLog(job, params));
  }

  /**
   * Store the HTTP submitted file in a temporary location.
   * @param file the HTTP submitted file. 
   * @return the path to the temporary file.
   * @throws IOException if there was a problem recovering and saving file.
   */
  private static Path storeTempFile(final MultipartFile file)
    throws IOException {
    Path tmpFile = createTempFile("uploads", file.getOriginalFilename());
    Files.copy(file.getInputStream(), tmpFile, StandardCopyOption.REPLACE_EXISTING);
    return tmpFile;
  }

  /**
   * Create a temporary file in a temporary location based on the given
   * sub-directory and using the given filename. 
   * @param subdir the sub-directory in which to create the temporary file.
   * @param filename the base filename for the temporary file.
   * @return the path to the temporary file.
   * @throws IOException if there was a problem creating the temporary directory.
   */
  private static Path createTempFile(final String subdir,
                                     final String filename)
    throws IOException {
    Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir")
                          + File.separator + "rade" + File.separator
                          + subdir + File.separator)
                       .toAbsolutePath()
                       .normalize();
    if (!Files.exists(tmpDir)) {
      log.info("Creating Temporary Directory: {}", tmpDir);
      Files.createDirectories(tmpDir);
    }
    return Files.createTempFile(tmpDir, filename, null);
  }
}
