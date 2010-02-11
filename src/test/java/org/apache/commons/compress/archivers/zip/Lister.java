/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.compress.archivers.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Simple command line application that lists the contents of a ZIP archive.
 *
 * <p>The name of the archive must be given as a command line argument.</p>
 *
 * <p>Optional command line arguments specify the encoding to assume
 * and whether to use ZipFile or ZipArchiveInputStream.</p>
 */
public final class Lister {
    private static class CommandLine {
        String archive;
        boolean useStream = false;
        String encoding;
    }

    public static void main(String[] args) throws IOException {
        CommandLine cl = parse(args);
        File f = new File(cl.archive);
        if (!f.isFile()) {
            System.err.println(f + " doesn't exists or is a directory");
            usage();
        }
        if (cl.useStream) {
            BufferedInputStream fs =
                new BufferedInputStream(new FileInputStream(f));
            try {
                ZipArchiveInputStream zs =
                    new ZipArchiveInputStream(fs, cl.encoding, true);
                for (ArchiveEntry entry = zs.getNextEntry();
                     entry != null;
                     entry = zs.getNextEntry()) {
                    list((ZipArchiveEntry) entry);
                }
            } finally {
                fs.close();
            }
        } else {
            ZipFile zf = new ZipFile(f, cl.encoding);
            try {
                for (Enumeration entries = zf.getEntries();
                     entries.hasMoreElements(); ) {
                    list((ZipArchiveEntry) entries.nextElement());
                }
            } finally {
                zf.close();
            }
        }
    }

    private static void list(ZipArchiveEntry entry) {
        System.out.println(entry.getName());
    }

    private static CommandLine parse(String[] args) {
        CommandLine cl = new CommandLine();
        boolean error = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-enc")) {
                if (args.length > i + 1) {
                    cl.encoding = args[++i];
                } else {
                    System.err.println("missing argument to -enc");
                    error = true;
                }
            } else if (args[i].equals("-stream")) {
                cl.useStream = true;
            } else if (args[i].equals("-file")) {
                cl.useStream = false;
            } else if (cl.archive != null) {
                System.err.println("Only one archive");
                error = true;
            } else {
                cl.archive = args[i];
            }
        }
        if (error || cl.archive == null) {
            usage();
        }
        return cl;
    }

    private static void usage() {
        System.err.println("lister [-enc encoding] [-stream] [-file] archive");
        System.exit(1);
    }
}