#!/bin/env groovy

@Grab('org.apache.commons:commons-compress:1.+')
import org.apache.commons.compress.compressors.gzip.*
import org.apache.commons.compress.archivers.tar.*
import org.apache.commons.compress.utils.*

def source='./archive1.tar.gz'
def target='/tmp'

new BufferedInputStream(new GzipCompressorInputStream(new FileInputStream(source))).with { gzip ->
    new TarArchiveInputStream(gzip, tar_buffer_size).with { tar ->
        def tarEntry
        while ((tarEntry = tar.nextTarEntry) != null) {
            def entryFile = new File("${target}/${tarEntry.name}")
            if (tarEntry.isDirectory()) {
                entryFile.mkdirs()
            }
            else {
                if (!entryFile.parentFile.exists()) {
                    entryFile.parentFile.mkdirs()
                }
                entryFile.withOutputStream { outputStream ->
                    IOUtils.copy(tar, outputStream, tar_buffer_size)
                }
                entryFile.with {
                    lastModified = tarEntry.modTime.time
                }
            }
        }
    }
}

