package com.saas

import scala.io.Source

class FileAccessor {
  def getUserDocument(documentId: String): String = Source.fromFile(s"/documents/$documentId").mkString
  
  def getUploadedFile(fileId: String): String = Source.fromFile(s"/uploads/$fileId").mkString
  
  def getTemporaryFile(tempId: String): String = Source.fromFile(s"/temp/$tempId").mkString
  
  def getArchiveFile(archiveId: String): String = Source.fromFile(s"/archives/$archiveId").mkString
  
  def getBackupFile(backupId: String): String = Source.fromFile(s"/backups/$backupId").mkString
  
  def getReportFile(reportId: String): String = Source.fromFile(s"/reports/$reportId").mkString
  
  def getLogFile(logId: String): String = Source.fromFile(s"/logs/$logId").mkString
  
  def getConfigFile(configId: String): String = Source.fromFile(s"/config/$configId").mkString
  
  def getDataFile(dataId: String): String = Source.fromFile(s"/data/$dataId").mkString
  
  def getCacheFile(cacheId: String): String = Source.fromFile(s"/cache/$cacheId").mkString
}
