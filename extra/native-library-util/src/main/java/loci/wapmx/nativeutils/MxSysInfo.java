//
// MxSysInfo.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package loci.wapmx.nativeutils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MxSysInfo {
    /**
     * Find the mx.sysinfo string for the current jvm
     * <p>
     * Can be overridden by specifying a mx.sysinfo system property
     */
    public static String getMxSysInfo() {
        String mxSysInfo = System.getProperty("mx.sysinfo");
        if (mxSysInfo != null) {
            return mxSysInfo;
        }
        else {
            return guessMxSysInfo();
        }
    }
    
    /**
     * Make a spirited attempt at guessing what the mx.sysinfo for the current jvm might be.
     */
    public static String guessMxSysInfo() {
        String arch = System.getProperty("os.arch");
        String os = System.getProperty("os.name");
        String extra = "unknown";

        if ("Linux".equals(os)) {
            try {
                String libc_dest = new File("/lib/libc.so.6").getCanonicalPath();
                Matcher libc_m = Pattern.compile(".*/libc-(\\d+)\\.(\\d+)\\..*").matcher(libc_dest);
                if (!libc_m.matches()) throw new IOException("libc symlink contains unexpected destination: "
                                                             + libc_dest);

                File libstdcxx_file = new File("/usr/lib/libstdc++.so.6");
                if (!libstdcxx_file.exists()) libstdcxx_file = new File("/usr/lib/libstdc++.so.5");

                String libstdcxx_dest = libstdcxx_file.getCanonicalPath();
                Matcher libstdcxx_m =
                        Pattern.compile(".*/libstdc\\+\\+\\.so\\.(\\d+)\\.0\\.(\\d+)").matcher(libstdcxx_dest);
                if (!libstdcxx_m.matches()) throw new IOException("libstdc++ symlink contains unexpected destination: "
                                                                  + libstdcxx_dest);
                String cxxver;
                if ("5".equals(libstdcxx_m.group(1))) {
                    cxxver = "5";
                }
                else if ("6".equals(libstdcxx_m.group(1))) {
                    int minor_ver = Integer.parseInt(libstdcxx_m.group(2));
                    if (minor_ver < 9) {
                        cxxver = "6";
                    }
                    else {
                        cxxver = "6" + libstdcxx_m.group(2);
                    }
                }
                else {
                    cxxver = libstdcxx_m.group(1) + libstdcxx_m.group(2);
                }

                extra = "c" + libc_m.group(1) + libc_m.group(2) + "cxx" + cxxver;
            }
            catch (IOException e) {
                extra = "unknown";
            }
            finally {

            }
        }

        return arch + "-" + os + "-" + extra;
    }
}
