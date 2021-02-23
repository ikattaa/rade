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
package fr.aesn.rade.batch.util;

import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Zip Tasklet that takes a file (typically a CSV file just generated) and zips
 * it.
 * @author Marc Gimpel (mgimpel@gmail.com)
 */
@Slf4j
public class ZipTasklet
  implements Tasklet, StepExecutionListener {
  /** */
  private String sourcefiles;
  /** */
  private String destfile;
  /**
   * Initialize the Tasklet by getting the various JobParameters.
   * @param stepExecution Spring Batch stepExecution Object.
   */
  @Override
  public void beforeStep(StepExecution stepExecution) {
    sourcefiles = stepExecution.getJobParameters().getString("inputFile");
    destfile = stepExecution.getJobParameters().getString("inputFile");
    log.info("Zip Tasket for source files: {}, and destination file: {}",
             sourcefiles, destfile);
  }

  /**
   * Given the current context in the form of a step contribution, do whatever
   * is necessary to process this unit inside a transaction.
   *
   * Implementations return RepeatStatus.FINISHED if finished. If not they
   * return RepeatStatus.CONTINUABLE. On failure throws an exception.
   *
   * @param contribution mutable state to be passed back to update the current
   * step execution.
   * @param chunkContext attributes shared between invocations but not between
   * restarts.
   */
  @Override
  public RepeatStatus execute(StepContribution contribution,
                              ChunkContext chunkContext) {
    Resource resource = new DefaultResourceLoader().getResource(destfile);
    try (ZipOutputStream zop = new ZipOutputStream(new FileOutputStream(resource.getFile()))) {
      ZipEntry entry = new ZipEntry("file.csv");
      zop.putNextEntry(entry);
      zop.close();
    } catch (Exception e) {
      log.info("Error zipping file", e);
    }
    return RepeatStatus.FINISHED;
  }

  /**
   * Give a listener a chance to modify the exit status from a step.
   * The value returned will be combined with the normal exit status using
   * ExitStatus.and(ExitStatus).
   *
   * Called after execution of step's processing logic (both successful or
   * failed).
   *
   * @param stepExecution Spring Batch stepExecution Object.
   * @return an ExitStatus to combine with the normal value.
   * Return null to leave the old value unchanged.
   */
  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    return null;
  }
}
